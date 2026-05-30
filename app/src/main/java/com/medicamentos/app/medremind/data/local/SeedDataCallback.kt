package com.medicamentos.app.medremind.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
import com.medicamentos.app.medremind.data.local.entity.MedicoPacienteEntity
import com.medicamentos.app.medremind.data.local.entity.PacienteEntity
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

class SeedDataCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch { seed() }
    }

    private fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun seed() {
        val database = GlobalContext.get().get<AppDatabase>()
        val usuarioDao = database.usuarioDao()
        val pacienteDao = database.pacienteDao()
        val medicamentoDao = database.medicamentoDao()
        val tratamientoDao = database.tratamientoDao()
        val tomaDao = database.tomaProgramadaDao()
        val registroDao = database.registroTomaDao()
        val medicoPacienteDao = database.medicoPacienteDao()

        // La identidad del paciente es su Usuario: los IDs de las fichas, tomas y
        // tratamientos coinciden con el usuario para que la vista del paciente
        // (que consulta por su userId) muestre sus datos de punta a punta.
        val medicoId = "medico-001"
        val medicoNombre = "Dr. Carlos García"
        val paciente1Id = "user-pac-001"
        val paciente2Id = "user-pac-002"

        // Usuarios de prueba — DiabeTrack
        usuarioDao.insert(UsuarioEntity(medicoId, "medico@medremind.com", medicoNombre, hash("medico123"), "MEDICO", avatarId = 0))
        usuarioDao.insert(UsuarioEntity(paciente1Id, "juan@medremind.com", "Juan Pérez", hash("paciente123"), "PACIENTE", avatarId = 0))
        usuarioDao.insert(UsuarioEntity(paciente2Id, "maria@medremind.com", "María López", hash("maria123"), "PACIENTE", avatarId = 1))

        // Ficha clínica opcional, con id = usuarioId
        pacienteDao.insert(PacienteEntity(paciente1Id, "Juan Pérez", 52, "Diabetes tipo 2", null, medicoId))
        pacienteDao.insert(PacienteEntity(paciente2Id, "María López", 38, "Diabetes tipo 1", null, medicoId))

        // Asociaciones médico-paciente (N:N)
        medicoPacienteDao.insert(MedicoPacienteEntity(medicoId, paciente1Id, now()))
        medicoPacienteDao.insert(MedicoPacienteEntity(medicoId, paciente2Id, now()))

        // Medicamentos para diabetes
        val med1Id = "med-001"; val med2Id = "med-002"; val med3Id = "med-003"; val med4Id = "med-004"
        medicamentoDao.insert(MedicamentoEntity(med1Id, "Metformina", "500 mg", "Oral", "Tomar con las comidas. Controla el azúcar en sangre.", null))
        medicamentoDao.insert(MedicamentoEntity(med2Id, "Glibenclamida", "5 mg", "Oral", "Tomar 30 min antes del desayuno. No omitir comidas.", null))
        medicamentoDao.insert(MedicamentoEntity(med3Id, "Insulina Glargina", "10 UI", "Subcutánea", "Aplicar en abdomen o muslo a la misma hora cada día.", null))
        medicamentoDao.insert(MedicamentoEntity(med4Id, "Sitagliptina", "100 mg", "Oral", "Una vez al día con o sin alimentos.", null))

        val trat1Id = "trat-001"; val trat2Id = "trat-002"; val trat3Id = "trat-003"
        tratamientoDao.insert(TratamientoEntity(trat1Id, paciente1Id, med1Id, "Metformina", "500 mg", 8, "08:00,14:00,20:00", now(), null, 90, "Controla el azúcar. Tomar con comidas.", medicoId, medicoNombre))
        tratamientoDao.insert(TratamientoEntity(trat2Id, paciente1Id, med2Id, "Glibenclamida", "5 mg", 24, "07:30", now(), null, 30, "Estimula producción de insulina. No saltear comidas.", medicoId, medicoNombre))
        tratamientoDao.insert(TratamientoEntity(trat3Id, paciente2Id, med3Id, "Insulina Glargina", "10 UI", 24, "22:00", now(), null, 60, "Insulina de acción prolongada. Aplicar antes de dormir.", medicoId, medicoNombre))

        val tomasHistorial = mutableListOf<TomaProgramadaEntity>()
        for (daysAgo in 13 downTo 1) {
            generarTomasParaDia(trat1Id, paciente1Id, "Metformina", "500 mg", listOf("08:00", "14:00", "20:00"), daysAgo, tomasHistorial)
            generarTomasParaDia(trat2Id, paciente1Id, "Glibenclamida", "5 mg", listOf("07:30"), daysAgo, tomasHistorial)
            generarTomasParaDia(trat3Id, paciente2Id, "Insulina Glargina", "10 UI", listOf("22:00"), daysAgo, tomasHistorial)
        }
        tomaDao.insertAll(tomasHistorial)

        val registros = mutableListOf<RegistroTomaEntity>()
        tomasHistorial.forEach { toma ->
            val r = Math.random()
            val estado = when { r < 0.78 -> "TOMADO"; r < 0.92 -> "OMITIDO"; else -> "TARDE" }
            tomaDao.updateEstado(toma.id, estado)
            if (estado != "OMITIDO") {
                registros.add(RegistroTomaEntity(UUID.randomUUID().toString(), toma.id, toma.pacienteId, toma.medicamentoNombre, toma.fechaHoraProgramada + (Math.random() * 1_800_000).toLong(), estado))
            }
        }
        registroDao.insertAll(registros)

        val tomasHoy = mutableListOf<TomaProgramadaEntity>()
        generarTomasParaDia(trat1Id, paciente1Id, "Metformina", "500 mg", listOf("08:00", "14:00", "20:00"), 0, tomasHoy)
        generarTomasParaDia(trat2Id, paciente1Id, "Glibenclamida", "5 mg", listOf("07:30"), 0, tomasHoy)
        generarTomasParaDia(trat3Id, paciente2Id, "Insulina Glargina", "10 UI", listOf("22:00"), 0, tomasHoy)
        tomaDao.insertAll(tomasHoy)
    }

    private fun generarTomasParaDia(
        tratId: String, pacId: String, med: String, dosis: String,
        horarios: List<String>, daysOffset: Int, list: MutableList<TomaProgramadaEntity>
    ) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        horarios.forEach { hora ->
            val parts = hora.split(":")
            val ts = dayStart + parts[0].toLong() * 3_600_000L + parts[1].toLong() * 60_000L
            list.add(TomaProgramadaEntity(UUID.randomUUID().toString(), tratId, pacId, med, dosis, ts, "PENDIENTE"))
        }
    }

    private fun now() = System.currentTimeMillis()
}
