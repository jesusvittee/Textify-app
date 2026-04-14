package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val correo: String,
    val contrasena: String, // Almacenada como hash
    val fechaRegistro: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
