package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val selectedTab: Int = 0,
    val rutLogin: String = "12.345.678-9",
    val passwordLogin: String = "admin123",
    val passwordLoginVisible: Boolean = false,

    val regNombre: String = "",
    val regRut: String = "",
    val regSeremi: String = "",
    val regCorreo: String = "",
    val regCentro: String = "",
    val regPassword: String = "",
    val regPasswordConfirm: String = "",
    val regPasswordVisible: Boolean = false,
    val regAceptaTerminos: Boolean = true,

    val centrosDisponibles: List<String> = emptyList(),
    val errorMessage: String? = null,
    val successDialogMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val usuarioAutenticado: Prevencionista? = null
)

class AuthViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            centrosDisponibles = repository.centrosCultivo,
            regCentro = repository.centrosCultivo.firstOrNull() ?: "Centro Melinka 1 - Canal Moraleda"
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    fun onRutLoginChange(value: String) {
        _uiState.update { it.copy(rutLogin = value, errorMessage = null) }
    }

    fun onPasswordLoginChange(value: String) {
        _uiState.update { it.copy(passwordLogin = value, errorMessage = null) }
    }

    fun togglePasswordLoginVisibility() {
        _uiState.update { it.copy(passwordLoginVisible = !it.passwordLoginVisible) }
    }

    fun onRegNombreChange(value: String) {
        _uiState.update { it.copy(regNombre = value, errorMessage = null) }
    }

    fun onRegRutChange(value: String) {
        _uiState.update { it.copy(regRut = value, errorMessage = null) }
    }

    fun onRegSeremiChange(value: String) {
        _uiState.update { it.copy(regSeremi = value, errorMessage = null) }
    }

    fun onRegCorreoChange(value: String) {
        _uiState.update { it.copy(regCorreo = value, errorMessage = null) }
    }

    fun onRegCentroChange(value: String) {
        _uiState.update { it.copy(regCentro = value, errorMessage = null) }
    }

    fun onRegPasswordChange(value: String) {
        _uiState.update { it.copy(regPassword = value, errorMessage = null) }
    }

    fun onRegPasswordConfirmChange(value: String) {
        _uiState.update { it.copy(regPasswordConfirm = value, errorMessage = null) }
    }

    fun toggleRegPasswordVisibility() {
        _uiState.update { it.copy(regPasswordVisible = !it.regPasswordVisible) }
    }

    fun onRegAceptaTerminosChange(value: Boolean) {
        _uiState.update { it.copy(regAceptaTerminos = value) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successDialogMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        val result = repository.login(state.rutLogin, state.passwordLogin)
        result.onSuccess { prev ->
            _uiState.update {
                it.copy(
                    isLoginSuccessful = true,
                    usuarioAutenticado = prev,
                    errorMessage = null
                )
            }
            onSuccess()
        }.onFailure { err ->
            _uiState.update {
                it.copy(errorMessage = err.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun registrar(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.regNombre.isBlank() || state.regRut.isBlank() || state.regSeremi.isBlank() ||
            state.regCorreo.isBlank() || state.regPassword.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Por favor complete todos los campos obligatorios (*).") }
            return
        }
        if (state.regPassword.length < 4) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 4 caracteres.") }
            return
        }
        if (state.regPassword != state.regPasswordConfirm) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }
        if (!state.regAceptaTerminos) {
            _uiState.update { it.copy(errorMessage = "Debe aceptar el compromiso de confidencialidad y protocolos DIRECTEMAR.") }
            return
        }

        val nuevo = Prevencionista(
            nombre = state.regNombre.trim(),
            rut = state.regRut.trim(),
            correo = state.regCorreo.trim(),
            registroSeremi = state.regSeremi.trim(),
            centro = state.regCentro,
            contrasena = state.regPassword
        )

        val result = repository.registrarPrevencionista(nuevo)
        result.onSuccess { prev ->
            _uiState.update {
                it.copy(
                    usuarioAutenticado = prev,
                    isLoginSuccessful = true,
                    successDialogMessage = "Cuenta creada con éxito para ${prev.nombre} (SEREMI: ${prev.registroSeremi})",
                    errorMessage = null
                )
            }
            onSuccess()
        }.onFailure { err ->
            _uiState.update { it.copy(errorMessage = err.message ?: "Error en el registro") }
        }
    }
}
