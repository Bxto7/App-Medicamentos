package com.medicamentos.app.medremind.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicamentos.app.medremind.data.local.dao.UsuarioDao
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.usecase.MarcarTomaUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerHistorialUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerMisTratamientosUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerTomasDelDiaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class PatientHomeUiState(
    val nombrePaciente: String = "",
    val tomasHoy: List<TomaProgramada> = emptyList(),
    val tomadas: Int = 0,
    val total: Int = 0,
    val historial: List<RegistroToma> = emptyList(),
    val tratamientos: List<Tratamiento> = emptyList(),
    val pacienteId: String = "",
    val avatarId: Int = 0
)

class PatientHomeViewModel(
    private val authRepository: AuthRepository,
    private val obtenerTomasDelDia: ObtenerTomasDelDiaUseCase,
    private val obtenerHistorial: ObtenerHistorialUseCase,
    private val obtenerMisTratamientos: ObtenerMisTratamientosUseCase,
    private val marcarToma: MarcarTomaUseCase,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val userId = authRepository.getUserId().filterNotNull()
    private val nombreFlow = authRepository.getNombre()
    private val _avatarId = MutableStateFlow(0)

    init {
        // Cargar avatarId del usuario actual
        userId.onEach { id ->
            val entity = usuarioDao.findById(id)
            _avatarId.value = entity?.avatarId ?: 0
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PatientHomeUiState> = combine(userId, nombreFlow, _avatarId) { id, nombre, avatarId ->
        Triple(id, nombre ?: "", avatarId)
    }.flatMapLatest { (id, nombre, avatarId) ->
        combine(obtenerTomasDelDia(id), obtenerHistorial(id), obtenerMisTratamientos(id)) { tomas, historial, tratamientos ->
            PatientHomeUiState(
                nombrePaciente = nombre,
                tomasHoy = tomas,
                tomadas = tomas.count { it.estado == EstadoToma.TOMADO },
                total = tomas.size,
                historial = historial,
                tratamientos = tratamientos,
                pacienteId = id,
                avatarId = avatarId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PatientHomeUiState())

    fun marcar(toma: TomaProgramada, estado: EstadoToma) {
        viewModelScope.launch { marcarToma(toma, estado) }
    }

    fun getRangoMes(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return inicio to cal.timeInMillis
    }
}
