package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aira_memories")
data class AiraMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "PREFERENCE", "CONTACT", "BEHAVIOR", "SECURITY", "MACRO"
    val key: String,
    val encryptedValue: String,
    val plainSummary: String, // Sanitized non-sensitive tag for UI display
    val confidence: Float = 0.95f,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "SCREEN_READ", "SEND_MESSAGE", "TAP_NODE", "SCROLL", "SYSTEM_SETTING", "VOICE_COMMAND"
    val targetApp: String,
    val summary: String,
    val securityStatus: String, // "VERIFIED_OFFLINE", "BIOMETRIC_APPROVED", "NEURAL_CONFIRMED"
    val cryptoHash: String
)

@Entity(tableName = "automation_routines")
data class AutomationRoutine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerPhrase: String,
    val description: String,
    val category: String, // "COMMUNICATION", "SYSTEM", "ACCESSIBILITY", "PRIVACY"
    val requiresBiometric: Boolean = false,
    val stepsJson: String,
    val isEnabled: Boolean = true
)
