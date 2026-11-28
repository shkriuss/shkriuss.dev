package dev.shkriuss.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

/**
 * Hardware security backing of the device Key Encryption Key (KEK), queried directly
 * from Android Keystore `KeyInfo.securityLevel`.
 */
enum class KeyBacking {
    STRONGBOX,
    HARDWARE_TEE,
    SOFTWARE_FALLBACK,
}

data class MasterKeyDescriptor(
    val alias: String,
    val keystoreUri: String,
    val backing: KeyBacking,
)

internal object KeystoreLock

/**
 * Provisions an AES-256-GCM master key inside Android Keystore for wrapping Tink keysets.
 * Prefers StrongBox (Titan M2 / dedicated SE on Pixel/GrapheneOS devices) and falls back
 * cleanly to hardware TEE on devices without StrongBox.
 */
object KeystoreMasterKeyProvider {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEYSTORE_URI_PREFIX = "android-keystore://"

    fun getOrCreateMasterKey(alias: String): AppResult<MasterKeyDescriptor> {
        if (alias.isBlank()) {
            return AppResult.Failure(AppError.Validation("Master key alias must not be blank"))
        }

        synchronized(KeystoreLock) {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                if (!keyStore.containsAlias(alias)) {
                    try {
                        generateAesGcmKey(alias = alias, useStrongBox = true)
                    } catch (_: StrongBoxUnavailableException) {
                        cleanupPartialAlias(keyStore, alias)
                        generateAesGcmKey(alias = alias, useStrongBox = false)
                    } catch (_: ProviderException) {
                        cleanupPartialAlias(keyStore, alias)
                        generateAesGcmKey(alias = alias, useStrongBox = false)
                    }
                }

                val backing = resolveKeyBacking(keyStore, alias)
                AppResult.Success(
                    MasterKeyDescriptor(
                        alias = alias,
                        keystoreUri = "$KEYSTORE_URI_PREFIX$alias",
                        backing = backing,
                    ),
                )
            } catch (_: Exception) {
                AppResult.Failure(AppError.Crypto("Failed to initialize Android Keystore master key"))
            }
        }
    }

    private fun cleanupPartialAlias(keyStore: KeyStore, alias: String) {
        runCatching {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }
    }

    private fun resolveKeyBacking(keyStore: KeyStore, alias: String): KeyBacking {
        return runCatching {
            val secretKey = keyStore.getKey(alias, null) as? SecretKey
                ?: return KeyBacking.HARDWARE_TEE
            val factory = SecretKeyFactory.getInstance(secretKey.algorithm, ANDROID_KEYSTORE)
            val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as? KeyInfo
                ?: return KeyBacking.HARDWARE_TEE
            when (keyInfo.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeyBacking.STRONGBOX
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT,
                KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE,
                -> KeyBacking.HARDWARE_TEE
                else -> KeyBacking.SOFTWARE_FALLBACK
            }
        }.getOrDefault(KeyBacking.HARDWARE_TEE)
    }

    private fun generateAesGcmKey(alias: String, useStrongBox: Boolean) {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setIsStrongBoxBacked(useStrongBox)
            .build()
        generator.init(spec)
        generator.generateKey()
    }
}
