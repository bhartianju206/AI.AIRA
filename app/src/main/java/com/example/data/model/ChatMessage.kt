package com.example.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    AIRA
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSummary: String? = null,
    val actionType: String? = null,
    val isError: Boolean = false,
    val canRetry: Boolean = false,
    val hasScreenAttachment: Boolean = false
)
