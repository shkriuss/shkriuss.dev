package dev.shkriuss.core.backup

import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.common.Clock
import dev.shkriuss.core.common.SystemClock
import dev.shkriuss.core.crypto.Argon2idKeyDeriver
import dev.shkriuss.core.crypto.Argon2idParams
import dev.shkriuss.core.crypto.StreamingAeadEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Unencrypted, AEAD-authenticated header written at the start of every `.shkriuss` backup file.
 * Modifying any field in this header invalidates the Streaming AEAD authentication tag.
 */
@Serializable
data class BackupHeader(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val appId: String,
    val appSchemaVersion: Int,
    val createdAtEpochMillis: Long,
    val kdf: String = KDF_ARGON2ID,
    val memoryKiB: Int,
    val iterations: Int,
    val parallelism: Int,
    val saltBase64: String,
    val segmentSizeBytes: Int = StreamingAeadEngine.DEFAULT_SEGMENT_SIZE_BYTES,
) {
    companion object {
        const val CURRENT_FORMAT_VERSION: Int = 1
        const val KDF_ARGON2ID: String = "argon2id"
    }
}

/**
 * Streaming end-to-end encrypted `.shkriuss` backup archiver designed for direct use
 * with Android Storage Access Framework (SAF) streams without buffering entire backups in RAM.
 *
 * Wire layout:
 *   [12-byte ASCII magic "SHKRIUSS_BKP"]
 *   [4-byte big-endian int headerByteLength]
 *   [headerByteLength UTF-8 JSON BackupHeader]
 *   [Tink AES-256-GCM-HKDF streaming ciphertext authenticated with AAD = magic || len || headerBytes]
 */
