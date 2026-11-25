package com.example.myapplication.data.model

/**
 * Fotografías exigidas por el control de equipos.
 *
 * No son una foto por equipo: son evidencia global de la etapa.
 * - Pre-equipación: una única foto general de los equipos.
 * - Equipado: una foto de frente y otra de atrás del buzo ya equipado.
 */
enum class TipoFotoEquipos(val etiqueta: String) {
    GENERAL("Foto general"),
    FRENTE("Frente"),
    ATRAS("Atrás");

    companion object {
        fun requeridas(etapa: EtapaComprobacion): List<TipoFotoEquipos> = when (etapa) {
            EtapaComprobacion.PRE_EQUIPACION -> listOf(GENERAL)
            EtapaComprobacion.EQUIPADO -> listOf(FRENTE, ATRAS)
        }
    }
}
