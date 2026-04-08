package com.textify.app.domain.model

data class Conversation(
    val id: String,
    val participantName: String,
    val lastMessage: String,
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)