package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AprobacionDatos
import com.example.myapplication.data.model.RegistroFaena

/**
 * Entidad de Room para la tabla de faenas AST en SQLite.
 */
@Entity(tableName = "faenas")
data class FaenaEntity(
    @PrimaryKey
    val id: String,
    val buzoId: String,
    val buzoNombre: String,
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String,
    val aprobadoPor: String? = null,
    val aprobadoEn: String? = null
) {
    fun toDomain(): RegistroFaena = RegistroFaena(
        id = id,
        buzoId = buzoId,
        buzoNombre = buzoNombre,
        empresa = empresa,
        centroCultivo = centroCultivo,
        tipoFaena = tipoFaena,
        profundidadMetros = profundidadMetros,
        tiempoMinutos = tiempoMinutos,
        fechaHora = fechaHora,
        estado = estado,
        aprobacion = if (aprobadoPor != null && aprobadoEn != null) {
            AprobacionDatos(aprobadoPor, aprobadoEn)
        } else null
    )

    companion object {
        fun fromDomain(faena: RegistroFaena): FaenaEntity = FaenaEntity(
            id = faena.id,
            buzoId = faena.buzoId,
            buzoNombre = faena.buzoNombre,
            empresa = faena.empresa,
            centroCultivo = faena.centroCultivo,
            tipoFaena = faena.tipoFaena,
            profundidadMetros = faena.profundidadMetros,
            tiempoMinutos = faena.tiempoMinutos,
            fechaHora = faena.fechaHora,
            estado = faena.estado,
            aprobadoPor = faena.aprobacion?.aprobadoPor,
            aprobadoEn = faena.aprobacion?.fechaHora
        )
    }
}
