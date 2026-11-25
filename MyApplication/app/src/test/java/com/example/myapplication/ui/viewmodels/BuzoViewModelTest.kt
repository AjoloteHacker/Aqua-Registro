package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuzoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var listaBuzos: List<Buzo>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        listaBuzos = listOf(
            Buzo(
                id = "1",
                rut = "15.489.231-1",
                nombre = "Carlos Soto Paredes",
                empresa = "Servicios Submarinos Austral SpA",
                matricula = "Buzo Comercial N° 2394",
                telefono = "+56 9 8451 2290",
                estadoSalud = EstadoSalud.APTO,
                categoria = "Buzo Comercial"
            ),
            Buzo(
                id = "2",
                rut = "17.912.453-K",
                nombre = "Pedro Almonacid Vera",
                empresa = "AquaSub Chile Limitada",
                matricula = "Buzo Básico N° 5812",
                telefono = "+56 9 7123 9940",
                estadoSalud = EstadoSalud.NO_APTO,
                categoria = "Buzo Básico"
            ),
            Buzo(
                id = "3",
                rut = "16.320.108-9",
                nombre = "Rodrigo Mansilla Cárdenas",
                empresa = "Buceos del Sur S.A.",
                matricula = "Buzo Especialista N° 1042",
                telefono = "+56 9 9234 5512",
                estadoSalud = EstadoSalud.PENDIENTE,
                categoria = "Buzo Especialista"
            )
        )
    }

    @Test
    fun buzosFiltrados_sinFiltros_retornaTodosLosBuzos() {
        val state = BuzoUiState(buzos = listaBuzos)
        assertEquals(3, state.buzosFiltrados.size)
    }

    @Test
    fun buzosFiltrados_porNombre_retornaCoincidencias() {
        val state = BuzoUiState(buzos = listaBuzos, searchQuery = "Carlos")
        assertEquals(1, state.buzosFiltrados.size)
        assertEquals("Carlos Soto Paredes", state.buzosFiltrados.first().nombre)
    }

    @Test
    fun buzosFiltrados_porRut_retornaCoincidencias() {
        val state = BuzoUiState(buzos = listaBuzos, searchQuery = "17.912")
        assertEquals(1, state.buzosFiltrados.size)
        assertEquals("Pedro Almonacid Vera", state.buzosFiltrados.first().nombre)
    }

    @Test
    fun buzosFiltrados_porEmpresa_retornaCoincidencias() {
        val state = BuzoUiState(buzos = listaBuzos, searchQuery = "AquaSub")
        assertEquals(1, state.buzosFiltrados.size)
        assertEquals("Pedro Almonacid Vera", state.buzosFiltrados.first().nombre)
    }

    @Test
    fun buzosFiltrados_porEstadoSaludApto_filtraSoloAptos() {
        val state = BuzoUiState(buzos = listaBuzos, filtroSalud = EstadoSalud.APTO)
        assertEquals(1, state.buzosFiltrados.size)
        assertEquals(EstadoSalud.APTO, state.buzosFiltrados.first().estadoSalud)
    }

    @Test
    fun buzosFiltrados_porEstadoSaludNoApto_filtraSoloNoAptos() {
        val state = BuzoUiState(buzos = listaBuzos, filtroSalud = EstadoSalud.NO_APTO)
        assertEquals(1, state.buzosFiltrados.size)
        assertEquals("Pedro Almonacid Vera", state.buzosFiltrados.first().nombre)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun buzosFiltrados_conBusquedaYSinCoincidencias_retornaListaVacia() {
        val state = BuzoUiState(buzos = listaBuzos, searchQuery = "Inexistente 999")
        assertTrue(state.buzosFiltrados.isEmpty())
    }

    @Test
    fun buzoViewModel_conMockK_guardarBuzoValidoInvocaRepositorio() {
        val repositoryMock = mockk<AquaRepository>(relaxed = true)
        every { repositoryMock.buzos } returns MutableStateFlow(listaBuzos)
        every { repositoryMock.empresasFlow } returns MutableStateFlow(listOf("AquaSub Chile Limitada"))
        every { repositoryMock.empresas } returns listOf("AquaSub Chile Limitada")
        every { repositoryMock.agregarBuzo(any()) } answers {
            Result.success(firstArg())
        }

        val viewModel = BuzoViewModel(repository = repositoryMock)
        viewModel.openAddDialog()
        viewModel.onNuevoNombreChange("Juan Pérez Soto")
        viewModel.onNuevoRutChange("12.345.678-5")
        viewModel.onNuevoTelefonoChange("+56 9 8765 4321")
        viewModel.guardarBuzo()

        verify(exactly = 1) {
            repositoryMock.agregarBuzo(match { it.nombre == "Juan Pérez Soto" && it.rut == "12.345.678-5" })
        }
        assertFalse(viewModel.uiState.value.showAddDialog)
    }
}
