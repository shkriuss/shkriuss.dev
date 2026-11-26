package dev.shkriuss.core.crypto

import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

/**
 * Versioned Argon2id parameters stored inside authenticated backup/sync headers.
 */
data class Argon2idParams(
    val memoryKiB: Int = DEFAULT_MEMORY_KIB,
    val iterations: Int = DEFAULT_ITERATIONS,
    val parallelism: Int = DEFAULT_PARALLELISM,
    val hashLengthBytes: Int = DEFAULT_KEY_LENGTH_BYTES,
) {
    init {
        require(memoryKiB >= MIN_MEMORY_KIB) { "memoryKiB must be at least $MIN_MEMORY_KIB KiB" }
        require(memoryKiB <= MAX_MEMORY_KIB) { "memoryKiB exceeds safety bound $MAX_MEMORY_KIB KiB" }
        require(iterations in 1..20) { "iterations must be between 1 and 20" }
        require(parallelism in 1..4) { "parallelism must be between 1 and 4" }
        require(hashLengthBytes in 16..64) { "hashLengthBytes must be between 16 and 64" }
    }

    companion object {
        // OWASP-aligned interactive mobile defaults: 64 MiB RAM, 3 iterations, 1 lane, 256-bit key.
        const val DEFAULT_MEMORY_KIB: Int = 65_536
        const val DEFAULT_ITERATIONS: Int = 3
        const val DEFAULT_PARALLELISM: Int = 1
        const val DEFAULT_KEY_LENGTH_BYTES: Int = 32
        const val DEFAULT_SALT_LENGTH_BYTES: Int = 16

        // Upper/lower defensive bounds when parsing untrusted backup headers.
        const val MIN_MEMORY_KIB: Int = 4_096
        const val MAX_MEMORY_KIB: Int = 262_144

        val MOBILE_DEFAULT: Argon2idParams = Argon2idParams()
    }
}

/**
 * Pure-JVM Argon2id key derivation backed by Bouncy Castle (zero bundled C/C++ native code).
 */
class Argon2idKeyDeriver(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun generateSalt(lengthBytes: Int = Argon2idParams.DEFAULT_SALT_LENGTH_BYTES): ByteArray {
        require(lengthBytes >= 16) { "Salt length must be at least 16 bytes" }
        return ByteArray(lengthBytes).also(secureRandom::nextBytes)
    }

    fun deriveKey(
        passphrase: CharArray,
        salt: ByteArray,
        params: Argon2idParams = Argon2idParams.MOBILE_DEFAULT,
    ): AppResult<SecureByteArray> {
        if (passphrase.isEmpty()) {
            return AppResult.Failure(AppError.Validation("Passphrase must not be empty"))
        }
        if (salt.size < 16) {
            return AppResult.Failure(AppError.Validation("Salt must be at least 16 bytes"))
        }

        val passphraseBytes = SecureByteArray.fromUtf8Chars(passphrase)
        return try {
            val argonParams = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withSalt(salt)
                .withMemoryAsKB(params.memoryKiB)
                .withIterations(params.iterations)
                .withParallelism(params.parallelism)
                .build()

            val generator = Argon2BytesGenerator()
            generator.init(argonParams)

            val output = ByteArray(params.hashLengthBytes)
            passphraseBytes.useBytes { secret ->
                generator.generateBytes(secret, output)
            }
            AppResult.Success(SecureByteArray(output))
        } catch (_: Exception) {
            AppResult.Failure(AppError.Crypto("Argon2id key derivation failed"))
        } finally {
            passphraseBytes.close()
        }
    }
}
