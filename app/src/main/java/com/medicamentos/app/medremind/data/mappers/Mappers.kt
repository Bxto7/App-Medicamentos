package com.medicamentos.app.medremind.data.mappers

import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
import com.medicamentos.app.medremind.data.local.entity.PacienteEntity
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.model.Usuario

fun UsuarioEntity.toDomain() = Usuario(
    id = id, email = email, nombre = nombre, rol = Rol.valueOf(rol), avatarId = avatarId
)

fun PacienteEntity.toDomain() = Paciente(
    id = id, nombre = nombre, edad = edad, diagnostico = diagnostico,
    fotoUrl = fotoUrl, medicoAsignadoId = medicoAsignadoId
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
    stockRestante = stockRestante, instrucciones = instrucciones
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
