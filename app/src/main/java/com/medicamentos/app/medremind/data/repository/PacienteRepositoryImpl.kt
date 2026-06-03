package com.medicamentos.app.medremind.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.medicamentos.app.medremind.data.local.dao.MedicamentoDao
import com.medicamentos.app.medremind.data.local.dao.MedicoPacienteDao
import com.medicamentos.app.medremind.data.local.dao.PacienteDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.dao.TratamientoDao
import com.medicamentos.app.medremind.data.local.entity.MedicoPacienteEntity
import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

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
    private val medicoPacienteDao: MedicoPacienteDao,
    private val firestore: FirebaseFirestore
) : AsociacionRepository {

    override fun getPacientesAsociables(medicoId: String): Flow<List<PacienteAsociable>> =
        medicoPacienteDao.getPacientesAsociables(medicoId).map { list -> list.map { it.toDomain() } }

    override suspend fun asociar(medicoId: String, diagnosticosPorPaciente: Map<String, String>) {
        val ahora = System.currentTimeMillis()
        diagnosticosPorPaciente.forEach { (pacienteId, diagnostico) ->
            val diag = diagnostico.trim()
            medicoPacienteDao.insert(MedicoPacienteEntity(medicoId, pacienteId, ahora, diag))
            // Si ya existía la asociación (insert IGNORE), igual actualizamos el diagnóstico.
            medicoPacienteDao.updateDiagnostico(medicoId, pacienteId, diag)
            // Sync a Firestore (async, no bloquea)
            firestoreSync {
                firestore.collection("asociaciones")
                    .document("${medicoId}_${pacienteId}")
                    .set(mapOf("medicoId" to medicoId, "pacienteId" to pacienteId, "fechaAsociacion" to ahora, "diagnostico" to diag))
                    .await()
            }
        }
    }

    override suspend fun desasociar(medicoId: String, pacienteId: String) {
        medicoPacienteDao.delete(medicoId, pacienteId)
        firestoreSync {
            firestore.collection("asociaciones")
                .document("${medicoId}_${pacienteId}")
                .delete().await()
        }
    }
}

class MedicamentoRepositoryImpl(
    private val medicamentoDao: MedicamentoDao,
    private val firestore: FirebaseFirestore
) : MedicamentoRepository {

    override fun getAll(): Flow<List<Medicamento>> =
        medicamentoDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(nombre: String, dosis: String, via: String, instrucciones: String) {
        val id = UUID.randomUUID().toString()
        val entity = MedicamentoEntity(id, nombre.trim(), dosis.trim(), via.trim(), instrucciones.trim(), null)
        medicamentoDao.insert(entity)
        // Sync a Firestore
        firestoreSync {
            firestore.collection("medicamentos").document(id).set(
                mapOf("nombre" to nombre.trim(), "dosis" to dosis.trim(), "via" to via.trim(), "instrucciones" to instrucciones.trim())
            ).await()
        }
    }
}

class TratamientoRepositoryImpl(
    private val tratamientoDao: TratamientoDao,
    private val tomaProgramadaDao: TomaProgramadaDao,
    private val firestore: FirebaseFirestore
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

        // Sync tratamiento a Firestore (async)
        firestoreSync {
            firestore.collection("tratamientos").document(tratamientoId).set(
                mapOf(
                    "pacienteId" to pacienteId,
                    "medicamentoId" to medicamentoId,
                    "medicamentoNombre" to medicamentoNombre,
                    "dosis" to dosis,
                    "frecuenciaHoras" to frecuenciaHoras,
                    "horarios" to horarios,
                    "fechaInicio" to fechaInicio,
                    "fechaFin" to fechaFin,
                    "stockRestante" to stockInicial,
                    "instrucciones" to instrucciones,
                    "medicoId" to medicoId,
                    "medicoNombre" to medicoNombre
                )
            ).await()
        }

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

// Helper: ejecuta el bloque de Firestore en background sin bloquear el caller
private fun firestoreSync(block: suspend () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try { block() } catch (_: Exception) { /* Sin red: se sincronizará luego */ }
    }
}
