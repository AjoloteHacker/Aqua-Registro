package com.example.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.AquaRepository

/**
 * =============================================================================
 * Chequeo
 * =============================================================================
 * Entidad que modela el registro de chequeo biomédico y evaluación de salud
 * pre-inmersión para un buzo profesional o comercial.
 */
data class Chequeo(
    val id: String,
    val buzoRut: String,
    val buzoNombre: String = "",
    val faenaId: String = "",
    val momento: String = "Antes de la faena",
    val fechaHora: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val esApto: Boolean,
    val pesoKg: Double,
    val tallaM: Double,
    val colesterolTotal: Int,
    val hdl: Int,
    val ldl: Int,
    val trigliceridos: Int,
    val hemograma: Boolean,
    val glicemiaBasal: Int,
    val presionArterial: String,
    val frecuenciaCardiaca: Int,
    val evaluadoPor: String = "Cristóbal Loncón C."
)

/**
 * =============================================================================
 * AppData
 * =============================================================================
 * Fachada Singleton para compatibilidad y acceso a datos.
 */
object AppData {
    val repository: AquaRepository get() = AquaRepository.getInstance()

    val empresas: List<String> get() = repository.empresas
    val centrosCultivo: List<String> get() = repository.centrosCultivo
    val tiposFaena: List<String> get() = repository.tiposFaena

    var prevencionistaActivo: Prevencionista
        get() = repository.prevencionistaActivo.value ?: repository.usuarios.value.first()
        set(value) {}

    val buzos: List<Buzo> get() = repository.buzos.value
    val faenas: List<RegistroFaena> get() = repository.faenas.value
    val usuarios: List<Prevencionista> get() = repository.usuarios.value

    val chequeos: List<Chequeo> get() = repository.historialMedico.value

    fun obtenerEquiposDefault(): List<EquipoChequeo> = repository.obtenerEquiposDefault()
}
