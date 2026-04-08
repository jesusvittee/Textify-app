package com.textify.app.data.remote.dto

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String = ""
)