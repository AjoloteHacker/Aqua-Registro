package com.example.myapplication.data.model

/**
 * Representa a un buzo registrado en el sistema.
 */
data class Buzo(
    val id: String,
    val rut: String,
    val nombre: String,
    val empresa: String,
    val matricula: String,
    val telefono: String,
    val estadoSalud: EstadoSalud = EstadoSalud.PENDIENTE,
    val ultimoChequeo: String = "No realizado hoy",
    val categoria: String = "Buzo Comercial",
    val vigenciaMatricula: String = "Al día"
)
