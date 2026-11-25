package com.example.myapplication.data.model

/**
 * Evaluación individual de un equipo dentro de una faena.
 *
 * El estado se comparte entre Pre-equipación y Equipado: se guarda una sola vez
 * por (faena, equipo). Además, para los 4 equipos críticos del Anexo B (EA4 / EA5)
 * —Maleta de suministro de aire, Filtros de compresor, Aletas y Dispositivo Census—
 * mantiene la fotografía/inspección de línea base y el resultado de su comparación.
 */
data class RevisionEquipo(
    val equipoId: String,
    val categoria: String,
    val nombre: String,
    val descripcionUso: String,
    val estado: EstadoEquipo = EstadoEquipo.SIN_REVISAR,
    val descripcionEstado: String = "",
    val registradoEn: Long? = null,
    val esEquipoCriticoLineaBase: Boolean = equipoId in EQUIPOS_CRITICOS_IDS,
    val codigoTipoEquipoBackend: String = codigoBackendPorId(equipoId),
    val fotoActualUri: String? = null,
    val fotoLineaBaseRef: String = referenciaLineaBasePorId(equipoId),
    val coincideConLineaBase: Boolean = true,
    val observacionLineaBase: String = ""
) {
    val tieneEstado get() = estado != EstadoEquipo.SIN_REVISAR
    val faltaDescripcion get() = tieneEstado && estado != EstadoEquipo.ACEPTABLE && descripcionEstado.isBlank()
    val evaluacionCompleta get() = tieneEstado && !faltaDescripcion

    companion object {
        val EQUIPOS_CRITICOS_IDS = setOf("e1", "e2", "e3", "e4")

        fun codigoBackendPorId(id: String): String = when (id) {
            "e1" -> "MALETA_AIRE"
            "e2" -> "FILTROS_COMPRESOR"
            "e3" -> "ALETAS"
            "e4" -> "CENSUS"
            else -> "EQUIPO_${id.uppercase()}"
        }

        fun referenciaLineaBasePorId(id: String): String = when (id) {
            "e1" -> "LB-MALETA-01 · Manómetros 200 BAR y válvulas estancas certificadas"
            "e2" -> "LB-FILTROS-02 · Cartucho coalescente limpio sin saturación ni humedad"
            "e3" -> "LB-ALETAS-03 · Hebillas y correas íntegras sin grietas estructurales"
            "e4" -> "LB-CENSUS-04 · Telemetría Census calibrada con sello intacto"
            else -> "LB-REF-${id.uppercase()} · Estándar técnico DIRECTEMAR"
        }
    }
}
