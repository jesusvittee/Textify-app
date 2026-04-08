package com.textify.app.domain.usecase

import com.textify.app.domain.model.Phrase
import com.textify.app.domain.repository.PhraseRepository

class GetPhrasesUseCase(
    private val repository: PhraseRepository
) {
    suspend operator fun invoke(): List<Phrase> {
        return repository.getPhrases()
    }
}
