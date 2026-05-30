package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TratamientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tratamiento: TratamientoEntity)

    @Query("SELECT * FROM tratamientos WHERE paciente_id = :pacienteId")
    fun getByPaciente(pacienteId: String): Flow<List<TratamientoEntity>>

    @Query("SELECT * FROM tratamientos WHERE paciente_id = :pacienteId")
    suspend fun getByPacienteOnce(pacienteId: String): List<TratamientoEntity>

    @Query("UPDATE tratamientos SET stock_restante = stock_restante - 1 WHERE id = :id AND stock_restante > 0")
    suspend fun decrementarStock(id: String)
}
