package com.textify.app.domain.usecase

import com.textify.app.domain.model.Message
import com.textify.app.domain.repository.MessageRepository

class SendMessageUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(message: Message) {
        repository.sendMessage(message)
    }
}