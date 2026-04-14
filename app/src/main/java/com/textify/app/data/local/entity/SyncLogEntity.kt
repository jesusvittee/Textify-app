package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sync_logs",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SyncLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val tablaAfectada: String,
    val idRegistroLocal: String,
    val accion: String, // INSERT, UPDATE, DELETE
    val estado: String = "Pendiente", // Pendiente, Sincronizado, Error
    val fechaRegistro: Long = System.currentTimeMillis()
)
