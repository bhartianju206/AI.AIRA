package com.example.data

import com.example.data.db.AiraDao
import com.example.data.model.AiraMemory
import com.example.data.model.AuditLog
import com.example.data.model.AutomationRoutine
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val dao: AiraDao) {

    val allMemories: Flow<List<AiraMemory>> = dao.getAllMemories()
    val allRoutines: Flow<List<AutomationRoutine>> = dao.getAllRoutines()
    val recentAuditLogs: Flow<List<AuditLog>> = dao.getRecentAuditLogs(100)

    suspend fun savePreference(key: String, rawValue: String, summary: String, category: String = "PREFERENCE") {
        val encrypted = CryptoVault.encrypt(rawValue)
        val existing = dao.getMemoryByKey(key)
        if (existing != null) {
            dao.updateMemory(
                existing.copy(
                    encryptedValue = encrypted,
                    plainSummary = summary,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertMemory(
                AiraMemory(
                    category = category,
                    key = key,
                    encryptedValue = encrypted,
                    plainSummary = summary,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        logAudit(
            actionType = "MEMORY_UPDATED",
            targetApp = "AIRA_VAULT",
            summary = "Encrypted memory saved for key: $key",
            status = "VERIFIED_OFFLINE"
        )
    }

    suspend fun getDecryptedValue(key: String): String? {
        val memory = dao.getMemoryByKey(key) ?: return null
        return CryptoVault.decrypt(memory.encryptedValue)
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemory(id)
        logAudit(
            actionType = "MEMORY_DELETED",
            targetApp = "AIRA_VAULT",
            summary = "Memory entity #$id permanently expunged",
            status = "VERIFIED_OFFLINE"
        )
    }

    suspend fun purgeAllMemories() {
        dao.clearAllMemories()
        logAudit(
            actionType = "VAULT_PURGED",
            targetApp = "AIRA_VAULT",
            summary = "EMERGENCY: Complete neural memory purge executed",
            status = "BIOMETRIC_APPROVED"
        )
    }

    suspend fun logAudit(actionType: String, targetApp: String, summary: String, status: String = "VERIFIED_OFFLINE") {
        val hash = CryptoVault.sha256("$actionType-$targetApp-$summary-${System.currentTimeMillis()}")
        val log = AuditLog(
            actionType = actionType,
            targetApp = targetApp,
            summary = summary,
            securityStatus = status,
            cryptoHash = hash
        )
        dao.insertAuditLog(log)
    }

    suspend fun setRoutineEnabled(id: Long, enabled: Boolean) {
        dao.setRoutineEnabled(id, enabled)
    }

    suspend fun addRoutine(routine: AutomationRoutine) {
        dao.insertRoutine(routine)
    }
}
