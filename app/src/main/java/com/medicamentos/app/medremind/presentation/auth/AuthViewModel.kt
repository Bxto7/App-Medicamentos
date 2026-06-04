package com.medicamentos.app.medremind.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.usecase.LoginUseCase
import com.medicamentos.app.medremind.domain.usecase.LogoutUseCase
import com.medicamentos.app.medremind.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SessionState(
    val isLoggedIn: Boolean = false,
    val rol: Rol? = null,
    val nombre: String = "",
    val isReady: Boolean = false
)

data class RegisterUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val avatarId: Int = 0,
    val rol: Rol = Rol.PACIENTE,
    val telefono: String = "",
    val telefonoFamiliar: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val registerUseCase: RegisterUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    val sessionState: StateFlow<SessionState> = combine(
        authRepository.isLoggedIn(),
        authRepository.getRol(),
        authRepository.getNombre()
    ) { isLoggedIn, rol, nombre ->
        SessionState(isLoggedIn = isLoggedIn, rol = rol, nombre = nombre ?: "", isReady = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState())

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(_uiState.value.email, _uiState.value.password)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }

    fun onRegNombreChange(v: String) = _registerState.update { it.copy(nombre = v, error = null) }
    fun onRegEmailChange(v: String) = _registerState.update { it.copy(email = v, error = null) }
    fun onRegPasswordChange(v: String) = _registerState.update { it.copy(password = v, error = null) }
    fun onRegConfirmPasswordChange(v: String) = _registerState.update { it.copy(confirmPassword = v, error = null) }
    fun onRegAvatarChange(id: Int) = _registerState.update { it.copy(avatarId = id) }
    fun onRegRolChange(rol: Rol) = _registerState.update { it.copy(rol = rol) }
    // Solo se aceptan dígitos para los teléfonos (validación de formato numérico).
    fun onRegTelefonoChange(v: String) = _registerState.update { it.copy(telefono = v.filter { c -> c.isDigit() }, error = null) }
    fun onRegTelefonoFamiliarChange(v: String) = _registerState.update { it.copy(telefonoFamiliar = v.filter { c -> c.isDigit() }, error = null) }

    fun register() {
        val s = _registerState.value
        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(
                nombre = s.nombre,
                email = s.email,
                password = s.password,
                confirmPassword = s.confirmPassword,
                rol = s.rol,
                avatarId = s.avatarId,
                telefono = s.telefono,
                telefonoFamiliar = s.telefonoFamiliar
            )
            result.fold(
                onSuccess = { _registerState.update { it.copy(isLoading = false, registroExitoso = true) } },
                onFailure = { e -> _registerState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
