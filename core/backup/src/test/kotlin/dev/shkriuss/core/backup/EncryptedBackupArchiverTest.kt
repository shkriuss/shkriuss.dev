package dev.shkriuss.core.backup

import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult
import dev.shkriuss.core.common.Clock
import dev.shkriuss.core.crypto.Argon2idParams
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class EncryptedBackupArchiverTest {
    private val fastTestParams = Argon2idParams(memoryKiB = 8_192, iterations = 2, parallelism = 1)
    private val archiver = EncryptedBackupArchiver(clock = Clock { 1_764_000_000_000L })

    @Test
    fun `roundtrips multi-segment encrypted backup archive via verified staging file`() {
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

        val stagingDir = Files.createTempDirectory("shkriuss-backup-test").toFile()
        try {
            val restoreResult = archiver.restoreToVerifiedFile(
                expectedAppId = "dev.shkriuss.notes",
                passphrase = "strong-test-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(archiveOut.toByteArray()),
                stagingDirectory = stagingDir,
            )
            assertTrue(restoreResult is AppResult.Success)
            val verified = (restoreResult as AppResult.Success).value
            assertEquals(writtenHeader, verified.header)
            assertArrayEquals(plaintext, verified.verifiedPlaintextFile.readBytes())
            verified.verifiedPlaintextFile.delete()
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    @Test
    fun `restoreToVerifiedFile wipes partial staging file when trailing segment is truncated`() {
        val plaintext = ByteArray(120_000) { (it % 199).toByte() }
        val archiveOut = ByteArrayOutputStream()
        archiver.writeEncryptedBackup(
            appId = "dev.shkriuss.notes",
            appSchemaVersion = 1,
            passphrase = "strong-test-passphrase".toCharArray(),
            plaintextSource = ByteArrayInputStream(plaintext),
            archiveDestination = archiveOut,
            kdfParams = fastTestParams,
            segmentSizeBytes = 16_384,
        )
        val fullArchive = archiveOut.toByteArray()
        // Keep first segments intact, truncate final segment so partial plaintext would have been emitted.
        val truncatedArchive = fullArchive.copyOf(fullArchive.size - 1_024)

        val stagingDir = Files.createTempDirectory("shkriuss-backup-trunc").toFile()
        try {
            val result = archiver.restoreToVerifiedFile(
                expectedAppId = "dev.shkriuss.notes",
                passphrase = "strong-test-passphrase".toCharArray(),
                archiveSource = ByteArrayInputStream(truncatedArchive),
                stagingDirectory = stagingDir,
            )
            assertTrue(result is AppResult.Failure)
            assertEquals(0, stagingDir.listFiles()?.size ?: 0)
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    @Test
    fun `rejects crafted oversized segmentSizeBytes header before allocating buffers`() {
        val maliciousHeader = BackupHeader(
            appId = "dev.shkriuss.notes",
            appSchemaVersion = 1,
            createdAtEpochMillis = 1_764_000_000_000L,
            memoryKiB = 8_192,
            iterations = 2,
            parallelism = 1,
            saltBase64 = "AAAAAAAAAAAAAAAAAAAAAA==",
            segmentSizeBytes = Int.MAX_VALUE,
        )
        val headerBytes = EncryptedBackupArchiver.DefaultBackupJson
            .encodeToString(maliciousHeader)
            .toByteArray(StandardCharsets.UTF_8)
        val crafted = ByteArrayOutputStream().apply {
            DataOutputStream(this).use { out ->
                out.write("SHKRIUSS_BKP".toByteArray(StandardCharsets.US_ASCII))
                out.writeInt(headerBytes.size)
                out.write(headerBytes)
            }
        }.toByteArray()

        val result = archiver.readEncryptedBackup(
            expectedAppId = "dev.shkriuss.notes",
            passphrase = "any-passphrase-at-all".toCharArray(),
            archiveSource = ByteArrayInputStream(crafted),
            plaintextDestination = ByteArrayOutputStream(),
        )
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Validation)
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
