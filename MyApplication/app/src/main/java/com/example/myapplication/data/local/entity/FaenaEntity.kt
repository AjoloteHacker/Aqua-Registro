package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AprobacionDatos
import com.example.myapplication.data.model.CierreSinTerminar
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.EncuestaSalud
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.EvidenciaEquipos
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.MotivoCierreSinTerminar
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.model.RevisionEquipo
import com.example.myapplication.data.model.TipoFotoEquipos

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
    val aprobadoEn: String? = null,
    val cierreMotivos: String? = null,
    val cierreOtros: String? = null,
    val cierreComentario: String? = null,
    val cierrePor: String? = null,
    val cierreFechaHora: String? = null
) {
    fun toDomain(): RegistroFaena {
        val motivosSet = cierreMotivos
            ?.split("||")
            ?.mapNotNull { name -> runCatching { MotivoCierreSinTerminar.valueOf(name) }.getOrNull() }
            ?.toSet()
            .orEmpty()
        val cierreObj = if (motivosSet.isNotEmpty()) {
            CierreSinTerminar(
                motivos = motivosSet,
                otrosMotivos = cierreOtros?.split("||")?.filter { it.isNotBlank() }.orEmpty(),
                comentario = cierreComentario.orEmpty(),
                cerradoPor = cierrePor.orEmpty(),
                fechaHora = cierreFechaHora.orEmpty()
            )
        } else null

        return RegistroFaena(
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
            } else null,
            cierreSinTerminar = cierreObj
        )
    }

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
            aprobadoEn = faena.aprobacion?.fechaHora,
            cierreMotivos = faena.cierreSinTerminar?.motivos?.joinToString("||") { it.name },
            cierreOtros = faena.cierreSinTerminar?.otrosMotivos?.joinToString("||"),
            cierreComentario = faena.cierreSinTerminar?.comentario,
            cierrePor = faena.cierreSinTerminar?.cerradoPor,
            cierreFechaHora = faena.cierreSinTerminar?.fechaHora
        )
    }
}

/**
 * Entidad de Room para persistir la evaluación de cada equipo por faena y buzo en SQLite.
 */
@Entity(tableName = "revisiones_equipo")
data class RevisionEquipoEntity(
    @PrimaryKey
    val id: String,
    val faenaId: String,
    val buzoId: String,
    val equipoId: String,
    val categoria: String,
    val nombre: String,
    val descripcionUso: String,
    val estado: String,
    val descripcionEstado: String,
    val registradoEn: Long? = null,
    val fotoActualUri: String? = null,
    val fotoLineaBaseRef: String = "",
    val coincideConLineaBase: Boolean = true,
    val observacionLineaBase: String = ""
) {
    fun toDomain(): RevisionEquipo = RevisionEquipo(
        equipoId = equipoId,
        categoria = categoria,
        nombre = nombre,
        descripcionUso = descripcionUso,
        estado = runCatching { EstadoEquipo.valueOf(estado) }.getOrDefault(EstadoEquipo.SIN_REVISAR),
        descripcionEstado = descripcionEstado,
        registradoEn = registradoEn,
        fotoActualUri = fotoActualUri,
        fotoLineaBaseRef = fotoLineaBaseRef.ifBlank { RevisionEquipo.referenciaLineaBasePorId(equipoId) },
        coincideConLineaBase = coincideConLineaBase,
        observacionLineaBase = observacionLineaBase
    )

    companion object {
        fun fromDomain(faenaId: String, buzoId: String, rev: RevisionEquipo): RevisionEquipoEntity =
            RevisionEquipoEntity(
                id = "$faenaId|$buzoId|${rev.equipoId}",
                faenaId = faenaId,
                buzoId = buzoId,
                equipoId = rev.equipoId,
                categoria = rev.categoria,
                nombre = rev.nombre,
                descripcionUso = rev.descripcionUso,
                estado = rev.estado.name,
                descripcionEstado = rev.descripcionEstado,
                registradoEn = rev.registradoEn,
                fotoActualUri = rev.fotoActualUri,
                fotoLineaBaseRef = rev.fotoLineaBaseRef,
                coincideConLineaBase = rev.coincideConLineaBase,
                observacionLineaBase = rev.observacionLineaBase
            )
    }
}

