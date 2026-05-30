package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.GlucosaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucosaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(glucosa: GlucosaEntity)

    @Query("SELECT * FROM registros_glucosa WHERE paciente_id = :pacienteId ORDER BY timestamp DESC")
    fun getByPaciente(pacienteId: String): Flow<List<GlucosaEntity>>

    @Query("SELECT * FROM registros_glucosa WHERE paciente_id = :pacienteId AND timestamp >= :desde ORDER BY timestamp ASC")
    fun getDesde(pacienteId: String, desde: Long): Flow<List<GlucosaEntity>>

    @Query("SELECT * FROM registros_glucosa WHERE paciente_id = :pacienteId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getUltima(pacienteId: String): GlucosaEntity?

    @Query("DELETE FROM registros_glucosa WHERE id = :id")
    suspend fun delete(id: String)
}
