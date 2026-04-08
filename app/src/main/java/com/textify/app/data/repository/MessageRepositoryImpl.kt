package com.textify.app.data.repository

import com.textify.app.data.local.dao.MessageDao
import com.textify.app.data.local.entity.MessageEntity
import com.textify.app.domain.model.Message
import com.textify.app.domain.repository.MessageRepository

class MessageRepositoryImpl(
    private val messageDao: MessageDao
) : MessageRepository {

    override suspend fun getMessages(conversationId: String): List<Message> {
        return messageDao.getMessages(conversationId).map { entity ->
            Message(
                id = entity.id,
                conversationId = entity.conversationId,
                text = entity.text,
                isOwn = entity.isOwn,
                timestamp = entity.timestamp
            )
        }
    }

    override suspend fun sendMessage(message: Message) {
        messageDao.insertMessage(
            MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                text = message.text,
                isOwn = message.isOwn,
                timestamp = message.timestamp
            )
        )
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }
}