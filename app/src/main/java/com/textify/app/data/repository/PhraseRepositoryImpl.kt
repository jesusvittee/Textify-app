package com.textify.app.data.repository

import com.textify.app.data.local.dao.PhraseDao
import com.textify.app.data.local.entity.PhraseEntity
import com.textify.app.domain.model.Phrase
import com.textify.app.domain.repository.PhraseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhraseRepositoryImpl(
    private val phraseDao: PhraseDao
) : PhraseRepository {

    override fun getPhrasesFlow(userId: String): Flow<List<Phrase>> {
        // CORRECCIÓN: Usamos el userId real pasado desde el ViewModel
        return phraseDao.getPhrasesByUsuario(userId).map { entities ->
            entities.map { entity ->
                Phrase(
                    id = entity.id,
                    text = entity.text,
                    usuarioId = entity.usuarioId,
                    isPinned = entity.isPinned
                )
            }
        }
    }

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
                isPinned = phrase.isPinned,
                isSynced = false
            )
        )
    }

    override suspend fun deletePhrase(phraseId: String) {
        phraseDao.deletePhraseById(phraseId)
    }
}
