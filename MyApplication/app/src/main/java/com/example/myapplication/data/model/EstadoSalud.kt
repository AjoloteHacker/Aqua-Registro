package com.example.myapplication.data.model

/**
 * Estado médico pre-buceo del trabajador.
 */
enum class EstadoSalud(val label: String) {
    APTO("Apto para Inmersión"),
    NO_APTO("No Apto - Precaución"),
    PENDIENTE("Chequeo Pendiente")
}
