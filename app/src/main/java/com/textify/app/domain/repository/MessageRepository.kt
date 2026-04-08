package com.textify.app.domain.repository

import com.textify.app.domain.model.Message

interface MessageRepository {
    suspend fun getMessages(conversationId: String): List<Message>
    suspend fun sendMessage(message: Message)
    suspend fun deleteMessage(messageId: String)
}
