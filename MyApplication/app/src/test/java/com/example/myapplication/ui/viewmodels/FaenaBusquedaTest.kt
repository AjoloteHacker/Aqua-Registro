package com.example.myapplication.ui.viewmodels

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaenaBusquedaTest {

    @Test
    fun normalizarBusqueda_ignoraMayusculasYAcentos() {
        assertTrue(normalizarBusqueda("  MELINKA  ") == "melinka")
        assertTrue(normalizarBusqueda("Chiloé").contains("chiloe"))
        assertTrue(normalizarBusqueda("Muñoz") == "munoz")
    }

    @Test
    fun coincideConBusqueda_busquedaVacia_muestraTodas() {
        assertTrue(coincideConBusqueda("", "F-101", "Centro Melinka"))
        assertTrue(coincideConBusqueda("   ", null, "Inspección"))
    }

    @Test
    fun coincideConBusqueda_encuentraCampoConAcentoYCase() {
        assertTrue(
            coincideConBusqueda(
                "CHILOE",
                "F-101",
                "Carlos Soto",
                "Ana López",
                "Inspección de Fondos",
                "Centro Chiloé"
            )
        )
        assertTrue(coincideConBusqueda("munoz", "José Muñoz"))
        assertTrue(coincideConBusqueda("f-101", "F-101"))
    }

    @Test
    fun coincideConBusqueda_sinCoincidencias_devuelveFalse() {
        assertFalse(coincideConBusqueda("No existe", "F-101", "Centro Melinka"))
        assertFalse(coincideConBusqueda("melinka", null, "Centro Chiloé"))
    }
}
