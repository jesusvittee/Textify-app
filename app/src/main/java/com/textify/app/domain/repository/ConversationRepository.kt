package com.textify.app.domain.repository

import com.textify.app.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<ConversationEntity>>
    suspend fun insertConversation(conversation: ConversationEntity)
    suspend fun updateConversation(conversation: ConversationEntity)
    suspend fun deleteConversation(id: String)
}
