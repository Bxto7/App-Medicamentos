package com.medicamentos.app.medremind.data.repository

import com.medicamentos.app.medremind.data.local.dao.MedicamentoDao
import com.medicamentos.app.medremind.data.local.dao.MedicoPacienteDao
import com.medicamentos.app.medremind.data.local.dao.PacienteDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.dao.TratamientoDao
import com.medicamentos.app.medremind.data.local.entity.MedicoPacienteEntity
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.PacienteAsociable
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.repository.AsociacionRepository
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.repository.PacienteRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

/**
 * La lista de pacientes de un médico se deriva de la tabla de unión
 * [MedicoPacienteDao] (usuarios con rol PACIENTE asociados), no de la tabla
 * `pacientes`, que ahora guarda solo la ficha clínica opcional.
 */
class PacienteRepositoryImpl(
    private val medicoPacienteDao: MedicoPacienteDao,
    private val pacienteDao: PacienteDao
) : PacienteRepository {
    override fun getPacientesByMedico(medicoId: String): Flow<List<Paciente>> =
        medicoPacienteDao.getPacientesByMedico(medicoId).map { list -> list.map { it.toDomain(medicoId) } }

    override fun searchPacientes(medicoId: String, query: String): Flow<List<Paciente>> =
        medicoPacienteDao.searchPacientesByMedico(medicoId, query).map { list -> list.map { it.toDomain(medicoId) } }

    override suspend fun getPacienteById(id: String): Paciente? =
        pacienteDao.getById(id)?.toDomain()

    override fun countPacientes(medicoId: String): Flow<Int> =
        medicoPacienteDao.countByMedico(medicoId)
}

class AsociacionRepositoryImpl(
    private val medicoPacienteDao: MedicoPacienteDao
) : AsociacionRepository {
    override fun getPacientesAsociables(medicoId: String): Flow<List<PacienteAsociable>> =
        medicoPacienteDao.getPacientesAsociables(medicoId).map { list -> list.map { it.toDomain() } }

    override suspend fun asociar(medicoId: String, pacienteIds: List<String>) {
        val ahora = System.currentTimeMillis()
        pacienteIds.forEach { pacienteId ->
            medicoPacienteDao.insert(MedicoPacienteEntity(medicoId, pacienteId, ahora))
        }
    }

    override suspend fun desasociar(medicoId: String, pacienteId: String) =
        medicoPacienteDao.delete(medicoId, pacienteId)
}

class MedicamentoRepositoryImpl(
    private val medicamentoDao: MedicamentoDao
) : MedicamentoRepository {
    override fun getAll(): Flow<List<Medicamento>> =
        medicamentoDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(nombre: String, dosis: String, via: String, instrucciones: String) {
        medicamentoDao.insert(
            com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity(
                id = UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                dosis = dosis.trim(),
                via = via.trim(),
                instrucciones = instrucciones.trim(),
                fotoUrl = null
            )
        )
    }
}

class TratamientoRepositoryImpl(
    private val tratamientoDao: TratamientoDao,
    private val tomaProgramadaDao: TomaProgramadaDao
) : TratamientoRepository {

    override fun getByPaciente(pacienteId: String): Flow<List<Tratamiento>> =
        tratamientoDao.getByPaciente(pacienteId).map { list -> list.map { it.toDomain() } }

    override suspend fun add(
        pacienteId: String,
        medicamentoId: String,
        medicamentoNombre: String,
        dosis: String,
        frecuenciaHoras: Int,
        horarios: List<String>,
        fechaInicio: Long,
        fechaFin: Long?,
        stockInicial: Int,
        instrucciones: String,
        medicoId: String,
        medicoNombre: String
    ): List<TomaProgramada> {
        val tratamientoId = UUID.randomUUID().toString()
        val entity = TratamientoEntity(
            id = tratamientoId,
            pacienteId = pacienteId,
            medicamentoId = medicamentoId,
            medicamentoNombre = medicamentoNombre,
            dosis = dosis,
            frecuenciaHoras = frecuenciaHoras,
            horarios = horarios.joinToString(","),
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            stockRestante = stockInicial,
            instrucciones = instrucciones,
            medicoId = medicoId,
            medicoNombre = medicoNombre
        )
        tratamientoDao.insert(entity)

        val tomas = generarTomas(tratamientoId, pacienteId, medicamentoNombre, dosis, horarios, fechaInicio, fechaFin)
        tomaProgramadaDao.insertAll(tomas)
        return tomas.map { it.toDomain() }
    }

    private fun generarTomas(
        tratamientoId: String, pacienteId: String, medicamentoNombre: String,
        dosis: String, horarios: List<String>, fechaInicio: Long, fechaFin: Long?
    ): List<TomaProgramadaEntity> {
        val ahora = System.currentTimeMillis()
        val diasAGenerar = 30
        val cal = Calendar.getInstance().apply {
            timeInMillis = fechaInicio
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val inicioMs = cal.timeInMillis
        val tomas = mutableListOf<TomaProgramadaEntity>()

        for (dia in 0 until diasAGenerar) {
            val diaMs = inicioMs + dia * 86_400_000L
            if (fechaFin != null && diaMs > fechaFin) break
            horarios.forEach { hora ->
                val parts = hora.split(":")
                if (parts.size != 2) return@forEach
                val h = parts[0].toIntOrNull() ?: return@forEach
                val m = parts[1].toIntOrNull() ?: return@forEach
                val ts = diaMs + h * 3_600_000L + m * 60_000L
                if (ts > ahora) {
                    tomas.add(
                        TomaProgramadaEntity(
                            id = UUID.randomUUID().toString(),
                            tratamientoId = tratamientoId,
                            pacienteId = pacienteId,
                            medicamentoNombre = medicamentoNombre,
                            dosis = dosis,
                            fechaHoraProgramada = ts,
                            estado = "PENDIENTE"
                        )
                    )
                }
            }
        }
        return tomas
    }
}
