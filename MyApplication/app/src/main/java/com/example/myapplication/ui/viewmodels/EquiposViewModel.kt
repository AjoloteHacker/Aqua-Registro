package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.EvidenciaEquipos
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.model.RevisionEquipo
import com.example.myapplication.data.model.TipoFotoEquipos
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
    val evidencia: EvidenciaEquipos = EvidenciaEquipos(),
    val userMessage: String? = null
) {
    val aceptables get() = equipos.count { it.estado == EstadoEquipo.ACEPTABLE }
    val riesgosos get() = equipos.count { it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO }
    val inutilizables get() = equipos.count { it.estado == EstadoEquipo.INUTILIZABLE }
    val sinRevisar get() = equipos.count { !it.tieneEstado }
    val completos get() = equipos.count { it.evaluacionCompleta }
    val fotosFaltantes get() = evidencia.faltan(etapa)
    val fotoCompleta get() = evidencia.estaCompleta(etapa)
    val todoCompleto get() = equipos.isNotEmpty() && completos == equipos.size && fotoCompleta
}

// ───────────────────────── ViewModel ─────────────────────────

/**
 * Control de equipos por FAENA: cada faena en curso tiene su propio checklist,
 * evidencia y etapas confirmadas (no se heredan de faenas anteriores del mismo buzo).
 *
 * La evaluación individual se comparte entre Pre-equipación y Equipado: se guarda
 * una sola vez por (faena, equipo). La evidencia fotográfica sí es por etapa:
 * una foto general en Pre-equipación y frente/atrás en Equipado.
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

    /** Llamar cuando la cámara devuelva la URI de una foto de evidencia de la etapa. */
    fun registrarFoto(tipo: TipoFotoEquipos, uri: String) {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        repository.guardarFotoEvidencia(faenaId, s.etapa, tipo, uri)
        recargar()
    }

    /** Todo ya está persistido; esto solo informa al usuario del estado del avance. */
    fun guardarAvance() {
        val s = _uiState.value
        val faena = faenasEnCurso.firstOrNull { it.id == s.faenaSeleccionadaId }
        val etiqueta = faena?.let { "${it.id} (${it.buzoNombre})" } ?: "la faena"
        val evidencia = if (s.fotoCompleta) "evidencia completa" else "fotos pendientes"
        _uiState.update {
            it.copy(
                userMessage = "Avance de $etiqueta guardado (${s.completos}/${s.equipos.size} equipos evaluados, " +
                        "$evidencia en ${s.etapa.etiqueta}). Debes completar el resto más tarde."
            )
        }
    }

    fun confirmarEtapa() {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        if (!s.todoCompleto) {
            _uiState.update { it.copy(userMessage = "Faltan evaluaciones o fotos por completar.") }
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
        repository.guardarRevision(faenaId, cambio(actual)) // se guarda al instante (estado compartido)
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val faenas = faenasEnCurso.map { f ->
            val items = repository.revisionesCombinadas(f, s.etapa)
            FaenaRevision(
                f.id,
                f.buzoNombre,
                f.tipoFaena,
                items.count { it.evaluacionCompleta },
                items.size
            )
        }
        val seleccionada = faenasEnCurso.firstOrNull { it.id == s.faenaSeleccionadaId }
        val equipos = seleccionada
            ?.let { repository.revisionesCombinadas(it, s.etapa) }
            ?: emptyList()
        val evidencia = seleccionada
            ?.let { repository.obtenerEvidencia(it.id, s.etapa) }
            ?: EvidenciaEquipos()
        _uiState.update { it.copy(faenas = faenas, equipos = equipos, evidencia = evidencia) }
    }
}
