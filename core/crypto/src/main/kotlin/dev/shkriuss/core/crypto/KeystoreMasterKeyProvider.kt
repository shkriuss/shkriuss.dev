package dev.shkriuss.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.KeyGenerator

/**
 * Hardware security backing of the device Key Encryption Key (KEK).
 */
enum class KeyBacking {
    STRONGBOX,
    HARDWARE_TEE,
}

data class MasterKeyDescriptor(
    val alias: String,
    val keystoreUri: String,
    val backing: KeyBacking,
)

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

        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                return AppResult.Success(
                    MasterKeyDescriptor(
                        alias = alias,
                        keystoreUri = "$KEYSTORE_URI_PREFIX$alias",
                        backing = KeyBacking.STRONGBOX,
                    ),
                )
            }

            val backing = try {
                generateAesGcmKey(alias = alias, useStrongBox = true)
                KeyBacking.STRONGBOX
            } catch (_: StrongBoxUnavailableException) {
                generateAesGcmKey(alias = alias, useStrongBox = false)
                KeyBacking.HARDWARE_TEE
            } catch (_: ProviderException) {
                generateAesGcmKey(alias = alias, useStrongBox = false)
                KeyBacking.HARDWARE_TEE
            }

            AppResult.Success(
                MasterKeyDescriptor(
                    alias = alias,
                    keystoreUri = "$KEYSTORE_URI_PREFIX$alias",
                    backing = backing,
                ),
            )
        } catch (t: Exception) {
            AppResult.Failure(AppError.Crypto("Failed to initialize Android Keystore master key"))
        }
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
