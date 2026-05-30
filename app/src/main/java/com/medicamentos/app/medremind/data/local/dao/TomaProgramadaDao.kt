package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TomaProgramadaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tomas: List<TomaProgramadaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(toma: TomaProgramadaEntity)

    @Query("SELECT * FROM tomas_programadas WHERE paciente_id = :pacienteId AND fecha_hora_programada BETWEEN :inicio AND :fin ORDER BY fecha_hora_programada ASC")
    fun getByPacienteEnRango(pacienteId: String, inicio: Long, fin: Long): Flow<List<TomaProgramadaEntity>>

    @Query("UPDATE tomas_programadas SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: String, estado: String)

    @Query("SELECT * FROM tomas_programadas WHERE estado = 'PENDIENTE' AND fecha_hora_programada > :ahora ORDER BY fecha_hora_programada ASC")
    suspend fun getAllPendientesFuturas(ahora: Long): List<TomaProgramadaEntity>

    @Query("SELECT * FROM tomas_programadas WHERE paciente_id = :pacienteId AND estado = 'PENDIENTE' AND fecha_hora_programada < :ahora")
    suspend fun getPendientesVencidas(pacienteId: String, ahora: Long): List<TomaProgramadaEntity>

    @Query("SELECT COUNT(*) FROM tomas_programadas WHERE paciente_id = :pacienteId AND fecha_hora_programada BETWEEN :inicio AND :fin")
    suspend fun countEnRango(pacienteId: String, inicio: Long, fin: Long): Int

    @Query("SELECT COUNT(*) FROM tomas_programadas WHERE paciente_id = :pacienteId AND estado = 'TOMADO' AND fecha_hora_programada BETWEEN :inicio AND :fin")
    suspend fun countTomadas(pacienteId: String, inicio: Long, fin: Long): Int
}
