package com.example.myapplication

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de arquitectura MVVM y Repositorio con StateFlow.
 */
class ExampleUnitTest {

    private lateinit var repository: AquaRepository

    @Before
    fun setup() {
        repository = AquaRepository.getInstance()
    }

    @Test
    fun testLogin_exitoso() {
        val result = repository.login("12.345.678-9", "admin123")
        assertTrue(result.isSuccess)
        assertEquals("Cristóbal Loncón C.", result.getOrNull()?.nombre)
        assertEquals("Cristóbal Loncón C.", repository.prevencionistaActivo.value?.nombre)
    }

    @Test
    fun testLogin_credencialesInvalidas() {
        val result = repository.login("12.345.678-9", "password_incorrecta")
        assertTrue(result.isFailure)
    }

    @Test
    fun testAgregarBuzo_actualizaStateFlow() {
        val buzoId = "test-${System.currentTimeMillis()}"
        val nuevoBuzo = Buzo(
            id = buzoId,
            rut = "19.999.888-7",
            nombre = "Buzo de Prueba",
            empresa = "AquaSub Chile Limitada",
            matricula = "Buzo Comercial N° 9999",
            telefono = "+56 9 1111 2222"
        )
        val result = repository.agregarBuzo(nuevoBuzo)
        assertTrue(result.isSuccess)

        val buzosActuales = repository.buzos.value
        assertTrue(buzosActuales.any { it.rut == "19.999.888-7" })
    }

    @Test
    fun testActualizarSaludBuzo_cambiaEstadoCorrectamente() {
        val buzoId = "1"
        val nuevoEstado = EstadoSalud.NO_APTO
        val detalle = "PA Alta: 150/95"

        val result = repository.actualizarSaludBuzo(buzoId, nuevoEstado, detalle)
        assertTrue(result.isSuccess)

        val buzoActualizado = repository.buzos.value.first { it.id == buzoId }
        assertEquals(EstadoSalud.NO_APTO, buzoActualizado.estadoSalud)
        assertEquals(detalle, buzoActualizado.ultimoChequeo)
    }

    @Test
    fun testAutorizarYCompletarFaena() {
        val faenaId = "F-TEST-${System.currentTimeMillis()}"
        val faena = RegistroFaena(
            id = faenaId,
            buzoNombre = "Carlos Soto Paredes",
            empresa = "Servicios Submarinos Austral SpA",
            centroCultivo = "Centro Melinka 1 - Canal Moraleda",
            tipoFaena = "Inspección de redes loberas",
            profundidadMetros = 15,
            tiempoMinutos = 30,
            fechaHora = "04/10/2026 23:45",
            estado = "Autorizada y en Curso"
        )

        val resultadoAutorizar = repository.autorizarFaena(faena)
        assertTrue(resultadoAutorizar.isSuccess)
        assertTrue(repository.faenas.value.any { it.id == faenaId && it.estado == "Autorizada y en Curso" })

        val resultadoCompletar = repository.completarFaena(faenaId)
        assertTrue(resultadoCompletar.isSuccess)

        val faenaCompletada = repository.faenas.value.first { it.id == faenaId }
        assertEquals("Completada con Éxito", faenaCompletada.estado)
    }
}