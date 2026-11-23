package com.example.myapplication.data.model

/** Evidencia fotográfica de una etapa del control de equipos, por faena. */
data class EvidenciaEquipos(
    val fotos: Map<TipoFotoEquipos, String> = emptyMap(),
    val registradoEn: Long? = null
) {
    fun falta(tipo: TipoFotoEquipos): Boolean = fotos[tipo] == null

    /** Tipos de foto que todavía faltan para la etapa indicada. */
    fun faltan(etapa: EtapaComprobacion): List<TipoFotoEquipos> =
        TipoFotoEquipos.requeridas(etapa).filter(::falta)

    fun estaCompleta(etapa: EtapaComprobacion): Boolean = faltan(etapa).isEmpty()
}
