package com.example.myapplication.data.model

/**
 * Representa al usuario prevencionista en terreno.
 */
data class Prevencionista(
    val nombre: String,
    val rut: String,
    val correo: String,
    val registroSeremi: String,
    val centro: String,
    val contrasena: String = "admin123",
    val fotoUrl: String? = "https://randomuser.me/api/portraits/men/75.jpg"
)
