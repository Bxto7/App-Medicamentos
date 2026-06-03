package com.medicamentos.app.medremind.domain.model

enum class Rol { MEDICO, PACIENTE }

enum class EstadoToma { PENDIENTE, TOMADO, OMITIDO, TARDE }

data class Usuario(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: Rol,
    val avatarId: Int = 0,
    val telefono: String? = null,
    val telefonoFamiliar: String? = null
)

data class Paciente(
    val id: String,
    val nombre: String,
    val edad: Int,
    val diagnostico: String,
    val fotoUrl: String?,
    val medicoAsignadoId: String
)

data class Medicamento(
    val id: String,
    val nombre: String,
    val dosis: String,
    val via: String,
    val instrucciones: String,
    val fotoUrl: String?
)

data class Tratamiento(
    val id: String,
    val pacienteId: String,
    val medicamentoId: String,
    val medicamentoNombre: String,
    val dosis: String,
    val frecuenciaHoras: Int,
    val horarios: List<String>,
    val fechaInicio: Long,
    val fechaFin: Long?,
    val stockRestante: Int,
    val instrucciones: String,
    val medicoId: String = "",
    val medicoNombre: String = ""
)

/**
 * Usuario con rol PACIENTE que un médico puede asociar a su cuenta, junto con
 * si ya está asociado actualmente.
 */
data class PacienteAsociable(
    val usuarioId: String,
    val nombre: String,
    val email: String,
    val yaAsociado: Boolean,
    val diagnosticoActual: String = ""
)

data class TomaProgramada(
    val id: String,
    val tratamientoId: String,
    val pacienteId: String,
    val medicamentoNombre: String,
    val dosis: String,
    val fechaHoraProgramada: Long,
    val estado: EstadoToma
)

data class RegistroToma(
    val id: String,
    val tomaProgramadaId: String,
    val pacienteId: String,
    val medicamentoNombre: String,
    val fechaHoraReal: Long,
    val estado: EstadoToma
)

data class ResumenAdherencia(
    val totalProgramadas: Int,
    val totalTomadas: Int,
    val porcentaje: Float = if (totalProgramadas > 0) totalTomadas.toFloat() / totalProgramadas * 100 else 0f
)

