package com.textify.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val gender: Gender = Gender.MASCULINO,
    val selectedVoiceId: String = "default_offline"
)

enum class Gender {
    MASCULINO, FEMENINO
}
