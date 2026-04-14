package com.textify.app.domain.repository

import com.textify.app.domain.model.Phrase
import kotlinx.coroutines.flow.Flow

interface PhraseRepository {
    fun getPhrasesFlow(userId: String): Flow<List<Phrase>>
    suspend fun getPhrases(): List<Phrase>
    suspend fun addPhrase(phrase: Phrase)
    suspend fun deletePhrase(phraseId: String)
}
