package com.medicamentos.app.medremind.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicamentos.app.medremind.domain.model.Medicamento
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.RegistroToma
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import com.medicamentos.app.medremind.domain.usecase.ObtenerPacientesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AdminUiState(
    val medicoId: String = "",
    val medicoNombre: String = "",
    val pacientes: List<Paciente> = emptyList(),
    val medicamentos: List<Medicamento> = emptyList(),
    val selectedPaciente: Paciente? = null,
    val tratamientosDelPaciente: List<Tratamiento> = emptyList(),
    val historialDelPaciente: List<RegistroToma> = emptyList()
)

class AdminViewModel(
    private val authRepository: AuthRepository,
    private val obtenerPacientes: ObtenerPacientesUseCase,
    private val medicamentoRepository: MedicamentoRepository,
    private val tratamientoRepository: TratamientoRepository,
    private val tomaRepository: TomaRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val medicoId = authRepository.getUserId().filterNotNull()
    private val medicoNombre = authRepository.getNombre()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pacientesFlow = combine(medicoId, _searchQuery) { id, q -> id to q }
        .flatMapLatest { (id, q) ->
            if (q.isBlank()) obtenerPacientes(id) else obtenerPacientes.buscar(id, q)
        }

    private val _selectedPaciente = MutableStateFlow<Paciente?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tratamientosFlow = _selectedPaciente
        .flatMapLatest { p -> if (p != null) tratamientoRepository.getByPaciente(p.id) else flowOf(emptyList()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val historialFlow = _selectedPaciente
        .flatMapLatest { p ->
            if (p != null) {
                val inicio = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
                tomaRepository.getRegistrosEnRango(p.id, inicio, System.currentTimeMillis())
            } else flowOf(emptyList())
        }

    private val baseState = combine(medicoId, medicoNombre, pacientesFlow, medicamentoRepository.getAll()) {
        medicoId, nombre, pacientes, medicamentos ->
        AdminUiState(medicoId = medicoId, medicoNombre = nombre ?: "", pacientes = pacientes, medicamentos = medicamentos)
    }

    val uiState: StateFlow<AdminUiState> = combine(baseState, _selectedPaciente, tratamientosFlow, historialFlow) {
        base, selected, tratamientos, historial ->
        base.copy(selectedPaciente = selected, tratamientosDelPaciente = tratamientos, historialDelPaciente = historial)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    fun onSearchChange(query: String) = _searchQuery.update { query }
    fun selectPaciente(paciente: Paciente) = _selectedPaciente.update { paciente }
    fun clearSelectedPaciente() = _selectedPaciente.update { null }

    fun addMedicamento(nombre: String, dosis: String, via: String, instrucciones: String, onResult: (Boolean) -> Unit) {
        if (nombre.isBlank() || dosis.isBlank() || via.isBlank()) { onResult(false); return }
        viewModelScope.launch {
            runCatching { medicamentoRepository.add(nombre, dosis, via, instrucciones) }
                .fold(onSuccess = { onResult(true) }, onFailure = { onResult(false) })
        }
    }

    fun updateNombre(nombre: String, onResult: (Boolean) -> Unit) {
        if (nombre.isBlank()) { onResult(false); return }
        viewModelScope.launch {
            runCatching { authRepository.updateNombre(nombre) }
                .fold(onSuccess = { onResult(true) }, onFailure = { onResult(false) })
        }
    }
}
