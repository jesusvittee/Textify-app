package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuario(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioByCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE isSynced = 0")
    suspend fun getUnsyncedUsuarios(): List<UsuarioEntity>
}
