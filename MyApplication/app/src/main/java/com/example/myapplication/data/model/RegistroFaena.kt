package com.example.myapplication.data.model

/**
 * Registro de autorización e inmersión submarina.
 *
 * Cada faena tiene DOS buzos asignados, que deben ser personas distintas:
 * - [buzoId] / [buzoNombre]: buzo ejecutor de la inmersión;
 * - [socorristaId] / [socorristaNombre]: socorrista (buzo secundario).
 *
 * Los registros de equipos (evaluación, fotos y etapas) son independientes por buzo
 * y se capturan uno seguido del otro. La encuesta de salud es única por faena.
 *
 * La faena sabe por sí misma si está lista para cerrarse. Flujo completo:
 *  1. Pre-equipación: estado individual de los equipos + una foto general, confirmado.
 *  2. Equipado: mismo estado compartido + fotos de frente y atrás, confirmado.
 *  3. Encuesta de salud ANTES (con el buzo ya equipado).
 *  4. Aprobación de que los datos de la faena son correctos.
 *  5. Encuesta de salud DESPUÉS (cuando todo lo demás ya se revisó).
 *  6. Finalizar.
 *
 * No depende del repositorio ni de la UI: recibe el [AvanceFaena] como parámetro.
 */
data class RegistroFaena(
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
    val estado: String = ESTADO_EN_CURSO,
    val autorizadoPor: String = "Cristóbal Loncón C. (Prevencionista)",
    val aprobacion: AprobacionDatos? = null
) {
    val enCurso: Boolean get() = estado.contains("Curso", ignoreCase = true)
    val completada: Boolean get() = estado.contains("Completada", ignoreCase = true)
    val datosAprobados: Boolean get() = aprobacion != null
    val socorristaAsignado: Boolean get() = socorristaId.isNotBlank() && socorristaNombre.isNotBlank()

    /** Buzos asignados, en el orden en que se capturan los registros. */
    fun asignaciones(): List<AsignacionBuzo> = buildList {
        add(AsignacionBuzo(RolBuzo.EJECUTOR, buzoId, buzoNombre))
        if (socorristaAsignado) {
            add(AsignacionBuzo(RolBuzo.SOCORRISTA, socorristaId, socorristaNombre))
        }
    }

    private fun tripulacion(): String =
        if (socorristaAsignado) "$buzoNombre y $socorristaNombre" else buzoNombre

    // ───────────────────────── Cierre ─────────────────────────

    /**
     * Lo que falta para poder finalizar (bloquea el cierre), en el orden del flujo:
     *  - dos buzos asignados y distintos;
     *  - evaluación individual de los equipos de cada buzo (compartida entre etapas);
     *  - evidencia y confirmación de cada etapa para cada buzo;
     *  - encuesta de salud ANTES registrada y con resultado APTO;
     *  - datos de la faena aprobados;
     *  - encuesta de salud DESPUÉS registrada (su resultado se reporta, no bloquea).
     */
    fun pendientes(avance: AvanceFaena): List<Pendiente> {
        val faltantes = mutableListOf<Pendiente>()

        if (!socorristaAsignado) {
            faltantes.add(
                Pendiente(CategoriaPendiente.DATOS, "Socorrista: falta asignar el buzo secundario")
            )
        } else if (socorristaId == buzoId) {
            faltantes.add(
                Pendiente(CategoriaPendiente.DATOS, "Socorrista: debe ser distinto del buzo ejecutor")
            )
        }

        asignaciones().forEach { asignacion ->
            val avanceBuzo = avance.buzos[asignacion.buzoId] ?: AvanceBuzo(buzo = null)
            if (avanceBuzo.buzo == null) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.DATOS,
                        "${asignacion.rol.etiqueta}: ${asignacion.buzoNombre} no encontrado en el sistema"
                    )
                )
            }
            faltantes.addAll(pendientesEquipos(asignacion.rol, avanceBuzo))
        }

        val antes = avance.encuestas[MomentoSalud.ANTES]
        if (antes == null) {
            faltantes.add(
                Pendiente(CategoriaPendiente.SALUD, "Encuesta de salud ANTES: sin registrar")
            )
        } else if (!antes.esApto) {
            faltantes.add(
                Pendiente(CategoriaPendiente.SALUD, "Encuesta de salud ANTES: ${tripulacion()} resultó NO APTO")
            )
        }

        if (!datosAprobados) {
            faltantes.add(
                Pendiente(CategoriaPendiente.DATOS, "Datos de la faena: falta la aprobación del prevencionista")
            )
        }

        if (MomentoSalud.DESPUES !in avance.encuestas) {
            faltantes.add(
                Pendiente(CategoriaPendiente.SALUD, "Encuesta de salud DESPUÉS: sin registrar")
            )
        }
        return faltantes
    }

    private fun pendientesEquipos(rol: RolBuzo, avanceBuzo: AvanceBuzo): List<Pendiente> {
        val faltantes = mutableListOf<Pendiente>()

        // La evaluación individual es compartida entre etapas: se informa una sola vez.
        val equipos = avanceBuzo.etapas[EtapaComprobacion.PRE_EQUIPACION].orEmpty()
        if (equipos.isEmpty()) {
            faltantes.add(
                Pendiente(
                    CategoriaPendiente.EQUIPOS,
                    "${rol.etiqueta}: no hay equipos registrados para revisar"
                )
            )
        } else {
            val sinEstado = equipos.count { !it.tieneEstado }
            val sinDescripcion = equipos.count { it.faltaDescripcion }
            val detalle = listOfNotNull(
                "$sinEstado sin revisar".takeIf { sinEstado > 0 },
                "$sinDescripcion sin descripción".takeIf { sinDescripcion > 0 }
            )
            if (detalle.isNotEmpty()) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.EQUIPOS,
                        "${rol.etiqueta}: evaluación individual: ${detalle.joinToString(", ")}"
                    )
                )
            }
        }

        EtapaComprobacion.entries.forEach { etapa ->
            val faltanFotos = (avanceBuzo.evidencia[etapa] ?: EvidenciaEquipos()).faltan(etapa)
            if (faltanFotos.isNotEmpty()) {
                val nombres = faltanFotos.joinToString(" y ") { it.etiqueta.lowercase() }
                val verbo = if (faltanFotos.size == 1) "falta" else "faltan"
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.EQUIPOS,
                        "${rol.etiqueta} · ${etapa.etiqueta}: $verbo $nombres"
                    )
                )
            } else if (etapa !in avanceBuzo.etapasConfirmadas) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.EQUIPOS,
                        "${rol.etiqueta} · ${etapa.etiqueta}: falta confirmar la etapa"
                    )
                )
            }
        }
        return faltantes
    }

    /**
     * Novedades que se reportan pero NO bloquean el cierre:
     * equipos riesgosos o inutilizables de cada buzo (la evaluación es compartida,
     * se listan una vez por buzo), y un resultado NO APTO en la encuesta DESPUÉS.
     */
    fun novedades(avance: AvanceFaena): List<String> {
        val lista = mutableListOf<String>()
        asignaciones().forEach { asignacion ->
            val avanceBuzo = avance.buzos[asignacion.buzoId] ?: return@forEach
            avanceBuzo.etapas[EtapaComprobacion.PRE_EQUIPACION].orEmpty()
                .filter {
                    it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO ||
                        it.estado == EstadoEquipo.INUTILIZABLE
                }
                .distinctBy { it.equipoId }
                .forEach { lista.add("${asignacion.rol.etiqueta}: ${it.nombre} (${it.estado.etiqueta})") }
        }
        val despues = avance.encuestas[MomentoSalud.DESPUES]
        if (despues != null && !despues.esApto) {
            lista.add("Encuesta de salud DESPUÉS: ${tripulacion()} resultó NO APTO (revisar posible incidente)")
        }
        return lista
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

    // ───────────────────────── Encuesta de salud ─────────────────────────

    /**
     * Motivo por el que todavía no se puede registrar la encuesta de [momento]
     * (null si ya está habilitada). La encuesta es única por faena y solo requiere
     * que la faena esté en curso.
     */
    fun bloqueoEncuesta(momento: MomentoSalud, avance: AvanceFaena): String? {
        if (!enCurso) return "La faena $id ya no está en curso."
        return null
    }

    // ───────────────────────── Datos de la faena ─────────────────────────

    /** Aprueba que los datos son correctos (queda registrado quién y cuándo). */
    fun aprobar(aprobadoPor: String, fechaHora: String): Result<RegistroFaena> =
        if (!enCurso) {
            Result.failure(IllegalStateException("La faena $id ya no está en curso."))
        } else {
            Result.success(copy(aprobacion = AprobacionDatos(aprobadoPor, fechaHora)))
        }

    /** Deja los datos sin aprobar otra vez. */
    fun revocarAprobacion(): Result<RegistroFaena> =
        if (!enCurso) {
            Result.failure(IllegalStateException("La faena $id ya no está en curso."))
        } else {
            Result.success(copy(aprobacion = null))
        }

    /**
     * Corrige los datos de la faena. Si algún dato cambia, la aprobación se anula
     * y los datos deben aprobarse de nuevo.
     */
    fun conDatos(
        centro: String,
        tipo: String,
        profundidad: Int?,
        minutos: Int?
    ): Result<RegistroFaena> {
        if (!enCurso) {
            return Result.failure(IllegalStateException("La faena $id ya no está en curso."))
        }
        if (centro.isBlank() || tipo.isBlank()) {
            return Result.failure(IllegalArgumentException("El centro y el tipo de faena son obligatorios."))
        }
        if (profundidad == null || profundidad <= 0) {
            return Result.failure(IllegalArgumentException("La profundidad debe ser un número mayor a 0."))
        }
        if (minutos == null || minutos <= 0) {
            return Result.failure(IllegalArgumentException("El tiempo máximo debe ser un número mayor a 0."))
        }
        val huboCambios = centro != centroCultivo ||
            tipo != tipoFaena ||
            profundidad != profundidadMetros ||
            minutos != tiempoMinutos
        return Result.success(
            copy(
                centroCultivo = centro,
                tipoFaena = tipo,
                profundidadMetros = profundidad,
                tiempoMinutos = minutos,
                aprobacion = if (huboCambios) null else aprobacion
            )
        )
    }

    companion object {
        const val ESTADO_EN_CURSO = "Autorizada y en Curso"
        const val ESTADO_COMPLETADA = "Completada con Éxito"
    }
}

