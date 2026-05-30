package com.medicamentos.app.medremind.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medicamentos.app.medremind.data.local.AppDatabase
import com.medicamentos.app.medremind.data.local.SessionManager
import com.medicamentos.app.medremind.data.mappers.toDomain
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker periódico que detecta tomas PENDIENTE cuya hora programada
 * ya pasó hace más de 2 horas y envía alertas a los contactos de emergencia.
 *
 * Se ejecuta cada hora. Si ya se alertó por una toma (estado != PENDIENTE),
 * no se repite la alerta.
 */
class CheckMissedDosesWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val db = GlobalContext.get().get<AppDatabase>()
            val sessionManager = GlobalContext.get().get<SessionManager>()

            val pacienteId = sessionManager.getUserId().first() ?: return Result.success()
            val dosHorasAtras = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)

            // Tomas PENDIENTE cuya hora ya pasó hace más de 2h
            val tomasPendientes = db.tomaProgramadaDao()
                .getPendientesVencidas(pacienteId, dosHorasAtras)

            if (tomasPendientes.isEmpty()) return Result.success()

            val contactos = db.contactoEmergenciaDao().getByUsuarioSync(pacienteId)
            if (contactos.isEmpty()) return Result.success()

            val fmt = SimpleDateFormat("HH:mm dd/MM", Locale("es", "PE"))

            tomasPendientes.forEach { toma ->
                val horaStr = fmt.format(Date(toma.fechaHoraProgramada))
                contactos.forEach { contacto ->
                    EmergencyAlertSender.sendWhatsAppAlert(
                        context = context,
                        telefono = contacto.telefono,
                        nombrePaciente = "el paciente",
                        medicamento = toma.medicamentoNombre,
                        horaProgramada = horaStr
                    )
                    EmergencyAlertSender.sendEmailAlert(
                        context = context,
                        correo = contacto.correo,
                        nombrePaciente = "el paciente",
                        medicamento = toma.medicamentoNombre,
                        horaProgramada = horaStr
                    )
                }
            }

            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "check_missed_doses"
    }
}
