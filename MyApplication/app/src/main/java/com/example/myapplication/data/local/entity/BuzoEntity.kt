package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud

/**
 * Entidad de Room para la tabla de buzos en SQLite.
 */
@Entity(tableName = "buzos")
data class BuzoEntity(
    @PrimaryKey
    val id: String,
    val rut: String,
    val nombre: String,
    val empresa: String,
    val matricula: String,
    val telefono: String,
    val estadoSalud: String,
    val ultimoChequeo: String,
    val categoria: String,
    val vigenciaMatricula: String,
    val fotoUrl: String?
) {
    fun toDomain(): Buzo = Buzo(
        id = id,
        rut = rut,
        nombre = nombre,
        empresa = empresa,
        matricula = matricula,
        telefono = telefono,
        estadoSalud = try { EstadoSalud.valueOf(estadoSalud) } catch (_: Exception) { EstadoSalud.PENDIENTE },
        ultimoChequeo = ultimoChequeo,
        categoria = categoria,
        vigenciaMatricula = vigenciaMatricula,
        fotoUrl = fotoUrl
    )

    companion object {
        fun fromDomain(buzo: Buzo): BuzoEntity = BuzoEntity(
            id = buzo.id,
            rut = buzo.rut,
            nombre = buzo.nombre,
            empresa = buzo.empresa,
            matricula = buzo.matricula,
            telefono = buzo.telefono,
            estadoSalud = buzo.estadoSalud.name,
            ultimoChequeo = buzo.ultimoChequeo,
            categoria = buzo.categoria,
            vigenciaMatricula = buzo.vigenciaMatricula,
            fotoUrl = buzo.fotoUrl
        )
    }
}
