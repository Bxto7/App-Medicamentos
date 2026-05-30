package com.medicamentos.app.medremind.domain.repository

import com.medicamentos.app.medremind.domain.model.ContactoEmergencia
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.GlucosaMedicion
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        rol: Rol,
        avatarId: Int,
        contactosEmergencia: List<ContactoEmergencia>
    ): Result<Usuario>
    suspend fun logout()
    fun getUserId(): Flow<String?>
    fun getRol(): Flow<Rol?>
    fun getNombre(): Flow<String?>
    fun isLoggedIn(): Flow<Boolean>
}

interface PacienteRepository {
    fun getPacientesByMedico(medicoId: String): Flow<List<Paciente>>
    fun searchPacientes(medicoId: String, query: String): Flow<List<Paciente>>
    suspend fun getPacienteById(id: String): Paciente?
    fun countPacientes(medicoId: String): Flow<Int>
}

interface MedicamentoRepository {
    fun getAll(): Flow<List<Medicamento>>
}

interface TratamientoRepository {
    fun getByPaciente(pacienteId: String): Flow<List<Tratamiento>>
    suspend fun add(
        pacienteId: String,
        medicamentoId: String,
        medicamentoNombre: String,
        dosis: String,
        frecuenciaHoras: Int,
        horarios: List<String>,
        fechaInicio: Long,
        fechaFin: Long?,
        stockInicial: Int,
        instrucciones: String
    ): List<TomaProgramada>
}

interface TomaRepository {
    fun getTomasDelDia(pacienteId: String, inicioDelDia: Long, finDelDia: Long): Flow<List<TomaProgramada>>
    fun getHistorial(pacienteId: String): Flow<List<RegistroToma>>
    fun getRegistrosEnRango(pacienteId: String, inicio: Long, fin: Long): Flow<List<RegistroToma>>
    suspend fun marcarToma(tomaId: String, tratamientoId: String, pacienteId: String, medicamentoNombre: String, estado: EstadoToma)
    suspend fun getTotalHoy(pacienteId: String, inicio: Long, fin: Long): Int
    suspend fun getTomadas(pacienteId: String, inicio: Long, fin: Long): Int
    suspend fun getPendientesAntesDeTimestamp(pacienteId: String, timestamp: Long): List<TomaProgramada>
}

interface ContactoEmergenciaRepository {
    fun getByUsuario(usuarioId: String): Flow<List<ContactoEmergencia>>
    suspend fun getByUsuarioSync(usuarioId: String): List<ContactoEmergencia>
    suspend fun saveContactos(usuarioId: String, contactos: List<ContactoEmergencia>)
}

interface GlucosaRepository {
    fun getByPaciente(pacienteId: String): Flow<List<GlucosaMedicion>>
    fun getDesde(pacienteId: String, desde: Long): Flow<List<GlucosaMedicion>>
    suspend fun registrar(medicion: GlucosaMedicion)
    suspend fun eliminar(id: String)
    suspend fun getUltima(pacienteId: String): GlucosaMedicion?
}
