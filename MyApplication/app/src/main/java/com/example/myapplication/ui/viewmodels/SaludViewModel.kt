package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppData
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SaludUiState(
    val buzos: List<Buzo> = emptyList(),
    val buzoSeleccionadoId: String? = null,

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
    val buzoSeleccionado: Buzo?
        get() = buzos.firstOrNull { it.id == buzoSeleccionadoId }

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
        val buzo = state.buzoSeleccionado
        if (buzo == null) {
            _uiState.update { it.copy(userMessage = "Seleccione un buzo antes de registrar el chequeo.") }
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

        val fechaChequeo = LocalDate.now()
        val nuevoChequeo = Chequeo(
            id = "${AppData.chequeos.size + 1}",
            buzoRut = buzo.rut,
            fecha = fechaChequeo,
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

        val nuevoEstado = if (state.esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO
        val detalle = "Hoy - PA: ${state.presionArterial} mmHg, Pulso: ${state.frecuenciaCardiaca} lpm"
        repository.actualizarSaludBuzo(buzo.id, nuevoEstado, detalle)

        val mensaje = if (state.esApto) {
            "Chequeo registrado: ${buzo.nombre} declarado APTO para inmersión."
        } else {
            "Alerta: ${buzo.nombre} declarado NO APTO. Notificado a supervisor."
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
