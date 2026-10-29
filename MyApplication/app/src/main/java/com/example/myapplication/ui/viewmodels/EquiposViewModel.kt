package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.model.RevisionEquipo
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.DestinoRevision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ───────────────────────── Modelo de UI ─────────────────────────

/** Faena en curso que se puede revisar, con el avance de la etapa seleccionada. */
data class FaenaRevision(
    val id: String,
    val buzoNombre: String,
    val tipoFaena: String,
    val equiposCompletos: Int,
    val totalEquipos: Int
)

data class EquiposUiState(
    val faenas: List<FaenaRevision> = emptyList(),
    val faenaSeleccionadaId: String? = null,
    val etapa: EtapaComprobacion = EtapaComprobacion.PRE_EQUIPACION,
    val equipos: List<RevisionEquipo> = emptyList(),
    val userMessage: String? = null
) {
    val aceptables get() = equipos.count { it.estado == EstadoEquipo.ACEPTABLE }
    val riesgosos get() = equipos.count { it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO }
    val inutilizables get() = equipos.count { it.estado == EstadoEquipo.INUTILIZABLE }
    val sinRevisar get() = equipos.count { !it.tieneEstado }
    val sinFoto get() = equipos.count { it.faltaFoto }
    val corroborables get() = equipos.count { it.esCorroborable }
    val completos get() = equipos.count { it.estaCompleto }
    val todoCompleto get() = equipos.isNotEmpty() && completos == equipos.size
}

// ───────────────────────── ViewModel ─────────────────────────

/**
 * Control de equipos por FAENA: cada faena en curso tiene su propio checklist,
 * evidencia y etapas confirmadas (no se heredan de faenas anteriores del mismo buzo).
 */
class EquiposViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    private var faenasEnCurso: List<RegistroFaena> = emptyList()

    init {
        viewModelScope.launch {
            repository.faenas.collect { lista ->
                faenasEnCurso = lista.filter { it.enCurso }
                val seleccionada = _uiState.value.faenaSeleccionadaId
                    ?.takeIf { id -> faenasEnCurso.any { it.id == id } }
                    ?: faenasEnCurso.firstOrNull()?.id
                _uiState.update { it.copy(faenaSeleccionadaId = seleccionada) }
                recargar()
            }
        }
        // Cuando Historial pide revisar una faena concreta, se preselecciona aquí.
        viewModelScope.launch {
            repository.solicitudRevision.collect { solicitud ->
                if (solicitud != null && solicitud.destino == DestinoRevision.EQUIPOS) {
                    seleccionarFaena(solicitud.faenaId)
                    repository.limpiarSolicitud(DestinoRevision.EQUIPOS)
                }
            }
        }
    }

    fun seleccionarFaena(faenaId: String) {
        _uiState.update { it.copy(faenaSeleccionadaId = faenaId, userMessage = null) }
        recargar()
    }

    fun seleccionarEtapa(etapa: EtapaComprobacion) {
        _uiState.update { it.copy(etapa = etapa, userMessage = null) }
        recargar()
    }

    fun actualizarEstado(equipoId: String, estado: EstadoEquipo) {
        modificar(equipoId) {
            it.copy(estado = estado, registradoEn = System.currentTimeMillis())
        }
    }

    fun actualizarDescripcion(equipoId: String, texto: String) {
        modificar(equipoId) { it.copy(descripcionEstado = texto) }
    }

    /** Llamar cuando la cámara devuelva la URI de la foto del equipo. */
    fun registrarFoto(equipoId: String, uri: String) {
        modificar(equipoId) {
            it.copy(fotoUri = uri, registradoEn = System.currentTimeMillis())
        }
    }

    /** Todo ya está persistido; esto solo informa al usuario del estado del avance. */
    fun guardarAvance() {
        val s = _uiState.value
        val faena = faenasEnCurso.firstOrNull { it.id == s.faenaSeleccionadaId }
        val etiqueta = faena?.let { "${it.id} (${it.buzoNombre})" } ?: "la faena"
        _uiState.update {
            it.copy(
                userMessage = "Avance de $etiqueta guardado (${s.completos}/${s.equipos.size} completos, " +
                        "${s.corroborables} con evidencia). Debes completar el resto más tarde."
            )
        }
    }

    fun confirmarEtapa() {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        if (!s.todoCompleto) {
            _uiState.update { it.copy(userMessage = "Faltan equipos por completar.") }
            return
        }
        repository.confirmarEtapa(faenaId, s.etapa)
        _uiState.update { it.copy(userMessage = "${s.etapa.etiqueta} confirmada.") }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ───────── Internos ─────────

    private fun modificar(equipoId: String, cambio: (RevisionEquipo) -> RevisionEquipo) {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        val actual = s.equipos.firstOrNull { it.equipoId == equipoId } ?: return
        repository.guardarRevision(faenaId, s.etapa, cambio(actual)) // persiste al instante
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val faenas = faenasEnCurso.map { f ->
            val items = repository.revisionesCombinadas(f, s.etapa)
            FaenaRevision(f.id, f.buzoNombre, f.tipoFaena, items.count { it.estaCompleto }, items.size)
        }
        val equipos = faenasEnCurso
            .firstOrNull { it.id == s.faenaSeleccionadaId }
            ?.let { repository.revisionesCombinadas(it, s.etapa) }
            ?: emptyList()
        _uiState.update { it.copy(faenas = faenas, equipos = equipos) }
    }
}
