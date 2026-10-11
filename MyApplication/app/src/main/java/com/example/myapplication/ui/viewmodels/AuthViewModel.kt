package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.util.Rutificador
import com.example.myapplication.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val selectedTab: Int = 0,
    val rutLogin: String = "12.345.678-5",
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
        val filtrado = value.filter { it.isDigit() || it.equals('k', ignoreCase = true) || it == '.' || it == '-' }
        _uiState.update { it.copy(regRut = filtrado, errorMessage = null) }
    }

    fun onRegSeremiChange(value: String) {
        _uiState.update { it.copy(regSeremi = value.uppercase(), errorMessage = null) }
    }

    fun onRegCorreoChange(value: String) {
        _uiState.update { it.copy(regCorreo = value.trim(), errorMessage = null) }
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

        // 1. Validación de Nombre Completo
        val (nombreOk, nombreError) = SecurityUtils.validarNombre(state.regNombre)
        if (!nombreOk) {
            _uiState.update { it.copy(errorMessage = nombreError) }
            return
        }

        // 2. Validación de RUT con Módulo 11 (Rutificador)
        val (rutOk, rutError) = SecurityUtils.validarRut(state.regRut)
        if (!rutOk) {
            _uiState.update { it.copy(errorMessage = rutError) }
            return
        }

        // 3. Validación de unicidad de RUT
        if (repository.existeRutPrevencionista(state.regRut)) {
            _uiState.update { it.copy(errorMessage = "Ya existe un prevencionista registrado con el RUT ${Rutificador.formatear(state.regRut)}.") }
            return
        }

        // 4. Validación de Registro SEREMI
        val (seremiOk, seremiError) = SecurityUtils.validarRegistroSeremi(state.regSeremi)
        if (!seremiOk) {
            _uiState.update { it.copy(errorMessage = seremiError) }
            return
        }

        // 5. Validación de Correo Institucional
        val (emailOk, emailError) = SecurityUtils.validarEmail(state.regCorreo)
        if (!emailOk) {
            _uiState.update { it.copy(errorMessage = emailError) }
            return
        }

        // 6. Validación de unicidad de Correo
        if (repository.existeCorreoPrevencionista(state.regCorreo)) {
            _uiState.update { it.copy(errorMessage = "Ya existe un usuario registrado con el correo ${state.regCorreo.trim()}.") }
            return
        }

        // 7. Validación de Contraseña
        val (passOk, passError) = SecurityUtils.validarPassword(state.regPassword)
        if (!passOk) {
            _uiState.update { it.copy(errorMessage = passError) }
            return
        }

        // 8. Confirmación de Contraseña
        val (confirmOk, confirmError) = SecurityUtils.validarPasswordConfirm(state.regPassword, state.regPasswordConfirm)
        if (!confirmOk) {
            _uiState.update { it.copy(errorMessage = confirmError) }
            return
        }

        // 9. Términos y condiciones
        if (!state.regAceptaTerminos) {
            _uiState.update { it.copy(errorMessage = "Debe aceptar el compromiso de confidencialidad y protocolos DIRECTEMAR.") }
            return
        }

        val nuevo = Prevencionista(
            nombre = state.regNombre.trim(),
            rut = Rutificador.formatear(state.regRut),
            correo = state.regCorreo.trim(),
            registroSeremi = state.regSeremi.trim().uppercase(),
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
