package com.example.myapplication.util

import org.junit.Assert.*
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun validarNombre_casosValidosEInvalidos() {
        // En blanco
        val (b1, m1) = SecurityUtils.validarNombre("")
        assertFalse(b1)
        assertNotNull(m1)

        // Caracteres inválidos o números
        val (b2, _) = SecurityUtils.validarNombre("Juan 123 Perez")
        assertFalse(b2)

        // Una sola palabra (longitud >= 5 pero sin apellido)
        val (b3, m3) = SecurityUtils.validarNombre("Carlos")
        assertFalse(b3)
        assertTrue(m3?.contains("tanto el nombre como el apellido") == true)

        // Parte con menos de 2 caracteres
        val (b4, _) = SecurityUtils.validarNombre("J Perez")
        assertFalse(b4)

        // Válido con tildes y caracteres chilenos
        val (b5, _) = SecurityUtils.validarNombre("Cristóbal Loncón Cárdenas")
        assertTrue(b5)
    }

    @Test
    fun validarEmail_casosValidosEInvalidos() {
        val (b1, _) = SecurityUtils.validarEmail("")
        assertFalse(b1)

        val (b2, _) = SecurityUtils.validarEmail("correo-invalido")
        assertFalse(b2)

        val (b3, _) = SecurityUtils.validarEmail("usuario@aquachile.cl")
        assertTrue(b3)
    }

    @Test
    fun validarRegistroSeremi_casosValidosEInvalidos() {
        val (b1, _) = SecurityUtils.validarRegistroSeremi("")
        assertFalse(b1)

        val (b2, _) = SecurityUtils.validarRegistroSeremi("12345")
        assertFalse(b2)

        val (b3, _) = SecurityUtils.validarRegistroSeremi("PR-1234-A")
        assertTrue(b3)
    }

    @Test
    fun validarPassword_casosValidosEInvalidos() {
        val (b1, _) = SecurityUtils.validarPassword("")
        assertFalse(b1)

        // Menos de 8 caracteres
        val (b2, _) = SecurityUtils.validarPassword("Ab1")
        assertFalse(b2)

        // Solo minúsculas
        val (b3, _) = SecurityUtils.validarPassword("abcdefgh1")
        assertFalse(b3)

        // Solo mayúsculas
        val (b4, _) = SecurityUtils.validarPassword("ABCDEFGH1")
        assertFalse(b4)

        // Sin números
        val (b5, _) = SecurityUtils.validarPassword("Abcdefghij")
        assertFalse(b5)

        // Válida
        val (b6, _) = SecurityUtils.validarPassword("Admin1234")
        assertTrue(b6)
    }

    @Test
    fun validarPasswordConfirm_coincidencia() {
        val (b1, _) = SecurityUtils.validarPasswordConfirm("Admin1234", "otra")
        assertFalse(b1)

        val (b2, _) = SecurityUtils.validarPasswordConfirm("Admin1234", "Admin1234")
        assertTrue(b2)
    }

    @Test
    fun validarRut_casos() {
        val (b1, _) = SecurityUtils.validarRut("")
        assertFalse(b1)

        val (b2, _) = SecurityUtils.validarRut("12.345.678-5")
        assertTrue(b2)
    }
}
