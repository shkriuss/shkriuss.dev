package dev.shkriuss.core.crypto

import dev.shkriuss.core.common.AppResult
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class CryptoCoreTest {
    @Test
    fun `tinkAead encrypts decrypts and rejects tampered ciphertext or mismatched aad`() {
        val engine = TinkAeadEngine.inMemoryForTesting()
        val plaintext = "Sensitive personal ledger entry".toByteArray(StandardCharsets.UTF_8)
        val aad = "dev.shkriuss.budget|ledger|42|v1".toByteArray(StandardCharsets.UTF_8)

        val encrypted1 = (engine.encrypt(plaintext, aad) as AppResult.Success).value
        val encrypted2 = (engine.encrypt(plaintext, aad) as AppResult.Success).value
        // Randomized AES-GCM IV ensures distinct ciphertexts for identical plaintext.
        assertFalse(encrypted1.contentEquals(encrypted2))

        val decrypted = (engine.decrypt(encrypted1, aad) as AppResult.Success).value
        assertArrayEquals(plaintext, decrypted)

        // Wrong AAD must fail authentication.
        val wrongAad = "dev.shkriuss.budget|ledger|43|v1".toByteArray(StandardCharsets.UTF_8)
        assertTrue(engine.decrypt(encrypted1, wrongAad) is AppResult.Failure)

        // Tampered ciphertext bit must fail authentication.
        val tampered = encrypted1.copyOf()
        tampered[tampered.lastIndex] = (tampered[tampered.lastIndex].toInt() xor 0x01).toByte()
        assertTrue(engine.decrypt(tampered, aad) is AppResult.Failure)
    }

    @Test
    fun `argon2id derives deterministic 32-byte keys for same salt and distinct keys for different salts`() {
        val deriver = Argon2idKeyDeriver()
        val testParams = Argon2idParams(memoryKiB = 8_192, iterations = 2, parallelism = 1)
        val salt1 = ByteArray(16) { it.toByte() }
        val salt2 = ByteArray(16) { (it + 1).toByte() }

        val key1a = (deriver.deriveKey("correct-horse-battery".toCharArray(), salt1, testParams) as AppResult.Success).value
        val key1b = (deriver.deriveKey("correct-horse-battery".toCharArray(), salt1, testParams) as AppResult.Success).value
        val key2 = (deriver.deriveKey("correct-horse-battery".toCharArray(), salt2, testParams) as AppResult.Success).value

        key1a.use { k1a ->
            key1b.use { k1b ->
                key2.use { k2 ->
                    assertEquals(32, k1a.size)
                    assertArrayEquals(k1a.copyBytes(), k1b.copyBytes())
                    assertFalse(k1a.copyBytes().contentEquals(k2.copyBytes()))
                }
            }
        }
    }

    @Test
    fun `secureByteArray zeroizes backing buffer on close and redacts toString`() {
        val raw = byteArrayOf(1, 2, 3, 4, 5)
        val secure = SecureByteArray(raw)
        assertEquals("SecureByteArray(redacted)", secure.toString())
        secure.close()
        assertArrayEquals(byteArrayOf(0, 0, 0, 0, 0), raw)
    }

    @Test
    fun `streamingAead roundtrips multi-segment payloads and detects tampering`() {
        val key = SecureByteArray(ByteArray(32) { (it * 7 + 3).toByte() })
        val aad = "backup-header-v1".toByteArray(StandardCharsets.UTF_8)
        val payload = ByteArray(150_000) { (it % 251).toByte() }

        key.use { k ->
            val engine = (StreamingAeadEngine.fromRawKey(k, ciphertextSegmentSizeBytes = 16_384) as AppResult.Success).value
            val cipherBuffer = ByteArrayOutputStream()
            engine.newEncryptingStream(cipherBuffer, aad).use { out ->
                out.write(payload)
            }
            val ciphertext = cipherBuffer.toByteArray()

            val plainBuffer = ByteArrayOutputStream()
            engine.newDecryptingStream(ByteArrayInputStream(ciphertext), aad).use { input ->
                input.copyTo(plainBuffer)
            }
            assertArrayEquals(payload, plainBuffer.toByteArray())

            // Wrong AAD must throw IOException during streaming decryption.
            var failed = false
            try {
                engine.newDecryptingStream(
                    ByteArrayInputStream(ciphertext),
                    "wrong-header".toByteArray(StandardCharsets.UTF_8),
                ).use { input ->
                    input.readBytes()
                }
            } catch (_: IOException) {
                failed = true
            }
            assertTrue(failed)
        }
    }
}
