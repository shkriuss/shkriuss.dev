package dev.shkriuss.core.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.crypto.AeadCipher
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
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
 * Provides hardware-wrapped encrypted preferences without deprecated AndroidX crypto wrappers.
 */
class EncryptedDataStoreSerializer<T>(
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val cipher: AeadCipher,
    private val aad: RecordAad,
    private val json: Json = EncryptedEnvelopeCodec.DefaultStorageJson,
) : Serializer<T> {

    private val codec = EncryptedEnvelopeCodec(cipher = cipher, json = json)

    override suspend fun readFrom(input: InputStream): T {
        val raw = input.readBytes()
        if (raw.isEmpty()) return defaultValue
        return when (val result = codec.open(raw, serializer, aad)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> throw CorruptionException(result.error.message)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        when (val sealed = codec.seal(t, serializer, aad)) {
            is AppResult.Success -> output.write(sealed.value)
            is AppResult.Failure -> throw CorruptionException(sealed.error.message)
        }
    }
}
