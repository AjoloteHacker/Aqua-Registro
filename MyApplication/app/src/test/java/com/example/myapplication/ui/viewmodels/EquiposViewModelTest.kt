package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.EvidenciaEquipos
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.SolicitudRevision
import io.mockk.every
import io.mockk.mockk
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

    @Test
    fun busquedaDeFaenas_filtraPorCamposYSeLimpiaAlSeleccionar() {
        val repositoryMock = mockk<AquaRepository>(relaxed = true)
        val melinka = RegistroFaena(
            id = "F-301",
            buzoId = "B-1",
            buzoNombre = "Carlos Soto",
            socorristaId = "B-2",
            socorristaNombre = "Ana López",
            empresa = "AquaChile",
            centroCultivo = "Centro Melinka",
            tipoFaena = "Inspección de Fondos",
            profundidadMetros = 20,
            tiempoMinutos = 40,
            fechaHora = "25/11/2026 10:00"
        )
        val chiloe = RegistroFaena(
            id = "F-302",
            buzoId = "B-3",
            buzoNombre = "Pedro Soto",
            empresa = "AquaSub",
            centroCultivo = "Centro Chiloé",
            tipoFaena = "Cuidado de redes",
            profundidadMetros = 18,
            tiempoMinutos = 30,
            fechaHora = "25/11/2026 12:00"
        )

        every { repositoryMock.faenas } returns MutableStateFlow(listOf(melinka, chiloe))
        every { repositoryMock.solicitudRevision } returns MutableStateFlow<SolicitudRevision?>(null)
        every { repositoryMock.revisionesCombinadas(any(), any(), any()) } returns emptyList()
        every { repositoryMock.obtenerEvidencia(any(), any(), any()) } returns EvidenciaEquipos()
        every { repositoryMock.etapaConfirmada(any(), any(), any()) } returns false

        val viewModelMock = EquiposViewModel(repository = repositoryMock)

        assertEquals(listOf("F-301", "F-302"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("PEDRO")
        assertEquals(listOf("F-302"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("lopez")
        assertEquals(listOf("F-301"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("MELINKA")
        assertEquals(listOf("F-301"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("redes")
        assertEquals(listOf("F-302"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("F-301")
        assertEquals(listOf("F-301"), viewModelMock.uiState.value.faenasFiltradas.map { it.id })

        viewModelMock.onBusquedaFaenaChange("sin coincidencias")
        assertTrue(viewModelMock.uiState.value.faenasFiltradas.isEmpty())

        viewModelMock.seleccionarFaena("F-301")
        assertEquals("", viewModelMock.uiState.value.busquedaFaena)
        assertEquals("F-301", viewModelMock.uiState.value.faenaSeleccionadaId)
    }
}
