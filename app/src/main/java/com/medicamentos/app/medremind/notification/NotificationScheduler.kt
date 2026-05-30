package com.medicamentos.app.medremind.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity

class NotificationScheduler(
    private val context: Context,
    private val tomaProgramadaDao: TomaProgramadaDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleForToma(toma: TomaProgramadaEntity) {
        if (toma.estado != "PENDIENTE") return
        if (toma.fechaHoraProgramada <= System.currentTimeMillis()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TOMA_ID, toma.id)
            putExtra(NotificationHelper.EXTRA_MEDICAMENTO, toma.medicamentoNombre)
            putExtra(NotificationHelper.EXTRA_DOSIS, toma.dosis)
            putExtra(NotificationHelper.EXTRA_PACIENTE_ID, toma.pacienteId)
            putExtra(NotificationHelper.EXTRA_TRATAMIENTO_ID, toma.tratamientoId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, toma.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            toma.fechaHoraProgramada,
            pendingIntent
        )
    }

    fun cancelForToma(tomaId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            context, tomaId.hashCode(),
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }

    suspend fun scheduleAllPendientes() {
        val tomas = tomaProgramadaDao.getAllPendientesFuturas(System.currentTimeMillis())
        tomas.forEach { scheduleForToma(it) }
    }
}
