package com.textify.app.domain.usecase

import com.textify.app.domain.model.Phrase
import com.textify.app.domain.repository.PhraseRepository
import kotlinx.coroutines.flow.Flow

class GetPhrasesUseCase(
    private val repository: PhraseRepository
) {
    operator fun invoke(userId: String): Flow<List<Phrase>> {
        return repository.getPhrasesFlow(userId)
    }
}
