package com.textify.app.domain.usecase

import com.textify.app.domain.model.Phrase
import com.textify.app.domain.repository.PhraseRepository

class AddPhraseUseCase(
    private val repository: PhraseRepository
) {
    suspend operator fun invoke(phrase: Phrase) {
        repository.addPhrase(phrase)
    }
}
