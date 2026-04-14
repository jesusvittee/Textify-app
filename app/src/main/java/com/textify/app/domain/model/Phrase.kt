package com.textify.app.domain.model

data class Phrase(
    val id: String,
    val text: String,
    val usuarioId: String = "default_user", // Added default to avoid breaking too many things
    val isPinned: Boolean = false
)
