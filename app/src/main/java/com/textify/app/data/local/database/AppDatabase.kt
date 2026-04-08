package com.textify.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.textify.app.data.local.dao.ConversationDao
import com.textify.app.data.local.dao.MessageDao
import com.textify.app.data.local.dao.PhraseDao
import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.data.local.entity.MessageEntity
import com.textify.app.data.local.entity.PhraseEntity

@Database(
    entities = [MessageEntity::class, PhraseEntity::class, ConversationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun phraseDao(): PhraseDao
    abstract fun conversationDao(): ConversationDao
}
