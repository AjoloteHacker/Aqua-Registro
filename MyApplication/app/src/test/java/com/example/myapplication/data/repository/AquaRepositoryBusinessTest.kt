package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.model.RegistroFaena
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AquaRepositoryBusinessTest {

    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        repository = AquaRepository.getInstance()
    }

    @Test
    fun login_credencialesValidas_retornaExito() {
        // Usuario por defecto: "12.345.678-5" / "admin123"
        val resultado = repository.login("12.345.678-5", "admin123")
        assertTrue(resultado.isSuccess)
        assertEquals("Cristóbal Loncón C.", resultado.getOrNull()?.nombre)
    }

    @Test
    fun login_rutSinFormato_retornaExito() {
        // Normalización automática de RUT sin puntos ni guiones
        val resultado = repository.login("123456785", "admin123")
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun login_contrasenaInvalida_retornaFallo() {
        val resultado = repository.login("12.345.678-5", "wrong_password")
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("inválidas") == true)
    }

    @Test
    fun registrarPrevencionista_rutDuplicado_retornaFallo() {
        val duplicado = Prevencionista(
            nombre = "Otro Usuario",
            rut = "12.345.678-5", // Ya existe
            correo = "otro@aquachile.com",
            registroSeremi = "PR-9999",
            centro = "Centro Melinka 1",
            contrasena = "123456"
        )
        val resultado = repository.registrarPrevencionista(duplicado)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Ya existe") == true)
    }

    @Test
    fun agregarBuzo_rutDuplicado_retornaFallo() {
        val buzoDuplicado = Buzo(
            id = "test-dup",
            rut = "15.489.231-1", // Ya existe en catálogo inicial (Carlos Soto)
            nombre = "Buzo Clon",
            empresa = "AquaSub",
            matricula = "N° 9999",
            telefono = "+56 9 1111 2222",
            estadoSalud = EstadoSalud.PENDIENTE
        )
        val resultado = repository.agregarBuzo(buzoDuplicado)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Ya existe un buzo") == true)
    }

    @Test
    fun eliminarBuzo_conFaenaEnCurso_retornaFallo() {
        // En catálogo inicial, Rodrigo Mansilla (ID 3) tiene faena F-102 en curso
        val resultado = repository.eliminarBuzo("3")
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("faena en curso") == true)
    }

    @Test
    fun agregarEmpresa_nombreValido_agregaExitosamente() {
        val nueva = "Austral Diving Safety Ltda"
        val resultado = repository.agregarEmpresa(nueva)
        assertTrue(resultado.isSuccess)
        assertTrue(repository.empresas.contains(nueva))
    }

    @Test
    fun agregarEmpresa_enBlanco_retornaFallo() {
        val resultado = repository.agregarEmpresa("   ")
        assertTrue(resultado.isFailure)
    }
}
