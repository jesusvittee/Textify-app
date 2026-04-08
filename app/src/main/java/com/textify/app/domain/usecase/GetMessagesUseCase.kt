package com.textify.app.domain.usecase

import com.textify.app.domain.model.Message
import com.textify.app.domain.repository.MessageRepository

class GetMessagesUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(conversationId: String): List<Message> {
        return repository.getMessages(conversationId)
    }
}
