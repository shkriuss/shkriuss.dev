package dev.shkriuss.core.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.crypto.AeadCipher
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Arrays

/**
 * Encrypts and decrypts `@Serializable` domain payloads for storage inside Room BLOB
 * columns using Tink AES-256-GCM bound to [RecordAad].
 */
class EncryptedEnvelopeCodec(
    private val cipher: AeadCipher,
    private val json: Json = DefaultStorageJson,
) {
    fun <T> seal(
        value: T,
        serializer: KSerializer<T>,
        aad: RecordAad,
    ): AppResult<ByteArray> {
        val plainBytes = try {
            json.encodeToString(serializer, value).toByteArray(StandardCharsets.UTF_8)
        } catch (_: Exception) {
            return AppResult.Failure(AppError.Storage("Failed to serialize record payload"))
        }
        return try {
            cipher.encrypt(plainBytes, aad.toBytes())
        } finally {
            Arrays.fill(plainBytes, 0.toByte())
        }
    }

    fun <T> open(
        ciphertext: ByteArray,
        serializer: KSerializer<T>,
        aad: RecordAad,
    ): AppResult<T> {
        val plainBytes = when (val result = cipher.decrypt(ciphertext, aad.toBytes())) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        return try {
            val decoded = json.decodeFromString(
                serializer,
                String(plainBytes, StandardCharsets.UTF_8),
            )
            AppResult.Success(decoded)
        } catch (_: Exception) {
            AppResult.Failure(AppError.Storage("Failed to deserialize decrypted record payload"))
        } finally {
            Arrays.fill(plainBytes, 0.toByte())
        }
    }

    companion object {
        val DefaultStorageJson: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
    }
}

/**
 * Jetpack DataStore [Serializer] backed by `:core:crypto` Tink AEAD.
 * Provides hardware-wrapped encrypted preferences without deprecated AndroidX crypto wrappers,
 * enforcing a strict upper ciphertext bound ([maxCiphertextBytes]) to prevent OOM on corrupt files.
 */
class EncryptedDataStoreSerializer<T>(
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val cipher: AeadCipher,
    private val aad: RecordAad,
    private val json: Json = EncryptedEnvelopeCodec.DefaultStorageJson,
    private val maxCiphertextBytes: Int = DEFAULT_MAX_DATASTORE_BYTES,
) : Serializer<T> {

    init {
        require(maxCiphertextBytes in 1_024..MAX_ALLOWED_DATASTORE_BYTES) {
            "maxCiphertextBytes must be between 1 KiB and 16 MiB"
        }
    }

    private val codec = EncryptedEnvelopeCodec(cipher = cipher, json = json)

    override suspend fun readFrom(input: InputStream): T {
        val raw = readBoundedBytes(input, maxCiphertextBytes)
        if (raw.isEmpty()) return defaultValue
        return when (val result = codec.open(raw, serializer, aad)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> throw CorruptionException(result.error.message)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        when (val sealed = codec.seal(t, serializer, aad)) {
            is AppResult.Success -> {
                if (sealed.value.size > maxCiphertextBytes) {
                    throw CorruptionException("Encrypted DataStore payload exceeds maximum allowed size")
                }
                output.write(sealed.value)
            }
            is AppResult.Failure -> throw CorruptionException(sealed.error.message)
        }
    }

    private fun readBoundedBytes(input: InputStream, maxBytes: Int): ByteArray {
        val buffer = ByteArray(8_192)
        val out = ByteArrayOutputStream()
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) {
                throw CorruptionException("Encrypted DataStore file exceeds maximum allowed size ($maxBytes bytes)")
            }
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }

    companion object {
        const val DEFAULT_MAX_DATASTORE_BYTES: Int = 4 * 1024 * 1024
        private const val MAX_ALLOWED_DATASTORE_BYTES: Int = 16 * 1024 * 1024
    }
}
