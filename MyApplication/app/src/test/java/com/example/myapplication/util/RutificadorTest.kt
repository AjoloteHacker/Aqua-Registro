package com.example.myapplication.util

import com.example.myapplication.data.remote.RetrofitClient
import org.junit.Assert.*
import org.junit.Test

class RutificadorTest {

    @Test
    fun limpiar_eliminaPuntosGuionesEspaciosYConvierteMayuscula() {
        assertEquals("12345678K", Rutificador.limpiar(" 12.345.678-k "))
        assertEquals("154892311", Rutificador.limpiar("15.489.231-1"))
        assertEquals("761234560", Rutificador.limpiar("76.123.456-0"))
    }

    @Test
    fun calcularDv_calculaCorrectamenteDigitoVerificadorModulo11() {
        assertEquals("5", Rutificador.calcularDv("12345678"))
        assertEquals("1", Rutificador.calcularDv("15489231"))
        assertEquals("K", Rutificador.calcularDv("17912453"))
        assertEquals("9", Rutificador.calcularDv("16320108"))
        assertEquals("0", Rutificador.calcularDv("76123456"))
    }

    @Test
    fun validar_rutValido_retornaTrue() {
        assertTrue(Rutificador.validar("12.345.678-5"))
        assertTrue(Rutificador.validar("15.489.231-1"))
        assertTrue(Rutificador.validar("17.912.453-K"))
        assertTrue(Rutificador.validar("17.912.453-k")) // En minúscula también es válido
        assertTrue(Rutificador.validar("163201089")) // Sin formato
    }

    @Test
    fun validar_rutInvalido_retornaFalse() {
        assertFalse(Rutificador.validar("12.345.678-9")) // DV incorrecto
        assertFalse(Rutificador.validar("15.489.231-K")) // DV incorrecto
        assertFalse(Rutificador.validar("123"))          // Demasiado corto
        assertFalse(Rutificador.validar("ABCDEFGHI-K"))  // Letras en cuerpo
        assertFalse(Rutificador.validar("00.000.000-0")) // Puros ceros
        assertFalse(Rutificador.validar(""))             // Cadena vacía
    }

    @Test
    fun formatear_aplicaMascaraCanonicaChilena() {
        assertEquals("12.345.678-5", Rutificador.formatear("123456785"))
        assertEquals("17.912.453-K", Rutificador.formatear("17912453k"))
        assertEquals("9.876.543-2", Rutificador.formatear("98765432"))
    }

    @Test
    fun formatearEnVivo_agregaPuntosYGuionMientrasEscribe() {
        assertEquals("", Rutificador.formatearEnVivo(""))
        assertEquals("123.456-7", Rutificador.formatearEnVivo("1234567"))
        assertEquals("1.234.567-8", Rutificador.formatearEnVivo("12345678"))
        assertEquals("12.345.678-5", Rutificador.formatearEnVivo("123456785"))
        assertEquals("17.912.453-K", Rutificador.formatearEnVivo("17.912.453-k99"))
    }

    @Test
    fun paisTelefono_formateaNumeroConPrefijoInternacional() {
        assertEquals("+56 9 8123 4567", PaisTelefono.CHILE.formatearNumero("981234567"))
        assertEquals("+54 11 1234 5678", PaisTelefono.ARGENTINA.formatearNumero("1112345678"))
        assertEquals("+51 987 654 321", PaisTelefono.PERU.formatearNumero("987654321"))
        assertEquals("+56 9 8123 4567", PaisTelefono.CHILE.formatearNumero("+56 9 8123 4567"))
        // Verifica escritura progresiva carácter por carácter sin duplicar el prefijo +56
        var acumulado = ""
        for (d in "984512290") {
            acumulado = PaisTelefono.CHILE.formatearNumero(acumulado + d)
        }
        assertEquals("+56 9 8451 2290", acumulado)
    }

    @Test
    fun retrofitClient_normalizaYActualizaBaseUrlAjoloServer() {
        assertEquals(
            "https://ajoloserver.tail19de00.ts.net:8443/api/",
            RetrofitClient.normalizarBaseUrl("ajoloserver.tail19de00.ts.net:8443")
        )
        assertEquals(
            "http://100.124.162.2:18080/api/",
            RetrofitClient.normalizarBaseUrl("http://100.124.162.2:18080/api")
        )
        val actualizada = RetrofitClient.actualizarBaseUrl(RetrofitClient.URL_AJOLOSERVER_PUBLICA)
        assertEquals("https://ajoloserver.tail19de00.ts.net:8443/api/", actualizada)
        assertEquals("https://ajoloserver.tail19de00.ts.net:8443/api/", RetrofitClient.BASE_URL_AQUACHECK)
    }
}
