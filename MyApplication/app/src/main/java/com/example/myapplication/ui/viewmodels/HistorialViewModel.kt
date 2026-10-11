package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val faenas: List<RegistroFaena> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Todas",
    val userMessage: String? = null
) {
    val faenasFiltradas: List<RegistroFaena>
        get() = faenas.filter { faena ->
            val coincideTexto = searchQuery.isBlank() ||
                    faena.buzoNombre.contains(searchQuery, ignoreCase = true) ||
                    faena.centroCultivo.contains(searchQuery, ignoreCase = true) ||
                    faena.tipoFaena.contains(searchQuery, ignoreCase = true) ||
                    faena.empresa.contains(searchQuery, ignoreCase = true) ||
                    faena.id.contains(searchQuery, ignoreCase = true)

            val coincideFiltro = when (selectedFilter) {
                "En Curso" -> faena.estado.contains("Curso", ignoreCase = true)
                "Completada" -> faena.estado.contains("Completada", ignoreCase = true)
                else -> true
            }

            coincideTexto && coincideFiltro
        }
}

class HistorialViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.faenas.collect { lista ->
                _uiState.update { it.copy(faenas = lista) }
            }
        }
    }

    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }
    fun onFilterChange(f: String) = _uiState.update { it.copy(selectedFilter = f) }

    fun finalizarFaena(faenaId: String) {
        val result = repository.completarFaena(faenaId)
        result.onSuccess {
            _uiState.update {
                it.copy(userMessage = "Inmersión $faenaId registrada como 'Completada con Éxito'.")
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
