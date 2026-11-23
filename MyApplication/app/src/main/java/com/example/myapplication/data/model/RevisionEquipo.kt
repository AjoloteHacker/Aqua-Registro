package com.example.myapplication.data.model

/**
 * Evaluación individual de un equipo dentro de una faena.
 *
 * El estado se comparte entre Pre-equipación y Equipado: se guarda una sola vez
 * por (faena, equipo). Las fotos NO van aquí: son evidencia global por etapa
 * (ver [EvidenciaEquipos]).
 */
data class RevisionEquipo(
    val equipoId: String,
    val categoria: String,
    val nombre: String,
    val descripcionUso: String,
    val estado: EstadoEquipo = EstadoEquipo.SIN_REVISAR,
    val descripcionEstado: String = "",
    val registradoEn: Long? = null
) {
    val tieneEstado get() = estado != EstadoEquipo.SIN_REVISAR
    val faltaDescripcion get() = tieneEstado && estado != EstadoEquipo.ACEPTABLE && descripcionEstado.isBlank()
    val evaluacionCompleta get() = tieneEstado && !faltaDescripcion
}
