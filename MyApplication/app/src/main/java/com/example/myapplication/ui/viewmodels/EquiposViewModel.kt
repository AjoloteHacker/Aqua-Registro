package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AsignacionBuzo
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
    val socorristaNombre: String = "",
    val tipoFaena: String,
    val equiposCompletos: Int,
    val totalEquipos: Int
)

data class EquiposUiState(
    val faenas: List<FaenaRevision> = emptyList(),
    val faenaSeleccionadaId: String? = null,
    /** Tripulación de la faena, en orden: 0 ejecutor, 1 socorrista. */
    val tripulacion: List<AsignacionBuzo> = emptyList(),
    /** Índice de [tripulacion] que se está registrando en este momento. */
    val indiceBuzo: Int = 0,
    val etapa: EtapaComprobacion = EtapaComprobacion.PRE_EQUIPACION,
    val equipos: List<RevisionEquipo> = emptyList(),
    val evidencia: EvidenciaEquipos = EvidenciaEquipos(),
    val etapaConfirmada: Boolean = false,
    /** Las cuatro combinaciones de buzo × etapa están confirmadas. */
    val ambosCompletos: Boolean = false,
    val userMessage: String? = null
) {
    val buzoActual: AsignacionBuzo? get() = tripulacion.getOrNull(indiceBuzo)
    val esUltimoBuzo get() = tripulacion.isNotEmpty() && indiceBuzo == tripulacion.lastIndex
    val aceptables get() = equipos.count { it.estado == EstadoEquipo.ACEPTABLE }
    val riesgosos get() = equipos.count { it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO }
    val inutilizables get() = equipos.count { it.estado == EstadoEquipo.INUTILIZABLE }
    val sinRevisar get() = equipos.count { !it.tieneEstado }
    val completos get() = equipos.count { it.evaluacionCompleta }
    val fotosFaltantes get() = evidencia.faltan(etapa)
    val fotoCompleta get() = evidencia.estaCompleta(etapa)
    /** Se completa al tener al menos 2 equipos evaluados (o todos si la lista tiene <= 2) + la evidencia fotográfica de la etapa. */
    val todoCompleto get() = equipos.isNotEmpty() && completos >= minOf(2, equipos.size) && fotoCompleta
}

// ───────────────────────── ViewModel ─────────────────────────

