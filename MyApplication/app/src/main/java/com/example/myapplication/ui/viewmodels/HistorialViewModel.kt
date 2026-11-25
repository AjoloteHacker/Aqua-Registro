package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.BitacoraPostInmersion
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.MotivoCierreSinTerminar
import com.example.myapplication.data.model.Pendiente
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.DestinoRevision
import com.example.myapplication.data.repository.SolicitudRevision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val selectedTab: Int = 0,
    val faenas: List<RegistroFaena> = emptyList(),
    val historialMedico: List<Chequeo> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Todas",
    val userMessage: String? = null,
    /** Lo que le falta a cada faena en curso para poder finalizarse (lo calcula la propia faena). */
    val pendientesPorFaena: Map<String, List<Pendiente>> = emptyMap(),
    /** Novedades reportadas por faena (equipos riesgosos, salud DESPUÉS no apta): no bloquean. */
    val novedadesPorFaena: Map<String, List<String>> = emptyMap(),
    /** Semáforo industrial calculado para cada faena (VERDE / AMARILLO / ROJO). */
    val semaforoPorFaena: Map<String, ColorSemaforo> = emptyMap(),
    /** IDs de registros pendientes de sincronizar con el backend Spring Boot. */
    val pendientesSincronizarIds: Set<String> = emptySet(),
    /** IDs de buzos con matrícula marítima DIRECTEMAR vencida. */
    val buzosConMatriculaVencidaIds: Set<String> = emptySet(),
    val isNetworkConnected: Boolean = true,
    /** Bitácoras post-inmersión registradas con velocidades de descenso/ascenso (24/9 m/min). */
    val bitacorasPostInmersion: List<BitacoraPostInmersion> = emptyList(),
    val esUsuarioSupervisor: Boolean = true,
    val rolUsuarioEtiqueta: String = "Supervisor / Prevencionista SEREMI",
    val registrandoBitacoraFaenaId: String? = null,
    val errorBitacora: String? = null,
    val cerrandoSinTerminarFaenaId: String? = null,
    val motivosAutomaticosCierre: Set<MotivoCierreSinTerminar> = emptySet(),
    val errorCierreSinTerminar: String? = null,
    val editandoComentarioFaenaId: String? = null,
    val faenaEnRevisionId: String? = null,
    val editandoFaenaId: String? = null,
    val errorEdicion: String? = null,
    val centrosDisponibles: List<String> = emptyList(),
    val tiposFaenaDisponibles: List<String> = emptyList()
) {
    val faenasFiltradas: List<RegistroFaena>
        get() = faenas.filter { faena ->
            val coincideTexto = searchQuery.isBlank() ||
                    faena.buzoNombre.contains(searchQuery, ignoreCase = true) ||
                    faena.socorristaNombre.contains(searchQuery, ignoreCase = true) ||
                    faena.centroCultivo.contains(searchQuery, ignoreCase = true) ||
                    faena.tipoFaena.contains(searchQuery, ignoreCase = true) ||
                    faena.empresa.contains(searchQuery, ignoreCase = true) ||
                    faena.autorizadoPor.contains(searchQuery, ignoreCase = true) ||
                    faena.id.contains(searchQuery, ignoreCase = true)

            val coincideFiltro = when (selectedFilter) {
                "En Curso" -> faena.enCurso
                "Completada" -> faena.completada
                "Sin Terminar" -> faena.cerradaSinTerminar
                else -> true
            }

            coincideTexto && coincideFiltro
        }

    val chequeosFiltrados: List<Chequeo>
        get() = historialMedico.filter { chequeo ->
            val coincideTexto = searchQuery.isBlank() ||
                    chequeo.buzoNombre.contains(searchQuery, ignoreCase = true) ||
                    chequeo.buzoRut.contains(searchQuery, ignoreCase = true) ||
                    chequeo.faenaId.contains(searchQuery, ignoreCase = true) ||
                    chequeo.evaluadoPor.contains(searchQuery, ignoreCase = true) ||
                    chequeo.id.contains(searchQuery, ignoreCase = true)

            val coincideFiltro = when (selectedFilter) {
                "En Curso", "Aptos" -> chequeo.esApto
                "Completada", "No Aptos" -> !chequeo.esApto
                else -> true
            }

            coincideTexto && coincideFiltro
        }

    /** Faena cuyo diálogo de revisión está abierto (null si ninguno, o si ya no está en curso). */
    val faenaEnRevision: RegistroFaena?
        get() = faenas.firstOrNull { it.id == faenaEnRevisionId && it.enCurso }

    /** Faena cuyos datos se están editando (null si ninguna, o si ya no está en curso). */
    val editandoFaena: RegistroFaena?
        get() = faenas.firstOrNull { it.id == editandoFaenaId && it.enCurso }

    /** Faena para la cual se está registrando o consultando la bitácora post-inmersión (24/9 m/min). */
    val faenaBitacoraActiva: RegistroFaena?
        get() = faenas.firstOrNull { it.id == registrandoBitacoraFaenaId }

    /** Faena que se está cerrando anticipadamente ("Faena sin terminar"). */
    val faenaCerrandoSinTerminar: RegistroFaena?
        get() = faenas.firstOrNull { it.id == cerrandoSinTerminarFaenaId && it.enCurso }

    /** Faena cerrada sin terminar cuyo comentario se está modificando post-cierre. */
    val faenaEditandoComentario: RegistroFaena?
        get() = faenas.firstOrNull { it.id == editandoComentarioFaenaId && it.cerradaSinTerminar }

    /** Alertas históricas de sobrevelocidad (>24 m/min descenso o >9 m/min ascenso) notificadas al Jefe de Centro. */
    val alertasVelocidad: List<BitacoraPostInmersion>
        get() = bitacorasPostInmersion.filter { it.alertaVelocidadSuperada }
}

class HistorialViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HistorialUiState(
            centrosDisponibles = repository.centrosCultivo,
            tiposFaenaDisponibles = repository.tiposFaena
        )
    )
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        // Se recalcula cuando cambian las faenas, la salud de los buzos, las revisiones de equipos,
        // la evidencia fotográfica, las etapas confirmadas o las encuestas de salud
        // (por ejemplo, al volver desde Equipos o Salud).
        viewModelScope.launch {
            val cambiosEquipos = combine(
                repository.revisiones,
                repository.evidenciasEquipos,
                repository.etapasConfirmadas,
                repository.encuestas
            ) { _, _, _, _ -> Unit }
            combine(
                combine(repository.faenas, repository.buzos) { faenas, _ -> faenas },
                cambiosEquipos
            ) { faenas, _ -> faenas }
                .collect { faenas -> actualizarFaenas(faenas) }
        }
        viewModelScope.launch {
            repository.historialMedico.collect { lista ->
                _uiState.update { it.copy(historialMedico = lista) }
            }
        }
        viewModelScope.launch {
            repository.bitacorasPostInmersion.collect { bitacoras ->
                _uiState.update { it.copy(bitacorasPostInmersion = bitacoras) }
            }
        }
        viewModelScope.launch {
            repository.prevencionistaActivo.collect { usuario ->
                _uiState.update {
                    it.copy(
                        esUsuarioSupervisor = usuario?.esSupervisor ?: true,
                        rolUsuarioEtiqueta = usuario?.rol?.etiqueta ?: "Supervisor / Prevencionista SEREMI"
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(
                repository.pendientesSincronizarIds,
                repository.isNetworkConnected
            ) { pendientes, online -> pendientes to online }
                .collect { (pendientes, online) ->
                    _uiState.update {
                        it.copy(
                            pendientesSincronizarIds = pendientes,
                            isNetworkConnected = online
                        )
                    }
                }
        }
    }

    fun onTabChange(tab: Int) = _uiState.update {
        it.copy(selectedTab = tab, selectedFilter = "Todas")
    }

    private fun actualizarFaenas(faenas: List<RegistroFaena>) {
        val enCurso = faenas.filter { it.enCurso }
        val avances = enCurso.associate { faena -> faena.id to repository.avanceDe(faena) }
        val pendientes = enCurso.associate { faena ->
            faena.id to faena.pendientes(avances.getValue(faena.id))
        }
        val novedades = enCurso.associate { faena ->
            faena.id to faena.novedades(avances.getValue(faena.id))
        }
        val semaforos = faenas.associate { faena ->
            val avance = avances[faena.id] ?: repository.avanceDe(faena)
            faena.id to if (faena.completada) ColorSemaforo.VERDE else faena.semaforo(avance)
        }
        val buzosVencidos = repository.buzos.value
            .filter { it.matriculaVencida }
            .map { it.id }
            .toSet()
        _uiState.update { estado ->
            estado.copy(
                faenas = faenas,
                pendientesPorFaena = pendientes,
                novedadesPorFaena = novedades,
                semaforoPorFaena = semaforos,
                buzosConMatriculaVencidaIds = buzosVencidos
            )
        }
    }

    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }
    fun onFilterChange(f: String) = _uiState.update { it.copy(selectedFilter = f) }

    fun abrirRevision(faenaId: String) = _uiState.update { it.copy(faenaEnRevisionId = faenaId) }
    fun cerrarRevision() = _uiState.update { it.copy(faenaEnRevisionId = null) }

    // ───────── Datos de la faena ─────────

    fun abrirEdicion(faenaId: String) =
        _uiState.update { it.copy(editandoFaenaId = faenaId, errorEdicion = null) }

    fun cerrarEdicion() =
        _uiState.update { it.copy(editandoFaenaId = null, errorEdicion = null) }

    fun editarDatos(
        faenaId: String,
        centro: String,
        tipo: String,
        profundidad: String,
        minutos: String
    ) {
        repository.editarDatosFaena(
            faenaId = faenaId,
            centro = centro,
            tipo = tipo,
            profundidad = profundidad.trim().toIntOrNull(),
            minutos = minutos.trim().toIntOrNull()
        )
            .onSuccess {
                _uiState.update {
                    it.copy(
                        editandoFaenaId = null,
                        errorEdicion = null,
                        userMessage = "Datos de $faenaId actualizados."
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(errorEdicion = error.message ?: "No se pudieron guardar los datos.")
                }
            }
    }

    fun aprobarDatos(faenaId: String) {
        repository.aprobarDatosFaena(faenaId)
            .onSuccess {
                _uiState.update { it.copy(userMessage = "Datos de $faenaId aprobados.") }
            }
            .onFailure { error ->
                _uiState.update { it.copy(userMessage = error.message ?: "No se pudo aprobar la faena $faenaId.") }
            }
    }

    fun revocarAprobacion(faenaId: String) {
        repository.revocarAprobacionFaena(faenaId)
            .onSuccess {
                _uiState.update { it.copy(userMessage = "Aprobación de $faenaId anulada.") }
            }
            .onFailure { error ->
                _uiState.update { it.copy(userMessage = error.message ?: "No se pudo anular la aprobación.") }
            }
    }

    // ───────── Accesos a otras pantallas ─────────

    /** Deja la faena preseleccionada en Control de Equipos y cierra el diálogo. */
    fun irAEquipos(faenaId: String) {
        repository.solicitarRevision(SolicitudRevision(faenaId, DestinoRevision.EQUIPOS))
        cerrarRevision()
    }

    /**
     * Deja la faena preseleccionada en Encuesta de Salud, con el momento que toca:
     * ANTES si falta o no resultó apto; DESPUÉS en caso contrario.
     */
    fun irASalud(faenaId: String) {
        val faena = _uiState.value.faenas.firstOrNull { it.id == faenaId } ?: return
        val antes = repository.avanceDe(faena).encuestas[MomentoSalud.ANTES]
        val momento = if (antes == null || !antes.esApto) MomentoSalud.ANTES else MomentoSalud.DESPUES
        repository.solicitarRevision(SolicitudRevision(faenaId, DestinoRevision.SALUD, momento))
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

    // ───────── Bitácora Post-Inmersión y Velocidades (24 / 9 m/min) ─────────

    fun abrirBitacoraPostInmersion(faenaId: String) {
        _uiState.update {
            it.copy(
                registrandoBitacoraFaenaId = faenaId,
                errorBitacora = null
            )
        }
    }

    fun cerrarBitacoraPostInmersion() {
        _uiState.update {
            it.copy(
                registrandoBitacoraFaenaId = null,
                errorBitacora = null
            )
        }
    }

    fun registrarBitacoraPostInmersion(
        faenaId: String,
        profundidad: String,
        tiempoFondo: String,
        velocidadDescenso: String,
        velocidadAscenso: String,
        origenDato: String,
        observaciones: String
    ) {
        val prof = profundidad.trim().toIntOrNull()
        val mins = tiempoFondo.trim().toIntOrNull()
        val velDesc = velocidadDescenso.trim().replace(',', '.').toDoubleOrNull()
        val velAsc = velocidadAscenso.trim().replace(',', '.').toDoubleOrNull()

        repository.registrarBitacoraPostInmersion(
            faenaId = faenaId,
            profundidadMetros = prof,
            tiempoFondoMinutos = mins,
            velocidadDescensoMpm = velDesc,
            velocidadAscensoMpm = velAsc,
            origenDato = origenDato,
            observaciones = observaciones
        ).onSuccess { bitacora ->
            val msg = if (bitacora.alertaVelocidadSuperada) {
                "ALERTA JEFE DE CENTRO (${bitacora.id}): ${bitacora.detalleAlerta}"
            } else {
                "Bitácora post-inmersión ${bitacora.id} registrada sin alertas de velocidad."
            }
            _uiState.update {
                it.copy(
                    registrandoBitacoraFaenaId = null,
                    errorBitacora = null,
                    userMessage = msg
                )
            }
        }.onFailure { err ->
            _uiState.update {
                it.copy(errorBitacora = err.message ?: "Error al registrar la bitácora post-inmersión.")
            }
        }
    }

    // ───────── Cierre Anticipado: Faenas sin terminar (docs/nota_3) ─────────

    fun abrirCierreSinTerminar(faenaId: String) {
        val automaticos = repository.motivosAutomaticosCierre(faenaId)
        _uiState.update {
            it.copy(
                cerrandoSinTerminarFaenaId = faenaId,
                motivosAutomaticosCierre = automaticos,
                errorCierreSinTerminar = null
            )
        }
    }

    fun cerrarDialogoSinTerminar() {
        _uiState.update {
            it.copy(
                cerrandoSinTerminarFaenaId = null,
                motivosAutomaticosCierre = emptySet(),
                errorCierreSinTerminar = null
            )
        }
    }

    fun confirmarCierreSinTerminar(
        faenaId: String,
        motivosSeleccionados: Set<MotivoCierreSinTerminar>,
        otrosMotivos: List<String>,
        comentario: String
    ) {
        repository.cerrarFaenaSinTerminar(
            faenaId = faenaId,
            motivosSeleccionados = motivosSeleccionados,
            otrosMotivos = otrosMotivos,
            comentario = comentario
        ).onSuccess {
            _uiState.update {
                it.copy(
                    cerrandoSinTerminarFaenaId = null,
                    faenaEnRevisionId = null,
                    motivosAutomaticosCierre = emptySet(),
                    errorCierreSinTerminar = null,
                    userMessage = "Faena $faenaId cerrada sin terminar."
                )
            }
        }.onFailure { err ->
            _uiState.update {
                it.copy(
                    errorCierreSinTerminar = err.message ?: "No se pudo cerrar la faena sin terminar."
                )
            }
        }
    }

    fun abrirEditarComentarioCierre(faenaId: String) {
        _uiState.update { it.copy(editandoComentarioFaenaId = faenaId) }
    }

    fun cerrarEditarComentarioCierre() {
        _uiState.update { it.copy(editandoComentarioFaenaId = null) }
    }

    fun guardarComentarioCierre(faenaId: String, nuevoComentario: String) {
        repository.actualizarComentarioCierreFaena(faenaId, nuevoComentario)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        editandoComentarioFaenaId = null,
                        userMessage = "Comentario de cierre de $faenaId actualizado."
                    )
                }
            }
            .onFailure { err ->
                _uiState.update {
                    it.copy(
                        editandoComentarioFaenaId = null,
                        userMessage = err.message ?: "No se pudo actualizar el comentario."
                    )
                }
            }
    }
}
