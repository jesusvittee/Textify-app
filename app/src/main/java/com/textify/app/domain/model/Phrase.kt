package com.textify.app.domain.model

data class Phrase(
    val id: String,
    val text: String,
    val isPinned: Boolean = false
)