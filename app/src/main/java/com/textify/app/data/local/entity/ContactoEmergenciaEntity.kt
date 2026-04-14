package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "contactos_emergencia",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactoEmergenciaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val nombre: String,
    val telefono: String,
    val relacion: String?,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
