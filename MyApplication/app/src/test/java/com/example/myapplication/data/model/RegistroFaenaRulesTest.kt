package com.example.myapplication.data.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RegistroFaenaRulesTest {

    private lateinit var faenaBase: RegistroFaena
    private lateinit var buzoTest: Buzo

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

        faenaBase = RegistroFaena(
            id = "F-100",
            buzoId = "B-1",
            buzoNombre = "Carlos Soto Paredes",
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
        val avanceSinBuzo = AvanceFaena(
            buzo = null,
            etapas = emptyMap(),
            etapasConfirmadas = emptySet(),
            encuestas = emptyMap()
        )

        val pendientes = faenaBase.pendientes(avanceSinBuzo)
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.DATOS && it.descripcion.contains("no encontrado") })
        assertFalse(faenaBase.puedeFinalizar(avanceSinBuzo))
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

        val avanceConPendientes = AvanceFaena(
            buzo = buzoTest,
            etapas = mapOf(EtapaComprobacion.PRE_EQUIPACION to listOf(equipoSinRevisar)),
            etapasConfirmadas = emptySet(),
            encuestas = emptyMap()
        )

        val pendientes = faenaBase.pendientes(avanceConPendientes)
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.EQUIPOS })
        assertFalse(faenaBase.puedeFinalizar(avanceConPendientes))
    }

    @Test
    fun encuestaAntesNoApto_bloqueaCierreDeFaena() {
        val encuestaNoApto = EncuestaSalud(
            faenaId = "F-100",
            momento = MomentoSalud.ANTES,
            esApto = false, // Buzo no apto antes de sumergirse
            presionArterial = "150/95",
            frecuenciaCardiaca = 110,
            registradaPor = "Supervisor",
            registradaEn = "21/11/2026 08:45"
        )

        val avanceConNoApto = AvanceFaena(
            buzo = buzoTest,
            etapas = emptyMap(),
            etapasConfirmadas = emptySet(),
            encuestas = mapOf(MomentoSalud.ANTES to encuestaNoApto)
        )

        val pendientes = faenaBase.pendientes(avanceConNoApto)
        assertTrue(pendientes.any { it.categoria == CategoriaPendiente.SALUD && it.descripcion.contains("NO APTO") })
    }

    @Test
    fun faenaSinAprobacionPrevencionista_bloqueaCierre() {
        val avanceValido = AvanceFaena(
            buzo = buzoTest,
            etapas = EtapaComprobacion.entries.associateWith { listOf(crearRevisionConforme("eq1")) },
            etapasConfirmadas = EtapaComprobacion.entries.toSet(),
            encuestas = mapOf(
                MomentoSalud.ANTES to crearEncuestaApto(MomentoSalud.ANTES),
                MomentoSalud.DESPUES to crearEncuestaApto(MomentoSalud.DESPUES)
            )
        )

        // faenaBase NO tiene aprobacion
        assertFalse(faenaBase.datosAprobados)
        val pendientes = faenaBase.pendientes(avanceValido)
        assertTrue(pendientes.any { it.descripcion.contains("falta la aprobación") })
        assertFalse(faenaBase.puedeFinalizar(avanceValido))
    }

    @Test
    fun faenaConTodosRequisitosCompletos_puedeFinalizarConExito() {
        val faenaAprobada = faenaBase.aprobar("Cristóbal Loncón", "21/11/2026 10:00")

        val avanceCompleto = AvanceFaena(
            buzo = buzoTest,
            etapas = EtapaComprobacion.entries.associateWith { listOf(crearRevisionConforme("eq1")) },
            etapasConfirmadas = EtapaComprobacion.entries.toSet(),
            encuestas = mapOf(
                MomentoSalud.ANTES to crearEncuestaApto(MomentoSalud.ANTES),
                MomentoSalud.DESPUES to crearEncuestaApto(MomentoSalud.DESPUES)
            )
        )

        assertTrue(faenaAprobada.puedeFinalizar(avanceCompleto))
        val resultado = faenaAprobada.finalizar(avanceCompleto)
        assertTrue(resultado.isSuccess)
        assertEquals(RegistroFaena.ESTADO_COMPLETADA, resultado.getOrNull()?.estado)
    }

    @Test
    fun novedades_reportaEquiposRiesgososYSinBloquearCierre() {
        val revisionRiesgosa = RevisionEquipo(
            equipoId = "eq1",
            categoria = "General",
            nombre = "Regulador alternativo",
            descripcionUso = "Uso auxiliar",
            estado = EstadoEquipo.UTILIZABLE_RIESGOSO,
            fotoUri = "file://foto.jpg"
        )

        val avance = AvanceFaena(
            buzo = buzoTest,
            etapas = mapOf(EtapaComprobacion.PRE_EQUIPACION to listOf(revisionRiesgosa)),
            etapasConfirmadas = setOf(EtapaComprobacion.PRE_EQUIPACION),
            encuestas = emptyMap()
        )

        val novedades = faenaBase.novedades(avance)
        assertEquals(1, novedades.size)
        assertTrue(novedades.first().contains("Regulador alternativo"))
    }

    private fun crearRevisionConforme(id: String): RevisionEquipo {
        return RevisionEquipo(
            equipoId = id,
            categoria = "Seguridad",
            nombre = "Manómetro de inmersión",
            descripcionUso = "Lectura correcta",
            estado = EstadoEquipo.ACEPTABLE,
            fotoUri = "content://media/foto.jpg"
        )
    }

    private fun crearEncuestaApto(momento: MomentoSalud): EncuestaSalud {
        return EncuestaSalud(
            faenaId = "F-100",
            momento = momento,
            esApto = true,
            presionArterial = "120/80",
            frecuenciaCardiaca = 72,
            registradaPor = "Supervisor",
            registradaEn = "21/11/2026 10:00"
        )
    }
}
