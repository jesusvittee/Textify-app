package com.textify.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "configuraciones_accesibilidad",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConfiguracionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val tamanoLetra: String = "Mediana",
    val fontScale: Float = 1.0f,
    val idiomaPreferido: String = "Español",
    val velocidadVoz: Float = 1.0f,
    val temaVisual: String = "Claro",
    val isDarkMode: Boolean = false,
    val voiceGender: String = "MALE",
    val selectedVoiceId: String = "default_offline",
    val localHistoryEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
