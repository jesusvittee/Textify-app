package com.textify.app.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val isOwn: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)