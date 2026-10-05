package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BuzoUiState(
    val buzos: List<Buzo> = emptyList(),
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val nuevoNombre: String = "",
    val nuevoRut: String = "",
    val nuevaMatricula: String = "",
    val nuevoTelefono: String = "",
    val empresaSeleccionada: String = "",
    val empresasDisponibles: List<String> = emptyList(),
    val userMessage: String? = null,
    val error: String? = null
) {
    val buzosFiltrados: List<Buzo>
        get() = if (searchQuery.isBlank()) buzos
        else buzos.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            it.rut.contains(searchQuery, ignoreCase = true) ||
            it.empresa.contains(searchQuery, ignoreCase = true)
        }
}

class BuzoViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BuzoUiState(
            empresasDisponibles = repository.empresas,
            empresaSeleccionada = repository.empresas.firstOrNull() ?: ""
        )
    )
    val uiState: StateFlow<BuzoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buzos.collect { lista ->
                _uiState.update { it.copy(buzos = lista) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, error = null) }
    }

    fun closeAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                nuevoNombre = "",
                nuevoRut = "",
                nuevaMatricula = "",
                nuevoTelefono = "",
                error = null
            )
        }
    }

    fun onNuevoNombreChange(v: String) = _uiState.update { it.copy(nuevoNombre = v, error = null) }
    fun onNuevoRutChange(v: String) = _uiState.update { it.copy(nuevoRut = v, error = null) }
    fun onNuevaMatriculaChange(v: String) = _uiState.update { it.copy(nuevaMatricula = v, error = null) }
    fun onNuevoTelefonoChange(v: String) = _uiState.update { it.copy(nuevoTelefono = v, error = null) }
    fun onEmpresaSeleccionadaChange(v: String) = _uiState.update { it.copy(empresaSeleccionada = v) }

    fun agregarBuzo() {
        val state = _uiState.value
        if (state.nuevoNombre.isBlank() || state.nuevoRut.isBlank()) {
            _uiState.update { it.copy(error = "El nombre y el RUT son obligatorios.") }
            return
        }

        val nuevo = Buzo(
            id = (state.buzos.size + 1).toString(),
            rut = state.nuevoRut.trim(),
            nombre = state.nuevoNombre.trim(),
            empresa = state.empresaSeleccionada,
            matricula = if (state.nuevaMatricula.isBlank()) "Buzo Comercial N° ${1000 + state.buzos.size}" else state.nuevaMatricula.trim(),
            telefono = if (state.nuevoTelefono.isBlank()) "+56 9 9000 0000" else state.nuevoTelefono.trim(),
            estadoSalud = EstadoSalud.PENDIENTE,
            ultimoChequeo = "Pendiente de faena"
        )

        val result = repository.agregarBuzo(nuevo)
        result.onSuccess { buzoAgregado ->
            _uiState.update {
                it.copy(
                    showAddDialog = false,
                    nuevoNombre = "",
                    nuevoRut = "",
                    nuevaMatricula = "",
                    nuevoTelefono = "",
                    userMessage = "Buzo ${buzoAgregado.nombre} agregado exitosamente.",
                    error = null
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(error = err.message ?: "Error al agregar buzo") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
