package com.medicamentos.app.medremind.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.usecase.AgregarTratamientoUseCase
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.usecase.ObtenerPacientesUseCase
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddTreatmentUiState(
    val medicamentos: List<Medicamento> = emptyList(),
    val pacientes: List<Paciente> = emptyList(),
    val selectedMedIndex: Int = -1,
    val selectedPacienteId: String = "",
    val selectedPacienteNombre: String = "",
    val dosis: String = "",
    val frecuenciaHoras: Int = 8,
    val horarios: List<String> = listOf("08:00", "14:00", "20:00"),
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long? = null,
    val stockInicial: String = "30",
    val instrucciones: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AddTreatmentViewModel(
    private val agregarTratamiento: AgregarTratamientoUseCase,
    private val notificationScheduler: NotificationScheduler,
    medicamentoRepository: MedicamentoRepository,
    private val obtenerPacientes: ObtenerPacientesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTreatmentUiState())
    val uiState: StateFlow<AddTreatmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            medicamentoRepository.getAll().collect { list ->
                _uiState.update { it.copy(medicamentos = list) }
            }
        }
        viewModelScope.launch {
            val medicoId = authRepository.getUserId().filterNotNull().first()
            obtenerPacientes(medicoId).collect { list ->
                _uiState.update { it.copy(pacientes = list) }
            }
        }
    }

    fun preseleccionarPaciente(id: String, nombre: String) {
        _uiState.update { it.copy(selectedPacienteId = id, selectedPacienteNombre = nombre) }
    }

    fun selectPaciente(paciente: Paciente) {
        _uiState.update { it.copy(selectedPacienteId = paciente.id, selectedPacienteNombre = paciente.nombre) }
    }

    fun selectMedicamento(index: Int) {
        val med = _uiState.value.medicamentos.getOrNull(index) ?: return
        _uiState.update { it.copy(selectedMedIndex = index, dosis = med.dosis) }
    }

    fun onDosisChange(v: String) = _uiState.update { it.copy(dosis = v) }
    fun onStockChange(v: String) = _uiState.update { it.copy(stockInicial = v) }
    fun onInstruccionesChange(v: String) = _uiState.update { it.copy(instrucciones = v) }
    fun setFechaInicio(ms: Long) = _uiState.update { it.copy(fechaInicio = ms) }
    fun setFechaFin(ms: Long?) = _uiState.update { it.copy(fechaFin = ms) }

    fun setFrecuencia(horas: Int) {
        val defaultHorarios = when (horas) {
            6 -> listOf("06:00", "12:00", "18:00", "00:00")
            8 -> listOf("08:00", "14:00", "20:00")
            12 -> listOf("08:00", "20:00")
            else -> listOf("08:00")
        }
        _uiState.update { it.copy(frecuenciaHoras = horas, horarios = defaultHorarios) }
    }

    fun updateHorario(index: Int, value: String) {
        val list = _uiState.value.horarios.toMutableList()
        if (index < list.size) list[index] = value
        _uiState.update { it.copy(horarios = list) }
    }

    fun save() {
        val state = _uiState.value
        if (state.selectedPacienteId.isBlank()) {
            _uiState.update { it.copy(error = "Selecciona un paciente") }
            return
        }
        val med = state.medicamentos.getOrNull(state.selectedMedIndex)
        if (med == null) { _uiState.update { it.copy(error = "Selecciona un medicamento") }; return }
        val stock = state.stockInicial.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = agregarTratamiento(
                pacienteId = state.selectedPacienteId,
                medicamentoId = med.id,
                medicamentoNombre = med.nombre,
                dosis = state.dosis.ifBlank { med.dosis },
                frecuenciaHoras = state.frecuenciaHoras,
                horarios = state.horarios,
                fechaInicio = startOfDay(state.fechaInicio),
                fechaFin = state.fechaFin,
                stockInicial = stock,
                instrucciones = state.instrucciones.ifBlank { med.instrucciones }
            )
            result.fold(
                onSuccess = { tomas ->
                    tomas.forEach { toma ->
                        notificationScheduler.scheduleForToma(
                            TomaProgramadaEntity(toma.id, toma.tratamientoId, toma.pacienteId, toma.medicamentoNombre, toma.dosis, toma.fechaHoraProgramada, toma.estado.name)
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, success = true) }
                },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun startOfDay(ms: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
