package com.example.myapplication.data.model

/**
 * Registro de autorización e inmersión submarina.
 */
data class RegistroFaena(
    val id: String,
    val buzoNombre: String,
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String = "Autorizada y en Curso"
)
