package dev.shkriuss.core.storage

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets

/**
 * Canonical length-prefixed Associated Authenticated Data (AAD) binding encrypted
 * records to their exact app, entity, record ID, and schema version so ciphertext
 * cannot be transplanted across rows, tables, or apps.
 */
data class RecordAad(
    val appId: String,
    val entityType: String,
    val recordId: String,
    val schemaVersion: Int,
) {
    init {
        require(appId.isNotBlank()) { "appId must not be blank" }
        require(entityType.isNotBlank()) { "entityType must not be blank" }
        require(recordId.isNotBlank()) { "recordId must not be blank" }
        require(schemaVersion >= 1) { "schemaVersion must be >= 1" }
    }

    fun toBytes(): ByteArray {
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { data ->
            writeUtf8Field(data, AAD_DOMAIN_TAG)
            writeUtf8Field(data, appId)
            writeUtf8Field(data, entityType)
            writeUtf8Field(data, recordId)
            data.writeInt(schemaVersion)
        }
        return out.toByteArray()
    }

    private fun writeUtf8Field(data: DataOutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        data.writeInt(bytes.size)
        data.write(bytes)
    }

    companion object {
        private const val AAD_DOMAIN_TAG = "shkriuss.record.v1"
    }
}
