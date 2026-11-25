package com.example.myapplication.data.model

/**
 * Rol y nivel de permisos del usuario dentro de AquaCheck (Anexo A EA4 / Paso 1 EA5).
 */
enum class RolUsuario(val etiqueta: String, val puedeAprobarYCerrarFaena: Boolean) {
    SUPERVISOR(
        etiqueta = "Supervisor / Prevencionista SEREMI",
        puedeAprobarYCerrarFaena = true
    ),
    OPERADOR_BUCEO(
        etiqueta = "Operador / Asistente de Terreno",
        puedeAprobarYCerrarFaena = false
    )
}

/**
 * Representa al usuario autenticado en terreno.
 */
data class Prevencionista(
    val nombre: String,
    val rut: String,
    val correo: String,
    val registroSeremi: String,
    val centro: String,
    val contrasena: String = "admin123",
    val fotoUrl: String? = "https://randomuser.me/api/portraits/men/75.jpg",
    val rol: RolUsuario = RolUsuario.SUPERVISOR
) {
    val esSupervisor: Boolean get() = rol.puedeAprobarYCerrarFaena
}
