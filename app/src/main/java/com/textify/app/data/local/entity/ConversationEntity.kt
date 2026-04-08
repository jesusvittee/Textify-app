package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val participantName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val isPinned: Boolean = false
)
