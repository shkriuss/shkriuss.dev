package dev.shkriuss.core.crypto

import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.subtle.AesGcmHkdfStreaming
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays

/**
 * Streaming AEAD engine (AES-256-GCM-HKDF-SHA256) for large files and E2EE backups.
 * Processes bounded segments so plaintext is never loaded wholly into RAM.
 */
class StreamingAeadEngine private constructor(
    private val streamingAead: StreamingAead,
) {
    fun newEncryptingStream(
        ciphertextDestination: OutputStream,
        associatedData: ByteArray,
    ): OutputStream = streamingAead.newEncryptingStream(ciphertextDestination, associatedData)

    fun newDecryptingStream(
        ciphertextSource: InputStream,
        associatedData: ByteArray,
    ): InputStream = streamingAead.newDecryptingStream(ciphertextSource, associatedData)

    companion object {
        const val KEY_SIZE_BYTES: Int = 32
        const val DEFAULT_SEGMENT_SIZE_BYTES: Int = 65_536
        private const val HKDF_ALG = "HmacSha256"

        /**
         * Builds a Streaming AEAD engine from a 256-bit key derived via Argon2id or HKDF.
         * Makes an internal defensive copy and clears the temporary buffer cleanly.
         */
        fun fromRawKey(
            keyMaterial: SecureByteArray,
            ciphertextSegmentSizeBytes: Int = DEFAULT_SEGMENT_SIZE_BYTES,
        ): AppResult<StreamingAeadEngine> {
            if (keyMaterial.size != KEY_SIZE_BYTES) {
                return AppResult.Failure(AppError.Validation("Streaming key must be exactly 32 bytes"))
            }
            if (ciphertextSegmentSizeBytes < 4_096) {
                return AppResult.Failure(AppError.Validation("Segment size must be at least 4096 bytes"))
            }
            val keyCopy = keyMaterial.copyBytes()
            return try {
                val streaming = AesGcmHkdfStreaming(
                    keyCopy,
                    HKDF_ALG,
                    KEY_SIZE_BYTES,
                    ciphertextSegmentSizeBytes,
                    0,
                )
                AppResult.Success(StreamingAeadEngine(streaming))
            } catch (_: Exception) {
                AppResult.Failure(AppError.Crypto("Failed to initialize Streaming AEAD engine"))
            } finally {
                Arrays.fill(keyCopy, 0.toByte())
            }
        }
    }
}
