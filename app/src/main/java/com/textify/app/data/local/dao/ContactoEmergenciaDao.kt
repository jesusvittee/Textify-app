package com.textify.app.data.local.dao

import androidx.room.*
import com.textify.app.data.local.entity.ContactoEmergenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoEmergenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacto(contacto: ContactoEmergenciaEntity)

    @Delete
    suspend fun deleteContacto(contacto: ContactoEmergenciaEntity)

    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId")
    fun getContactosByUsuario(usuarioId: String): Flow<List<ContactoEmergenciaEntity>>

    @Query("SELECT * FROM contactos_emergencia WHERE isSynced = 0")
    suspend fun getUnsyncedContactos(): List<ContactoEmergenciaEntity>
}
