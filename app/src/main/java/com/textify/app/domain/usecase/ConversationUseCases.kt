package com.textify.app.domain.usecase

import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class GetConversationsUseCase(private val repository: ConversationRepository) {
    operator fun invoke(): Flow<List<ConversationEntity>> = repository.getAllConversations()
}

class CreateConversationUseCase(private val repository: ConversationRepository) {
    suspend operator fun invoke(conversation: ConversationEntity) = repository.insertConversation(conversation)
}

class UpdateConversationUseCase(private val repository: ConversationRepository) {
    suspend operator fun invoke(conversation: ConversationEntity) = repository.updateConversation(conversation)
}

class DeleteConversationUseCase(private val repository: ConversationRepository) {
    suspend operator fun invoke(id: String) = repository.deleteConversation(id)
}
