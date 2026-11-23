package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.Buzo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrabajoUiStateTest {

    private lateinit var buzos: List<Buzo>

    @Before
    fun setUp() {
        buzos = listOf(
            Buzo(id = "1", nombre = "Carlos Soto Paredes"),
            Buzo(id = "2", nombre = "Pedro Almonacid Vera"),
            Buzo(id = "3", nombre = "Rodrigo Mansilla Cárdenas")
        )
    }

    @Test
    fun tripulacionConDosBuzosDistintos_puedeAutorizar() {
        val state = TrabajoUiState(
            buzos = buzos,
            buzoSeleccionadoId = "1",
            socorristaSeleccionadoId = "2"
        )

        assertTrue(state.tripulacionCompleta)
        assertTrue(state.puedeAutorizar)
        assertEquals("Pedro Almonacid Vera", state.socorristaSeleccionado?.nombre)
    }

    @Test
    fun mismoBuzoParaAmbosRoles_noPuedeAutorizar() {
        val state = TrabajoUiState(
            buzos = buzos,
            buzoSeleccionadoId = "1",
            socorristaSeleccionadoId = "1"
        )

        assertFalse(state.tripulacionCompleta)
        assertFalse(state.puedeAutorizar)
        assertTrue(state.socorristasDisponibles.none { it.id == "1" })
    }

    @Test
    fun sinSocorrista_noPuedeAutorizar() {
        val state = TrabajoUiState(
            buzos = buzos,
            buzoSeleccionadoId = "1",
            socorristaSeleccionadoId = null
        )

        assertFalse(state.puedeAutorizar)
    }
}
