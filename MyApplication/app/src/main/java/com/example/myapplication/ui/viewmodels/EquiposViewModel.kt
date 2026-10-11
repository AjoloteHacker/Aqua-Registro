package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.RevisionEquipo

// ───────────────────────── Modelo ─────────────────────────

enum class EstadoEquipo(val etiqueta: String) {
    SIN_REVISAR("Sin revisar"),
    ACEPTABLE("Aceptable"),
    UTILIZABLE_RIESGOSO("Riesgoso"),
    INUTILIZABLE("Inutilizable")
}

enum class EtapaComprobacion(val etiqueta: String) {
    PRE_EQUIPACION("Pre-equipación"),
    EQUIPADO("Equipado")
}

data class RevisionEquipo(
    val equipoId: String,
    val categoria: String,
    val nombre: String,
    val descripcionUso: String,
    val estado: EstadoEquipo = EstadoEquipo.SIN_REVISAR,
    val descripcionEstado: String = "",
    val fotoUri: String? = null,
    val registradoEn: Long? = null
) {
    val tieneEstado get() = estado != EstadoEquipo.SIN_REVISAR
    val esCorroborable get() = tieneEstado && fotoUri != null
    val estaCompleto get() = esCorroborable &&
            (estado == EstadoEquipo.ACEPTABLE || descripcionEstado.isNotBlank())
    val faltaFoto get() = tieneEstado && fotoUri == null
    val faltaDescripcion get() = tieneEstado && estado != EstadoEquipo.ACEPTABLE && descripcionEstado.isBlank()
}

data class BuzoRevision(
    val id: String,
    val nombre: String,
    val empresa: String,
    val equiposCompletos: Int,
    val totalEquipos: Int
)

data class EquiposUiState(
    val buzos: List<BuzoRevision> = emptyList(),
    val buzoSeleccionadoId: String? = null,
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

class EquiposViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    private var listaBuzos: List<Buzo> = emptyList()

    init {
        viewModelScope.launch {
            repository.buzos.collect { lista ->
                listaBuzos = lista
                val seleccionado = _uiState.value.buzoSeleccionadoId
                    ?.takeIf { id -> lista.any { it.id == id } }
                    ?: lista.firstOrNull()?.id
                _uiState.update { it.copy(buzoSeleccionadoId = seleccionado) }
                recargar()
            }
        }
    }

    fun seleccionarBuzo(buzoId: String) {
        _uiState.update { it.copy(buzoSeleccionadoId = buzoId, userMessage = null) }
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
        val buzo = listaBuzos.firstOrNull { it.id == s.buzoSeleccionadoId }?.nombre ?: "Buzo"
        _uiState.update {
            it.copy(
                userMessage = "Avance de $buzo guardado (${s.completos}/${s.equipos.size} completos, " +
                        "${s.corroborables} con evidencia). Debes completar el resto más tarde."
            )
        }
    }

    fun confirmarEtapa() {
        val s = _uiState.value
        val buzoId = s.buzoSeleccionadoId ?: return
        if (!s.todoCompleto) {
            _uiState.update { it.copy(userMessage = "Faltan equipos por completar.") }
            return
        }
        repository.confirmarEtapa(buzoId, s.etapa)
        _uiState.update { it.copy(userMessage = "${s.etapa.etiqueta} confirmada.") }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ───────── Internos ─────────

    private fun modificar(equipoId: String, cambio: (RevisionEquipo) -> RevisionEquipo) {
        val s = _uiState.value
        val buzoId = s.buzoSeleccionadoId ?: return
        val actual = s.equipos.firstOrNull { it.equipoId == equipoId } ?: return
        repository.guardarRevision(buzoId, s.etapa, cambio(actual)) // persiste al instante
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val buzos = listaBuzos.map { b ->
            val items = combinar(b.id, s.etapa)
            BuzoRevision(b.id, b.nombre, b.empresa, items.count { it.estaCompleto }, items.size)
        }
        val equipos = s.buzoSeleccionadoId?.let { combinar(it, s.etapa) } ?: emptyList()
        _uiState.update { it.copy(buzos = buzos, equipos = equipos) }
    }

    /** Catálogo del buzo + revisiones guardadas para la etapa. Sin revisión => SIN_REVISAR. */
    private fun combinar(buzoId: String, etapa: EtapaComprobacion): List<RevisionEquipo> {
        val guardadas = repository.obtenerRevisiones(buzoId, etapa).associateBy { it.equipoId }
        return repository.obtenerEquiposParaBuzo(buzoId).map { eq ->
            guardadas[eq.id] ?: RevisionEquipo(
                equipoId = eq.id,
                categoria = "General", // EquipoChequeo aún no tiene categoría
                nombre = eq.nombre,
                descripcionUso = eq.descripcion
            )
        }
    }
}