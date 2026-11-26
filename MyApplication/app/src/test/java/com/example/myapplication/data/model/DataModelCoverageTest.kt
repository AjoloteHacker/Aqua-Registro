package com.example.myapplication.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DataModelCoverageTest {

    @Test
    fun buzo_gettersYMetodos() {
        val buzo = Buzo(
            id = "B-TEST",
            rut = "15.489.231-1",
            nombre = "Carlos Soto",
            empresa = "AquaChile",
            matricula = "BC-100",
            telefono = "+56911112222",
            estadoSalud = EstadoSalud.APTO,
            edad = LocalDate.of(1990, 5, 15),
            categoria = "Buzo Comercial",
            testEsfuerzoVigente = true,
            examenPreocupacionalVigente = true,
            puntajeCalbuco = 95,
            estatura = 1.75f,
            peso = 75f
        )

        assertEquals("B-TEST", buzo.id)
        assertEquals("Carlos Soto", buzo.nombre)
        assertNotNull(buzo.edadAnios)
        assertNotNull(buzo.imc)
        assertFalse(buzo.bloqueadoPorEdadSinTestEsfuerzo)
        assertFalse(buzo.matriculaVencida)
    }

    @Test
    fun enums_cobertura() {
        EstadoEquipo.values().forEach { assertNotNull(it.name) }
        EstadoSalud.values().forEach { assertNotNull(it.name) }
        EtapaComprobacion.values().forEach { assertNotNull(it.name) }
        TipoFotoEquipos.values().forEach { assertNotNull(it.name) }
        MomentoSalud.values().forEach { assertNotNull(it.name) }
        ColorSemaforo.values().forEach { assertNotNull(it.name) }
        MotivoCierreSinTerminar.values().forEach { assertNotNull(it.name) }
        RolUsuario.values().forEach { assertNotNull(it.name) }
    }

    @Test
    fun registroFaena_model() {
        val faena = RegistroFaena(
            id = "F-TEST-MODEL",
            buzoId = "B-1",
            buzoNombre = "Carlos",
            socorristaId = "B-2",
            socorristaNombre = "Pedro",
            empresa = "AquaChile",
            centroCultivo = "Centro 1",
            tipoFaena = "Inspección",
            profundidadMetros = 15,
            tiempoMinutos = 30,
            fechaHora = "25/11/2026 10:00",
            autorizadoPor = "Supervisor"
        )

        assertEquals("F-TEST-MODEL", faena.id)
        assertTrue(faena.enCurso)
        assertFalse(faena.completada)
        assertFalse(faena.cerradaSinTerminar)
        assertEquals(2, faena.asignaciones().size)
    }

    @Test
    fun revisionEquipo_model() {
        val rev = RevisionEquipo(
            equipoId = "e1",
            nombre = "Compresor",
            categoria = "Suministro",
            descripcionUso = "Certificado al día",
            estado = EstadoEquipo.ACEPTABLE
        )

        assertEquals("e1", rev.equipoId)
        assertTrue(rev.evaluacionCompleta)
        assertTrue(rev.tieneEstado)
        assertEquals(EstadoEquipo.ACEPTABLE, rev.estado)
        assertTrue(rev.esEquipoCriticoLineaBase)
    }

    @Test
    fun evidenciaEquipos_model() {
        val ev = EvidenciaEquipos(
            fotos = mapOf(TipoFotoEquipos.GENERAL to "uri1")
        )

        assertTrue(ev.estaCompleta(EtapaComprobacion.PRE_EQUIPACION))
        assertFalse(ev.estaCompleta(EtapaComprobacion.EQUIPADO))
    }
}
