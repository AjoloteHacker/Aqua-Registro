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
 *  5. Encuesta de salud DESPUÉS: informativa. Es única por faena (cubre a ambos
 *     buzos, incluido el socorrista) y NO se exige para finalizar; si da NO APTO
 *     se reporta como novedad.
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
    val aprobacion: AprobacionDatos? = null,
    val cierreSinTerminar: CierreSinTerminar? = null
) {
    val enCurso: Boolean get() = estado.contains("Curso", ignoreCase = true)
    val completada: Boolean get() = estado.contains("Completada", ignoreCase = true)
    val cerradaSinTerminar: Boolean get() = estado.contains("sin Terminar", ignoreCase = true) || cierreSinTerminar != null
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
     *  - datos de la faena aprobados.
     *
     * La encuesta DESPUÉS NO se exige: es informativa y cubre a ambos buzos,
     * incluido el socorrista. Aunque falte, la faena se puede finalizar; si se
     * registró con resultado NO APTO, se reporta en [novedades] sin bloquear.
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
            } else if (avanceBuzo.buzo.bloqueadoPorEdadSinTestEsfuerzo) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.SALUD,
                        "BLOQUEO CRÍTICO (${asignacion.rol.etiqueta}): ${avanceBuzo.buzo.nombre} tiene " +
                            "${avanceBuzo.buzo.edadAnios} años (>45) sin Test de Esfuerzo vigente"
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
            val detalleMotivo = antes.motivosBloqueo.firstOrNull()
                ?: "Encuesta de salud ANTES: ${tripulacion()} resultó NO APTO"
            faltantes.add(
                Pendiente(
                    CategoriaPendiente.SALUD,
                    if (detalleMotivo.contains("NO APTO") || detalleMotivo.contains("Encuesta")) {
                        detalleMotivo
                    } else {
                        "Encuesta de salud ANTES (NO APTO): $detalleMotivo"
                    }
                )
            )
        }

        if (!datosAprobados) {
            faltantes.add(
                Pendiente(CategoriaPendiente.DATOS, "Datos de la faena: falta la aprobación del prevencionista")
            )
        }

        // La encuesta DESPUÉS (socorrista incluido) es informativa: nunca bloquea el cierre.
        return faltantes
    }

    private fun pendientesEquipos(rol: RolBuzo, avanceBuzo: AvanceBuzo): List<Pendiente> {
        val faltantes = mutableListOf<Pendiente>()

        // La evaluación individual es compartida entre etapas: basta con al menos 2 equipos evaluados (o todos si la lista tiene <= 2).
        val equipos = avanceBuzo.etapas[EtapaComprobacion.PRE_EQUIPACION].orEmpty()
        if (equipos.isEmpty()) {
            faltantes.add(
                Pendiente(
                    CategoriaPendiente.EQUIPOS,
                    "${rol.etiqueta}: no hay equipos registrados para revisar"
                )
            )
        } else {
            val minRequeridos = minOf(2, equipos.size)
            val evaluados = equipos.count { it.evaluacionCompleta }
            val sinDescripcion = equipos.count { it.faltaDescripcion }
            val detalle = buildList {
                if (evaluados < minRequeridos) {
                    val sinEstado = equipos.count { !it.tieneEstado }
                    if (sinEstado > 0) add("$sinEstado sin revisar (mínimo $minRequeridos requeridos)")
                }
                if (sinDescripcion > 0) add("$sinDescripcion sin descripción")
            }
            if (detalle.isNotEmpty()) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.EQUIPOS,
                        "${rol.etiqueta}: evaluación individual: ${detalle.joinToString(", ")}"
                    )
                )
            }
            val inutilizables = equipos.filter { it.estado == EstadoEquipo.INUTILIZABLE }
            if (inutilizables.isNotEmpty()) {
                faltantes.add(
                    Pendiente(
                        CategoriaPendiente.EQUIPOS,
                        "${rol.etiqueta}: BLOQUEO por equipo(s) en «No cumple» / Inutilizable (${inutilizables.joinToString { it.nombre }})"
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

    /**
     * Calcula el estado del Semáforo Industrial (VERDE / AMARILLO / ROJO) para la faena:
     * - ROJO: Existe un bloqueo crítico (buzo >45 años sin test de esfuerzo, encuesta ANTES en NO APTO,
     *         o algún equipo marcado como INUTILIZABLE / «No cumple»).
     * - AMARILLO: Sin bloqueos críticos, pero aún hay verificaciones pendientes o novedades/advertencias
     *             (equipos riesgosos, puntaje Calbuco/IMC en observación o encuesta DESPUÉS no apta).
     * - VERDE: Todos los requisitos completos, aprobados y sin alertas críticas.
     */
    fun semaforo(avance: AvanceFaena): ColorSemaforo {
        val hayBuzoMayor45SinTest = asignaciones().any { asig ->
            avance.buzos[asig.buzoId]?.buzo?.bloqueadoPorEdadSinTestEsfuerzo == true
        }
        val hayEquipoInutilizable = asignaciones().any { asig ->
            avance.buzos[asig.buzoId]
                ?.etapas
                ?.get(EtapaComprobacion.PRE_EQUIPACION)
                .orEmpty()
                .any { it.estado == EstadoEquipo.INUTILIZABLE }
        }
        val encuestaAntes = avance.encuestas[MomentoSalud.ANTES]
        val haySaludCritica = encuestaAntes != null && (!encuestaAntes.esApto || encuestaAntes.semaforo == ColorSemaforo.ROJO)

        if (hayBuzoMayor45SinTest || hayEquipoInutilizable || haySaludCritica) {
            return ColorSemaforo.ROJO
        }

        val tieneObservaciones = pendientes(avance).isNotEmpty() ||
            novedades(avance).isNotEmpty() ||
            encuestaAntes?.semaforo == ColorSemaforo.AMARILLO ||
            asignaciones().any { asig ->
                val b = avance.buzos[asig.buzoId]?.buzo
                b != null && (b.puntajeCalbuco < 70 || (b.imc != null && (b.imc!! < 19.0 || b.imc!! > 30.0)))
            }

        return if (tieneObservaciones) ColorSemaforo.AMARILLO else ColorSemaforo.VERDE
    }

    /** Devuelve la faena completada, o falla con [FaenaIncompletaException] si falta algo. */
    fun finalizar(avance: AvanceFaena): Result<RegistroFaena> {
        if (completada) {
            return Result.failure(IllegalStateException("La faena $id ya está completada."))
        }
        if (cerradaSinTerminar) {
            return Result.failure(IllegalStateException("La faena $id ya fue cerrada sin terminar."))
        }
        val faltantes = pendientes(avance)
        return if (faltantes.isEmpty()) {
            Result.success(copy(estado = ESTADO_COMPLETADA))
        } else {
            Result.failure(FaenaIncompletaException(id, faltantes))
        }
    }

    /**
     * Calcula qué motivos de cierre anticipado deben marcarse en automático según el estado
     * actual de la faena:
     * - [MotivoCierreSinTerminar.EQUIPO_MAL_ESTADO]: si algún equipo está en INUTILIZABLE o no coincide con línea base.
     * - [MotivoCierreSinTerminar.BUZOS_ENFERMOS]: si algún buzo está en NO_APTO, bloqueado (>45 sin test) o la encuesta dio NO APTO.
     */
    fun motivosAutomaticosCierre(avance: AvanceFaena): Set<MotivoCierreSinTerminar> = buildSet {
        val hayEquipoInutilizable = asignaciones().any { asig ->
            avance.buzos[asig.buzoId]
                ?.etapas
                ?.get(EtapaComprobacion.PRE_EQUIPACION)
                .orEmpty()
                .any { it.estado == EstadoEquipo.INUTILIZABLE || !it.coincideConLineaBase }
        }
        if (hayEquipoInutilizable) {
            add(MotivoCierreSinTerminar.EQUIPO_MAL_ESTADO)
        }

        val hayBuzoNoApto = avance.encuestas.values.any { !it.esApto } ||
            asignaciones().any { asig ->
                val b = avance.buzos[asig.buzoId]?.buzo
                b != null && (b.estadoSalud == EstadoSalud.NO_APTO || b.bloqueadoPorEdadSinTestEsfuerzo)
            }
        if (hayBuzoNoApto) {
            add(MotivoCierreSinTerminar.BUZOS_ENFERMOS)
        }
    }

    /**
     * Cierra la faena anticipadamente (sin terminar) con selección múltiple de motivos,
     * aplicando el marcado automático de las 2 primeras opciones cuando corresponda y
     * permitiendo listar múltiples opciones en «Otro» más un comentario editable post-cierre.
     */
    fun cerrarSinTerminar(
        motivosSeleccionados: Set<MotivoCierreSinTerminar>,
        otrosMotivos: List<String>,
        comentario: String,
        cerradoPor: String,
        fechaHora: String,
        avance: AvanceFaena
    ): Result<RegistroFaena> {
        if (!enCurso) {
            return Result.failure(IllegalStateException("La faena $id ya no está en curso."))
        }
        val motivosFinales = motivosSeleccionados + motivosAutomaticosCierre(avance)
        if (motivosFinales.isEmpty()) {
            return Result.failure(IllegalArgumentException("Seleccione al menos un motivo por el cual se cierra la faena sin terminar."))
        }
        val otrosLimpios = otrosMotivos.map { it.trim() }.filter { it.isNotBlank() }
        if (MotivoCierreSinTerminar.OTRO in motivosFinales && otrosLimpios.isEmpty()) {
            return Result.failure(IllegalArgumentException("Al elegir 'Otro', debe especificar al menos un motivo adicional."))
        }
        return Result.success(
            copy(
                estado = ESTADO_SIN_TERMINAR,
                cierreSinTerminar = CierreSinTerminar(
                    motivos = motivosFinales,
                    otrosMotivos = if (MotivoCierreSinTerminar.OTRO in motivosFinales) otrosLimpios else emptyList(),
                    comentario = comentario.trim(),
                    cerradoPor = cerradoPor,
                    fechaHora = fechaHora
                )
            )
        )
    }

    /** Permite modificar el comentario de una faena cerrada sin terminar incluso después de su cierre. */
    fun actualizarComentarioCierre(nuevoComentario: String): Result<RegistroFaena> {
        val actual = cierreSinTerminar
            ?: return Result.failure(IllegalStateException("La faena $id no tiene un registro de cierre sin terminar."))
        return Result.success(
            copy(cierreSinTerminar = actual.copy(comentario = nuevoComentario.trim()))
        )
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
        const val ESTADO_SIN_TERMINAR = "Cerrada sin Terminar"
    }
}

/** Motivos de selección múltiple para cerrar una faena sin terminar (docs/nota_3). */
enum class MotivoCierreSinTerminar(val etiqueta: String) {
    EQUIPO_MAL_ESTADO("Equipo en mal estado"),
    BUZOS_ENFERMOS("Buzos enfermos"),
    ACCIDENTE_BUZO("Accidente de buzo"),
    PREVENCIONISTA_ENFERMO("Prevencionista enfermo"),
    MAL_TIEMPO("Mal tiempo"),
    OTRO("Otro")
}

/** Detalle de cierre anticipado de una faena sin terminar, con comentario editable post-cierre. */
data class CierreSinTerminar(
    val motivos: Set<MotivoCierreSinTerminar>,
    val otrosMotivos: List<String> = emptyList(),
    val comentario: String = "",
    val cerradoPor: String = "",
    val fechaHora: String = ""
) {
    fun resumenMotivos(): String {
        val base = motivos
            .filter { it != MotivoCierreSinTerminar.OTRO }
            .map { it.etiqueta }
        val otros = if (MotivoCierreSinTerminar.OTRO in motivos && otrosMotivos.isNotEmpty()) {
            listOf("Otro: ${otrosMotivos.joinToString(", ")}")
        } else if (MotivoCierreSinTerminar.OTRO in motivos) {
            listOf("Otro")
        } else {
            emptyList()
        }
        return (base + otros).joinToString(" · ")
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

/**
 * Bitácora post-inmersión y trazabilidad de velocidades de descenso/ascenso (Anexo C EA4 / EA5).
 * Alerta automáticamente al jefe de centro ante velocidades de descenso > 24 m/min o ascenso > 9 m/min.
 */
data class BitacoraPostInmersion(
    val id: String,
    val faenaId: String,
    val buzoId: String,
    val buzoNombre: String,
    val fechaHora: String,
    val profundidadMetros: Int,
    val tiempoFondoMinutos: Int,
    val velocidadDescensoMpm: Double,
    val velocidadAscensoMpm: Double,
    val origenDato: String = "Dispositivo Census / Simulación",
    val observaciones: String = "",
    val registradoPor: String = "Cristóbal Loncón C."
) {
    val superaDescenso: Boolean get() = velocidadDescensoMpm > LIMITE_DESCENSO_MPM
    val superaAscenso: Boolean get() = velocidadAscensoMpm > LIMITE_ASCENSO_MPM
    val alertaVelocidadSuperada: Boolean get() = superaDescenso || superaAscenso
    val estadoRevision: String
        get() = if (alertaVelocidadSuperada) {
            "ALERTA NOTIFICADA A JEFE DE CENTRO"
        } else {
            "CONFORME"
        }

    val detalleAlerta: String
        get() = buildList {
            if (superaDescenso) {
                add(
                    "ALERTA CRÍTICA JEFE DE CENTRO: Velocidad de descenso excesiva " +
                        "($velocidadDescensoMpm m/min > límite de $LIMITE_DESCENSO_MPM m/min). Riesgo de barotrauma."
                )
            }
            if (superaAscenso) {
                add(
                    "ALERTA CRÍTICA JEFE DE CENTRO: Velocidad de ascenso excesiva " +
                        "($velocidadAscensoMpm m/min > límite de $LIMITE_ASCENSO_MPM m/min). Riesgo de enfermedad descompresiva."
                )
            }
        }.ifEmpty {
            listOf(
                "Parámetros de descenso ($velocidadDescensoMpm m/min) y ascenso ($velocidadAscensoMpm m/min) dentro de norma."
            )
        }.joinToString(" | ")

    companion object {
        const val LIMITE_DESCENSO_MPM = 24.0
        const val LIMITE_ASCENSO_MPM = 9.0
    }
}
