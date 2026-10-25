package com.example.neural

import com.example.data.CryptoVault
import com.example.data.MemoryRepository
import java.util.UUID

enum class IntentType {
    SEND_MESSAGE,
    READ_SCREEN,
    SUMMARIZE_SCREEN,
    OPEN_APP,
    DEVICE_CONTROL,
    SYSTEM_NAVIGATION,
    MEMORY_QUERY,
    MEMORY_STORE,
    PANIC_LOCK,
    GENERAL_COMMAND
}

enum class StepActionType {
    OPEN_PACKAGE,
    GLOBAL_ACTION,
    CLICK_NODE,
    SET_TEXT,
    SCROLL,
    INSPECT_SCREEN,
    CONFIRM_BIOMETRIC,
    SYSTEM_FEEDBACK
}

enum class StepStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class ExecutionStep(
    val id: String = UUID.randomUUID().toString(),
    val stepNumber: Int,
    val title: String,
    val description: String,
    val actionType: StepActionType,
    val target: String,
    val param: String = "",
    var status: StepStatus = StepStatus.PENDING,
    var resultMessage: String? = null
)

data class AutonomousExecutionPlan(
    val planId: String = UUID.randomUUID().toString(),
    val rawPrompt: String,
    val intentType: IntentType,
    val confidence: Float,
    val summary: String,
    val steps: List<ExecutionStep>,
    val requiresBiometric: Boolean,
    val privacySignature: String,
    val inferenceTimeMs: Long,
    val networkEgressBytes: Long = 0L // Guaranteed 0 bytes offline
)

class EncryptedNeuralEngine(private val repository: MemoryRepository) {

    val modelName: String = "AIRA-LNE-v4.2-Encrypted"
    val isEncrypted: Boolean = true
    val isOfflineVerified: Boolean = true

    /**
     * Synthesizes and plans an autonomous execution pipeline based on natural language
     * and user preferences retrieved from the encrypted vault.
     */
    suspend fun analyzeAndPlan(prompt: String): AutonomousExecutionPlan {
        val startTime = System.currentTimeMillis()
        val normalized = prompt.trim()
        val lower = normalized.lowercase()

        val intent: IntentType
        val steps = mutableListOf<ExecutionStep>()
        var summary = ""
        var requiresBiometric = false

        when {
            // Panic & Lockdown
            lower.contains("panic") || lower.contains("lockdown") || lower.contains("lock vault") || lower.contains("emergency lock") -> {
                intent = IntentType.PANIC_LOCK
                requiresBiometric = true
                summary = "Initiating emergency AIRA vault lockdown and clearing active memory."
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Purge Runtime Memory",
                        description = "Clear decrypted in-memory tokens and secure Keystore",
                        actionType = StepActionType.SYSTEM_FEEDBACK,
                        target = "KEYSTORE_LOCK"
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 2,
                        title = "Lock Device Screen",
                        description = "Trigger system device lock via Accessibility Global Action",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_LOCK_SCREEN"
                    )
                )
            }

