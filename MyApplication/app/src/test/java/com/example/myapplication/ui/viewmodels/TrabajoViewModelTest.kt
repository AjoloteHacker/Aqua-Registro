package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrabajoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: TrabajoViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        repository.login("12.345.678-5", "admin123")
        viewModel = TrabajoViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun seleccionBuzos_validaciones() {
        val buzos = viewModel.uiState.value.buzos
        assertTrue(buzos.size >= 2)

        val b1 = buzos[0].id
        val b2 = buzos[1].id

        viewModel.seleccionarBuzo(b1)
        assertEquals(b1, viewModel.uiState.value.buzoSeleccionadoId)

        // Socorrista igual a buzo debe fallar
        viewModel.seleccionarSocorrista(b1)
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Socorrista distinto
        viewModel.seleccionarSocorrista(b2)
        assertEquals(b2, viewModel.uiState.value.socorristaSeleccionadoId)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun inputsFormulario_actualizanEstado() {
        viewModel.onCentroChange("Centro Melinka 2")
        assertEquals("Centro Melinka 2", viewModel.uiState.value.centroCultivo)

        viewModel.onTipoFaenaChange("Reparación de Redes")
        assertEquals("Reparación de Redes", viewModel.uiState.value.tipoFaena)

        viewModel.onProfundidadChange("25")
        assertEquals("25", viewModel.uiState.value.profundidad)

        viewModel.onDuracionChange("50")
        assertEquals("50", viewModel.uiState.value.duracionMinutos)
    }

    @Test
    fun autorizarFaena_cicloExitoso() {
        val buzos = viewModel.uiState.value.buzos
        viewModel.seleccionarBuzo(buzos[0].id)
        viewModel.seleccionarSocorrista(buzos[1].id)
        viewModel.onProfundidadChange("15")
        viewModel.onDuracionChange("30")

        viewModel.autorizarFaena()
        assertTrue(viewModel.uiState.value.successDialog)
        assertNotNull(viewModel.uiState.value.faenaAutorizadaId)

        viewModel.dismissSuccessDialog()
        assertFalse(viewModel.uiState.value.successDialog)

        viewModel.clearUserMessage()
        assertNull(viewModel.uiState.value.userMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
