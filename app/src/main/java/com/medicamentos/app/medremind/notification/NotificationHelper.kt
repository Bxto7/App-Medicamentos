package com.medicamentos.app.medremind.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medicamentos.app.medremind.MainActivity
import com.medicamentos.app.medremind.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val CHANNEL_NAME = "Recordatorios de medicamentos"
        const val EXTRA_TOMA_ID = "TOMA_ID"
        const val EXTRA_MEDICAMENTO = "MEDICAMENTO_NOMBRE"
        const val EXTRA_DOSIS = "DOSIS"
        const val EXTRA_PACIENTE_ID = "PACIENTE_ID"
        const val EXTRA_TRATAMIENTO_ID = "TRATAMIENTO_ID"
        const val ACTION_MARK_TAKEN = "com.medicamentos.app.medremind.MARK_TAKEN"
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas para tomar medicamentos a tiempo"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager().createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(
        tomaId: String,
        medicamentoNombre: String,
        dosis: String,
        pacienteId: String,
        tratamientoId: String
    ) {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val takenIntent = PendingIntent.getBroadcast(
            context,
            tomaId.hashCode(),
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_TOMA_ID, tomaId)
                putExtra(EXTRA_PACIENTE_ID, pacienteId)
                putExtra(EXTRA_MEDICAMENTO, medicamentoNombre)
                putExtra(EXTRA_TRATAMIENTO_ID, tratamientoId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hora de tu medicamento")
            .setContentText("$medicamentoNombre · $dosis")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Es momento de tomar $medicamentoNombre ($dosis). Pulsa 'Tomado' cuando lo hayas tomado."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_notification, "Tomado", takenIntent)
            .setAutoCancel(true)
            .build()

        notificationManager().notify(tomaId.hashCode(), notification)
    }

    fun cancelNotification(tomaId: String) {
        notificationManager().cancel(tomaId.hashCode())
    }

    private fun notificationManager() =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
