package com.example.myapplication.data.model

enum class EstadoEquipo(val etiqueta: String) {
    SIN_REVISAR("Sin revisar"),
    ACEPTABLE("Aceptable"),
    UTILIZABLE_RIESGOSO("Riesgoso"),
    INUTILIZABLE("Inutilizable")
}