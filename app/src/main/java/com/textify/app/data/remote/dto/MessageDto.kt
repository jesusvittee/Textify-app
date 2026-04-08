package com.textify.app.data.remote.dto

data class MessageDto(
    val id: String,
    val conversationId: String,
    val text: String,
    val isOwn: Boolean,
    val timestamp: Long
)