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
class MenuViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MenuViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = MenuViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun accionesMenu_ejecutanSinError() {
        assertNotNull(viewModel.buzos.value)
        assertNotNull(viewModel.faenas.value)
        assertNotNull(viewModel.climaMaritimo.value)
        assertNotNull(viewModel.syncStatus.value)

        viewModel.sincronizarAhora()
        viewModel.alternarModoOfflineDemo()
        viewModel.cambiarUrlServidor("https://test.server.com/api/")
        assertEquals("https://test.server.com/api/", viewModel.serverUrl.value)

        viewModel.probarConexionServidor()
        viewModel.actualizarClima("Centro Melinka 1")

        viewModel.logout()
        assertNull(viewModel.prevencionistaActivo.value)
    }
}
