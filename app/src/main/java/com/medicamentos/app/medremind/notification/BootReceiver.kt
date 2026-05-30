package com.medicamentos.app.medremind.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val koin = GlobalContext.get()
                val tomaDao: TomaProgramadaDao = koin.get()
                val scheduler: NotificationScheduler = koin.get()
                val tomas = tomaDao.getAllPendientesFuturas(System.currentTimeMillis())
                tomas.forEach { scheduler.scheduleForToma(it) }
            } finally {
                result.finish()
            }
        }
    }
}
