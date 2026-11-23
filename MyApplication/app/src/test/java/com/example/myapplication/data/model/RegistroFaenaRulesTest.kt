package com.example.myapplication.data.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RegistroFaenaRulesTest {

    private lateinit var faenaBase: RegistroFaena
    private lateinit var buzoTest: Buzo
    private lateinit var socorristaTest: Buzo

    @Before
    fun setUp() {
        buzoTest = Buzo(
            id = "B-1",
            rut = "15.489.231-1",
            nombre = "Carlos Soto Paredes",
            empresa = "Servicios Submarinos Austral SpA",
            matricula = "Buzo Comercial N° 2394",
            telefono = "+56 9 8451 2290",
            estadoSalud = EstadoSalud.APTO
        )
        socorristaTest = buzoTest.copy(
            id = "B-2",
            rut = "17.912.453-K",
            nombre = "Pedro Almonacid Vera",
            matricula = "Buzo Básico N° 5812"
        )
        faenaBase = RegistroFaena(
            id = "F-100",
            buzoId = "B-1",
            buzoNombre = "Carlos Soto Paredes",
            socorristaId = "B-2",
            socorristaNombre = "Pedro Almonacid Vera",
            empresa = "Servicios Submarinos Austral SpA",
            centroCultivo = "Centro Melinka 1",
            tipoFaena = "Inspección de redes",
            profundidadMetros = 18,
            tiempoMinutos = 40,
            fechaHora = "21/11/2026 09:00",
            estado = RegistroFaena.ESTADO_EN_CURSO
        )
    }

    @Test
    fun faenaSinBuzo_generaPendienteDeDatos() {
        val avanceSinBuzo = avance(ejecutor = avanceBuzo(buzo = null))

        val pendientes = faenaBase.pendientes(avanceSinBuzo)
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.DATOS && it.detalle.contains("no encontrado") })
        assertFalse(faenaBase.puedeFinalizar(avanceSinBuzo))
    }

    @Test
    fun faenaSinSocorristaAsignado_bloqueaCierre() {
        val faenaSinSocorrista = faenaBase.copy(socorristaId = "", socorristaNombre = "")
        val avanceCompleto = avance(socorrista = null) // solo se registra el ejecutor

        val pendientes = faenaSinSocorrista.pendientes(avanceCompleto)
        assertTrue(pendientes.any { it.detalle.contains("falta asignar el buzo secundario") })
        assertFalse(faenaSinSocorrista.puedeFinalizar(avanceCompleto))
    }

    @Test
    fun socorristaIgualAlEjecutor_bloqueaCierre() {
        val repetida = faenaBase.copy(socorristaId = "B-1", socorristaNombre = buzoTest.nombre)

        val pendientes = repetida.pendientes(avance())
        assertTrue(pendientes.any { it.detalle.contains("debe ser distinto") })
        assertFalse(repetida.puedeFinalizar(avance()))
    }

    @Test
    fun faenaConEquiposSinRevisar_generaPendientesDeEquipos() {
        val equipoSinRevisar = RevisionEquipo(
            equipoId = "e1",
            categoria = "General",
            nombre = "Traje de buceo",
            descripcionUso = "Estanco"
            // sin estado, sin foto
        )

        val avanceConPendientes = avance(
            ejecutor = avanceBuzo(
                buzo = buzoTest,
                revisiones = mapOf(EtapaComprobacion.PRE_EQUIPACION to listOf(equipoSinRevisar)),
                confirmadas = emptySet(),
                evidencia = emptyMap()
            )
        )

        val pendientes = faenaBase.pendientes(avanceConPendientes)
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.EQUIPOS && it.detalle.contains("Buzo ejecutor") })
        assertFalse(faenaBase.puedeFinalizar(avanceConPendientes))
    }

    @Test
    fun socorristaIncompleto_bloqueaCierreAunqueEjecutorEsteCompleto() {
        val incompleto = avanceBuzo(
            buzo = socorristaTest,
            revisiones = emptyMap(),
            confirmadas = emptySet(),
            evidencia = emptyMap()
        )

        val pendientes = faenaBase.pendientes(avance(socorrista = incompleto))
        assertTrue(pendientes.any { it.detalle.contains("Socorrista") })
        assertFalse(faenaBase.puedeFinalizar(avance(socorrista = incompleto)))
    }

    @Test
    fun encuestaAntesNoApto_bloqueaCierreDeFaena() {
        val encuestaNoApto = crearEncuestaApto(MomentoSalud.ANTES).copy(
            esApto = false,
            presionArterial = "150/95",
            frecuenciaCardiaca = 110
        )

        val pendientes = faenaBase.pendientes(
            avance(encuestas = mapOf(MomentoSalud.ANTES to encuestaNoApto))
        )
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.SALUD && it.detalle.contains("NO APTO") })
        assertTrue(pendientes.any { it.detalle.contains(socorristaTest.nombre) }) // aplica a ambos buzos
    }

    @Test
    fun faenaSinAprobacionPrevencionista_bloqueaCierre() {
        assertFalse(faenaBase.datosAprobados)
        val pendientes = faenaBase.pendientes(avance())
        assertTrue(pendientes.any { it.detalle.contains("falta la aprobación") })
        assertFalse(faenaBase.puedeFinalizar(avance()))
    }

    @Test
    fun faenaConTodosRequisitosCompletos_puedeFinalizarConExito() {
        val faenaAprobada = faenaBase.aprobar("Cristóbal Loncón", "21/11/2026 10:00").getOrThrow()
        val avanceCompleto = avance()

        assertTrue(faenaAprobada.puedeFinalizar(avanceCompleto))
        val resultado = faenaAprobada.finalizar(avanceCompleto)
        assertTrue(resultado.isSuccess)
        assertEquals(RegistroFaena.ESTADO_COMPLETADA, resultado.getOrNull()?.estado)
    }

    @Test
    fun encuestaUnicaPorFaena_noGeneraPendientesDuplicadosPorBuzo() {
        val faenaAprobada = faenaBase.aprobar("Cristóbal Loncón", "21/11/2026 10:00").getOrThrow()

        val encuestas = avance().encuestas
        val salud = faenaAprobada.pendientes(avance()).filter { it.categoria == CategoriaPendiente.SALUD }
        assertTrue(salud.isEmpty())
        // Una encuesta por momento (ANTES y DESPUÉS), nunca una por buzo.
        assertEquals(2, encuestas.size)
        assertEquals(MomentoSalud.entries.toSet(), encuestas.keys)
    }

    @Test
    fun novedades_reportaEquiposRiesgososYSinBloquearCierre() {
        val revisionRiesgosa = RevisionEquipo(
            equipoId = "eq1",
            categoria = "General",
            nombre = "Regulador alternativo",
            descripcionUso = "Uso auxiliar",
            estado = EstadoEquipo.UTILIZABLE_RIESGOSO
        )

        val avanceConRiesgo = avance(
            ejecutor = avanceBuzo(
                buzo = buzoTest,
                revisiones = mapOf(EtapaComprobacion.PRE_EQUIPACION to listOf(revisionRiesgosa)),
                confirmadas = setOf(EtapaComprobacion.PRE_EQUIPACION),
                evidencia = emptyMap()
            ),
            socorrista = avanceBuzo(
                buzo = socorristaTest,
                revisiones = mapOf(EtapaComprobacion.PRE_EQUIPACION to listOf(revisionRiesgosa)),
                confirmadas = setOf(EtapaComprobacion.PRE_EQUIPACION),
                evidencia = emptyMap()
            ),
            encuestas = emptyMap()
        )

        val novedades = faenaBase.novedades(avanceConRiesgo)
        assertEquals(2, novedades.count { it.contains("Regulador alternativo") })
        assertTrue(novedades.any { it.contains("Buzo ejecutor") })
        assertTrue(novedades.any { it.contains("Socorrista") })
    }

    @Test
    fun preEquipacionSinFotoGeneral_bloqueaCierreYNoExigeFotoPorEquipo() {
        val revision = crearRevisionConforme("eq1")
        val avanceSinGeneral = avance(
            ejecutor = avanceBuzo(
                buzo = buzoTest,
                evidencia = mapOf(
                    EtapaComprobacion.PRE_EQUIPACION to EvidenciaEquipos(),
                    EtapaComprobacion.EQUIPADO to evidenciaCompleta()[EtapaComprobacion.EQUIPADO]!!
                )
            )
        )

        val pendientes = faenaBase.pendientes(avanceSinGeneral)
        assertTrue(pendientes.any { it.detalle.contains("foto general", ignoreCase = true) })
        assertFalse(pendientes.any { it.detalle.contains("sin foto", ignoreCase = true) })
        assertFalse(faenaBase.puedeFinalizar(avanceSinGeneral))
    }

    @Test
    fun equipadoConSoloFotoDeFrente_bloqueaCierre() {
        val avanceSinAtras = avance(
            ejecutor = avanceBuzo(
                buzo = buzoTest,
                evidencia = mapOf(
                    EtapaComprobacion.PRE_EQUIPACION to EvidenciaEquipos(
                        fotos = mapOf(TipoFotoEquipos.GENERAL to "content://media/general.jpg")
                    ),
                    EtapaComprobacion.EQUIPADO to EvidenciaEquipos(
                        fotos = mapOf(TipoFotoEquipos.FRENTE to "content://media/frente.jpg")
                    )
                )
            )
        )

        val pendientes = faenaBase.pendientes(avanceSinAtras)
        assertTrue(pendientes.any { it.detalle.contains("atrás") })
        assertFalse(pendientes.any { it.detalle.contains("foto general", ignoreCase = true) })
        assertFalse(faenaBase.puedeFinalizar(avanceSinAtras))
    }

    @Test
    fun evaluacionCompartida_novedadesSeReportanUnaVezPorBuzo() {
        val revisionRiesgosa = crearRevisionConforme("eq1").copy(
            estado = EstadoEquipo.UTILIZABLE_RIESGOSO
        )
        val avanceConRiesgo = avance(
            ejecutor = avanceBuzo(
                buzo = buzoTest,
                revisiones = EtapaComprobacion.entries.associateWith { listOf(revisionRiesgosa) },
                confirmadas = emptySet(),
                evidencia = emptyMap()
            ),
            socorrista = avanceBuzo(
                buzo = socorristaTest,
                revisiones = EtapaComprobacion.entries.associateWith { listOf(revisionRiesgosa) },
                confirmadas = emptySet(),
                evidencia = emptyMap()
            ),
            encuestas = emptyMap()
        )

        assertEquals(2, faenaBase.novedades(avanceConRiesgo).count { it.contains(revisionRiesgosa.nombre) })
    }

    // ───────── Helpers ─────────

    private fun avanceBuzo(
        buzo: Buzo? = buzoTest,
        revisiones: Map<EtapaComprobacion, List<RevisionEquipo>> =
            EtapaComprobacion.entries.associateWith { listOf(crearRevisionConforme("eq1")) },
        confirmadas: Set<EtapaComprobacion> = EtapaComprobacion.entries.toSet(),
        evidencia: Map<EtapaComprobacion, EvidenciaEquipos> = evidenciaCompleta()
    ): AvanceBuzo = if (buzo == null) {
        AvanceBuzo(buzo = null)
    } else {
        AvanceBuzo(
            buzo = buzo,
            etapas = revisiones,
            etapasConfirmadas = confirmadas,
            evidencia = evidencia
        )
    }

    private fun avance(
        ejecutor: AvanceBuzo? = avanceBuzo(buzoTest),
        socorrista: AvanceBuzo? = avanceBuzo(socorristaTest),
        encuestas: Map<MomentoSalud, EncuestaSalud> = mapOf(
            MomentoSalud.ANTES to crearEncuestaApto(MomentoSalud.ANTES),
            MomentoSalud.DESPUES to crearEncuestaApto(MomentoSalud.DESPUES)
        )
    ): AvanceFaena = AvanceFaena(
        buzos = buildMap {
            if (ejecutor != null) put(buzoTest.id, ejecutor)
            if (socorrista != null) put(socorristaTest.id, socorrista)
        },
        encuestas = encuestas
    )

    private fun crearRevisionConforme(id: String): RevisionEquipo = RevisionEquipo(
        equipoId = id,
        categoria = "Seguridad",
        nombre = "Manómetro de inmersión",
        descripcionUso = "Lectura correcta",
        estado = EstadoEquipo.ACEPTABLE
    )

    private fun evidenciaCompleta(): Map<EtapaComprobacion, EvidenciaEquipos> =
        EtapaComprobacion.entries.associateWith { etapa ->
            val fotos = TipoFotoEquipos.requeridas(etapa).associateWith { "content://media/foto_${it.name}.jpg" }
            EvidenciaEquipos(fotos = fotos)
        }

    private fun crearEncuestaApto(momento: MomentoSalud): EncuestaSalud = EncuestaSalud(
        faenaId = "F-100",
        momento = momento,
        esApto = true,
        presionArterial = "120/80",
        frecuenciaCardiaca = 72,
        registradaPor = "Supervisor",
        registradaEn = "21/11/2026 10:00"
    )
}
