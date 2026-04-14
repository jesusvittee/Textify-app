package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases")
    suspend fun getPhrases(): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE usuarioId = :usuarioId")
    fun getPhrasesByUsuario(usuarioId: String): Flow<List<PhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)

    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)

    @Query("DELETE FROM phrases WHERE id = :phraseId")
    suspend fun deletePhraseById(phraseId: String)

    @Query("DELETE FROM phrases WHERE usuarioId = :usuarioId")
    suspend fun deleteAllPhrasesByUsuario(usuarioId: String)

    @Query("SELECT * FROM phrases WHERE isSynced = 0")
    suspend fun getUnsyncedPhrases(): List<PhraseEntity>
}
