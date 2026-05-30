package com.medicamentos.app.medremind.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.context.GlobalContext

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tomaId = intent.getStringExtra(NotificationHelper.EXTRA_TOMA_ID) ?: return
        val medicamentoNombre = intent.getStringExtra(NotificationHelper.EXTRA_MEDICAMENTO) ?: return
        val dosis = intent.getStringExtra(NotificationHelper.EXTRA_DOSIS) ?: return
        val pacienteId = intent.getStringExtra(NotificationHelper.EXTRA_PACIENTE_ID) ?: return
        val tratamientoId = intent.getStringExtra(NotificationHelper.EXTRA_TRATAMIENTO_ID) ?: ""

        val notificationHelper: NotificationHelper = GlobalContext.get().get()
        notificationHelper.showMedicationReminder(tomaId, medicamentoNombre, dosis, pacienteId, tratamientoId)
    }
}
