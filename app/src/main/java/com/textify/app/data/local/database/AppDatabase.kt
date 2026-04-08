package com.textify.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.textify.app.data.local.dao.MessageDao
import com.textify.app.data.local.dao.PhraseDao
import com.textify.app.data.local.entity.MessageEntity
import com.textify.app.data.local.entity.PhraseEntity

@Database(
    entities = [MessageEntity::class, PhraseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun phraseDao(): PhraseDao
}