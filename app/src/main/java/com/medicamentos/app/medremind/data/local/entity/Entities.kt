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
    val instrucciones: String
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

@Entity(tableName = "contactos_emergencia")
data class ContactoEmergenciaEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "usuario_id") val usuarioId: String,
    val nombre: String,
    val telefono: String,
    val correo: String,
    val orden: Int  // 1 o 2
)

@Entity(tableName = "registros_glucosa")
data class GlucosaEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "paciente_id") val pacienteId: String,
    val valor: Int,             // mg/dL
    val momento: String,        // "ayunas","post-desayuno","post-almuerzo","post-cena"
    val timestamp: Long,
    val notas: String?
)
