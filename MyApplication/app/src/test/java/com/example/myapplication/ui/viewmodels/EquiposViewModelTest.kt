package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
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
class EquiposViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: EquiposViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = EquiposViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun seleccionFaenaYEtapa() {
        val faenas = repository.faenas.value.filter { it.enCurso }
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.seleccionarFaena(f.id)
            assertEquals(f.id, viewModel.uiState.value.faenaSeleccionadaId)

            viewModel.seleccionarEtapa(EtapaComprobacion.EQUIPADO)
            assertEquals(EtapaComprobacion.EQUIPADO, viewModel.uiState.value.etapa)

            viewModel.seleccionarBuzo(0)
            assertEquals(0, viewModel.uiState.value.indiceBuzo)
        }
    }

    @Test
    fun actualizacionEquiposYFotos() {
        val equipos = viewModel.uiState.value.equipos
        if (equipos.isNotEmpty()) {
            val primerEquipo = equipos.first()
            viewModel.actualizarEstado(primerEquipo.equipoId, EstadoEquipo.ACEPTABLE)
            viewModel.actualizarDescripcion(primerEquipo.equipoId, "Todo OK")
            viewModel.registrarFotoEquipoCritico(primerEquipo.equipoId, "content://foto.jpg")
            viewModel.actualizarComparacionLineaBase(primerEquipo.equipoId, true, "Idéntico")
        }

        viewModel.guardarAvance()
        viewModel.clearUserMessage()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun confirmacionEtapasYFotos() {
        val faenas = repository.faenas.value.filter { it.enCurso }
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.seleccionarFaena(f.id)
            viewModel.registrarFoto(com.example.myapplication.data.model.TipoFotoEquipos.GENERAL, "content://foto_general.jpg")
            viewModel.confirmarEtapa()
            assertNotNull(viewModel.uiState.value.userMessage)
        }
    }
}
