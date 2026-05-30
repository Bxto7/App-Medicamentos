package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.ContactoEmergenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoEmergenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contactos: List<ContactoEmergenciaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contacto: ContactoEmergenciaEntity)

    @Query("SELECT * FROM contactos_emergencia WHERE usuario_id = :usuarioId ORDER BY orden ASC")
    fun getByUsuario(usuarioId: String): Flow<List<ContactoEmergenciaEntity>>

    @Query("SELECT * FROM contactos_emergencia WHERE usuario_id = :usuarioId ORDER BY orden ASC")
    suspend fun getByUsuarioSync(usuarioId: String): List<ContactoEmergenciaEntity>

    @Query("DELETE FROM contactos_emergencia WHERE usuario_id = :usuarioId")
    suspend fun deleteByUsuario(usuarioId: String)
}
