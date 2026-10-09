package com.example.myapplication

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.util.Rutificador
import com.example.myapplication.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de arquitectura MVVM, Repositorio, Rutificador y Seguridad.
 */
class ExampleUnitTest {

    private lateinit var repository: AquaRepository

    @Before
    fun setup() {
        repository = AquaRepository.getInstance()
    }

    @Test
    fun testLogin_exitoso() {
        val result = repository.login("12.345.678-5", "admin123")
        assertTrue(result.isSuccess)
        assertEquals("Cristóbal Loncón C.", result.getOrNull()?.nombre)
        assertEquals("Cristóbal Loncón C.", repository.prevencionistaActivo.value?.nombre)
    }

    @Test
    fun testLogin_conRutSinFormato() {
        val result = repository.login("123456785", "admin123")
        assertTrue(result.isSuccess)
        assertEquals("Cristóbal Loncón C.", result.getOrNull()?.nombre)
    }

    @Test
    fun testLogin_credencialesInvalidas() {
        val result = repository.login("12.345.678-5", "password_incorrecta")
        assertTrue(result.isFailure)
    }

    @Test
    fun testRutificador_validacionesModulo11() {
        // RUTs válidos
        assertTrue("RUT 12.345.678-5 debe ser válido", Rutificador.validar("12.345.678-5"))
        assertTrue("RUT 123456785 sin formato debe ser válido", Rutificador.validar("123456785"))
        assertTrue("RUT con K debe ser válido", Rutificador.validar("17.912.453-K"))
        assertTrue("RUT con k minúscula debe ser válido", Rutificador.validar("17912453k"))

        // RUTs inválidos o al azar
        assertFalse("RUT aleatorio debe ser inválido", Rutificador.validar("123456789"))
        assertFalse("RUT con dígito verificador erróneo", Rutificador.validar("12.345.678-9"))
        assertFalse("RUT vacío debe ser inválido", Rutificador.validar(""))
        assertFalse("RUT con letras en el cuerpo debe ser inválido", Rutificador.validar("12A45678-5"))
        assertFalse("RUT demasiado corto debe ser inválido", Rutificador.validar("12345-6"))
    }

    @Test
    fun testRutificador_formateoCanónico() {
        assertEquals("12.345.678-5", Rutificador.formatear("123456785"))
        assertEquals("17.912.453-K", Rutificador.formatear("17912453k"))
        assertEquals("7.654.321-4", Rutificador.formatear("76543214"))
    }

    @Test
    fun testSecurityUtils_validacionNombre() {
        val (okValido, _) = SecurityUtils.validarNombre("Rodrigo Valenzuela")
        assertTrue(okValido)

        val (okInvalido1, err1) = SecurityUtils.validarNombre("asdf123")
        assertFalse(okInvalido1)
        assertNotNull(err1)

        val (okSoloNombre, err2) = SecurityUtils.validarNombre("Rodrigo")
        assertFalse(okSoloNombre)
        assertNotNull(err2)
    }

    @Test
    fun testSecurityUtils_validacionEmail() {
        val (okEmail, _) = SecurityUtils.validarEmail("prevencionista@aquachile.cl")
        assertTrue(okEmail)

        val (okFalso, err) = SecurityUtils.validarEmail("correo_invalido_sin_arroba")
        assertFalse(okFalso)
        assertNotNull(err)
    }

    @Test
    fun testSecurityUtils_validacionSeremi() {
        val (okSeremi, _) = SecurityUtils.validarRegistroSeremi("PR-10892-X")
        assertTrue(okSeremi)

        val (okMalo, err) = SecurityUtils.validarRegistroSeremi("12345")
        assertFalse(okMalo)
        assertNotNull(err)
    }

    @Test
    fun testSecurityUtils_validacionPassword() {
        val (okPassword, _) = SecurityUtils.validarPassword("AquaChile2026!")
        assertTrue(okPassword)

        val (okCorta, _) = SecurityUtils.validarPassword("1234")
        assertFalse(okCorta)

        val (okSinMayus, _) = SecurityUtils.validarPassword("aquachile2026")
        assertFalse(okSinMayus)

        val (okSinNumero, _) = SecurityUtils.validarPassword("AquaChileSegura")
        assertFalse(okSinNumero)
    }

    @Test
    fun testRegistrarPrevencionista_rechazaDuplicados() {
        val usuarioDuplicadoRut = Prevencionista(
            nombre = "Carlos Duplicado",
            rut = "12.345.678-5",
            correo = "otro.correo@aquachile.com",
            registroSeremi = "PR-99999-A",
            centro = "Centro Melinka 1",
            contrasena = "Segura123!"
        )
        val resultado = repository.registrarPrevencionista(usuarioDuplicadoRut)
        assertTrue(resultado.isFailure)

        val usuarioDuplicadoCorreo = Prevencionista(
            nombre = "Carlos Nuevo",
            rut = "17.912.453-K",
            correo = "c.loncon@aquachile.com",
            registroSeremi = "PR-88888-B",
            centro = "Centro Melinka 1",
            contrasena = "Segura123!"
        )
        val resultadoCorreo = repository.registrarPrevencionista(usuarioDuplicadoCorreo)
        assertTrue(resultadoCorreo.isFailure)
    }

    @Test
    fun testAgregarBuzo_actualizaStateFlow() {
        val buzoId = "test-${System.currentTimeMillis()}"
        val nuevoBuzo = Buzo(
            id = buzoId,
            rut = "19.999.888-9",
            nombre = "Buzo de Prueba",
            empresa = "AquaSub Chile Limitada",
            matricula = "Buzo Comercial N° 9999",
            telefono = "+56 9 1111 2222"
        )
        val result = repository.agregarBuzo(nuevoBuzo)
        assertTrue(result.isSuccess)

        val buzosActuales = repository.buzos.value
        assertTrue(buzosActuales.any { it.rut == "19.999.888-9" })
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