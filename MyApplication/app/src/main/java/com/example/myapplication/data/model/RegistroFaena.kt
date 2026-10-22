package com.example.myapplication.data.model

/**
 * Registro de autorización e inmersión submarina.
 *
 * La faena sabe por sí misma si está lista para cerrarse: [pendientes] devuelve lo que falta
 * y [finalizar] solo la completa cuando no falta nada. No depende del repositorio ni de la UI;
 * recibe el [AvanceFaena] (estado actual del buzo y de sus revisiones) como parámetro.
 */
data class RegistroFaena(
    val id: String,
    val buzoId: String,
    val buzoNombre: String,
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String = ESTADO_EN_CURSO
) {
    val enCurso: Boolean get() = estado.contains("Curso", ignoreCase = true)
    val completada: Boolean get() = estado.contains("Completada", ignoreCase = true)

    /**
     * Lo que falta para poder finalizar (bloquea el cierre):
     *  - el buzo debe estar APTO;
     *  - en cada etapa, todos los equipos con estado, foto y descripción (si no es Aceptable);
     *  - cada etapa debe estar confirmada.
     */
    fun pendientes(avance: AvanceFaena): List<String> {
        val faltantes = mutableListOf<String>()

        val buzo = avance.buzo
        when {
            buzo == null -> faltantes += "Buzo $buzoNombre no encontrado en el sistema"
            buzo.estadoSalud != EstadoSalud.APTO ->
                faltantes += "Salud: ${buzo.nombre} no está apto (${buzo.estadoSalud.label})"
        }

        EtapaComprobacion.entries.forEach { etapa ->
            val equipos = avance.etapas[etapa].orEmpty()
            val sinEstado = equipos.count { !it.tieneEstado }
            val sinFoto = equipos.count { it.faltaFoto }
            val sinDescripcion = equipos.count { it.faltaDescripcion }
            val detalle = listOfNotNull(
                "$sinEstado sin revisar".takeIf { sinEstado > 0 },
                "$sinFoto sin foto".takeIf { sinFoto > 0 },
                "$sinDescripcion sin descripción".takeIf { sinDescripcion > 0 }
            )
            when {
                equipos.isEmpty() ->
                    faltantes += "${etapa.etiqueta}: no hay equipos registrados para revisar"
                detalle.isNotEmpty() ->
                    faltantes += "${etapa.etiqueta}: ${detalle.joinToString(", ")}"
                etapa !in avance.etapasConfirmadas ->
                    faltantes += "${etapa.etiqueta}: falta confirmar la etapa"
            }
        }
        return faltantes
    }

    /**
     * Equipos reportados como riesgosos o inutilizables. Es solo informativo:
     * NO bloquea el cierre de la faena.
     */
    fun novedadesEquipos(avance: AvanceFaena): List<String> =
        EtapaComprobacion.entries.flatMap { etapa ->
            avance.etapas[etapa].orEmpty()
                .filter {
                    it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO ||
                        it.estado == EstadoEquipo.INUTILIZABLE
                }
                .map { "${etapa.etiqueta}: ${it.nombre} (${it.estado.etiqueta})" }
        }

    fun puedeFinalizar(avance: AvanceFaena): Boolean = pendientes(avance).isEmpty()

    /** Devuelve la faena completada, o falla con [FaenaIncompletaException] si falta algo. */
    fun finalizar(avance: AvanceFaena): Result<RegistroFaena> {
        if (completada) {
            return Result.failure(IllegalStateException("La faena $id ya está completada."))
        }
        val faltantes = pendientes(avance)
        return if (faltantes.isEmpty()) {
            Result.success(copy(estado = ESTADO_COMPLETADA))
        } else {
            Result.failure(FaenaIncompletaException(id, faltantes))
        }
    }

    companion object {
        const val ESTADO_EN_CURSO = "Autorizada y en Curso"
        const val ESTADO_COMPLETADA = "Completada con Éxito"
    }
}

/** Foto actual de lo que se ha comprobado para el buzo de una faena. */
data class AvanceFaena(
    val buzo: Buzo?,
    val etapas: Map<EtapaComprobacion, List<RevisionEquipo>>,
    val etapasConfirmadas: Set<EtapaComprobacion>
)

class FaenaIncompletaException(
    val faenaId: String,
    val pendientes: List<String>
) : IllegalStateException(
    "La faena $faenaId tiene ${pendientes.size} pendiente(s) y no puede finalizarse."
)
