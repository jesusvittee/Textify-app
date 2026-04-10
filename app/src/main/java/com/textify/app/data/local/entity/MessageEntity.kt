package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.textify.app.domain.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val text: String,
    val isOwn: Boolean,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long
)
