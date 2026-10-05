package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EquipoChequeo
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EquiposUiState(
    val buzos: List<Buzo> = emptyList(),
    val buzoSeleccionadoId: String? = null,
    val equipos: List<EquipoChequeo> = emptyList(),
    val userMessage: String? = null
) {
    val buzoSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == buzoSeleccionadoId }

    val totalEquipos: Int get() = equipos.size
    val equiposValidados: Int get() = equipos.count { it.verificado }
    val esConforme: Boolean get() = totalEquipos > 0 && equiposValidados == totalEquipos
}

class EquiposViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buzos.collect { listaBuzos ->
                val primerBuzoId = _uiState.value.buzoSeleccionadoId ?: listaBuzos.firstOrNull()?.id
                val equipos = if (primerBuzoId != null) repository.obtenerEquiposParaBuzo(primerBuzoId) else emptyList()
                _uiState.update {
                    it.copy(
                        buzos = listaBuzos,
                        buzoSeleccionadoId = primerBuzoId,
                        equipos = equipos
                    )
                }
            }
        }
    }

    fun seleccionarBuzo(buzoId: String) {
        val equipos = repository.obtenerEquiposParaBuzo(buzoId)
        _uiState.update {
            it.copy(
                buzoSeleccionadoId = buzoId,
                equipos = equipos,
                userMessage = null
            )
        }
    }

    fun toggleEquipo(equipoId: String, verificado: Boolean) {
        _uiState.update { state ->
            val actualizados = state.equipos.map {
                if (it.id == equipoId) it.copy(verificado = verificado) else it
            }
            state.copy(equipos = actualizados)
        }
    }

    fun alternarMarcarTodos() {
        _uiState.update { state ->
            val marcar = !state.esConforme
            val actualizados = state.equipos.map { it.copy(verificado = marcar) }
            state.copy(equipos = actualizados)
        }
    }

    fun guardarConformidad() {
        val state = _uiState.value
        val buzoId = state.buzoSeleccionadoId ?: return
        val buzoNombre = state.buzoSeleccionado?.nombre ?: "Buzo"

        repository.guardarEquiposParaBuzo(buzoId, state.equipos)

        val mensaje = if (state.esConforme) {
            "Equipos de $buzoNombre aprobados al 100% conforme."
        } else {
            "Checklist guardado: ${state.equiposValidados} de ${state.totalEquipos} equipos verificados."
        }

        _uiState.update { it.copy(userMessage = mensaje) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
