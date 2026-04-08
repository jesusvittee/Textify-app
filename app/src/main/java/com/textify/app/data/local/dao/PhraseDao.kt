package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.PhraseEntity

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases")
    suspend fun getPhrases(): List<PhraseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)

    @Query("DELETE FROM phrases WHERE id = :phraseId")
    suspend fun deletePhrase(phraseId: String)
}