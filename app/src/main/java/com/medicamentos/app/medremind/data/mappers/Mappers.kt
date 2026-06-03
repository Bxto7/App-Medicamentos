package com.medicamentos.app.medremind.data.mappers

import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
import com.medicamentos.app.medremind.data.local.entity.PacienteEntity
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity
import com.medicamentos.app.medremind.data.local.dao.PacienteAsociableRow
import com.medicamentos.app.medremind.data.local.dao.PacienteConPerfil
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.PacienteAsociable
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.model.Usuario

fun UsuarioEntity.toDomain() = Usuario(
    id = id, email = email, nombre = nombre, rol = Rol.valueOf(rol), avatarId = avatarId,
    telefono = telefono, telefonoFamiliar = telefonoFamiliar
)

fun PacienteEntity.toDomain() = Paciente(
    id = id, nombre = nombre, edad = edad, diagnostico = diagnostico,
    fotoUrl = fotoUrl, medicoAsignadoId = medicoAsignadoId
)

fun PacienteConPerfil.toDomain(medicoId: String) = Paciente(
    id = id,
    nombre = nombre,
    edad = edad ?: 0,
    diagnostico = diagnostico?.takeIf { it.isNotBlank() } ?: "Sin diagnóstico registrado",
    fotoUrl = fotoUrl,
    medicoAsignadoId = medicoId
)

fun PacienteAsociableRow.toDomain() = PacienteAsociable(
    usuarioId = id,
    nombre = nombre,
    email = email,
    yaAsociado = asociado != 0,
    diagnosticoActual = diagnostico ?: ""
)

fun MedicamentoEntity.toDomain() = Medicamento(
    id = id, nombre = nombre, dosis = dosis, via = via,
    instrucciones = instrucciones, fotoUrl = fotoUrl
)

fun TratamientoEntity.toDomain() = Tratamiento(
    id = id, pacienteId = pacienteId, medicamentoId = medicamentoId,
    medicamentoNombre = medicamentoNombre, dosis = dosis,
    frecuenciaHoras = frecuenciaHoras,
    horarios = horarios.split(",").filter { it.isNotBlank() },
    fechaInicio = fechaInicio, fechaFin = fechaFin,
    stockRestante = stockRestante, instrucciones = instrucciones,
    medicoId = medicoId, medicoNombre = medicoNombre
)

fun TomaProgramadaEntity.toDomain() = TomaProgramada(
    id = id, tratamientoId = tratamientoId, pacienteId = pacienteId,
    medicamentoNombre = medicamentoNombre, dosis = dosis,
    fechaHoraProgramada = fechaHoraProgramada, estado = EstadoToma.valueOf(estado)
)

fun RegistroTomaEntity.toDomain() = RegistroToma(
    id = id, tomaProgramadaId = tomaProgramadaId, pacienteId = pacienteId,
    medicamentoNombre = medicamentoNombre, fechaHoraReal = fechaHoraReal,
    estado = EstadoToma.valueOf(estado)
)
