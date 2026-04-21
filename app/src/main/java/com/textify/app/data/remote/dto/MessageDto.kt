package com.textify.app.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.textify.app.data.util.BooleanTypeAdapter

data class MessageDto(
    val id: String,
    val conversationId: String,
    val text: String,
    @JsonAdapter(BooleanTypeAdapter::class)
    val isOwn: Boolean,
    val timestamp: Long,
    val updatedAt: Long
)