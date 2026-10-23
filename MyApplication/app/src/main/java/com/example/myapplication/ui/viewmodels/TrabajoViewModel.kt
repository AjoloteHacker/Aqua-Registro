package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrabajoUiState(
    val buzos: List<Buzo> = emptyList(),
    val buzoSeleccionadoId: String? = null,
    val centrosDisponibles: List<String> = emptyList(),
    val centroCultivo: String = "",
    val tiposFaenaDisponibles: List<String> = emptyList(),
    val tipoFaena: String = "",
    val profundidad: String = "18",
    val duracionMinutos: String = "40",
    val supervisor: String = "Cristóbal Loncón (Prevencionista)",
    val errorMessage: String? = null,
    val successDialog: Boolean = false,
    val userMessage: String? = null
) {
    val buzoSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == buzoSeleccionadoId }

    val esBuzoApto: Boolean
        get() = buzoSeleccionado?.estadoSalud == EstadoSalud.APTO
}

class TrabajoViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TrabajoUiState(
            centrosDisponibles = repository.centrosCultivo,
            centroCultivo = repository.centrosCultivo.firstOrNull() ?: "",
            tiposFaenaDisponibles = repository.tiposFaena,
            tipoFaena = repository.tiposFaena.firstOrNull() ?: ""
        )
    )
    val uiState: StateFlow<TrabajoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buzos.collect { lista ->
                val primerBuzoId = _uiState.value.buzoSeleccionadoId ?: lista.firstOrNull()?.id
                _uiState.update {
                    it.copy(
                        buzos = lista,
                        buzoSeleccionadoId = primerBuzoId
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.prevencionistaActivo.collect { prev ->
                if (prev != null) {
                    _uiState.update {
                        it.copy(supervisor = "${prev.nombre} (Prevencionista)")
                    }
                }
            }
        }
    }

    fun seleccionarBuzo(buzoId: String) {
        _uiState.update { it.copy(buzoSeleccionadoId = buzoId, errorMessage = null) }
    }

    fun onCentroChange(v: String) = _uiState.update { it.copy(centroCultivo = v) }
    fun onTipoFaenaChange(v: String) = _uiState.update { it.copy(tipoFaena = v) }
    fun onProfundidadChange(v: String) = _uiState.update { it.copy(profundidad = v) }
    fun onDuracionChange(v: String) = _uiState.update { it.copy(duracionMinutos = v) }
    fun onSupervisorChange(v: String) = _uiState.update { it.copy(supervisor = v) }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(successDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun autorizarFaena() {
        val state = _uiState.value
        val buzo = state.buzoSeleccionado

        if (buzo == null) {
            _uiState.update { it.copy(errorMessage = "Seleccione un buzo válido.") }
            return
        }

        // La aptitud de salud ya no se exige al autorizar: se evalúa por faena
        // (encuesta ANTES con el buzo equipado y encuesta DESPUÉS al cerrar).

        val prof = state.profundidad.toIntOrNull() ?: 15
        val mins = state.duracionMinutos.toIntOrNull() ?: 30
        val fechaHoraActual = repository.obtenerFechaHoraActual()
        val nuevoFolio = "F-${100 + repository.faenas.value.size + 1}"

        val nuevaFaena = RegistroFaena(
            id = nuevoFolio,
            buzoId = buzo.id,
            buzoNombre = buzo.nombre,
            empresa = buzo.empresa,
            centroCultivo = state.centroCultivo,
            tipoFaena = state.tipoFaena,
            profundidadMetros = prof,
            tiempoMinutos = mins,
            fechaHora = fechaHoraActual,
            estado = "Autorizada y en Curso"
        )

        repository.autorizarFaena(nuevaFaena)

        _uiState.update {
            it.copy(
                successDialog = true,
                errorMessage = null,
                userMessage = "Faena $nuevoFolio autorizada e iniciada exitosamente."
            )
        }
    }
}
