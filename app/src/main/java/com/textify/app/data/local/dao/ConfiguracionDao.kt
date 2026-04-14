package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracion(configuracion: ConfiguracionEntity)

    @Update
    suspend fun updateConfiguracion(configuracion: ConfiguracionEntity)

    @Query("SELECT * FROM configuraciones_accesibilidad WHERE usuarioId = :usuarioId LIMIT 1")
    fun getConfiguracionByUsuario(usuarioId: String): Flow<ConfiguracionEntity?>

    @Query("SELECT * FROM configuraciones_accesibilidad WHERE isSynced = 0")
    suspend fun getUnsyncedConfiguraciones(): List<ConfiguracionEntity>
}