/**
 * Entidad de Room para persistir las fotografías de evidencia y confirmación por etapa en SQLite.
 */
@Entity(tableName = "evidencias_etapa")
data class EvidenciaEtapaEntity(
    @PrimaryKey
    val id: String,
    val faenaId: String,
    val buzoId: String,
    val etapa: String,
    val fotoGeneralUri: String? = null,
    val fotoFrenteUri: String? = null,
    val fotoAtrasUri: String? = null,
    val etapaConfirmada: Boolean = false,
    val registradoEn: Long? = null
) {
    fun toEvidenciaDomain(): EvidenciaEquipos {
        val mapa = buildMap {
            fotoGeneralUri?.let { put(TipoFotoEquipos.GENERAL, it) }
            fotoFrenteUri?.let { put(TipoFotoEquipos.FRENTE, it) }
            fotoAtrasUri?.let { put(TipoFotoEquipos.ATRAS, it) }
        }
        return EvidenciaEquipos(fotos = mapa, registradoEn = registradoEn)
    }

    companion object {
        fun fromDomain(
            faenaId: String,
            buzoId: String,
            etapa: EtapaComprobacion,
            evidencia: EvidenciaEquipos,
            confirmada: Boolean
        ): EvidenciaEtapaEntity = EvidenciaEtapaEntity(
            id = "$faenaId|$buzoId|${etapa.name}",
            faenaId = faenaId,
            buzoId = buzoId,
            etapa = etapa.name,
            fotoGeneralUri = evidencia.fotos[TipoFotoEquipos.GENERAL],
            fotoFrenteUri = evidencia.fotos[TipoFotoEquipos.FRENTE],
            fotoAtrasUri = evidencia.fotos[TipoFotoEquipos.ATRAS],
            etapaConfirmada = confirmada,
            registradoEn = evidencia.registradoEn
        )
    }
}

/**
 * Entidad de Room para persistir las encuestas de salud por faena y momento (ANTES / DESPUÉS) en SQLite.
 */
@Entity(tableName = "encuestas_salud")
data class EncuestaSaludEntity(
    @PrimaryKey
    val id: String,
    val faenaId: String,
    val momento: String,
    val esApto: Boolean,
    val presionArterial: String,
    val frecuenciaCardiaca: Int,
    val registradaPor: String,
    val registradaEn: String,
    val semaforo: String = "VERDE",
    val motivosBloqueoText: String = "",
    val advertenciasText: String = ""
) {
    fun toDomain(): EncuestaSalud {
        val mom = runCatching { MomentoSalud.valueOf(momento) }.getOrDefault(MomentoSalud.ANTES)
        val sem = runCatching { ColorSemaforo.valueOf(semaforo) }
            .getOrDefault(if (esApto) ColorSemaforo.VERDE else ColorSemaforo.ROJO)
        return EncuestaSalud(
            faenaId = faenaId,
            momento = mom,
            esApto = esApto,
            presionArterial = presionArterial,
            frecuenciaCardiaca = frecuenciaCardiaca,
            registradaPor = registradaPor,
            registradaEn = registradaEn,
            semaforo = sem,
            motivosBloqueo = motivosBloqueoText.split("||").filter { it.isNotBlank() },
            advertencias = advertenciasText.split("||").filter { it.isNotBlank() }
        )
    }

    companion object {
        fun fromDomain(enc: EncuestaSalud): EncuestaSaludEntity = EncuestaSaludEntity(
            id = "${enc.faenaId}|${enc.momento.name}",
            faenaId = enc.faenaId,
            momento = enc.momento.name,
            esApto = enc.esApto,
            presionArterial = enc.presionArterial,
            frecuenciaCardiaca = enc.frecuenciaCardiaca,
            registradaPor = enc.registradaPor,
            registradaEn = enc.registradaEn,
            semaforo = enc.semaforo.name,
            motivosBloqueoText = enc.motivosBloqueo.joinToString("||"),
            advertenciasText = enc.advertencias.joinToString("||")
        )
    }
}
