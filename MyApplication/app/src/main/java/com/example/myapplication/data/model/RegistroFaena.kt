package com.example.myapplication.data.model

/**
 * Registro de autorización e inmersión submarina.
 *
 * La faena sabe por sí misma si está lista para cerrarse. Flujo completo:
 *  1. Pre-equipación y 2. Equipado: revisión de equipos, confirmada por etapa.
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
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String = ESTADO_EN_CURSO,
    val aprobacion: AprobacionDatos? = null
) {
    val enCurso: Boolean get() = estado.contains("Curso", ignoreCase = true)
    val completada: Boolean get() = estado.contains("Completada", ignoreCase = true)
    val datosAprobados: Boolean get() = aprobacion != null

    // ───────────────────────── Cierre ─────────────────────────

    /**
     * Lo que falta para poder finalizar (bloquea el cierre), en el orden del flujo:
     *  - equipos de ambas etapas completos y confirmados;
     *  - encuesta de salud ANTES registrada y con resultado APTO;
     *  - datos de la faena aprobados;
     *  - encuesta de salud DESPUÉS registrada (su resultado se reporta, no bloquea).
     */
    fun pendientes(avance: AvanceFaena): List<Pendiente> {
        val faltantes = mutableListOf<Pendiente>()

        if (avance.buzo == null) {
            faltantes.add(
                Pendiente(CategoriaPendiente.DATOS, "Buzo $buzoNombre no encontrado en el sistema")
            )
        }

        faltantes.addAll(pendientesEquipos(avance))

        val antes = avance.encuestas[MomentoSalud.ANTES]
        if (antes == null) {
            faltantes.add(
                Pendiente(CategoriaPendiente.SALUD, "Encuesta de salud ANTES: sin registrar")
            )
        } else if (!antes.esApto) {
            faltantes.add(
                Pendiente(CategoriaPendiente.SALUD, "Encuesta de salud ANTES: $buzoNombre resultó NO APTO")
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

    private fun pendientesEquipos(avance: AvanceFaena): List<Pendiente> {
        val faltantes = mutableListOf<Pendiente>()
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
            if (equipos.isEmpty()) {
                faltantes.add(
                    Pendiente(CategoriaPendiente.EQUIPOS, "${etapa.etiqueta}: no hay equipos registrados para revisar")
                )
            } else if (detalle.isNotEmpty()) {
                faltantes.add(
                    Pendiente(CategoriaPendiente.EQUIPOS, "${etapa.etiqueta}: ${detalle.joinToString(", ")}")
                )
            } else if (etapa !in avance.etapasConfirmadas) {
                faltantes.add(
                    Pendiente(CategoriaPendiente.EQUIPOS, "${etapa.etiqueta}: falta confirmar la etapa")
                )
            }
        }
        return faltantes
    }

    /**
     * Novedades que se reportan pero NO bloquean el cierre:
     * equipos riesgosos o inutilizables, y un resultado NO APTO en la encuesta DESPUÉS.
     */
    fun novedades(avance: AvanceFaena): List<String> {
        val lista = mutableListOf<String>()
        EtapaComprobacion.entries.forEach { etapa ->
            avance.etapas[etapa].orEmpty()
                .filter {
                    it.estado == EstadoEquipo.UTILIZABLE_RIESGOSO ||
                        it.estado == EstadoEquipo.INUTILIZABLE
                }
                .forEach { lista.add("${etapa.etiqueta}: ${it.nombre} (${it.estado.etiqueta})") }
        }
        val despues = avance.encuestas[MomentoSalud.DESPUES]
        if (despues != null && !despues.esApto) {
            lista.add("Encuesta de salud DESPUÉS: $buzoNombre resultó NO APTO (revisar posible incidente)")
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
     * (null si ya está habilitada):
     *  - ANTES: requiere la etapa «Equipado» confirmada (el buzo ya está equipado).
     *  - DESPUÉS: requiere equipos completos y confirmados, encuesta ANTES y datos aprobados.
     */
    fun bloqueoEncuesta(momento: MomentoSalud, avance: AvanceFaena): String? {
        if (!enCurso) return "La faena $id ya no está en curso."
        return when (momento) {
            MomentoSalud.ANTES ->
                if (EtapaComprobacion.EQUIPADO in avance.etapasConfirmadas) {
                    null
                } else {
                    "Primero confirma la etapa «Equipado» en Control de Equipos: " +
                        "la encuesta ANTES se aplica con el buzo ya equipado."
                }

            MomentoSalud.DESPUES -> {
                val faltan = mutableListOf<String>()
                if (pendientesEquipos(avance).isNotEmpty()) {
                    faltan.add("completar y confirmar la revisión de equipos")
                }
                if (MomentoSalud.ANTES !in avance.encuestas) {
                    faltan.add("registrar la encuesta ANTES")
                }
                if (!datosAprobados) {
                    faltan.add("aprobar los datos de la faena")
                }
                if (faltan.isEmpty()) {
                    null
                } else {
                    "La encuesta DESPUÉS se habilita cuando todo lo demás está revisado. " +
                        "Falta: ${faltan.joinToString(", ")}."
                }
            }
        }
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

/** Foto actual de lo que se ha comprobado para la faena. */
data class AvanceFaena(
    val buzo: Buzo?,
    val etapas: Map<EtapaComprobacion, List<RevisionEquipo>>,
    val etapasConfirmadas: Set<EtapaComprobacion>,
    val encuestas: Map<MomentoSalud, EncuestaSalud>
)

class FaenaIncompletaException(
    val faenaId: String,
    val pendientes: List<Pendiente>
) : IllegalStateException(
    "La faena $faenaId tiene ${pendientes.size} pendiente(s) y no puede finalizarse."
)
