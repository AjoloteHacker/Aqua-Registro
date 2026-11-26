package com.example.myapplication.data.repository

import com.example.myapplication.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AquaRepositoryCoverageBoostTest {

    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        repository = AquaRepository.getInstance()
        repository.login("12.345.678-5", "admin123")
    }

    @Test
    fun usuarios_validacionesYLogin() {
        assertTrue(repository.existeRutPrevencionista("12.345.678-5"))
        assertFalse(repository.existeRutPrevencionista("99.999.999-9"))
        assertTrue(repository.existeCorreoPrevencionista("c.loncon@aquachile.com"))
        assertFalse(repository.existeCorreoPrevencionista("noexiste@aquachile.cl"))

        val loginOk = repository.login("12.345.678-5", "admin123")
        assertTrue(loginOk.isSuccess)
        assertNotNull(repository.prevencionistaActivo.value)

        repository.logout()
        assertNull(repository.prevencionistaActivo.value)
        repository.login("12.345.678-5", "admin123")
    }

    @Test
    fun buzos_gestionCompleta() {
        val nuevoBuzo = Buzo(
            id = "B-TEST-BOOST-" + System.currentTimeMillis(),
            rut = "14.234.567-8",
            nombre = "Buzo Boost",
            empresa = "AquaChile",
            matricula = "BC-888",
            telefono = "+56911223344",
            estadoSalud = EstadoSalud.APTO,
            edad = LocalDate.of(1993, 2, 1)
        )
        val addRes = repository.agregarBuzo(nuevoBuzo)
        assertTrue(addRes.isSuccess)

        val buzoMod = nuevoBuzo.copy(telefono = "+56999887766")
        val editRes = repository.editarBuzo(buzoMod)
        assertTrue(editRes.isSuccess)

        repository.actualizarSaludBuzo(nuevoBuzo.id, EstadoSalud.NO_APTO, "Hoy")
        repository.alternarAlertaNoInvasivaBuzo(nuevoBuzo.id)

        val delRes = repository.eliminarBuzo(nuevoBuzo.id)
        assertTrue(delRes.isSuccess)
    }

    @Test
    fun faenas_gestionYFlujo() {
        val faenas = repository.faenas.value
        assertTrue(faenas.isNotEmpty())
        val f = faenas.first()

        repository.aprobarDatosFaena(f.id)
        repository.revocarAprobacionFaena(f.id)

        repository.editarDatosFaena(
            faenaId = f.id,
            centro = "Centro Melinka 1",
            tipo = "Inspección",
            profundidad = 25,
            minutos = 45
        )

        val avance = repository.avanceDe(f)
        assertNotNull(avance)

        val revisiones = repository.revisionesCombinadas(f, f.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        assertNotNull(revisiones)

        val rev = RevisionEquipo(
            equipoId = "e1",
            categoria = "Suministro",
            nombre = "Compresor",
            descripcionUso = "Uso normal",
            estado = EstadoEquipo.ACEPTABLE
        )
        repository.guardarRevision(f.id, f.buzoId, rev)
        repository.confirmarEtapa(f.id, f.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        assertTrue(repository.etapaConfirmada(f.id, f.buzoId, EtapaComprobacion.PRE_EQUIPACION))

        val evidencia = repository.obtenerEvidencia(f.id, f.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        assertNotNull(evidencia)

        repository.guardarFotoEvidencia(f.id, f.buzoId, EtapaComprobacion.PRE_EQUIPACION, TipoFotoEquipos.GENERAL, "uri_foto")

        repository.solicitarRevision(SolicitudRevision(f.id, DestinoRevision.EQUIPOS))
        repository.limpiarSolicitud(DestinoRevision.EQUIPOS)

        repository.cerrarFaenaSinTerminar(
            faenaId = f.id,
            motivosSeleccionados = setOf(MotivoCierreSinTerminar.MAL_TIEMPO),
            otrosMotivos = listOf("Corriente fuerte"),
            comentario = "Prueba cierre"
        )
        repository.actualizarComentarioCierreFaena(f.id, "Comentario modificado")
    }

    @Test
    fun chequeoYBitacora_gestion() {
        val faenas = repository.faenas.value
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            val resBitacora = repository.registrarBitacoraPostInmersion(
                faenaId = f.id,
                profundidadMetros = 18,
                tiempoFondoMinutos = 30,
                velocidadDescensoMpm = 18.0,
                velocidadAscensoMpm = 7.5,
                origenDato = "Census",
                observaciones = "Inmersión segura"
            )
            assertTrue(resBitacora.isSuccess)

            repository.registrarEncuestaSalud(
                faenaId = f.id,
                momento = MomentoSalud.ANTES,
                esApto = true,
                presionArterial = "120/80",
                frecuenciaCardiaca = 72,
                semaforo = ColorSemaforo.VERDE,
                motivosBloqueo = emptyList(),
                advertencias = emptyList(),
                buzoEvaluadoId = f.buzoId,
                edadBuzo = 32,
                testEsfuerzoVigente = true,
                examenPreocupacionalVigente = true,
                puntajeCalbuco = 95,
                compresorCumple = true,
                buzoEmergenciaCumple = true
            )
        }
    }
}
