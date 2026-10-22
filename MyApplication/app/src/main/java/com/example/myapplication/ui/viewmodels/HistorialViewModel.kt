package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val faenas: List<RegistroFaena> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Todas",
    val userMessage: String? = null,
    /** Lo que le falta a cada faena en curso para poder finalizarse (lo calcula la propia faena). */
    val pendientesPorFaena: Map<String, List<String>> = emptyMap(),
    /** Equipos riesgosos/inutilizables reportados por faena (informativo, no bloquea). */
    val novedadesPorFaena: Map<String, List<String>> = emptyMap(),
    val faenaEnRevisionId: String? = null
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
                "En Curso" -> faena.enCurso
                "Completada" -> faena.completada
                else -> true
            }

            coincideTexto && coincideFiltro
        }

    /** Faena cuyo diálogo de revisión está abierto (null si ninguno, o si ya no está en curso). */
    val faenaEnRevision: RegistroFaena?
        get() = faenas.firstOrNull { it.id == faenaEnRevisionId && it.enCurso }
}

class HistorialViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        // Se recalcula cuando cambian las faenas, la salud de los buzos, las revisiones
        // de equipos o las etapas confirmadas (por ejemplo, al volver desde Equipos).
        viewModelScope.launch {
            combine(
                repository.faenas,
                repository.buzos,
                repository.revisiones,
                repository.etapasConfirmadas
            ) { faenas, _, _, _ -> faenas }
                .collect { faenas -> actualizarFaenas(faenas) }
        }
    }

    private fun actualizarFaenas(faenas: List<RegistroFaena>) {
        val enCurso = faenas.filter { it.enCurso }
        val avances = enCurso.associate { faena -> faena.id to repository.avanceDe(faena) }
        val pendientes = enCurso.associate { faena ->
            faena.id to faena.pendientes(avances.getValue(faena.id))
        }
        val novedades = enCurso.associate { faena ->
            faena.id to faena.novedadesEquipos(avances.getValue(faena.id))
        }
        _uiState.update { estado ->
            estado.copy(
                faenas = faenas,
                pendientesPorFaena = pendientes,
                novedadesPorFaena = novedades
            )
        }
    }

    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }
    fun onFilterChange(f: String) = _uiState.update { it.copy(selectedFilter = f) }

    fun abrirRevision(faenaId: String) = _uiState.update { it.copy(faenaEnRevisionId = faenaId) }
    fun cerrarRevision() = _uiState.update { it.copy(faenaEnRevisionId = null) }

    /** Deja la faena preseleccionada en Control de Equipos y cierra el diálogo. */
    fun prepararRevisionEquipos(faenaId: String) {
        repository.solicitarRevisionDe(faenaId)
        cerrarRevision()
    }

    fun finalizarFaena(faenaId: String) {
        repository.completarFaena(faenaId)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        faenaEnRevisionId = null,
                        userMessage = "Inmersión $faenaId registrada como 'Completada con Éxito'."
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(userMessage = error.message ?: "No se pudo finalizar la faena $faenaId.")
                }
            }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
