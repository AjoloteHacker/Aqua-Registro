package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaludUiState(
    val buzos: List<Buzo> = emptyList(),
    val buzoSeleccionadoId: String? = null,
    val presionArterial: String = "120/80",
    val frecuenciaCardiaca: String = "72",
    val descansoAdecuado: Boolean = true,
    val sinSustancias: Boolean = true,
    val sinCongestion: Boolean = true,
    val condicionOptima: Boolean = true,
    val userMessage: String? = null
) {
    val buzoSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == buzoSeleccionadoId }

    val esApto: Boolean
        get() = descansoAdecuado && sinSustancias && sinCongestion && condicionOptima
}

class SaludViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaludUiState())
    val uiState: StateFlow<SaludUiState> = _uiState.asStateFlow()

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
    }

    fun seleccionarBuzo(buzoId: String) {
        _uiState.update { it.copy(buzoSeleccionadoId = buzoId, userMessage = null) }
    }

    fun onPresionChange(v: String) = _uiState.update { it.copy(presionArterial = v) }
    fun onFrecuenciaChange(v: String) = _uiState.update { it.copy(frecuenciaCardiaca = v) }
    fun onDescansoChange(v: Boolean) = _uiState.update { it.copy(descansoAdecuado = v) }
    fun onSinSustanciasChange(v: Boolean) = _uiState.update { it.copy(sinSustancias = v) }
    fun onSinCongestionChange(v: Boolean) = _uiState.update { it.copy(sinCongestion = v) }
    fun onCondicionOptimaChange(v: Boolean) = _uiState.update { it.copy(condicionOptima = v) }

    fun guardarDictamen() {
        val state = _uiState.value
        val buzo = state.buzoSeleccionado ?: return

        val nuevoEstado = if (state.esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO
        val detalle = "Hoy - PA: ${state.presionArterial} mmHg, Pulso: ${state.frecuenciaCardiaca} lpm"

        repository.actualizarSaludBuzo(buzo.id, nuevoEstado, detalle)

        val mensaje = if (state.esApto) {
            "Dictamen emitido: ${buzo.nombre} declarado APTO para inmersión."
        } else {
            "Alerta: ${buzo.nombre} declarado NO APTO. Notificado a supervisor."
        }

        _uiState.update { it.copy(userMessage = mensaje) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
