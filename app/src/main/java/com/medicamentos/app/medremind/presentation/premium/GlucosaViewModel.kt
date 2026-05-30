package com.medicamentos.app.medremind.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicamentos.app.medremind.domain.model.GlucosaMedicion
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.usecase.ObtenerGlucosaUseCase
import com.medicamentos.app.medremind.domain.usecase.RegistrarGlucosaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlucosaUiState(
    val pacienteId: String = "",
    val registros: List<GlucosaMedicion> = emptyList(),
    val valorInput: String = "",
    val momentoSeleccionado: String = "ayunas",
    val notasInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

val MOMENTOS_GLUCOSA = listOf("ayunas", "post-desayuno", "post-almuerzo", "post-cena", "antes de dormir")

class GlucosaViewModel(
    private val registrarUseCase: RegistrarGlucosaUseCase,
    private val obtenerUseCase: ObtenerGlucosaUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlucosaUiState())
    val uiState: StateFlow<GlucosaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val id = authRepository.getUserId().first() ?: return@launch
            _uiState.update { it.copy(pacienteId = id) }
            obtenerUseCase.invoke14Dias(id)
                .onEach { lista -> _uiState.update { it.copy(registros = lista) } }
                .launchIn(this)
        }
    }

    fun onValorChange(v: String) = _uiState.update { it.copy(valorInput = v, error = null) }
    fun onMomentoChange(m: String) = _uiState.update { it.copy(momentoSeleccionado = m) }
    fun onNotasChange(n: String) = _uiState.update { it.copy(notasInput = n) }

    fun registrar() {
        val s = _uiState.value
        val valor = s.valorInput.toIntOrNull()
        if (valor == null) {
            _uiState.update { it.copy(error = "Ingresa un valor numérico") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = registrarUseCase(s.pacienteId, valor, s.momentoSeleccionado, s.notasInput)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, valorInput = "", notasInput = "", successMessage = "¡Glucosa registrada!") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
}
