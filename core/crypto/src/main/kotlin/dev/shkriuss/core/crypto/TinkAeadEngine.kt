package dev.shkriuss.core.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import java.security.GeneralSecurityException

/**
 * Minimal AEAD contract used by `:core:storage` and app repositories so cryptographic
 * operations are strongly typed, testable, and require explicit Associated Authenticated Data.
 */
interface AeadCipher {
    fun encrypt(plaintext: ByteArray, associatedData: ByteArray): AppResult<ByteArray>
    fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): AppResult<ByteArray>
}

internal object TinkRegistry {
    @Volatile
    private var initialized: Boolean = false

    fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (!initialized) {
                AeadConfig.register()
                StreamingAeadConfig.register()
                initialized = true
            }
        }
    }
}

/**
 * Google Tink AES-256-GCM AEAD engine backed either by Android Keystore/StrongBox on device
 * or by an ephemeral in-memory keyset for JVM unit tests.
 */
class TinkAeadEngine private constructor(
    private val aead: Aead,
) : AeadCipher {

    override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): AppResult<ByteArray> {
        return try {
            AppResult.Success(aead.encrypt(plaintext, associatedData))
        } catch (_: GeneralSecurityException) {
            AppResult.Failure(AppError.Crypto("AEAD encryption failed"))
        }
    }

    override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): AppResult<ByteArray> {
        return try {
            AppResult.Success(aead.decrypt(ciphertext, associatedData))
        } catch (_: GeneralSecurityException) {
            AppResult.Failure(AppError.Crypto("AEAD decryption or authentication failed"))
        }
    }

    companion object {
        private const val KEY_TEMPLATE_AES256_GCM = "AES256_GCM"

        /**
         * Creates an Android Keystore-wrapped Tink AES-256-GCM AEAD instance.
         * Never stores unencrypted key material on disk.
         */
        fun forAndroidKeystore(
            context: Context,
            keysetName: String,
            prefFileName: String,
            masterKeyAlias: String,
        ): AppResult<TinkAeadEngine> {
            return try {
                TinkRegistry.ensureInitialized()
                val masterKey = when (val keyResult = KeystoreMasterKeyProvider.getOrCreateMasterKey(masterKeyAlias)) {
                    is AppResult.Success -> keyResult.value
                    is AppResult.Failure -> return keyResult
                }
                val keysetHandle = AndroidKeysetManager.Builder()
                    .withSharedPref(context.applicationContext, keysetName, prefFileName)
                    .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE_AES256_GCM))
                    .withMasterKeyUri(masterKey.keystoreUri)
                    .build()
                    .keysetHandle
                val primitive = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
                AppResult.Success(TinkAeadEngine(primitive))
            } catch (_: Exception) {
                AppResult.Failure(AppError.Crypto("Failed to create Android Keystore AEAD engine"))
            }
        }

        /**
         * Creates an ephemeral in-memory Tink AES-256-GCM AEAD instance for JVM tests.
         */
        fun inMemoryForTesting(): TinkAeadEngine {
            TinkRegistry.ensureInitialized()
            val handle = KeysetHandle.generateNew(KeyTemplates.get(KEY_TEMPLATE_AES256_GCM))
            val primitive = handle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
            return TinkAeadEngine(primitive)
        }
    }
}
