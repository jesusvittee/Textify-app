package com.textify.app.data.repository

import com.textify.app.data.local.dao.PhraseDao
import com.textify.app.data.local.entity.PhraseEntity
import com.textify.app.domain.model.Phrase
import com.textify.app.domain.repository.PhraseRepository

class PhraseRepositoryImpl(
    private val phraseDao: PhraseDao
) : PhraseRepository {

    override suspend fun getPhrases(): List<Phrase> {
        return phraseDao.getPhrases().map { entity ->
            Phrase(
                id = entity.id,
                text = entity.text,
                usuarioId = entity.usuarioId,
                isPinned = entity.isPinned
            )
        }
    }

    override suspend fun addPhrase(phrase: Phrase) {
        phraseDao.insertPhrase(
            PhraseEntity(
                id = phrase.id,
                usuarioId = phrase.usuarioId,
                text = phrase.text,
                isPinned = phrase.isPinned
            )
        )
    }

    override suspend fun deletePhrase(phraseId: String) {
        phraseDao.deletePhraseById(phraseId)
    }
}
