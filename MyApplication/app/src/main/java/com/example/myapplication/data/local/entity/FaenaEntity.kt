package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AprobacionDatos
import com.example.myapplication.data.model.RegistroFaena

/**
 * Entidad de Room para la tabla de faenas AST en SQLite.
 *
 * [socorristaId] y [socorristaNombre] se agregaron en la versión 6: las faenas
 * antiguas conservan sus valores por defecto (cadena vacía = socorrista no asignado).
 */
@Entity(tableName = "faenas")
data class FaenaEntity(
    @PrimaryKey
    val id: String,
    val buzoId: String,
    val buzoNombre: String,
    val socorristaId: String = "",
    val socorristaNombre: String = "",
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String,
    val autorizadoPor: String = "Cristóbal Loncón C. (Prevencionista)",
    val aprobadoPor: String? = null,
    val aprobadoEn: String? = null
) {
    fun toDomain(): RegistroFaena = RegistroFaena(
        id = id,
        buzoId = buzoId,
        buzoNombre = buzoNombre,
        socorristaId = socorristaId,
        socorristaNombre = socorristaNombre,
        empresa = empresa,
        centroCultivo = centroCultivo,
        tipoFaena = tipoFaena,
        profundidadMetros = profundidadMetros,
        tiempoMinutos = tiempoMinutos,
        fechaHora = fechaHora,
        estado = estado,
        autorizadoPor = autorizadoPor,
        aprobacion = if (aprobadoPor != null && aprobadoEn != null) {
            AprobacionDatos(aprobadoPor, aprobadoEn)
        } else null
    )

    companion object {
        fun fromDomain(faena: RegistroFaena): FaenaEntity = FaenaEntity(
            id = faena.id,
            buzoId = faena.buzoId,
            buzoNombre = faena.buzoNombre,
            socorristaId = faena.socorristaId,
            socorristaNombre = faena.socorristaNombre,
            empresa = faena.empresa,
            centroCultivo = faena.centroCultivo,
            tipoFaena = faena.tipoFaena,
            profundidadMetros = faena.profundidadMetros,
            tiempoMinutos = faena.tiempoMinutos,
            fechaHora = faena.fechaHora,
            estado = faena.estado,
            autorizadoPor = faena.autorizadoPor,
            aprobadoPor = faena.aprobacion?.aprobadoPor,
            aprobadoEn = faena.aprobacion?.fechaHora
        )
    }
}
