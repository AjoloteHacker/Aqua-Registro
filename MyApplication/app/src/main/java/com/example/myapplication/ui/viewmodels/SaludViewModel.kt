package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppData
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EncuestaSalud
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

data class SaludUiState(
    val buzos: List<Buzo> = emptyList(),
    /** Faenas en curso: la encuesta se registra por faena y por momento (ANTES / DESPUÉS). */
    val faenas: List<RegistroFaena> = emptyList(),
    val faenaSeleccionadaId: String? = null,
    val momento: MomentoSalud = MomentoSalud.ANTES,
    /** Motivo por el que aún no se puede registrar la encuesta (null = habilitada). */
    val bloqueo: String? = null,
    /** Encuesta ya registrada para la faena y el momento seleccionados (si existe). */
    val encuestaRegistrada: EncuestaSalud? = null,

    // Signos vitales
    val presionArterial: String = "120/80",
    val frecuenciaCardiaca: String = "72",

    // Antropometría y Laboratorio
    val pesoKg: String = "",
    val talla: String = "",
    val colesterol: String = "",
    val hdl: String = "",
    val ldl: String = "",
    val trigliceridos: String = "",
    val hemograma: Boolean = true,
    val glicemiaBasal: String = "",

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

    val buzoSeleccionado: Buzo?
        get() = faenaSeleccionada?.let { faena -> buzos.firstOrNull { it.id == faena.buzoId } }

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

    // Valores numéricos de antropometría y laboratorio
    val pesoVal: Double?
        get() = pesoKg.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

    val tallaVal: Double?
        get() = talla.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

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

    val esApto: Boolean
        get() = cuestionarioOk && signosEnRango

    val motivoNoApto: String
        get() = if (!cuestionarioOk) {
            "Existen factores de riesgo en el cuestionario físico. Se prohíbe el buceo hasta normalización."
        } else {
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
        // Cuando Historial pide registrar la encuesta de una faena, se preselecciona aquí.
        viewModelScope.launch {
            repository.solicitudRevision.collect { solicitud ->
                if (solicitud != null && solicitud.destino == DestinoRevision.SALUD) {
                    _uiState.update {
                        it.copy(
                            faenaSeleccionadaId = solicitud.faenaId,
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
        _uiState.update { it.copy(faenaSeleccionadaId = faenaId, userMessage = null) }
        recalcular()
    }

    fun seleccionarMomento(momento: MomentoSalud) {
        _uiState.update { it.copy(momento = momento, userMessage = null) }
        recalcular()
    }

    private fun sincronizar(enCurso: List<RegistroFaena>, buzos: List<Buzo>) {
        _uiState.update { s ->
            val seleccionada = s.faenaSeleccionadaId
                ?.takeIf { id -> enCurso.any { it.id == id } }
                ?: enCurso.firstOrNull()?.id
            s.copy(buzos = buzos, faenas = enCurso, faenaSeleccionadaId = seleccionada)
        }
        recalcular()
    }

    /** Qué momento está habilitado y qué encuesta ya existe, según lo que dice la propia faena. */
    private fun recalcular() {
        val s = _uiState.value
        val faena = s.faenaSeleccionada
        if (faena == null) {
            _uiState.update { it.copy(bloqueo = null, encuestaRegistrada = null) }
            return
        }
        val avance = repository.avanceDe(faena)
        _uiState.update {
            it.copy(
                bloqueo = faena.bloqueoEncuesta(s.momento, avance),
                encuestaRegistrada = avance.encuestas[s.momento]
            )
        }
    }

    // Mutadores signos vitales
    fun onPresionChange(v: String) = _uiState.update { it.copy(presionArterial = v) }
    fun onFrecuenciaChange(v: String) {
        if (v.all { it.isDigit() }) {
            _uiState.update { it.copy(frecuenciaCardiaca = v) }
        }
    }

    // Mutadores antropometría y laboratorio
    fun onPesoChange(v: String) = _uiState.update { it.copy(pesoKg = v) }
    fun onTallaChange(v: String) = _uiState.update { it.copy(talla = v) }
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
        if (!state.signosEnRango ||
            state.pesoVal == null || state.tallaVal == null ||
            state.colesterolVal == null || state.hdlVal == null ||
            state.ldlVal == null || state.trigliceridosVal == null ||
            state.glicemiaVal == null
        ) {
            _uiState.update {
                it.copy(
                    mostrarErrores = true,
                    userMessage = "Complete todos los campos obligatorios antes de emitir el dictamen."
                )
            }
            return
        }

        // La faena decide si la encuesta puede registrarse (orden del flujo); el repositorio la guarda.
        repository.registrarEncuestaSalud(
            faenaId = faena.id,
            momento = state.momento,
            esApto = state.esApto,
            presionArterial = state.presionArterial.trim(),
            frecuenciaCardiaca = state.pulso!!
        ).onFailure { error ->
            _uiState.update { it.copy(userMessage = error.message ?: "No se pudo registrar la encuesta.") }
            return
        }

        // Registro histórico del chequeo (igual que antes)
        val nuevoChequeo = Chequeo(
            id = "${AppData.chequeos.size + 1}",
            buzoRut = buzo.rut,
            fecha = LocalDate.now(),
            esApto = state.esApto,
            pesoKg = state.pesoVal!!,
            tallaM = state.tallaVal!!,
            colesterolTotal = state.colesterolVal!!,
            hdl = state.hdlVal!!,
            ldl = state.ldlVal!!,
            trigliceridos = state.trigliceridosVal!!,
            hemograma = state.hemograma,
            glicemiaBasal = state.glicemiaVal!!,
            presionArterial = state.presionArterial.trim(),
            frecuenciaCardiaca = state.pulso!!
        )
        AppData.chequeos.add(nuevoChequeo)

        val mensaje = if (state.esApto) {
            "Encuesta (${state.momento.etiqueta}) de ${faena.id} registrada: ${buzo.nombre} declarado APTO."
        } else {
            "Alerta: ${buzo.nombre} declarado NO APTO (${state.momento.etiqueta}, ${faena.id}). Notificado a supervisor."
        }

        _uiState.update {
            it.copy(
                userMessage = mensaje,
                mostrarErrores = false,
                pesoKg = "",
                talla = "",
                colesterol = "",
                hdl = "",
                ldl = "",
                trigliceridos = "",
                glicemiaBasal = "",
                hemograma = true,
                testEsfuerzo = true,
                gastroIntestinal = true,
                cardiovascular = true,
                audiometria = true,
                optometria = true,
                electrocardiograma = true,
                descansoAdecuado = true,
                sinSustancias = true,
                sinCongestion = true,
                condicionOptima = true
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
