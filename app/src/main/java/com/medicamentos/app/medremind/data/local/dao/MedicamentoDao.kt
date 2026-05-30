package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicamento: MedicamentoEntity)

    @Query("SELECT * FROM medicamentos ORDER BY nombre ASC")
    fun getAll(): Flow<List<MedicamentoEntity>>

    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MedicamentoEntity?
}
