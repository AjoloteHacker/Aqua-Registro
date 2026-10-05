package com.example.myapplication.data

import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.AquaRepository

/**
 * Fachada para compatibilidad con código anterior.
 */
object AppData {
    val repository: AquaRepository get() = AquaRepository.getInstance()

    val empresas: List<String> get() = repository.empresas
    val centrosCultivo: List<String> get() = repository.centrosCultivo
    val tiposFaena: List<String> get() = repository.tiposFaena

    var prevencionistaActivo: Prevencionista
        get() = repository.prevencionistaActivo.value ?: repository.usuarios.value.first()
        set(value) {}

    val buzos: List<Buzo> get() = repository.buzos.value
    val faenas: List<RegistroFaena> get() = repository.faenas.value
    val usuarios: List<Prevencionista> get() = repository.usuarios.value

    fun obtenerEquiposDefault(): List<EquipoChequeo> = repository.obtenerEquiposDefault()
}
