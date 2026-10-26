package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EmpresaUiState(
    val empresas: List<String> = emptyList(),
    val showAddDialog: Boolean = false,
    val nuevaEmpresaNombre: String = "",
    val userMessage: String? = null,
    val error: String? = null
)

class EmpresaViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EmpresaUiState(empresas = repository.empresas)
    )
    val uiState: StateFlow<EmpresaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.empresasFlow.collect { empresas ->
                _uiState.update { it.copy(empresas = empresas) }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, nuevaEmpresaNombre = "", error = null) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, nuevaEmpresaNombre = "", error = null) }
    }

    fun onNuevaEmpresaNombreChange(v: String) {
        _uiState.update { it.copy(nuevaEmpresaNombre = v, error = null) }
    }

    fun guardarNuevaEmpresa() {
        val nombre = _uiState.value.nuevaEmpresaNombre.trim()
        val result = repository.agregarEmpresa(nombre)
        result.onSuccess { empresaCreada ->
            _uiState.update {
                it.copy(
                    showAddDialog = false,
                    nuevaEmpresaNombre = "",
                    userMessage = "Empresa '$empresaCreada' agregada a los registros."
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(error = err.message) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
