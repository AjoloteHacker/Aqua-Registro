package com.example.myapplication.data.model

data class RevisionEquipo(
    val equipoId: String,
    val categoria: String,
    val nombre: String,
    val descripcionUso: String,
    val estado: EstadoEquipo = EstadoEquipo.SIN_REVISAR,
    val descripcionEstado: String = "",
    val fotoUri: String? = null,
    val registradoEn: Long? = null
) {
    val tieneEstado get() = estado != EstadoEquipo.SIN_REVISAR
    val esCorroborable get() = tieneEstado && fotoUri != null
    val estaCompleto get() = esCorroborable &&
            (estado == EstadoEquipo.ACEPTABLE || descripcionEstado.isNotBlank())
    val faltaFoto get() = tieneEstado && fotoUri == null
    val faltaDescripcion get() = tieneEstado && estado != EstadoEquipo.ACEPTABLE && descripcionEstado.isBlank()
}