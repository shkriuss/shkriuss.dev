package dev.shkriuss.core.backup

import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.common.Clock
import dev.shkriuss.core.crypto.Argon2idParams
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EncryptedBackupArchiverTest {
    private val fastTestParams = Argon2idParams(memoryKiB = 8_192, iterations = 2, parallelism = 1)
    private val archiver = EncryptedBackupArchiver(clock = Clock { 1_764_000_000_000L })

    @Test
    fun `roundtrips multi-segment encrypted backup archive`() {
        val plaintext = ByteArray(200_000) { (it * 13 + 7).toByte() }
        val archiveOut = ByteArrayOutputStream()

        val writeResult = archiver.writeEncryptedBackup(
            appId = "dev.shkriuss.notes",
            appSchemaVersion = 3,
            passphrase = "strong-test-passphrase".toCharArray(),
            plaintextSource = ByteArrayInputStream(plaintext),
            archiveDestination = archiveOut,
            kdfParams = fastTestParams,
            segmentSizeBytes = 16_384,
        )
        assertTrue(writeResult is AppResult.Success)
        val writtenHeader = (writeResult as AppResult.Success).value
        assertEquals("dev.shkriuss.notes", writtenHeader.appId)
        assertEquals(3, writtenHeader.appSchemaVersion)

        val restoredOut = ByteArrayOutputStream()
        val readResult = archiver.readEncryptedBackup(
            expectedAppId = "dev.shkriuss.notes",
            passphrase = "strong-test-passphrase".toCharArray(),
            archiveSource = ByteArrayInputStream(archiveOut.toByteArray()),
            plaintextDestination = restoredOut,
        )
        assertTrue(readResult is AppResult.Success)
        assertEquals(writtenHeader, (readResult as AppResult.Success).value)
        assertArrayEquals(plaintext, restoredOut.toByteArray())
    }

    @Test
    fun `rejects wrong passphrase tampered header tampered ciphertext and wrong appId`() {
        val plaintext = "Private journal contents".toByteArray()
        val archiveOut = ByteArrayOutputStream()
        archiver.writeEncryptedBackup(
            appId = "dev.shkriuss.budget",
            appSchemaVersion = 1,
            passphrase = "my-secret-passphrase".toCharArray(),
            plaintextSource = ByteArrayInputStream(plaintext),
            archiveDestination = archiveOut,
            kdfParams = fastTestParams,
        )
        val archiveBytes = archiveOut.toByteArray()

        // 1. Wrong passphrase
        assertTrue(
            archiver.readEncryptedBackup(
                expectedAppId = "dev.shkriuss.budget",
                passphrase = "wrong-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(archiveBytes),
                plaintextDestination = ByteArrayOutputStream(),
            ) is AppResult.Failure,
        )

        // 2. Wrong appId
        assertTrue(
            archiver.readEncryptedBackup(
                expectedAppId = "dev.shkriuss.notes",
                passphrase = "my-secret-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(archiveBytes),
                plaintextDestination = ByteArrayOutputStream(),
            ) is AppResult.Failure,
        )

        // 3. Tampered ciphertext tail
        val tamperedCipher = archiveBytes.copyOf()
        tamperedCipher[tamperedCipher.lastIndex] =
            (tamperedCipher[tamperedCipher.lastIndex].toInt() xor 0x01).toByte()
        assertTrue(
            archiver.readEncryptedBackup(
                expectedAppId = "dev.shkriuss.budget",
                passphrase = "my-secret-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(tamperedCipher),
                plaintextDestination = ByteArrayOutputStream(),
            ) is AppResult.Failure,
        )

        // 4. Tampered authenticated header timestamp (replace one digit in createdAtEpochMillis)
        val archiveString = String(archiveBytes, Charsets.ISO_8859_1)
        val tamperedHeaderBytes = archiveString
            .replace("1764000000000", "1764000000001")
            .toByteArray(Charsets.ISO_8859_1)
        assertTrue(
            archiver.readEncryptedBackup(
                expectedAppId = "dev.shkriuss.budget",
                passphrase = "my-secret-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(tamperedHeaderBytes),
                plaintextDestination = ByteArrayOutputStream(),
            ) is AppResult.Failure,
        )
    }
}
