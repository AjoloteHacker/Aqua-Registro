package com.example.myapplication.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RandomUserGeneratorTest {

    @Test
    fun generarUrlFallback_generaUrlValida() {
        val urlHombre = RandomUserGenerator.generarUrlFallback("men")
        assertTrue(urlHombre.startsWith("https://randomuser.me/api/portraits/men/"))
        assertTrue(urlHombre.endsWith(".jpg"))

        val urlMujer = RandomUserGenerator.generarUrlFallback("women")
        assertTrue(urlMujer.startsWith("https://randomuser.me/api/portraits/women/"))

        val urlDefault = RandomUserGenerator.generarUrlFallback(null)
        assertTrue(urlDefault.startsWith("https://randomuser.me/api/portraits/men/"))
    }

    @Test
    fun obtenerFotoAleatoria_retornaUrl() = runBlocking {
        // En entorno de test unitario local puede fallar la red y usar fallback de forma transparente
        val foto = RandomUserGenerator.obtenerFotoAleatoria("women")
        assertNotNull(foto)
        assertTrue(foto.isNotBlank())
    }
}
