package dev.shkriuss.core.storage

import androidx.datastore.core.CorruptionException
import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.crypto.TinkAeadEngine
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Serializable
private data class SampleVaultPrefs(
    val appLockEnabled: Boolean = false,
    val hideScreenInRecents: Boolean = true,
)

class EncryptedStorageTest {
    @Test
    fun `recordAad prevents delimiter collisions between fields`() {
        val aad1 = RecordAad("dev.shkriuss.notes", "note|a", "b", 1).toBytes()
        val aad2 = RecordAad("dev.shkriuss.notes", "note", "a|b", 1).toBytes()
        assertFalse(aad1.contentEquals(aad2))
    }

    @Test
    fun `encryptedEnvelopeCodec seals opens and rejects mismatched record context`() {
        val codec = EncryptedEnvelopeCodec(TinkAeadEngine.inMemoryForTesting())
        val payload = SampleVaultPrefs(appLockEnabled = true, hideScreenInRecents = true)
        val aad = RecordAad("dev.shkriuss.notes", "note", "id-1", schemaVersion = 1)

        val sealed = (codec.seal(payload, SampleVaultPrefs.serializer(), aad) as AppResult.Success).value
        val opened = (codec.open(sealed, SampleVaultPrefs.serializer(), aad) as AppResult.Success).value
        assertEquals(payload, opened)

        // Moving blob to another row ID must fail authentication.
        val otherRowAad = aad.copy(recordId = "id-2")
        assertTrue(codec.open(sealed, SampleVaultPrefs.serializer(), otherRowAad) is AppResult.Failure)
    }

    @Test
    fun `encryptedDataStoreSerializer roundtrips and throws CorruptionException on tampering`() = runTest {
        val serializer = EncryptedDataStoreSerializer(
            defaultValue = SampleVaultPrefs(),
            serializer = SampleVaultPrefs.serializer(),
            cipher = TinkAeadEngine.inMemoryForTesting(),
            aad = RecordAad("dev.shkriuss.budget", "prefs", "main", schemaVersion = 1),
        )

        // Empty input yields defaultValue cleanly.
        assertEquals(SampleVaultPrefs(), serializer.readFrom(ByteArrayInputStream(ByteArray(0))))

        val saved = SampleVaultPrefs(appLockEnabled = true, hideScreenInRecents = false)
        val buffer = ByteArrayOutputStream()
        serializer.writeTo(saved, buffer)
        val encryptedBytes = buffer.toByteArray()

        val restored = serializer.readFrom(ByteArrayInputStream(encryptedBytes))
        assertEquals(saved, restored)

        // Corrupting a byte must raise CorruptionException.
        val corrupted = encryptedBytes.copyOf()
        corrupted[corrupted.lastIndex] = (corrupted[corrupted.lastIndex].toInt() xor 0xFF).toByte()
        var threwCorruption = false
        try {
            serializer.readFrom(ByteArrayInputStream(corrupted))
        } catch (_: CorruptionException) {
            threwCorruption = true
        }
        assertTrue(threwCorruption)

        // Oversized input must raise CorruptionException before allocating unbounded memory.
        val cappedSerializer = EncryptedDataStoreSerializer(
            defaultValue = SampleVaultPrefs(),
            serializer = SampleVaultPrefs.serializer(),
            cipher = TinkAeadEngine.inMemoryForTesting(),
            aad = RecordAad("dev.shkriuss.budget", "prefs", "main", schemaVersion = 1),
            maxCiphertextBytes = 2_048,
        )
        var threwOversize = false
        try {
            cappedSerializer.readFrom(ByteArrayInputStream(ByteArray(4_096)))
        } catch (_: CorruptionException) {
            threwOversize = true
        }
        assertTrue(threwOversize)
    }
}
