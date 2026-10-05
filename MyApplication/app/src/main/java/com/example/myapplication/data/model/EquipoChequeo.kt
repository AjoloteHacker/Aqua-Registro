package com.example.myapplication.data.model

/**
 * Implemento individual del checklist de seguridad para buceo.
 */
data class EquipoChequeo(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val verificado: Boolean = false,
    val obligatorio: Boolean = true
)
