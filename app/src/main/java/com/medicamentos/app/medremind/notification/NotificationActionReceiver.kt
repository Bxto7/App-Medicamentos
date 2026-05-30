package com.medicamentos.app.medremind.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medicamentos.app.medremind.data.local.dao.RegistroTomaDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.UUID

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_MARK_TAKEN) return
        val tomaId = intent.getStringExtra(NotificationHelper.EXTRA_TOMA_ID) ?: return
        val pacienteId = intent.getStringExtra(NotificationHelper.EXTRA_PACIENTE_ID) ?: return
        val medicamentoNombre = intent.getStringExtra(NotificationHelper.EXTRA_MEDICAMENTO) ?: ""

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val koin = GlobalContext.get()
                val tomaDao: TomaProgramadaDao = koin.get()
                val registroDao: RegistroTomaDao = koin.get()

                tomaDao.updateEstado(tomaId, "TOMADO")
                registroDao.insert(
                    RegistroTomaEntity(
                        id = UUID.randomUUID().toString(),
                        tomaProgramadaId = tomaId,
                        pacienteId = pacienteId,
                        medicamentoNombre = medicamentoNombre,
                        fechaHoraReal = System.currentTimeMillis(),
                        estado = "TOMADO"
                    )
                )
                val notificationHelper: NotificationHelper = koin.get()
                notificationHelper.cancelNotification(tomaId)
            } finally {
                result.finish()
            }
        }
    }
}
