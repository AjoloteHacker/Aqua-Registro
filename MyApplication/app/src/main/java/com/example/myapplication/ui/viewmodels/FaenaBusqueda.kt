package com.example.myapplication.ui.viewmodels

import java.text.Normalizer
import java.util.Locale

/** Normaliza texto para búsquedas tolerantes a mayúsculas y acentos. */
fun normalizarBusqueda(texto: String): String =
    Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
        .lowercase(Locale.ROOT)

/**
 * Indica si la búsqueda coincide con alguno de los campos de una faena.
 * Una búsqueda vacía muestra todas las opciones.
 */
fun coincideConBusqueda(busqueda: String, vararg campos: String?): Boolean {
    val consulta = normalizarBusqueda(busqueda)
    if (consulta.isBlank()) return true
    return campos.any { campo ->
        campo != null && normalizarBusqueda(campo).contains(consulta)
    }
}