/** Rol que cumple un buzo dentro de la faena. */
enum class RolBuzo(val etiqueta: String, val corto: String) {
    EJECUTOR("Buzo ejecutor", "Ejecutor"),
    SOCORRISTA("Socorrista (buzo secundario)", "Socorrista")
}

/** Buzo asignado a una faena junto con el rol que cumple. */
data class AsignacionBuzo(
    val rol: RolBuzo,
    val buzoId: String,
    val buzoNombre: String
)

/** Quién aprobó que los datos de la faena son correctos, y cuándo. */
data class AprobacionDatos(
    val aprobadoPor: String,
    val fechaHora: String
)

/** Área a la que pertenece un pendiente (para ofrecer el acceso correcto en la UI). */
enum class CategoriaPendiente { EQUIPOS, SALUD, DATOS }

data class Pendiente(
    val categoria: CategoriaPendiente,
    val detalle: String
)

/** Checklist, evidencia y etapas de UN buzo dentro de una faena. */
data class AvanceBuzo(
    val buzo: Buzo?,
    val etapas: Map<EtapaComprobacion, List<RevisionEquipo>> = emptyMap(),
    val etapasConfirmadas: Set<EtapaComprobacion> = emptySet(),
    val evidencia: Map<EtapaComprobacion, EvidenciaEquipos> = emptyMap()
)

/** Foto actual de lo que se ha comprobado para la faena. */
data class AvanceFaena(
    /** Clave = id del buzo; incluye al ejecutor y al socorrista asignado. */
    val buzos: Map<String, AvanceBuzo> = emptyMap(),
    /** La encuesta de salud es única por faena y momento. */
    val encuestas: Map<MomentoSalud, EncuestaSalud> = emptyMap()
)

class FaenaIncompletaException(
    val faenaId: String,
    val pendientes: List<Pendiente>
) : IllegalStateException(
    "La faena $faenaId tiene ${pendientes.size} pendiente(s) y no puede finalizarse."
)
