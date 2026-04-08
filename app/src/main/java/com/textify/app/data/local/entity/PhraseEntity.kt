package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val isPinned: Boolean = false
)