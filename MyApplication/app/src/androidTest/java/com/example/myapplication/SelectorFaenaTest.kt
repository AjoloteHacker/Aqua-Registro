package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.ui.components.OpcionSelectorFaena
import com.example.myapplication.ui.components.SelectorFaena
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectorFaenaTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun abrirSelectorYSeleccionarFaena_noCrash() {
        val opciones = listOf(
            OpcionSelectorFaena(
                id = "F-1",
                titulo = "F-1 · Juan Pérez",
                detalle = "Inspección · Centro Chiloé",
                meta = "2/3 equipos"
            )
        )
        val busqueda = mutableStateOf("")
        var seleccionada: String? = null

        composeRule.setContent {
            SelectorFaena(
                opciones = opciones,
                seleccionada = null,
                busqueda = busqueda.value,
                onBusquedaChange = { busqueda.value = it },
                onSeleccionar = { seleccionada = it }
            )
        }

        // Abrir el menú era lo que disparaba el crash de medición intrínseca.
        composeRule.onNodeWithText("Seleccionar faena").performClick()
        composeRule.onNodeWithText("F-1 · Juan Pérez").assertIsDisplayed().performClick()

        assertEquals("F-1", seleccionada)
        assertEquals("", busqueda.value)
    }
}
