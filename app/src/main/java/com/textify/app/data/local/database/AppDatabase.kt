package com.textify.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.textify.app.data.local.dao.*
import com.textify.app.data.local.entity.*

@Database(
    entities = [
        UsuarioEntity::class,
        ConfiguracionEntity::class,
        ContactoEmergenciaEntity::class,
        PhraseEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        SyncLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun configuracionDao(): ConfiguracionDao
    abstract fun contactoEmergenciaDao(): ContactoEmergenciaDao
    abstract fun phraseDao(): PhraseDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
