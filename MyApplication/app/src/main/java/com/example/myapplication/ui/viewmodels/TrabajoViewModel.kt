package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.DestinoRevision
import com.example.myapplication.data.repository.SolicitudRevision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrabajoUiState(
    val buzos: List<Buzo> = emptyList(),
    val buzoSeleccionadoId: String? = null,
    val socorristaSeleccionadoId: String? = null,
    val centrosDisponibles: List<String> = emptyList(),
    val centroCultivo: String = "",
    val tiposFaenaDisponibles: List<String> = emptyList(),
    val tipoFaena: String = "",
    val profundidad: String = "18",
    val duracionMinutos: String = "40",
    /** Supervisor implícito: se deriva de la sesión del prevencionista, nunca se edita a mano. */
    val supervisor: String = "",
    val errorMessage: String? = null,
    val successDialog: Boolean = false,
    /** Folio recién autorizado, para encadenar el flujo guiado a Control de Equipos. */
    val faenaAutorizadaId: String? = null,
    val userMessage: String? = null
) {
    val buzoSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == buzoSeleccionadoId }

    val socorristaSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == socorristaSeleccionadoId }

    /** El socorrista es un buzo secundario: nunca puede ser el ejecutor. */
    val socorristasDisponibles: List<Buzo>
        get() = buzos.filterNot { it.id == buzoSeleccionadoId }

    val tripulacionCompleta: Boolean
        get() = buzoSeleccionado != null &&
            socorristaSeleccionado != null &&
            buzoSeleccionadoId != socorristaSeleccionadoId

    val esBuzoApto: Boolean
        get() = buzoSeleccionado?.estadoSalud == EstadoSalud.APTO

    val esSocorristaApto: Boolean
        get() = socorristaSeleccionado?.estadoSalud == EstadoSalud.APTO

    /** Sin prevencionista en sesión no hay supervisor que autorice la faena. */
    val supervisorActivo: Boolean get() = supervisor.isNotBlank()

    /** La aptitud se evalúa en la encuesta de salud de la faena; aquí se exigen la tripulación y la sesión. */
    val puedeAutorizar: Boolean get() = tripulacionCompleta && supervisorActivo
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
                val actual = _uiState.value
                val ejecutorId = actual.buzoSeleccionadoId
                    ?.takeIf { id -> lista.any { it.id == id } }
                    ?: lista.firstOrNull()?.id
                val socorristaId = actual.socorristaSeleccionadoId
                    ?.takeIf { id -> lista.any { it.id == id } && id != ejecutorId }
                    ?: lista.firstOrNull { it.id != ejecutorId }?.id
                _uiState.update {
                    it.copy(
                        buzos = lista,
                        buzoSeleccionadoId = ejecutorId,
                        socorristaSeleccionadoId = socorristaId
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.prevencionistaActivo.collect { prev ->
                val nombreSupervisor = prev?.let { p -> "${p.nombre} (SEREMI: ${p.registroSeremi})" }.orEmpty()
                _uiState.update { it.copy(supervisor = nombreSupervisor) }
            }
        }
    }

    /** Cambia el ejecutor. Si coincide con el socorrista, se libera ese rol para evitar duplicados. */
    fun seleccionarBuzo(buzoId: String) {
        _uiState.update {
            it.copy(
                buzoSeleccionadoId = buzoId,
                socorristaSeleccionadoId = it.socorristaSeleccionadoId?.takeIf { id -> id != buzoId },
                errorMessage = null
            )
        }
    }

    fun seleccionarSocorrista(buzoId: String) {
        if (buzoId == _uiState.value.buzoSeleccionadoId) {
            _uiState.update {
                it.copy(errorMessage = "El socorrista debe ser distinto del buzo ejecutor.")
            }
            return
        }
        _uiState.update { it.copy(socorristaSeleccionadoId = buzoId, errorMessage = null) }
    }

    fun onCentroChange(v: String) = _uiState.update { it.copy(centroCultivo = v) }
    fun onTipoFaenaChange(v: String) = _uiState.update { it.copy(tipoFaena = v) }
    fun onProfundidadChange(v: String) = _uiState.update { it.copy(profundidad = v) }
    fun onDuracionChange(v: String) = _uiState.update { it.copy(duracionMinutos = v) }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(successDialog = false, faenaAutorizadaId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun autorizarFaena() {
        val prevencionista = repository.prevencionistaActivo.value
        if (prevencionista == null) {
            _uiState.update {
                it.copy(
                    supervisor = "",
                    errorMessage = "Inicie sesión como prevencionista para autorizar la faena."
                )
            }
            return
        }
        val supervisor = "${prevencionista.nombre} (SEREMI: ${prevencionista.registroSeremi})"

        val state = _uiState.value
        val buzo = state.buzoSeleccionado
        val socorrista = state.socorristaSeleccionado

        when {
            buzo == null -> {
                _uiState.update { it.copy(errorMessage = "Seleccione un buzo ejecutor válido.") }
                return
            }
            socorrista == null -> {
                _uiState.update { it.copy(errorMessage = "Seleccione un buzo socorrista (secundario).") }
                return
            }
            buzo.id == socorrista.id -> {
                _uiState.update {
                    it.copy(errorMessage = "El socorrista debe ser distinto del buzo ejecutor.")
                }
                return
            }
        }

        // La aptitud de salud ya no se exige al autorizar: se evalúa por faena
        // (encuesta ANTES con el buzo equipado; la DESPUÉS es informativa).

        val prof = state.profundidad.toIntOrNull() ?: 15
        val mins = state.duracionMinutos.toIntOrNull() ?: 30
        val fechaHoraActual = repository.obtenerFechaHoraActual()
        val nuevoFolio = "F-${100 + repository.faenas.value.size + 1}"

        val nuevaFaena = RegistroFaena(
            id = nuevoFolio,
            buzoId = buzo.id,
            buzoNombre = buzo.nombre,
            socorristaId = socorrista.id,
            socorristaNombre = socorrista.nombre,
            empresa = buzo.empresa,
            centroCultivo = state.centroCultivo,
            tipoFaena = state.tipoFaena,
            profundidadMetros = prof,
            tiempoMinutos = mins,
            fechaHora = fechaHoraActual,
            estado = RegistroFaena.ESTADO_EN_CURSO,
            autorizadoPor = supervisor
        )

        repository.autorizarFaena(nuevaFaena)

        _uiState.update {
            it.copy(
                successDialog = true,
                faenaAutorizadaId = nuevoFolio,
                supervisor = supervisor,
                errorMessage = null,
                userMessage = "Faena $nuevoFolio autorizada e iniciada con ${buzo.nombre} y ${socorrista.nombre}."
            )
        }
    }

    /**
     * Faena Completa: encadena la faena recién creada con Control de Equipos,
     * donde quedará preseleccionada para capturar a los dos buzos.
     */
    fun irAControlDeEquipos() {
        val faenaId = _uiState.value.faenaAutorizadaId ?: return
        repository.solicitarRevision(SolicitudRevision(faenaId, DestinoRevision.EQUIPOS))
    }
}
