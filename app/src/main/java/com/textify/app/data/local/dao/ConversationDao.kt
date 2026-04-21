package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE usuarioId = :usuarioId ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getConversationsByUsuario(usuarioId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE usuarioId = :usuarioId")
    suspend fun getConversationsByUsuarioSync(usuarioId: String): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM conversations WHERE usuarioId = :usuarioId")
    suspend fun deleteAllConversationsByUsuario(usuarioId: String)

    @Query("SELECT * FROM conversations WHERE isSynced = 0")
    suspend fun getUnsyncedConversations(): List<ConversationEntity>
}
