package dev.shkriuss.core.crypto

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.util.Arrays

/**
 * AutoCloseable wrapper around sensitive byte material (derived keys, passphrases, salts).
 * Overwrites backing memory with zeros on [close] and prevents accidental secret logging.
 */
class SecureByteArray(private val data: ByteArray) : AutoCloseable {
    @Volatile
    private var closed: Boolean = false

    val size: Int
        get() {
            check(!closed) { "SecureByteArray has already been closed and zeroized" }
            return data.size
        }

    /**
     * Executes [block] with direct access to the underlying buffer without copying.
     */
    inline fun <R> useBytes(block: (ByteArray) -> R): R {
        return block(unsafeBytes())
    }

    @PublishedApi
    internal fun unsafeBytes(): ByteArray {
        check(!closed) { "SecureByteArray has already been closed and zeroized" }
        return data
    }

    /**
     * Returns a copy for APIs that require owned byte arrays. Callers should zeroize the copy after use.
     */
    fun copyBytes(): ByteArray {
        check(!closed) { "SecureByteArray has already been closed and zeroized" }
        return data.copyOf()
    }

    override fun close() {
        if (!closed) {
            closed = true
            Arrays.fill(data, 0.toByte())
        }
    }

    override fun toString(): String = "SecureByteArray(redacted)"

    companion object {
        /**
         * Encodes [chars] as UTF-8 into a [SecureByteArray] without creating an immutable [String].
         */
        fun fromUtf8Chars(chars: CharArray): SecureByteArray {
            val charBuffer = CharBuffer.wrap(chars)
            val byteBuffer: ByteBuffer = StandardCharsets.UTF_8.encode(charBuffer)
            val result = ByteArray(byteBuffer.remaining())
            byteBuffer.get(result)
            if (byteBuffer.hasArray()) {
                Arrays.fill(byteBuffer.array(), 0.toByte())
            }
            return SecureByteArray(result)
        }
    }
}
