package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.MomentoSalud
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
class SaludViewModelFullCoverageTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SaludViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        repository.login("12.345.678-5", "admin123")
        viewModel = SaludViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun seleccionFaenaMomentoYBuzo() {
        val faenas = repository.faenas.value.filter { it.enCurso }
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            viewModel.seleccionarFaena(f.id)
            assertEquals(f.id, viewModel.uiState.value.faenaSeleccionadaId)

            viewModel.seleccionarMomento(MomentoSalud.DESPUES)
            assertEquals(MomentoSalud.DESPUES, viewModel.uiState.value.momento)

            viewModel.seleccionarBuzoEvaluado(f.buzoId)
            assertEquals(f.buzoId, viewModel.uiState.value.buzoEvaluadoId)
        }
    }

    @Test
    fun mutadoresValoresBiomedicosYEncuesta() {
        viewModel.onEdadBuzoChange("36")
        assertEquals("36", viewModel.uiState.value.edadBuzo)

        viewModel.onPuntajeCalbucoChange("95")
        assertEquals("95", viewModel.uiState.value.puntajeCalbuco)

        viewModel.onExamenPreocupacionalChange(true)
        assertTrue(viewModel.uiState.value.examenPreocupacional)

        viewModel.onCompresorCumpleChange(true)
        assertTrue(viewModel.uiState.value.compresorCumple)

        viewModel.onBuzoEmergenciaCumpleChange(true)
        assertTrue(viewModel.uiState.value.buzoEmergenciaCumple)

        viewModel.onPresionChange("120/80")
        assertEquals("120/80", viewModel.uiState.value.presionArterial)

        viewModel.onFrecuenciaChange("72")
        assertEquals("72", viewModel.uiState.value.frecuenciaCardiaca)

        viewModel.onColesterolChange("180")
        assertEquals("180", viewModel.uiState.value.colesterol)

        viewModel.onHdlChange("50")
        assertEquals("50", viewModel.uiState.value.hdl)

        viewModel.onLdlChange("100")
        assertEquals("100", viewModel.uiState.value.ldl)

        viewModel.onTrigliceridosChange("120")
        assertEquals("120", viewModel.uiState.value.trigliceridos)

        viewModel.onHemogramaChange(true)
        assertTrue(viewModel.uiState.value.hemograma)

        viewModel.onGlicemiaChange("90")
        assertEquals("90", viewModel.uiState.value.glicemiaBasal)

        viewModel.onTestEsfuerzoChange(true)
        viewModel.onGastroIntestinalChange(true)
        viewModel.onCardiovascularChange(true)
        viewModel.onAudiometriaChange(true)
        viewModel.onOptometriaChange(true)
        viewModel.onElectrocardiogramaChange(true)
        viewModel.onDescansoChange(true)
        viewModel.onSinSustanciasChange(true)
        viewModel.onSinCongestionChange(true)
        viewModel.onCondicionOptimaChange(true)

        assertTrue(viewModel.uiState.value.cuestionarioOk)
        assertTrue(viewModel.uiState.value.esApto)

        viewModel.guardarDictamen()
        assertNotNull(viewModel.uiState.value.userMessage)

        viewModel.clearUserMessage()
        assertNull(viewModel.uiState.value.userMessage)
    }
}
