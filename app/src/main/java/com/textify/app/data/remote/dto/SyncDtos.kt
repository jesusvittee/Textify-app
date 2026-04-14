package com.textify.app.data.remote.dto

import com.textify.app.data.local.entity.*

data class UsuarioDto(
    val id: String,
    val nombre: String,
    val correo: String,
    val fechaRegistro: Long,
    val updatedAt: Long
)

data class ConfiguracionDto(
    val id: String,
    val usuarioId: String,
    val tamanoLetra: String,
    val idiomaPreferido: String,
    val velocidadVoz: Float,
    val temaVisual: String,
    val updatedAt: Long
)

data class ContactoDto(
    val id: String,
    val usuarioId: String,
    val nombre: String,
    val telefono: String,
    val relacion: String?,
    val updatedAt: Long
)

data class PhraseDto(
    val id: String,
    val usuarioId: String,
    val text: String,
    val categoria: String?,
    val isPinned: Boolean,
    val updatedAt: Long
)

data class ConversationDto(
    val id: String,
    val usuarioId: String,
    val participantName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val estado: String,
    val isPinned: Boolean,
    val updatedAt: Long
)

data class SyncMessageDto(
    val id: String,
    val conversationId: String,
    val text: String,
    val emisor: String,
    val timestamp: Long,
    val updatedAt: Long
)

// Extensiones para convertir de DTO a Entity
fun PhraseDto.toEntity() = PhraseEntity(
    id = id,
    usuarioId = usuarioId,
    text = text,
    categoria = categoria,
    isPinned = isPinned,
    updatedAt = updatedAt,
    isSynced = true
)

fun ConversationDto.toEntity() = ConversationEntity(
    id = id,
    usuarioId = usuarioId,
    participantName = participantName,
    lastMessage = lastMessage,
    lastMessageTime = lastMessageTime,
    estado = estado,
    isPinned = isPinned,
    updatedAt = updatedAt,
    isSynced = true
)
