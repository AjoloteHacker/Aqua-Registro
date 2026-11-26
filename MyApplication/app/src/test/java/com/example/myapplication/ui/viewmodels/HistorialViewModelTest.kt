package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.MotivoCierreSinTerminar
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
class HistorialViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HistorialViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = HistorialViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tabsYFiltros() {
        viewModel.onTabChange(1)
        assertEquals(1, viewModel.uiState.value.selectedTab)

        viewModel.onSearchQueryChange("Carlos")
        assertEquals("Carlos", viewModel.uiState.value.searchQuery)
        assertNotNull(viewModel.uiState.value.faenasFiltradas)
        assertNotNull(viewModel.uiState.value.chequeosFiltrados)

        viewModel.onFilterChange("En Curso")
        assertEquals("En Curso", viewModel.uiState.value.selectedFilter)

        viewModel.onFilterChange("Completada")
        assertEquals("Completada", viewModel.uiState.value.selectedFilter)

        viewModel.onFilterChange("Sin Terminar")
        assertEquals("Sin Terminar", viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun dialogoRevisionYEdicion() {
        val faenas = repository.faenas.value
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.abrirRevision(f.id)
            assertEquals(f.id, viewModel.uiState.value.faenaEnRevisionId)
            viewModel.cerrarRevision()
            assertNull(viewModel.uiState.value.faenaEnRevisionId)

            viewModel.abrirEdicion(f.id)
            assertEquals(f.id, viewModel.uiState.value.editandoFaenaId)
            viewModel.editarDatos(
                faenaId = f.id,
                centro = "Centro Melinka 1",
                tipo = "Inspección",
                profundidad = "20",
                minutos = "40"
            )
            viewModel.cerrarEdicion()
            assertNull(viewModel.uiState.value.editandoFaenaId)

            viewModel.aprobarDatos(f.id)
            viewModel.revocarAprobacion(f.id)
            viewModel.irAEquipos(f.id)
            viewModel.irASalud(f.id)
            viewModel.finalizarFaena(f.id)
            viewModel.clearUserMessage()
            assertNull(viewModel.uiState.value.userMessage)
        }
    }

    @Test
    fun bitacoraPostInmersionYVelocidades() {
        val faenas = repository.faenas.value
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.abrirBitacoraPostInmersion(f.id)
            assertEquals(f.id, viewModel.uiState.value.registrandoBitacoraFaenaId)

            viewModel.registrarBitacoraPostInmersion(
                faenaId = f.id,
                profundidad = "25",
                tiempoFondo = "45",
                velocidadDescenso = "20.5",
                velocidadAscenso = "8.0",
                origenDato = "Census",
                observaciones = "Prueba velocidad"
            )
            viewModel.cerrarBitacoraPostInmersion()
            assertNull(viewModel.uiState.value.registrandoBitacoraFaenaId)
        }
    }

    @Test
    fun cierreSinTerminarYComentarios() {
        val faenas = repository.faenas.value
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.abrirCierreSinTerminar(f.id)
            assertEquals(f.id, viewModel.uiState.value.cerrandoSinTerminarFaenaId)

            viewModel.confirmarCierreSinTerminar(
                faenaId = f.id,
                motivosSeleccionados = setOf(MotivoCierreSinTerminar.MAL_TIEMPO),
                otrosMotivos = emptyList(),
                comentario = "Oleaje fuerte"
            )
            viewModel.cerrarDialogoSinTerminar()
            assertNull(viewModel.uiState.value.cerrandoSinTerminarFaenaId)

            viewModel.abrirEditarComentarioCierre(f.id)
            viewModel.guardarComentarioCierre(f.id, "Nuevo comentario de cierre")
            viewModel.cerrarEditarComentarioCierre()
            assertNull(viewModel.uiState.value.editandoComentarioFaenaId)
        }
    }
}
