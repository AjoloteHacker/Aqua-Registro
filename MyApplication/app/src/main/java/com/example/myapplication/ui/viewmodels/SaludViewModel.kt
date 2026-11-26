package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.AsignacionBuzo
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.EncuestaSalud
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.DestinoRevision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

data class SaludUiState(
    val buzos: List<Buzo> = emptyList(),
    /** Faenas en curso: la encuesta se registra por faena y por momento (ANTES / DESPUÉS). */
    val faenas: List<RegistroFaena> = emptyList(),
    val faenaSeleccionadaId: String? = null,
    /** Texto del buscador del selector de faenas. */
    val busquedaFaena: String = "",
    /** Buzo de la tripulación seleccionado para validar en el semáforo (ejecutor o socorrista). */
    val buzoEvaluadoId: String? = null,
    val momento: MomentoSalud = MomentoSalud.ANTES,
    /** Motivo por el que aún no se puede registrar la encuesta (null = habilitada). */
    val bloqueo: String? = null,
    /** Encuesta ya registrada para la faena y el momento seleccionados (si existe). */
    val encuestaRegistrada: EncuestaSalud? = null,

    // Parámetros de validación cruzada del Semáforo Industrial (Anexo A EA4 / EA5)
    val edadBuzo: String = "35",
    val puntajeCalbuco: String = "90",
    val examenPreocupacional: Boolean = true,
    val compresorCumple: Boolean = true,
    val buzoEmergenciaCumple: Boolean = true,
    val equiposInutilizablesCount: Int = 0,
    val equiposRiesgososCount: Int = 0,

    // Signos vitales
    val presionArterial: String = "120/80",
    val frecuenciaCardiaca: String = "72",

    // Laboratorio
    val colesterol: String = "180",
    val hdl: String = "50",
    val ldl: String = "105",
    val trigliceridos: String = "130",
    val hemograma: Boolean = true,
    val glicemiaBasal: String = "90",
    val historialMedico: List<Chequeo> = emptyList(),

    // Cuestionario de salud
    val testEsfuerzo: Boolean = true,
    val gastroIntestinal: Boolean = true,
    val cardiovascular: Boolean = true,
    val audiometria: Boolean = true,
    val optometria: Boolean = true,
    val electrocardiograma: Boolean = true,
    val descansoAdecuado: Boolean = true,
    val sinSustancias: Boolean = true,
    val sinCongestion: Boolean = true,
    val condicionOptima: Boolean = true,

    // Control de validación y retroalimentación
    val mostrarErrores: Boolean = false,
    val userMessage: String? = null
) {
    val faenaSeleccionada: RegistroFaena?
        get() = faenas.firstOrNull { it.id == faenaSeleccionadaId }

    /** Faenas que coinciden con el buscador (folio, buzos, tipo y centro). */
    val faenasFiltradas: List<RegistroFaena>
        get() = faenas.filter {
            coincideConBusqueda(
                busquedaFaena,
                it.id,
                it.buzoNombre,
                it.socorristaNombre,
                it.tipoFaena,
                it.centroCultivo
            )
        }

    val tripulacionFaena: List<AsignacionBuzo>
        get() = faenaSeleccionada?.asignaciones().orEmpty()

    val buzoSeleccionado: Buzo?
        get() {
            val targetId = buzoEvaluadoId?.takeIf { it.isNotBlank() } ?: faenaSeleccionada?.buzoId
            return targetId?.let { id -> buzos.firstOrNull { it.id == id } }
        }

    // Validación cruzada de edad, IMC y Escuela de Calbuco (obtenidos del perfil del buzo)
    val edadBuzoVal: Int?
        get() = buzoSeleccionado?.edadAnios ?: edadBuzo.trim().toIntOrNull()?.takeIf { it in 18..85 }

    val puntajeCalbucoVal: Int?
        get() = buzoSeleccionado?.puntajeCalbuco ?: puntajeCalbuco.trim().toIntOrNull()?.takeIf { it in 0..100 }

    val imcBuzo: Double?
        get() = buzoSeleccionado?.imc

    /** Regla Crítica 1 (Anexo A EA4/EA5): Buzo >45 años sin test de esfuerzo vigente -> Bloqueo Semáforo ROJO. */
    val bloqueoMayor45SinTest: Boolean
        get() = (edadBuzoVal ?: 0) > 45 && !testEsfuerzo

    /** Regla Crítica 2 (Anexo A EA4/EA5): Compresor, buzo de emergencia o equipo en «No cumple» -> Bloqueo Semáforo ROJO. */
    val bloqueoEquiposCriticos: Boolean
        get() = !compresorCumple || !buzoEmergenciaCumple || equiposInutilizablesCount > 0

    // Rango médico
    val pulso: Int?
        get() = frecuenciaCardiaca.trim().toIntOrNull()

    val pulsoValido: Boolean
        get() = pulso != null && pulso!! in 50..100

    val partesPresion: List<String>
        get() = presionArterial.split("/")

    val sistolica: Int?
        get() = partesPresion.getOrNull(0)?.trim()?.toIntOrNull()

    val diastolica: Int?
        get() = if (partesPresion.size == 2) partesPresion[1].trim().toIntOrNull() else null

    val presionValida: Boolean
        get() = sistolica != null && diastolica != null && sistolica!! in 90..139 && diastolica!! in 60..89

    val signosEnRango: Boolean
        get() = pulsoValido && presionValida

    // Valores numéricos de laboratorio
    val colesterolVal: Int?
        get() = colesterol.toIntOrNull()?.takeIf { it > 0 }

    val hdlVal: Int?
        get() = hdl.toIntOrNull()?.takeIf { it > 0 }

    val ldlVal: Int?
        get() = ldl.toIntOrNull()?.takeIf { it > 0 }

    val trigliceridosVal: Int?
        get() = trigliceridos.toIntOrNull()?.takeIf { it > 0 }

    val glicemiaVal: Int?
        get() = glicemiaBasal.toIntOrNull()?.takeIf { it > 0 }

    val cuestionarioOk: Boolean
        get() = testEsfuerzo && gastroIntestinal && cardiovascular && audiometria &&
                optometria && electrocardiograma && descansoAdecuado &&
                sinSustancias && sinCongestion && condicionOptima

    val motivosBloqueo: List<String>
        get() = buildList {
            if (bloqueoMayor45SinTest) {
                add("BLOQUEO CRÍTICO: Buzo tiene ${edadBuzoVal} años (>45) y NO cuenta con Test de Esfuerzo vigente.")
            }
            if (!compresorCumple) {
                add("BLOQUEO CRÍTICO: Compresor principal de suministro de aire en estado «No cumple».")
            }
            if (!buzoEmergenciaCumple) {
                add("BLOQUEO CRÍTICO: Buzo de emergencia / socorrista en superficie en estado «No cumple».")
            }
            if (equiposInutilizablesCount > 0) {
                add("BLOQUEO TÉCNICO: $equiposInutilizablesCount equipo(s) de la faena en estado Inutilizable («No cumple»).")
            }
            if (!examenPreocupacional) {
                add("BLOQUEO MÉDICO: Examen médico preocupacional vencido o no vigente.")
            }
            if (!signosEnRango) {
                add("BLOQUEO BIOMÉDICO: Presión arterial ($presionArterial) o pulso ($frecuenciaCardiaca lpm) fuera del rango normativo.")
            }
            if (!cuestionarioOk && !bloqueoMayor45SinTest) {
                add("BLOQUEO FÍSICO: Factores de riesgo en cuestionario de declaración de salud.")
            }
        }

    val advertenciasSemaforo: List<String>
        get() = buildList {
            val calbuco = puntajeCalbucoVal
            if (calbuco != null && calbuco < 70) {
                add("ADVERTENCIA OPERATIVA: Puntaje Escuela de Buceo de Calbuco ($calbuco/100 < 70) requiere supervisión.")
            }
            val imc = imcBuzo
            if (imc != null && (imc < 19.0 || imc > 30.0)) {
                add("ADVERTENCIA BIOMÉDICA: IMC del buzo (${String.format(Locale.US, "%.1f", imc)}) fuera de rango estándar (19.0 - 30.0).")
            }
            if (equiposRiesgososCount > 0) {
                add("ADVERTENCIA TÉCNICA: $equiposRiesgososCount equipo(s) de la faena marcado(s) como Utilizable Riesgoso.")
            }
        }

    val esApto: Boolean
        get() = cuestionarioOk && signosEnRango && !bloqueoMayor45SinTest && examenPreocupacional && !bloqueoEquiposCriticos

    val semaforo: ColorSemaforo
        get() = when {
            !esApto || motivosBloqueo.isNotEmpty() -> ColorSemaforo.ROJO
            advertenciasSemaforo.isNotEmpty() -> ColorSemaforo.AMARILLO
            else -> ColorSemaforo.VERDE
        }

    val motivoNoApto: String
        get() = when {
            bloqueoMayor45SinTest ->
                "BLOQUEO CRÍTICO: Buzo mayor de 45 años (${edadBuzoVal} años) sin Test de Esfuerzo vigente."
            bloqueoEquiposCriticos ->
                motivosBloqueo.firstOrNull() ?: "BLOQUEO CRÍTICO: Equipo crítico o buzo de emergencia en «No cumple»."
            !examenPreocupacional ->
                "BLOQUEO MÉDICO: El buzo no cuenta con Examen Preocupacional vigente."
            !cuestionarioOk ->
                "Existen factores de riesgo en el cuestionario físico. Se prohíbe el buceo hasta normalización."
            else ->
                "Revise presión arterial y pulso: están fuera del rango normativo seguro."
        }
}

class SaludViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaludUiState())
    val uiState: StateFlow<SaludUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.faenas,
                repository.buzos,
                repository.encuestas,
                repository.revisiones,
                repository.etapasConfirmadas
            ) { faenas, buzos, _, _, _ -> faenas.filter { it.enCurso } to buzos }
                .collect { (enCurso, buzos) -> sincronizar(enCurso, buzos) }
        }
        viewModelScope.launch {
            repository.historialMedico.collect { lista ->
                _uiState.update { it.copy(historialMedico = lista) }
            }
        }
        // Cuando Historial pide registrar la encuesta de una faena, se preselecciona aquí.
        viewModelScope.launch {
            repository.solicitudRevision.collect { solicitud ->
                if (solicitud != null && solicitud.destino == DestinoRevision.SALUD) {
                    _uiState.update {
                        it.copy(
                            faenaSeleccionadaId = solicitud.faenaId,
                            busquedaFaena = "",
                            momento = solicitud.momento ?: it.momento,
                            userMessage = null
                        )
                    }
                    repository.limpiarSolicitud(DestinoRevision.SALUD)
                    recalcular()
                }
            }
        }
    }

    fun seleccionarFaena(faenaId: String) {
        _uiState.update {
            it.copy(
                faenaSeleccionadaId = faenaId,
                busquedaFaena = "",
                buzoEvaluadoId = null,
                userMessage = null
            )
        }
        recalcular(cargarDatosBuzo = true)
    }

    fun onBusquedaFaenaChange(texto: String) {
        _uiState.update { it.copy(busquedaFaena = texto) }
    }

    fun seleccionarBuzoEvaluado(buzoId: String) {
        _uiState.update { it.copy(buzoEvaluadoId = buzoId, userMessage = null) }
        recalcular(cargarDatosBuzo = true)
    }

    fun seleccionarMomento(momento: MomentoSalud) {
        _uiState.update { it.copy(momento = momento, userMessage = null) }
        recalcular(cargarDatosBuzo = false)
    }

    private fun sincronizar(enCurso: List<RegistroFaena>, buzos: List<Buzo>) {
        val prevFaenaId = _uiState.value.faenaSeleccionadaId
        val nuevaFaenaId = prevFaenaId
            ?.takeIf { id -> enCurso.any { it.id == id } }
            ?: enCurso.firstOrNull()?.id
        val cambioDeFaena = prevFaenaId != nuevaFaenaId || _uiState.value.buzoEvaluadoId == null
        _uiState.update { s ->
            s.copy(buzos = buzos, faenas = enCurso, faenaSeleccionadaId = nuevaFaenaId)
        }
        recalcular(cargarDatosBuzo = cambioDeFaena)
    }

    /** Qué momento está habilitado y qué encuesta ya existe, sincronizando los parámetros del semáforo. */
    private fun recalcular(cargarDatosBuzo: Boolean = false) {
        val s = _uiState.value
        val faena = s.faenaSeleccionada
        if (faena == null) {
            _uiState.update { it.copy(bloqueo = null, encuestaRegistrada = null) }
            return
        }
        val avance = repository.avanceDe(faena)
        val asignaciones = faena.asignaciones()

        // Si no se ha elegido un buzo concreto de la tripulación, priorizar si alguno tiene bloqueo (>45 sin test)
        val buzoTargetId = s.buzoEvaluadoId?.takeIf { id -> asignaciones.any { it.buzoId == id } }
            ?: asignaciones.firstOrNull { asig ->
                s.buzos.firstOrNull { it.id == asig.buzoId }?.bloqueadoPorEdadSinTestEsfuerzo == true
            }?.buzoId
            ?: faena.buzoId

        val buzoObj = s.buzos.firstOrNull { it.id == buzoTargetId }
        val todasRevisiones = avance.buzos.values.flatMap {
            it.etapas[EtapaComprobacion.PRE_EQUIPACION].orEmpty()
        }
        val inutilizables = todasRevisiones.count { it.estado == EstadoEquipo.INUTILIZABLE }
        val riesgosos = todasRevisiones.count { it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO }

        _uiState.update { state ->
            state.copy(
                buzoEvaluadoId = buzoTargetId,
                bloqueo = faena.bloqueoEncuesta(s.momento, avance),
                encuestaRegistrada = avance.encuestas[s.momento],
                equiposInutilizablesCount = inutilizables,
                equiposRiesgososCount = riesgosos,
                edadBuzo = if (cargarDatosBuzo && buzoObj?.edadAnios != null) buzoObj.edadAnios.toString() else state.edadBuzo,
                testEsfuerzo = if (cargarDatosBuzo && buzoObj != null) buzoObj.testEsfuerzoVigente else state.testEsfuerzo,
                examenPreocupacional = if (cargarDatosBuzo && buzoObj != null) buzoObj.examenPreocupacionalVigente else state.examenPreocupacional,
                puntajeCalbuco = if (cargarDatosBuzo && buzoObj != null) buzoObj.puntajeCalbuco.toString() else state.puntajeCalbuco
            )
        }
    }

    // Mutadores validación cruzada semáforo
    fun onEdadBuzoChange(v: String) {
        if (v.all { it.isDigit() } && v.length <= 2) {
            _uiState.update { it.copy(edadBuzo = v) }
        }
    }

    fun onPuntajeCalbucoChange(v: String) {
        if (v.all { it.isDigit() } && v.length <= 3) {
            _uiState.update { it.copy(puntajeCalbuco = v) }
        }
    }

    fun onExamenPreocupacionalChange(v: Boolean) = _uiState.update { it.copy(examenPreocupacional = v) }
    fun onCompresorCumpleChange(v: Boolean) = _uiState.update { it.copy(compresorCumple = v) }
    fun onBuzoEmergenciaCumpleChange(v: Boolean) = _uiState.update { it.copy(buzoEmergenciaCumple = v) }

    // Mutadores signos vitales
    fun onPresionChange(v: String) = _uiState.update { it.copy(presionArterial = v) }
    fun onFrecuenciaChange(v: String) {
        if (v.all { it.isDigit() }) {
            _uiState.update { it.copy(frecuenciaCardiaca = v) }
        }
    }

    // Mutadores laboratorio
    fun onColesterolChange(v: String) = _uiState.update { it.copy(colesterol = v) }
    fun onHdlChange(v: String) = _uiState.update { it.copy(hdl = v) }
    fun onLdlChange(v: String) = _uiState.update { it.copy(ldl = v) }
    fun onTrigliceridosChange(v: String) = _uiState.update { it.copy(trigliceridos = v) }
    fun onHemogramaChange(v: Boolean) = _uiState.update { it.copy(hemograma = v) }
    fun onGlicemiaChange(v: String) = _uiState.update { it.copy(glicemiaBasal = v) }

    // Mutadores cuestionario
    fun onTestEsfuerzoChange(v: Boolean) = _uiState.update { it.copy(testEsfuerzo = v) }
    fun onGastroIntestinalChange(v: Boolean) = _uiState.update { it.copy(gastroIntestinal = v) }
    fun onCardiovascularChange(v: Boolean) = _uiState.update { it.copy(cardiovascular = v) }
    fun onAudiometriaChange(v: Boolean) = _uiState.update { it.copy(audiometria = v) }
    fun onOptometriaChange(v: Boolean) = _uiState.update { it.copy(optometria = v) }
    fun onElectrocardiogramaChange(v: Boolean) = _uiState.update { it.copy(electrocardiograma = v) }
    fun onDescansoChange(v: Boolean) = _uiState.update { it.copy(descansoAdecuado = v) }
    fun onSinSustanciasChange(v: Boolean) = _uiState.update { it.copy(sinSustancias = v) }
    fun onSinCongestionChange(v: Boolean) = _uiState.update { it.copy(sinCongestion = v) }
    fun onCondicionOptimaChange(v: Boolean) = _uiState.update { it.copy(condicionOptima = v) }

    fun guardarDictamen() {
        val state = _uiState.value
        val faena = state.faenaSeleccionada
        val buzo = state.buzoSeleccionado
        if (faena == null || buzo == null) {
            _uiState.update { it.copy(userMessage = "Seleccione una faena en curso antes de registrar la encuesta.") }
            return
        }
        if (state.bloqueo != null) {
            _uiState.update { it.copy(userMessage = state.bloqueo) }
            return
        }

        // Validar campos obligatorios
        if (state.pulso == null || state.sistolica == null || state.diastolica == null ||
            state.edadBuzoVal == null || state.puntajeCalbucoVal == null ||
            state.colesterolVal == null || state.hdlVal == null ||
            state.ldlVal == null || state.trigliceridosVal == null ||
            state.glicemiaVal == null
        ) {
            _uiState.update {
                it.copy(
                    mostrarErrores = true,
                    userMessage = "Complete todos los campos numéricos obligatorios con formato válido antes de emitir el dictamen."
                )
            }
            return
        }

        // La faena decide si la encuesta puede registrarse (orden del flujo); el repositorio la guarda junto con el semáforo.
        repository.registrarEncuestaSalud(
            faenaId = faena.id,
            momento = state.momento,
            esApto = state.esApto,
            presionArterial = state.presionArterial.trim(),
            frecuenciaCardiaca = state.pulso!!,
            semaforo = state.semaforo,
            motivosBloqueo = state.motivosBloqueo,
            advertencias = state.advertenciasSemaforo,
            buzoEvaluadoId = buzo.id,
            edadBuzo = state.edadBuzoVal,
            testEsfuerzoVigente = state.testEsfuerzo,
            examenPreocupacionalVigente = state.examenPreocupacional,
            puntajeCalbuco = state.puntajeCalbucoVal,
            compresorCumple = state.compresorCumple,
            buzoEmergenciaCumple = state.buzoEmergenciaCumple
        ).onFailure { error ->
            _uiState.update { it.copy(userMessage = error.message ?: "No se pudo registrar la encuesta.") }
            return
        }

        // Registro histórico del chequeo médico completo
        val evaluador = repository.prevencionistaActivo.value?.let {
            "${it.nombre} (SEREMI: ${it.registroSeremi})"
        } ?: "Prevencionista de Turno"

        val nuevoChequeo = Chequeo(
            id = "CH-${100 + repository.historialMedico.value.size + 1}",
            buzoRut = buzo.rut,
            buzoNombre = buzo.nombre,
            faenaId = faena.id,
            momento = state.momento.etiqueta,
            fechaHora = repository.obtenerFechaHoraActual(),
            fecha = LocalDate.now(),
            esApto = state.esApto,
            colesterolTotal = state.colesterolVal!!,
            hdl = state.hdlVal!!,
            ldl = state.ldlVal!!,
            trigliceridos = state.trigliceridosVal!!,
            hemograma = state.hemograma,
            glicemiaBasal = state.glicemiaVal!!,
            presionArterial = state.presionArterial.trim(),
            frecuenciaCardiaca = state.pulso!!,
            evaluadoPor = evaluador
        )
        repository.registrarChequeoMedico(nuevoChequeo)

        val mensaje = when (state.semaforo) {
            ColorSemaforo.VERDE ->
                "Semáforo VERDE (${state.momento.etiqueta}) en ${faena.id}: ${buzo.nombre} declarado APTO."
            ColorSemaforo.AMARILLO ->
                "Semáforo AMARILLO (${state.momento.etiqueta}) en ${faena.id}: ${buzo.nombre} APTO con observaciones."
            ColorSemaforo.ROJO ->
                "Semáforo ROJO (${state.momento.etiqueta}) en ${faena.id}: BLOQUEO para ${buzo.nombre}. ${state.motivoNoApto}"
        }

        _uiState.update {
            it.copy(
                userMessage = mensaje,
                mostrarErrores = false
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
