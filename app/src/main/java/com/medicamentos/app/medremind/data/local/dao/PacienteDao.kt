package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.PacienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes WHERE medico_asignado_id = :medicoId ORDER BY nombre ASC")
    fun getByMedico(medicoId: String): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PacienteEntity?

    @Query("SELECT * FROM pacientes WHERE medico_asignado_id = :medicoId AND (nombre LIKE '%' || :query || '%' OR diagnostico LIKE '%' || :query || '%') ORDER BY nombre ASC")
    fun searchByMedico(medicoId: String, query: String): Flow<List<PacienteEntity>>

    @Query("SELECT COUNT(*) FROM pacientes WHERE medico_asignado_id = :medicoId")
    fun countByMedico(medicoId: String): Flow<Int>
}