/**
 * Control de equipos por FAENA y BUZO: cada buzo de cada faena en curso tiene su
 * propio checklist, evidencia y etapas confirmadas.
 *
 * La captura es seguida, sin selector: primero se registran las dos etapas del
 * ejecutor y, al confirmar «Equipado», se pasa automáticamente a la Pre-equipación
 * del socorrista. Se puede volver al ejecutor para corregir.
 *
 * La evaluación individual se comparte entre Pre-equipación y Equipado de un mismo
 * buzo; la evidencia fotográfica sí es por etapa.
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
        _uiState.update {
            it.copy(
                faenaSeleccionadaId = faenaId,
                indiceBuzo = 0,
                etapa = EtapaComprobacion.PRE_EQUIPACION,
                userMessage = null
            )
        }
        recargar()
    }

    /** Permite volver al ejecutor para corregir sin perder lo ya registrado. */
    fun seleccionarBuzo(indice: Int) {
        _uiState.update { it.copy(indiceBuzo = indice, userMessage = null) }
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

    /** Registra la fotografía de un equipo crítico (maleta aire, filtros, aletas, Census) para comparar con Línea Base. */
    fun registrarFotoEquipoCritico(equipoId: String, uri: String) {
        modificar(equipoId) {
            it.copy(
                fotoActualUri = uri,
                registradoEn = System.currentTimeMillis()
            )
        }
    }

    /** Actualiza el resultado de comparación visual contra la fotografía de Línea Base (Anexo B EA4/EA5). */
    fun actualizarComparacionLineaBase(equipoId: String, coincide: Boolean, observacion: String? = null) {
        modificar(equipoId) {
            it.copy(
                coincideConLineaBase = coincide,
                observacionLineaBase = observacion ?: it.observacionLineaBase,
                estado = if (!coincide) EstadoEquipo.INUTILIZABLE else it.estado,
                descripcionEstado = if (!coincide && it.descripcionEstado.isBlank()) {
                    "Discrepancia detectada frente a Línea Base (${it.fotoLineaBaseRef})"
                } else {
                    it.descripcionEstado
                }
            )
        }
    }

    /** Llamar cuando la cámara devuelva la URI de una foto de evidencia de la etapa. */
    fun registrarFoto(tipo: TipoFotoEquipos, uri: String) {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        val buzoId = s.buzoActual?.buzoId ?: return
        repository.guardarFotoEvidencia(faenaId, buzoId, s.etapa, tipo, uri)
        recargar()
    }

    /** Todo ya está persistido; esto solo informa al usuario del estado del avance. */
    fun guardarAvance() {
        val s = _uiState.value
        val etiqueta = s.buzoActual?.let { "${it.rol.etiqueta}: ${it.buzoNombre}" } ?: "la faena"
        val evidencia = if (s.fotoCompleta) "evidencia completa" else "fotos pendientes"
        _uiState.update {
            it.copy(
                userMessage = "Avance de $etiqueta guardado (${s.completos}/${s.equipos.size} equipos evaluados, " +
                    "$evidencia en ${s.etapa.etiqueta}). Debes completar el resto más tarde."
            )
        }
    }

    /**
     * Confirma la etapa del buzo actual. Al terminar EQUIPADO del ejecutor se pasa
     * automáticamente a la Pre-equipación del socorrista (captura seguida).
     */
    fun confirmarEtapa() {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        val buzo = s.buzoActual ?: return
        if (!s.todoCompleto) {
            _uiState.update { it.copy(userMessage = "Faltan evaluaciones o fotos por completar.") }
            return
        }
        repository.confirmarEtapa(faenaId, buzo.buzoId, s.etapa)

        val mensaje = when {
            // Última etapa del ejecutor: sigue el socorrista, sin selector.
            s.etapa == EtapaComprobacion.EQUIPADO && !s.esUltimoBuzo -> {
                _uiState.update {
                    it.copy(
                        indiceBuzo = it.indiceBuzo + 1,
                        etapa = EtapaComprobacion.PRE_EQUIPACION
                    )
                }
                "${buzo.rol.etiqueta} completado. Ahora registra la Pre-equipación del socorrista."
            }
            s.esUltimoBuzo && s.tripulacion.size > 1 ->
                "Ambos buzos completos. Ya puede revisar la encuesta de salud."
            s.esUltimoBuzo ->
                "Buzo ejecutor completado. Ya puede revisar la encuesta de salud."
            else -> "${s.etapa.etiqueta} confirmada para ${buzo.rol.etiqueta}."
        }
        if (s.etapa != EtapaComprobacion.EQUIPADO) {
            _uiState.update { it.copy(etapa = EtapaComprobacion.EQUIPADO) }
        }
        recargar()
        _uiState.update { it.copy(userMessage = mensaje) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ───────── Internos ─────────

    private fun modificar(equipoId: String, cambio: (RevisionEquipo) -> RevisionEquipo) {
        val s = _uiState.value
        val faenaId = s.faenaSeleccionadaId ?: return
        val buzoId = s.buzoActual?.buzoId ?: return
        val actual = s.equipos.firstOrNull { it.equipoId == equipoId } ?: return
        repository.guardarRevision(faenaId, buzoId, cambio(actual)) // se guarda al instante
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val faenas = faenasEnCurso.map { f ->
            val items = repository.revisionesCombinadas(f, f.buzoId, s.etapa)
            FaenaRevision(
                id = f.id,
                buzoNombre = f.buzoNombre,
                socorristaNombre = f.socorristaNombre,
                tipoFaena = f.tipoFaena,
                equiposCompletos = items.count { it.evaluacionCompleta },
                totalEquipos = items.size
            )
        }
        val seleccionada = faenasEnCurso.firstOrNull { it.id == s.faenaSeleccionadaId }
        val tripulacion = seleccionada?.asignaciones().orEmpty()
        // Si la faena aún no tiene socorrista o se cambió de faena, el índice debe ser válido.
        val indice = s.indiceBuzo.coerceIn(0, (tripulacion.size - 1).coerceAtLeast(0))
        val buzoId = tripulacion.getOrNull(indice)?.buzoId
        val equipos = buzoId
            ?.let { repository.revisionesCombinadas(requireNotNull(seleccionada), it, s.etapa) }
            ?: emptyList()
        val evidencia = buzoId
            ?.let { repository.obtenerEvidencia(requireNotNull(seleccionada).id, it, s.etapa) }
            ?: EvidenciaEquipos()
        val confirmada = buzoId?.let {
            repository.etapaConfirmada(requireNotNull(seleccionada).id, it, s.etapa)
        } ?: false
        val ambosCompletos = seleccionada?.let { faena ->
            faena.asignaciones().all { asignacion ->
                EtapaComprobacion.entries.all { etapa ->
                    repository.etapaConfirmada(faena.id, asignacion.buzoId, etapa)
                }
            }
        } ?: false
        _uiState.update {
            it.copy(
                faenas = faenas,
                tripulacion = tripulacion,
                indiceBuzo = indice,
                equipos = equipos,
                evidencia = evidencia,
                etapaConfirmada = confirmada,
                ambosCompletos = ambosCompletos
            )
        }
    }
}
