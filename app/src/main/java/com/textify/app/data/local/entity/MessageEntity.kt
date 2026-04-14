package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.textify.app.domain.model.MessageType
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val text: String, // Contenido_Texto
    val isOwn: Boolean, // Mapea a Emisor (Usuario vs Interlocutor)
    val emisor: String = "Usuario", // Usuario, Interlocutor, Interprete
    val type: MessageType = MessageType.TEXT, // Tipo_Procesamiento
    val timestamp: Long = System.currentTimeMillis(), // Fecha_Hora
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