class EncryptedBackupArchiver(
    private val keyDeriver: Argon2idKeyDeriver = Argon2idKeyDeriver(),
    private val clock: Clock = SystemClock,
    private val json: Json = DefaultBackupJson,
) {
    fun writeEncryptedBackup(
        appId: String,
        appSchemaVersion: Int,
        passphrase: CharArray,
        plaintextSource: InputStream,
        archiveDestination: OutputStream,
        kdfParams: Argon2idParams = Argon2idParams.MOBILE_DEFAULT,
        segmentSizeBytes: Int = StreamingAeadEngine.DEFAULT_SEGMENT_SIZE_BYTES,
    ): AppResult<BackupHeader> {
        if (appId.isBlank()) {
            return AppResult.Failure(AppError.Validation("appId must not be blank"))
        }
        if (appSchemaVersion < 1) {
            return AppResult.Failure(AppError.Validation("appSchemaVersion must be >= 1"))
        }

        val salt = keyDeriver.generateSalt()
        val header = BackupHeader(
            formatVersion = BackupHeader.CURRENT_FORMAT_VERSION,
            appId = appId,
            appSchemaVersion = appSchemaVersion,
            createdAtEpochMillis = clock.nowEpochMillis(),
            kdf = BackupHeader.KDF_ARGON2ID,
            memoryKiB = kdfParams.memoryKiB,
            iterations = kdfParams.iterations,
            parallelism = kdfParams.parallelism,
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            segmentSizeBytes = segmentSizeBytes,
        )

        val headerBytes = json.encodeToString(header).toByteArray(StandardCharsets.UTF_8)
        if (headerBytes.size > MAX_HEADER_BYTES) {
            return AppResult.Failure(AppError.Validation("Backup header exceeds maximum size"))
        }
        val headerEnvelope = encodeHeaderEnvelope(headerBytes)

        val derivedKey = when (val keyResult = keyDeriver.deriveKey(passphrase, salt, kdfParams)) {
            is AppResult.Success -> keyResult.value
            is AppResult.Failure -> return keyResult
        }

        return derivedKey.use { key ->
            val engine = when (val engineResult = StreamingAeadEngine.fromRawKey(key, segmentSizeBytes)) {
                is AppResult.Success -> engineResult.value
                is AppResult.Failure -> return engineResult
            }
            try {
                archiveDestination.write(headerEnvelope)
                // Bind the entire magic + length + JSON header as Streaming AEAD AAD.
                engine.newEncryptingStream(archiveDestination, headerEnvelope).use { encryptingOut ->
                    plaintextSource.copyTo(encryptingOut, bufferSize = IO_BUFFER_BYTES)
                }
                AppResult.Success(header)
            } catch (_: IOException) {
                AppResult.Failure(AppError.Storage("Failed to write encrypted backup stream"))
            } catch (_: Exception) {
                AppResult.Failure(AppError.Crypto("Failed to encrypt backup stream"))
            }
        }
    }

    fun readEncryptedBackup(
        expectedAppId: String,
        passphrase: CharArray,
        archiveSource: InputStream,
        plaintextDestination: OutputStream,
    ): AppResult<BackupHeader> {
        val dataIn = DataInputStream(archiveSource)
        val magic = ByteArray(MAGIC_BYTES.size)
        try {
            dataIn.readFully(magic)
        } catch (_: EOFException) {
            return AppResult.Failure(AppError.Validation("Backup file is empty or truncated"))
        } catch (_: IOException) {
            return AppResult.Failure(AppError.Storage("Failed to read backup header"))
        }

        if (!magic.contentEquals(MAGIC_BYTES)) {
            return AppResult.Failure(AppError.Validation("Not a valid shkriuss backup file"))
        }

        val headerLength = try {
            dataIn.readInt()
        } catch (_: IOException) {
            return AppResult.Failure(AppError.Validation("Corrupted backup header length"))
        }
        if (headerLength <= 0 || headerLength > MAX_HEADER_BYTES) {
            return AppResult.Failure(AppError.Validation("Invalid backup header length"))
        }

        val headerBytes = ByteArray(headerLength)
        try {
            dataIn.readFully(headerBytes)
        } catch (_: IOException) {
            return AppResult.Failure(AppError.Validation("Truncated backup header"))
        }

        val header = try {
            json.decodeFromString(
                BackupHeader.serializer(),
                String(headerBytes, StandardCharsets.UTF_8),
            )
        } catch (_: Exception) {
            return AppResult.Failure(AppError.Validation("Unreadable backup header metadata"))
        }

        if (header.formatVersion != BackupHeader.CURRENT_FORMAT_VERSION) {
            return AppResult.Failure(
                AppError.Validation("Unsupported backup format version: ${header.formatVersion}"),
            )
        }
        if (header.appId != expectedAppId) {
            return AppResult.Failure(
                AppError.Validation("Backup belongs to ${header.appId}, not $expectedAppId"),
            )
        }
        if (header.kdf != BackupHeader.KDF_ARGON2ID) {
            return AppResult.Failure(AppError.Validation("Unsupported backup KDF: ${header.kdf}"))
        }

        val salt = try {
            Base64.getDecoder().decode(header.saltBase64)
        } catch (_: IllegalArgumentException) {
            return AppResult.Failure(AppError.Validation("Invalid backup salt encoding"))
        }

        val kdfParams = try {
            Argon2idParams(
                memoryKiB = header.memoryKiB,
                iterations = header.iterations,
                parallelism = header.parallelism,
            )
        } catch (e: IllegalArgumentException) {
            return AppResult.Failure(AppError.Validation(e.message ?: "Invalid Argon2id parameters"))
        }

        val headerEnvelope = encodeHeaderEnvelope(headerBytes)
        val derivedKey = when (val keyResult = keyDeriver.deriveKey(passphrase, salt, kdfParams)) {
            is AppResult.Success -> keyResult.value
            is AppResult.Failure -> return keyResult
        }

        return derivedKey.use { key ->
            val engine = when (val engineResult = StreamingAeadEngine.fromRawKey(key, header.segmentSizeBytes)) {
                is AppResult.Success -> engineResult.value
                is AppResult.Failure -> return engineResult
            }
            try {
                engine.newDecryptingStream(archiveSource, headerEnvelope).use { decryptingIn ->
                    decryptingIn.copyTo(plaintextDestination, bufferSize = IO_BUFFER_BYTES)
                }
                AppResult.Success(header)
            } catch (_: IOException) {
                AppResult.Failure(
                    AppError.Crypto("Backup decryption failed — wrong passphrase or modified file"),
                )
            } catch (_: Exception) {
                AppResult.Failure(AppError.Crypto("Backup decryption failed"))
            }
        }
    }

    private fun encodeHeaderEnvelope(headerBytes: ByteArray): ByteArray {
        val out = java.io.ByteArrayOutputStream(MAGIC_BYTES.size + 4 + headerBytes.size)
        DataOutputStream(out).use { data ->
            data.write(MAGIC_BYTES)
            data.writeInt(headerBytes.size)
            data.write(headerBytes)
        }
        return out.toByteArray()
    }

    companion object {
        private val MAGIC_BYTES: ByteArray = "SHKRIUSS_BKP".toByteArray(StandardCharsets.US_ASCII)
        private const val MAX_HEADER_BYTES: Int = 16_384
        private const val IO_BUFFER_BYTES: Int = 16_384

        val DefaultBackupJson: Json = Json {
            ignoreUnknownKeys = false
            encodeDefaults = true
        }
    }
}
