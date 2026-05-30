package com.medicamentos.app.medremind.notification

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Envía alertas de emergencia cuando un paciente no registra
 * la toma de su medicamento para diabetes.
 *
 * WhatsApp: abre la app con un mensaje prellenado (requiere WhatsApp instalado).
 * Email: abre el cliente de correo con destinatario y cuerpo prellenados.
 *
 * Nota de seguridad: el número de teléfono debe estar en formato internacional
 * sin espacios ni guiones (ej: 51987654321 para Perú).
 */
object EmergencyAlertSender {

    fun sendWhatsAppAlert(
        context: Context,
        telefono: String,
        nombrePaciente: String,
        medicamento: String,
        horaProgramada: String
    ) {
        val mensaje = buildWhatsAppMessage(nombrePaciente, medicamento, horaProgramada)
        val telefonoClean = telefono.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("https://wa.me/$telefonoClean?text=${Uri.encode(mensaje)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun sendEmailAlert(
        context: Context,
        correo: String,
        nombrePaciente: String,
        medicamento: String,
        horaProgramada: String
    ) {
        val asunto = "⚠️ ALERTA: $nombrePaciente no tomó su medicamento"
        val cuerpo = buildEmailBody(nombrePaciente, medicamento, horaProgramada)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(correo))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun buildWhatsAppMessage(
        nombrePaciente: String,
        medicamento: String,
        horaProgramada: String
    ): String = """
        ⚠️ ALERTA DIABETRACK
        
        Hola, te enviamos este mensaje porque $nombrePaciente no ha registrado la toma de su medicamento para diabetes.
        
        💊 Medicamento: $medicamento
        🕐 Hora programada: $horaProgramada
        
        Por favor, verifica que haya tomado su medicamento. Esto es importante para el control de su diabetes.
        
        — DiabeTrack 🩺
    """.trimIndent()

    private fun buildEmailBody(
        nombrePaciente: String,
        medicamento: String,
        horaProgramada: String
    ): String = """
        ALERTA DE MEDICAMENTO OMITIDO — DIABETRACK
        
        Estimado contacto de emergencia,
        
        Le informamos que $nombrePaciente NO ha registrado la toma de su medicamento para diabetes en la aplicación DiabeTrack.
        
        Detalles:
        • Paciente: $nombrePaciente
        • Medicamento: $medicamento
        • Hora programada: $horaProgramada
        
        Le recomendamos verificar con el paciente si tomó el medicamento y recordarle registrarlo en la app.
        
        IMPORTANTE: El control regular de la medicación es fundamental para el manejo de la diabetes.
        
        Este es un mensaje automático de DiabeTrack.
        Por favor no responda a este correo.
        
        — Equipo DiabeTrack 🩺
    """.trimIndent()
}
