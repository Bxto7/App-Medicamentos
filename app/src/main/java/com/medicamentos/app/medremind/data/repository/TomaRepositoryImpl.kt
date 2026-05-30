package com.medicamentos.app.medremind.data.repository

import com.medicamentos.app.medremind.data.local.dao.RegistroTomaDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.dao.TratamientoDao
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class TomaRepositoryImpl @Inject constructor(
    private val tomaProgramadaDao: TomaProgramadaDao,
    private val registroTomaDao: RegistroTomaDao,
    private val tratamientoDao: TratamientoDao
) : TomaRepository {

    override fun getTomasDelDia(pacienteId: String, inicioDelDia: Long, finDelDia: Long): Flow<List<TomaProgramada>> =
        tomaProgramadaDao.getByPacienteEnRango(pacienteId, inicioDelDia, finDelDia)
            .map { list -> list.map { it.toDomain() } }

    override fun getHistorial(pacienteId: String): Flow<List<RegistroToma>> =
        registroTomaDao.getByPaciente(pacienteId).map { list -> list.map { it.toDomain() } }

    override fun getRegistrosEnRango(pacienteId: String, inicio: Long, fin: Long): Flow<List<RegistroToma>> =
        registroTomaDao.getByPacienteEnRango(pacienteId, inicio, fin).map { list -> list.map { it.toDomain() } }

    override suspend fun marcarToma(
        tomaId: String,
        tratamientoId: String,
        pacienteId: String,
        medicamentoNombre: String,
        estado: EstadoToma
    ) {
        tomaProgramadaDao.updateEstado(tomaId, estado.name)
        registroTomaDao.insert(
            RegistroTomaEntity(
                id = UUID.randomUUID().toString(),
                tomaProgramadaId = tomaId,
                pacienteId = pacienteId,
                medicamentoNombre = medicamentoNombre,
                fechaHoraReal = System.currentTimeMillis(),
                estado = estado.name
            )
        )
        if (estado == EstadoToma.TOMADO) {
            tratamientoDao.decrementarStock(tratamientoId)
        }
    }

    override suspend fun getTotalHoy(pacienteId: String, inicio: Long, fin: Long): Int =
        tomaProgramadaDao.countEnRango(pacienteId, inicio, fin)

    override suspend fun getTomadas(pacienteId: String, inicio: Long, fin: Long): Int =
        tomaProgramadaDao.countTomadas(pacienteId, inicio, fin)

    override suspend fun getPendientesAntesDeTimestamp(
        pacienteId: String,
        timestamp: Long
    ): List<TomaProgramada> =
        tomaProgramadaDao.getPendientesVencidas(pacienteId, timestamp).map { it.toDomain() }
}
