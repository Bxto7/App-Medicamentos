package com.medicamentos.app.medremind.domain.usecase

import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Usuario
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.repository.PacienteRepository
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LoginUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Usuario> {
        if (email.isBlank()) return Result.failure(Exception("Ingresa tu correo"))
        if (password.isBlank()) return Result.failure(Exception("Ingresa tu contraseña"))
        return repo.login(email, password)
    }
}

class LogoutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

class RegisterUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(
        nombre: String,
        email: String,
        password: String,
        confirmPassword: String,
        rol: Rol,
        avatarId: Int
    ): Result<Usuario> {
        if (nombre.isBlank()) return Result.failure(Exception("Ingresa tu nombre completo"))
        if (email.isBlank()) return Result.failure(Exception("Ingresa tu correo"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.failure(Exception("Correo no válido"))
        if (password.length < 6) return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        if (password != confirmPassword) return Result.failure(Exception("Las contraseñas no coinciden"))
        return repo.register(nombre, email, password, rol, avatarId)
    }
}

class ObtenerPacientesUseCase(private val repo: PacienteRepository) {
    operator fun invoke(medicoId: String): Flow<List<Paciente>> = repo.getPacientesByMedico(medicoId)
    fun buscar(medicoId: String, query: String): Flow<List<Paciente>> = repo.searchPacientes(medicoId, query)
}

class ObtenerTomasDelDiaUseCase(private val repo: TomaRepository) {
    operator fun invoke(pacienteId: String): Flow<List<TomaProgramada>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val fin = cal.timeInMillis
        return repo.getTomasDelDia(pacienteId, inicio, fin)
    }
}

class MarcarTomaUseCase(private val repo: TomaRepository) {
    suspend operator fun invoke(toma: TomaProgramada, estado: EstadoToma) =
        repo.marcarToma(toma.id, toma.tratamientoId, toma.pacienteId, toma.medicamentoNombre, estado)
}

class ObtenerHistorialUseCase(private val repo: TomaRepository) {
    operator fun invoke(pacienteId: String): Flow<List<RegistroToma>> = repo.getHistorial(pacienteId)
}

class AgregarTratamientoUseCase(private val repo: TratamientoRepository) {
    suspend operator fun invoke(
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
    ): Result<List<TomaProgramada>> = runCatching {
        if (medicamentoId.isBlank()) error("Selecciona un medicamento")
        if (horarios.isEmpty()) error("Agrega al menos un horario")
        if (stockInicial < 0) error("El stock no puede ser negativo")
        repo.add(pacienteId, medicamentoId, medicamentoNombre, dosis, frecuenciaHoras, horarios, fechaInicio, fechaFin, stockInicial, instrucciones)
    }
}
