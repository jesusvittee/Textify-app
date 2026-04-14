package com.textify.app.data.repository

import com.textify.app.data.local.dao.ConversationDao
import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao
) : ConversationRepository {
    override fun getAllConversations(): Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    
    override suspend fun insertConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    override suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    override suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversation(id)
    }
}
