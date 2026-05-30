package com.medicamentos.app.medremind.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val email: String,
    val nombre: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val rol: String,
    @ColumnInfo(name = "avatar_id") val avatarId: Int = 0
)

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val edad: Int,
    val diagnostico: String,
    @ColumnInfo(name = "foto_url") val fotoUrl: String?,
    @ColumnInfo(name = "medico_asignado_id") val medicoAsignadoId: String
)

@Entity(tableName = "medicamentos")
data class MedicamentoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val dosis: String,
    val via: String,
    val instrucciones: String,
    @ColumnInfo(name = "foto_url") val fotoUrl: String?
)

@Entity(tableName = "tratamientos")
data class TratamientoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "paciente_id") val pacienteId: String,
    @ColumnInfo(name = "medicamento_id") val medicamentoId: String,
    @ColumnInfo(name = "medicamento_nombre") val medicamentoNombre: String,
    val dosis: String,
    @ColumnInfo(name = "frecuencia_horas") val frecuenciaHoras: Int,
    val horarios: String,
    @ColumnInfo(name = "fecha_inicio") val fechaInicio: Long,
    @ColumnInfo(name = "fecha_fin") val fechaFin: Long?,
    @ColumnInfo(name = "stock_restante") val stockRestante: Int,
    val instrucciones: String,
    @ColumnInfo(name = "medico_id", defaultValue = "") val medicoId: String = "",
    @ColumnInfo(name = "medico_nombre", defaultValue = "") val medicoNombre: String = ""
)

/**
 * Tabla de unión N:N entre médicos y pacientes. Ambos IDs referencian
 * usuarios.id: medicoId apunta a un Usuario con rol MEDICO y pacienteId a uno
 * con rol PACIENTE. La clave primaria compuesta impide asociaciones duplicadas.
 */
@Entity(tableName = "medico_paciente", primaryKeys = ["medico_id", "paciente_id"])
data class MedicoPacienteEntity(
    @ColumnInfo(name = "medico_id") val medicoId: String,
    @ColumnInfo(name = "paciente_id") val pacienteId: String,
    @ColumnInfo(name = "fecha_asociacion") val fechaAsociacion: Long
)

@Entity(tableName = "tomas_programadas")
data class TomaProgramadaEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "tratamiento_id") val tratamientoId: String,
    @ColumnInfo(name = "paciente_id") val pacienteId: String,
    @ColumnInfo(name = "medicamento_nombre") val medicamentoNombre: String,
    val dosis: String,
    @ColumnInfo(name = "fecha_hora_programada") val fechaHoraProgramada: Long,
    val estado: String
)

@Entity(tableName = "registros_toma")
data class RegistroTomaEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "toma_programada_id") val tomaProgramadaId: String,
    @ColumnInfo(name = "paciente_id") val pacienteId: String,
    @ColumnInfo(name = "medicamento_nombre") val medicamentoNombre: String,
    @ColumnInfo(name = "fecha_hora_real") val fechaHoraReal: Long,
    val estado: String
)
