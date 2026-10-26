package com.example.data.model

import java.util.UUID

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val timestamp: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)
