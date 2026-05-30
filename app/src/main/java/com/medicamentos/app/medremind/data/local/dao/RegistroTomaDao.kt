package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroTomaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroTomaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(registros: List<RegistroTomaEntity>)

    @Query("SELECT * FROM registros_toma WHERE paciente_id = :pacienteId ORDER BY fecha_hora_real DESC")
    fun getByPaciente(pacienteId: String): Flow<List<RegistroTomaEntity>>

    @Query("SELECT * FROM registros_toma WHERE paciente_id = :pacienteId AND fecha_hora_real BETWEEN :inicio AND :fin ORDER BY fecha_hora_real DESC")
    fun getByPacienteEnRango(pacienteId: String, inicio: Long, fin: Long): Flow<List<RegistroTomaEntity>>
}
