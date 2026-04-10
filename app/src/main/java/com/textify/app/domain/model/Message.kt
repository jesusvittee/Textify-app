package com.textify.app.domain.model

enum class MessageType { TEXT, AUDIO }

data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val isOwn: Boolean,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)
