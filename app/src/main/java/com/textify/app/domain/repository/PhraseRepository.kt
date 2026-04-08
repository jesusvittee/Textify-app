package com.textify.app.domain.repository

import com.textify.app.domain.model.Phrase

interface PhraseRepository {
    suspend fun getPhrases(): List<Phrase>
    suspend fun addPhrase(phrase: Phrase)
    suspend fun deletePhrase(phraseId: String)
}