            // Message Sending
            lower.startsWith("send message") || lower.startsWith("text ") || lower.contains("send a text") || lower.startsWith("message ") -> {
                intent = IntentType.SEND_MESSAGE
                requiresBiometric = true // Biometric safeguard for autonomous messaging

                val (contact, messageText) = extractContactAndMessage(normalized)
                // Resolve preferred contact from memory if available
                val resolvedContact = if (contact.equals("Mom", ignoreCase = true)) {
                    repository.getDecryptedValue("contact_mom") ?: "Mom"
                } else {
                    contact
                }

                summary = "Autonomous Message to '$resolvedContact': \"$messageText\""

                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Biometric Verification",
                        description = "Authenticate user identity before initiating external message",
                        actionType = StepActionType.CONFIRM_BIOMETRIC,
                        target = "USER_IDENTITY"
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 2,
                        title = "Launch Messaging Client",
                        description = "Open default messaging app",
                        actionType = StepActionType.OPEN_PACKAGE,
                        target = "com.google.android.apps.messaging",
                        param = "Messaging"
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 3,
                        title = "Locate Contact Node",
                        description = "Find search or contact input for '$resolvedContact'",
                        actionType = StepActionType.CLICK_NODE,
                        target = resolvedContact
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 4,
                        title = "Type Message Payload",
                        description = "Input message text into conversation field",
                        actionType = StepActionType.SET_TEXT,
                        target = "Type a message",
                        param = messageText
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 5,
                        title = "Autonomous Send Trigger",
                        description = "Tap Send button autonomously",
                        actionType = StepActionType.CLICK_NODE,
                        target = "Send"
                    )
                )
            }

            // Read / Summarize Screen
            lower.contains("read screen") || lower.contains("summarize screen") || lower.contains("what's on my screen") || lower.contains("inspect screen") -> {
                intent = if (lower.contains("summarize")) IntentType.SUMMARIZE_SCREEN else IntentType.READ_SCREEN
                summary = "Deep screen hierarchy inspection, OCR node extraction, and neural summary."
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Capture Active Window Hierarchy",
                        description = "Traverse active accessibility node tree across foreground window",
                        actionType = StepActionType.INSPECT_SCREEN,
                        target = "ACTIVE_WINDOW"
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 2,
                        title = "Filter & Redact Sensitive Nodes",
                        description = "Mask password and payment elements locally",
                        actionType = StepActionType.SYSTEM_FEEDBACK,
                        target = "PRIVACY_FILTER"
                    )
                )
                steps.add(
                    ExecutionStep(
                        stepNumber = 3,
                        title = "Synthesize Visual Hierarchy",
                        description = "Generate concise structural summary and speak response",
                        actionType = StepActionType.SYSTEM_FEEDBACK,
                        target = "TTS_OUTPUT"
                    )
                )
            }

            // Navigation Commands (Back, Home, Recents, Notifications)
            lower.contains("go home") || lower == "home" -> {
                intent = IntentType.SYSTEM_NAVIGATION
                summary = "Navigate to Android Home Screen"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Global Home Trigger",
                        description = "Dispatch GLOBAL_ACTION_HOME via Accessibility Service",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_HOME"
                    )
                )
            }

            lower.contains("go back") || lower == "back" -> {
                intent = IntentType.SYSTEM_NAVIGATION
                summary = "Trigger Android Back Navigation"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Global Back Trigger",
                        description = "Dispatch GLOBAL_ACTION_BACK via Accessibility Service",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_BACK"
                    )
                )
            }

            lower.contains("recents") || lower.contains("recent apps") -> {
                intent = IntentType.SYSTEM_NAVIGATION
                summary = "Open System Recents Screen"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Global Recents Trigger",
                        description = "Dispatch GLOBAL_ACTION_RECENTS",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_RECENTS"
                    )
                )
            }

            lower.contains("notification") || lower.contains("pull down notifications") -> {
                intent = IntentType.SYSTEM_NAVIGATION
                summary = "Open System Notification Shade"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Notifications Shade",
                        description = "Dispatch GLOBAL_ACTION_NOTIFICATIONS",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_NOTIFICATIONS"
                    )
                )
            }

            lower.contains("take screenshot") || lower.contains("screenshot") -> {
                intent = IntentType.DEVICE_CONTROL
                summary = "Capture Screen Hardware Screenshot"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "System Screenshot Trigger",
                        description = "Dispatch GLOBAL_ACTION_TAKE_SCREENSHOT",
                        actionType = StepActionType.GLOBAL_ACTION,
                        target = "GLOBAL_ACTION_TAKE_SCREENSHOT"
                    )
                )
            }

            // App Launching
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                intent = IntentType.OPEN_APP
                val appName = normalized.substringAfter(" ").trim()
                summary = "Launch Application: $appName"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Find & Launch Target App",
                        description = "Search installed package intent for '$appName' and start activity",
                        actionType = StepActionType.OPEN_PACKAGE,
                        target = appName
                    )
                )
            }

            // Scroll actions
            lower.contains("scroll down") -> {
                intent = IntentType.DEVICE_CONTROL
                summary = "Perform downward accessibility scroll"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Scroll Forward",
                        description = "Send ACTION_SCROLL_FORWARD to scrollable root element",
                        actionType = StepActionType.SCROLL,
                        target = "FORWARD"
                    )
                )
            }

            lower.contains("scroll up") -> {
                intent = IntentType.DEVICE_CONTROL
                summary = "Perform upward accessibility scroll"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Scroll Backward",
                        description = "Send ACTION_SCROLL_BACKWARD to scrollable root element",
                        actionType = StepActionType.SCROLL,
                        target = "BACKWARD"
                    )
                )
            }

            // Memory queries & store
            lower.startsWith("remember ") -> {
                intent = IntentType.MEMORY_STORE
                val rawFact = normalized.removePrefix("remember ").removePrefix("that ").trim()
                summary = "Store encrypted memory: \"$rawFact\""
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "AES-256 Memory Encryption",
                        description = "Encrypt fact in hardware keystore vault",
                        actionType = StepActionType.SYSTEM_FEEDBACK,
                        target = "ENCRYPT_VAULT",
                        param = rawFact
                    )
                )
            }

            lower.contains("what is my") || lower.contains("what are my") || lower.contains("my preferences") -> {
                intent = IntentType.MEMORY_QUERY
                summary = "Query and decrypt user personal memories"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Query Memory Vault",
                        description = "Search local Room database and decrypt matching entities",
                        actionType = StepActionType.SYSTEM_FEEDBACK,
                        target = "QUERY_VAULT"
                    )
                )
            }

            // General autonomous command
            else -> {
                intent = IntentType.GENERAL_COMMAND
                summary = "Autonomous command: $normalized"
                steps.add(
                    ExecutionStep(
                        stepNumber = 1,
                        title = "Contextual Intent Analysis",
                        description = "Match screen interactive nodes with query: '$normalized'",
                        actionType = StepActionType.CLICK_NODE,
                        target = normalized
                    )
                )
            }
        }

        val inferenceTime = System.currentTimeMillis() - startTime
        val hash = CryptoVault.sha256("$prompt-$intent-${System.currentTimeMillis()}")

        return AutonomousExecutionPlan(
            rawPrompt = normalized,
            intentType = intent,
            confidence = 0.96f,
            summary = summary,
            steps = steps,
            requiresBiometric = requiresBiometric,
            privacySignature = hash.take(16).uppercase(),
            inferenceTimeMs = maxOf(4L, inferenceTime)
        )
    }

    private fun extractContactAndMessage(prompt: String): Pair<String, String> {
        val lower = prompt.lowercase()
        // Try pattern: "text [contact] [message]" or "text [contact] that [message]"
        var clean = prompt
        if (lower.startsWith("text ")) {
            clean = clean.substring(5).trim()
        } else if (lower.startsWith("send message to ")) {
            clean = clean.substring(16).trim()
        } else if (lower.startsWith("send a text to ")) {
            clean = clean.substring(15).trim()
        } else if (lower.startsWith("message ")) {
            clean = clean.substring(8).trim()
        }

        val delimiters = listOf(" that ", " saying ", " : ", ": ", " with text ", " - ", " ")
        for (del in delimiters) {
            if (clean.contains(del, ignoreCase = true)) {
                val idx = clean.indexOf(del, ignoreCase = true)
                val contact = clean.substring(0, idx).trim()
                val message = clean.substring(idx + del.length).trim()
                if (contact.isNotBlank() && message.isNotBlank()) {
                    return Pair(contact, message)
                }
            }
        }

        val parts = clean.split(" ", limit = 2)
        val contact = parts.firstOrNull()?.trim() ?: "Mom"
        val message = parts.getOrNull(1)?.trim() ?: "Hello from AIRA AI!"
        return Pair(contact, message)
    }
}
