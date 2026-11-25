package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.Prevencionista
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
class TrabajoUiStateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var buzos: List<Buzo>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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
            socorristaSeleccionadoId = "2",
            supervisor = "Cristóbal Loncón (SEREMI: PR-10892-X)"
        )

        assertTrue(state.tripulacionCompleta)
        assertTrue(state.supervisorActivo)
        assertTrue(state.puedeAutorizar)
        assertEquals("Pedro Almonacid Vera", state.socorristaSeleccionado?.nombre)
    }

    @Test
    fun sinPrevencionistaEnSesion_noPuedeAutorizar() {
        val state = TrabajoUiState(
            buzos = buzos,
            buzoSeleccionadoId = "1",
            socorristaSeleccionadoId = "2",
            supervisor = ""
        )

        assertTrue(state.tripulacionCompleta)
        assertFalse(state.supervisorActivo)
        assertFalse(state.puedeAutorizar)
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

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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

    @Test
    fun trabajoViewModel_conMockK_autorizaFaenaConDosBuzosYSupervisorImplicito() {
        val repositoryMock = mockk<AquaRepository>(relaxed = true)
        every { repositoryMock.buzos } returns MutableStateFlow(buzos)
        every { repositoryMock.centrosCultivo } returns listOf("Centro Melinka 1")
        every { repositoryMock.tiposFaena } returns listOf("Inspección de redes")
        every { repositoryMock.faenas } returns MutableStateFlow(emptyList())
        every { repositoryMock.prevencionistaActivo } returns MutableStateFlow(
            Prevencionista("Cristóbal Loncón", "12.345.678-5", "c@aquachile.com", "PR-10892-X", "Centro Melinka 1")
        )
        every { repositoryMock.obtenerFechaHoraActual() } returns "24/11/2026 11:00"
        every { repositoryMock.autorizarFaena(any()) } returns Result.success(Unit)

        val viewModel = TrabajoViewModel(repository = repositoryMock)
        viewModel.seleccionarBuzo("1")
        viewModel.seleccionarSocorrista("2")
        viewModel.autorizarFaena()

        verify(exactly = 1) {
            repositoryMock.autorizarFaena(
                match {
                    it.buzoId == "1" &&
                        it.socorristaId == "2" &&
                        it.autorizadoPor.contains("Cristóbal Loncón")
                }
            )
        }
        assertTrue(viewModel.uiState.value.successDialog)
    }
}
