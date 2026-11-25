package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import java.time.LocalDate

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
    val fotoUrl: String? = null,
    val estatura: Float? = null,
    val talla: Float? = null,
    val peso: Float? = null,
    val alergias: String? = null,
    val edadIso: String? = null,
    val testEsfuerzoVigente: Boolean = true,
    val examenPreocupacionalVigente: Boolean = true,
    val puntajeCalbuco: Int = 90,
    val certificadoAchsUri: String? = null,
    val fechaVencimientoMatriculaIso: String? = null,
    val alertaMatriculaNoInvasiva: Boolean = false
) {
    fun toDomain(): Buzo = Buzo(
        id = id,
        rut = rut,
        nombre = nombre,
        empresa = empresa,
        matricula = matricula,
        telefono = telefono,
        estadoSalud = try {
            EstadoSalud.valueOf(estadoSalud)
        } catch (_: Exception) {
            EstadoSalud.PENDIENTE
        },
        ultimoChequeo = ultimoChequeo,
        categoria = categoria,
        vigenciaMatricula = vigenciaMatricula,
        fotoUrl = fotoUrl,
        estatura = estatura,
        talla = talla,
        peso = peso,
        alergias = alergias,
        edad = edadIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        testEsfuerzoVigente = testEsfuerzoVigente,
        examenPreocupacionalVigente = examenPreocupacionalVigente,
        puntajeCalbuco = puntajeCalbuco,
        certificadoAchsUri = certificadoAchsUri,
        fechaVencimientoMatricula = fechaVencimientoMatriculaIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        alertaMatriculaNoInvasiva = alertaMatriculaNoInvasiva
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
            fotoUrl = buzo.fotoUrl,
            estatura = buzo.estatura,
            talla = buzo.talla,
            peso = buzo.peso,
            alergias = buzo.alergias,
            edadIso = buzo.edad?.toString(),
            testEsfuerzoVigente = buzo.testEsfuerzoVigente,
            examenPreocupacionalVigente = buzo.examenPreocupacionalVigente,
            puntajeCalbuco = buzo.puntajeCalbuco,
            certificadoAchsUri = buzo.certificadoAchsUri,
            fechaVencimientoMatriculaIso = buzo.fechaVencimientoMatricula?.toString(),
            alertaMatriculaNoInvasiva = buzo.alertaMatriculaNoInvasiva
        )
    }
}
