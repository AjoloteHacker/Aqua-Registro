package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.util.PaisTelefono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BuzoViewModelFullCoverageTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: BuzoViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = BuzoViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filtrosYBusquedas() {
        viewModel.onSearchQueryChange("Carlos")
        assertEquals("Carlos", viewModel.uiState.value.searchQuery)
        assertFalse(viewModel.uiState.value.buzosFiltrados.isEmpty())

        viewModel.onFiltroSaludChange(EstadoSalud.APTO)
        assertEquals(EstadoSalud.APTO, viewModel.uiState.value.filtroSalud)

        viewModel.onFiltroSaludChange(null)
        assertNull(viewModel.uiState.value.filtroSalud)
    }

    @Test
    fun dialogosDetalleYEliminar() {
        val buzos = repository.buzos.value
        if (buzos.isNotEmpty()) {
            val buzo = buzos.first()
            viewModel.openDetalle(buzo)
            assertEquals(buzo.id, viewModel.uiState.value.buzoDetalle?.id)

            viewModel.closeDetalle()
            assertNull(viewModel.uiState.value.buzoDetalle)

            viewModel.solicitarEliminar(buzo)
            assertEquals(buzo.id, viewModel.uiState.value.buzoAEliminar?.id)

            viewModel.cancelarEliminar()
            assertNull(viewModel.uiState.value.buzoAEliminar)
        }
    }

    @Test
    fun inputsFormularioBuzo() {
        viewModel.openAddDialog()
        assertTrue(viewModel.uiState.value.showAddDialog)

        viewModel.onNuevoNombreChange("Juan Carlos Perez")
        assertEquals("Juan Carlos Perez", viewModel.uiState.value.nuevoNombre)

        viewModel.onNuevoRutChange("15.489.231-1")
        assertEquals("15.489.231-1", viewModel.uiState.value.nuevoRut)
        // Simular borrado de guión (longitud menor sin cambiar dígitos)
        viewModel.onNuevoRutChange("15.489.2311")
        assertNotNull(viewModel.uiState.value.nuevoRut)

        viewModel.onNuevoTelefonoChange("987654321")
        assertNotNull(viewModel.uiState.value.nuevoTelefono)
        // Simular borrado de espacio en teléfono
        val telPrev = viewModel.uiState.value.nuevoTelefono
        viewModel.onNuevoTelefonoChange(telPrev.dropLast(1))
        assertNotNull(viewModel.uiState.value.nuevoTelefono)

        viewModel.onPaisTelefonoChange(PaisTelefono.CHILE)
        assertEquals(PaisTelefono.CHILE, viewModel.uiState.value.paisTelefono)

        viewModel.onNuevaMatriculaChange("BC-999")
        assertEquals("BC-999", viewModel.uiState.value.nuevaMatricula)

        viewModel.onNuevaCategoriaChange("Buzo Especialista")
        assertEquals("Buzo Especialista", viewModel.uiState.value.nuevaCategoria)

        viewModel.onNuevaVigenciaChange("Por renovar (< 30 días)")
        assertEquals("Por renovar (< 30 días)", viewModel.uiState.value.nuevaVigencia)

        viewModel.onFechaVencimientoMatriculaChange(LocalDate.now().plusDays(40))
        assertNotNull(viewModel.uiState.value.nuevaFechaVencimientoMatricula)

        viewModel.onPesoChange("82.5")
        assertEquals(82.5f, viewModel.uiState.value.nuevoPeso)

        viewModel.onEstaturaChange("1.78")
        assertEquals(1.78f, viewModel.uiState.value.nuevaEstatura)

        viewModel.onTallaChange("43")
        assertEquals(43f, viewModel.uiState.value.nuevaTalla)

        viewModel.onNuevoPuntajeCalbucoChange("95")
        assertEquals("95", viewModel.uiState.value.nuevoPuntajeCalbuco)

        viewModel.onNuevasAlergiasChange("Penicilina")
        assertEquals("Penicilina", viewModel.uiState.value.nuevasAlergias)

        viewModel.onNuevaEdadChange(LocalDate.of(1992, 4, 10))
        assertEquals(LocalDate.of(1992, 4, 10), viewModel.uiState.value.nuevaEdad)

        viewModel.onCertificadoAchsChange("content://achs.pdf")
        assertEquals("content://achs.pdf", viewModel.uiState.value.nuevoCertificadoAchsUri)

        viewModel.onEmpresaSeleccionadaChange("AquaChile")
        assertEquals("AquaChile", viewModel.uiState.value.empresaSeleccionada)

        viewModel.closeAddDialog()
        assertFalse(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun dialogoNuevaEmpresa() {
        viewModel.openNuevaEmpresaDialog()
        assertTrue(viewModel.uiState.value.showNuevaEmpresaDialog)

        viewModel.onNuevaEmpresaNombreChange("Empresa Submarina Austral")
        assertEquals("Empresa Submarina Austral", viewModel.uiState.value.nuevaEmpresaNombre)

        viewModel.closeNuevaEmpresaDialog()
        assertFalse(viewModel.uiState.value.showNuevaEmpresaDialog)
    }

    @Test
    fun alternarAlertaNoInvasiva() {
        val buzos = repository.buzos.value
        if (buzos.isNotEmpty()) {
            viewModel.alternarAlertaNoInvasiva(buzos.first().id)
        }
    }

    @Test
    fun guardarBuzo_crearYEditarYValidaciones() {
        viewModel.openAddDialog()
        
        // Validación nombre vacío
        viewModel.onNuevoNombreChange("")
        viewModel.guardarBuzo()
        assertNotNull(viewModel.uiState.value.error)

        // Validación rut inválido
        viewModel.onNuevoNombreChange("Pedro Gomez")
        viewModel.onNuevoRutChange("111")
        viewModel.guardarBuzo()
        assertNotNull(viewModel.uiState.value.error)

        // Validación teléfono inválido
        viewModel.onNuevoRutChange("17.123.456-5")
        viewModel.onNuevoTelefonoChange("12")
        viewModel.guardarBuzo()
        assertNotNull(viewModel.uiState.value.error)

        // Guardar buzo nuevo con éxito
        viewModel.onNuevoTelefonoChange("987654321")
        viewModel.guardarBuzo()
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.showAddDialog)
        assertNotNull(viewModel.uiState.value.userMessage)

        // Editar buzo nuevo (sin tocar los buzos semilla)
        val buzoNuevo = repository.buzos.value.firstOrNull { it.rut == "17.123.456-5" }
        assertNotNull(buzoNuevo)
        viewModel.openEditDialog(buzoNuevo!!)
        assertTrue(viewModel.uiState.value.showAddDialog)
        assertEquals(buzoNuevo.id, viewModel.uiState.value.buzoAEditar?.id)

        viewModel.onNuevoNombreChange("Pedro Gomez Modificado")
        viewModel.guardarBuzo()
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun eliminarBuzo_flujoCompleto() {
        // 1. Buzo con faena en curso falla al eliminar
        val buzoConFaena = repository.buzos.value.firstOrNull { it.id == "3" }
        if (buzoConFaena != null) {
            viewModel.solicitarEliminar(buzoConFaena)
            viewModel.confirmarEliminar()
            assertNotNull(viewModel.uiState.value.error)
            viewModel.cancelarEliminar()
        }

        // 2. Eliminar el buzo de prueba si existe
        val buzoPrueba = repository.buzos.value.firstOrNull { it.rut == "17.123.456-5" }
        if (buzoPrueba != null) {
            viewModel.solicitarEliminar(buzoPrueba)
            assertEquals(buzoPrueba, viewModel.uiState.value.buzoAEliminar)

            viewModel.confirmarEliminar()
            assertNull(viewModel.uiState.value.buzoAEliminar)
            assertNotNull(viewModel.uiState.value.userMessage)

            viewModel.clearUserMessage()
            assertNull(viewModel.uiState.value.userMessage)
        }
    }

    @Test
    fun guardarNuevaEmpresa_flujoCompleto() {
        viewModel.openNuevaEmpresaDialog()
        viewModel.onNuevaEmpresaNombreChange("Empresa Test Nueva")
        viewModel.guardarNuevaEmpresa()
        assertFalse(viewModel.uiState.value.showNuevaEmpresaDialog)
        assertNotNull(viewModel.uiState.value.userMessage)
    }
}
