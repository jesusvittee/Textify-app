package com.textify.app.domain.usecase

import com.textify.app.domain.repository.PhraseRepository

class DeletePhraseUseCase(
    private val repository: PhraseRepository
) {
    suspend operator fun invoke(phraseId: String) {
        repository.deletePhrase(phraseId)
    }
}
