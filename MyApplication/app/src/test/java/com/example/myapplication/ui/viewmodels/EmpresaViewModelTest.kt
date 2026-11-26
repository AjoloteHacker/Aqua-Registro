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
class EmpresaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: EmpresaViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = EmpresaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cicloVidaDialogoYGuardarEmpresa() {
        viewModel.openAddDialog()
        assertTrue(viewModel.uiState.value.showAddDialog)

        viewModel.onNuevaEmpresaNombreChange("Servicios Austral Test")
        assertEquals("Servicios Austral Test", viewModel.uiState.value.nuevaEmpresaNombre)

        viewModel.guardarNuevaEmpresa()
        assertFalse(viewModel.uiState.value.showAddDialog)
        assertNotNull(viewModel.uiState.value.userMessage)

        viewModel.clearUserMessage()
        assertNull(viewModel.uiState.value.userMessage)

        // Intento con nombre vacío
        viewModel.openAddDialog()
        viewModel.onNuevaEmpresaNombreChange("")
        viewModel.guardarNuevaEmpresa()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.closeAddDialog()
        assertFalse(viewModel.uiState.value.showAddDialog)
    }
}
