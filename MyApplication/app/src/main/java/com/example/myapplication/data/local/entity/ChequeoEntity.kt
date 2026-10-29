package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.Chequeo
import java.time.LocalDate

/**
 * Entidad de Room para la tabla de chequeos biomédicos (Historial Médico) en SQLite.
 */
@Entity(tableName = "chequeos")
data class ChequeoEntity(
    @PrimaryKey
    val id: String,
    val buzoRut: String,
    val buzoNombre: String,
    val faenaId: String,
    val momento: String,
    val fechaHora: String,
    val fechaIso: String,
    val esApto: Boolean,
    val pesoKg: Double,
    val tallaM: Double,
    val colesterolTotal: Int,
    val hdl: Int,
    val ldl: Int,
    val trigliceridos: Int,
    val hemograma: Boolean,
    val glicemiaBasal: Int,
    val presionArterial: String,
    val frecuenciaCardiaca: Int,
    val evaluadoPor: String
) {
    fun toDomain(): Chequeo = Chequeo(
        id = id,
        buzoRut = buzoRut,
        buzoNombre = buzoNombre,
        faenaId = faenaId,
        momento = momento,
        fechaHora = fechaHora,
        fecha = runCatching { LocalDate.parse(fechaIso) }.getOrElse { LocalDate.now() },
        esApto = esApto,
        pesoKg = pesoKg,
        tallaM = tallaM,
        colesterolTotal = colesterolTotal,
        hdl = hdl,
        ldl = ldl,
        trigliceridos = trigliceridos,
        hemograma = hemograma,
        glicemiaBasal = glicemiaBasal,
        presionArterial = presionArterial,
        frecuenciaCardiaca = frecuenciaCardiaca,
        evaluadoPor = evaluadoPor
    )

    companion object {
        fun fromDomain(chequeo: Chequeo): ChequeoEntity = ChequeoEntity(
            id = chequeo.id,
            buzoRut = chequeo.buzoRut,
            buzoNombre = chequeo.buzoNombre,
            faenaId = chequeo.faenaId,
            momento = chequeo.momento,
            fechaHora = chequeo.fechaHora,
            fechaIso = chequeo.fecha.toString(),
            esApto = chequeo.esApto,
            pesoKg = chequeo.pesoKg,
            tallaM = chequeo.tallaM,
            colesterolTotal = chequeo.colesterolTotal,
            hdl = chequeo.hdl,
            ldl = chequeo.ldl,
            trigliceridos = chequeo.trigliceridos,
            hemograma = chequeo.hemograma,
            glicemiaBasal = chequeo.glicemiaBasal,
            presionArterial = chequeo.presionArterial,
            frecuenciaCardiaca = chequeo.frecuenciaCardiaca,
            evaluadoPor = chequeo.evaluadoPor
        )
    }
}
