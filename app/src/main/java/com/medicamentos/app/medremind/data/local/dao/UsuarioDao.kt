package com.medicamentos.app.medremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email AND password_hash = :passwordHash LIMIT 1")
    suspend fun findByEmailAndPassword(email: String, passwordHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UsuarioEntity?

    @Query("UPDATE usuarios SET nombre = :nombre WHERE id = :id")
    suspend fun updateNombre(id: String, nombre: String)
}
