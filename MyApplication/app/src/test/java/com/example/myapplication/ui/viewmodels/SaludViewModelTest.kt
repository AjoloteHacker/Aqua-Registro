package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.AvanceBuzo
import com.example.myapplication.data.model.AvanceFaena
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.EncuestaSalud
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.data.repository.SolicitudRevision
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SaludViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun signosVitales_rangoNormal_resultaApto() {
        val state = SaludUiState(
            presionArterial = "120/80",
            frecuenciaCardiaca = "72",
            testEsfuerzo = true,
            descansoAdecuado = true,
            sinSustancias = true,
            sinCongestion = true,
            condicionOptima = true
        )

        assertTrue(state.pulsoValido)
        assertTrue(state.presionValida)
        assertTrue(state.signosEnRango)
        assertTrue(state.cuestionarioOk)
        assertTrue(state.esApto)
    }

    @Test
    fun pulsoFueraDeRango_resultaNoApto() {
        val stateBradicardia = SaludUiState(frecuenciaCardiaca = "44") // < 50
        assertFalse(stateBradicardia.pulsoValido)
        assertFalse(stateBradicardia.esApto)

        val stateTaquicardia = SaludUiState(frecuenciaCardiaca = "115") // > 100
        assertFalse(stateTaquicardia.pulsoValido)
        assertFalse(stateTaquicardia.esApto)
    }

    @Test
    fun presionArterialHipertensa_resultaNoApto() {
        val stateHipertension = SaludUiState(
            presionArterial = "145/95", // Sistólica > 139, Diastólica > 89
            frecuenciaCardiaca = "75"
        )

        assertFalse(stateHipertension.presionValida)
        assertFalse(stateHipertension.signosEnRango)
        assertFalse(stateHipertension.esApto)
        assertTrue(stateHipertension.motivoNoApto.contains("presión arterial y pulso"))
    }

    @Test
    fun cuestionarioConFactoresDeRiesgo_resultaNoApto() {
        val stateRiesgo = SaludUiState(
            presionArterial = "120/80",
            frecuenciaCardiaca = "70",
            sinCongestion = false // Congestión nasal o pulmonar (riesgo crítico de barotrauma de oído/senos)
        )

        assertFalse(stateRiesgo.cuestionarioOk)
        assertFalse(stateRiesgo.esApto)
        assertTrue(stateRiesgo.motivoNoApto.contains("cuestionario físico"))
    }

    @Test
    fun ruedaSelectora_calculaValoresNumericosDePesoYTalla() {
        val buzoState = BuzoUiState(
            nuevoPeso = 82.5f,
            nuevaEstatura = 1.78f,
            nuevaTalla = 42f
        )

        assertEquals(82.5f, buzoState.nuevoPeso ?: 0f, 0.01f)
        assertEquals(1.78f, buzoState.nuevaEstatura ?: 0f, 0.01f)
        assertEquals(42f, buzoState.nuevaTalla ?: 0f, 0.01f)

        val imc = buzoState.nuevoPeso!! / (buzoState.nuevaEstatura!! * buzoState.nuevaEstatura!!)
        assertTrue(imc in 25.0f..27.0f) // Sobrepeso leve

        val saludState = SaludUiState(
            colesterol = "185",
            hdl = "52",
            ldl = "110",
            trigliceridos = "128",
            glicemiaBasal = "91"
        )
        assertEquals(185, saludState.colesterolVal)
        assertEquals(52, saludState.hdlVal)
        assertEquals(110, saludState.ldlVal)
        assertEquals(128, saludState.trigliceridosVal)
        assertEquals(91, saludState.glicemiaVal)
    }

    @Test
    fun buzoMayor45SinTestEsfuerzo_disparaBloqueoYSemaforoRojo() {
        val state = SaludUiState(
            edadBuzo = "47",
            testEsfuerzo = false,
            presionArterial = "120/80",
            frecuenciaCardiaca = "72"
        )

        assertTrue(state.bloqueoMayor45SinTest)
        assertFalse(state.esApto)
        assertEquals(ColorSemaforo.ROJO, state.semaforo)
        assertTrue(state.motivosBloqueo.any { it.contains(">45") && it.contains("Test de Esfuerzo") })
    }

    @Test
    fun compresorOBuzoEmergenciaEnNoCumple_disparaBloqueoYSemaforoRojo() {
        val stateCompresorFalla = SaludUiState(
            compresorCumple = false
        )
        assertTrue(stateCompresorFalla.bloqueoEquiposCriticos)
        assertFalse(stateCompresorFalla.esApto)
        assertEquals(ColorSemaforo.ROJO, stateCompresorFalla.semaforo)

        val stateBuzoEmergenciaFalla = SaludUiState(
            buzoEmergenciaCumple = false
        )
        assertTrue(stateBuzoEmergenciaFalla.bloqueoEquiposCriticos)
        assertFalse(stateBuzoEmergenciaFalla.esApto)
        assertEquals(ColorSemaforo.ROJO, stateBuzoEmergenciaFalla.semaforo)
    }

    @Test
    fun puntajeCalbucoBajoSinBloqueoCritico_resultaSemaforoAmarillo() {
        val state = SaludUiState(
            edadBuzo = "38",
            testEsfuerzo = true,
            puntajeCalbuco = "65", // < 70
            presionArterial = "120/80",
            frecuenciaCardiaca = "70"
        )

        assertTrue(state.esApto)
        assertEquals(ColorSemaforo.AMARILLO, state.semaforo)
        assertTrue(state.advertenciasSemaforo.any { it.contains("Calbuco") })
    }

    @Test
    fun saludViewModel_conMockK_detectaBuzoMayor45SinTestYRegistraBloqueoSemaforoRojo() {
        val repositoryMock = mockk<AquaRepository>(relaxed = true)
        val buzoMayorSinTest = Buzo(
            id = "2",
            rut = "17.912.453-K",
            nombre = "Pedro Almonacid Vera",
            empresa = "AquaSub",
            matricula = "Buzo Básico N° 5812",
            edad = LocalDate.now().minusYears(47),
            testEsfuerzoVigente = false,
            puntajeCalbuco = 65
        )
        val faenaActiva = RegistroFaena(
            id = "F-102",
            buzoId = "2",
            buzoNombre = "Pedro Almonacid Vera",
            socorristaId = "1",
            socorristaNombre = "Carlos Soto Paredes",
            empresa = "AquaSub",
            centroCultivo = "Centro Chiloé Norte",
            tipoFaena = "Inspección",
            profundidadMetros = 20,
            tiempoMinutos = 30,
            fechaHora = "24/11/2026 10:00"
        )

        every { repositoryMock.faenas } returns MutableStateFlow(listOf(faenaActiva))
        every { repositoryMock.buzos } returns MutableStateFlow(listOf(buzoMayorSinTest))
        every { repositoryMock.encuestas } returns MutableStateFlow(emptyMap())
        every { repositoryMock.revisiones } returns MutableStateFlow(emptyMap())
        every { repositoryMock.etapasConfirmadas } returns MutableStateFlow(emptySet())
        every { repositoryMock.historialMedico } returns MutableStateFlow(emptyList<Chequeo>())
        every { repositoryMock.solicitudRevision } returns MutableStateFlow<SolicitudRevision?>(null)
        every { repositoryMock.prevencionistaActivo } returns MutableStateFlow(
            Prevencionista("Cristóbal Loncón", "12.345.678-5", "c@aquachile.com", "PR-10892-X", "Melinka")
        )
        every { repositoryMock.avanceDe(any()) } returns AvanceFaena(
            buzos = mapOf("2" to AvanceBuzo(buzo = buzoMayorSinTest))
        )
        every { repositoryMock.obtenerFechaHoraActual() } returns "24/11/2026 10:15"
        every {
            repositoryMock.registrarEncuestaSalud(
                faenaId = any(),
                momento = any(),
                esApto = any(),
                presionArterial = any(),
                frecuenciaCardiaca = any(),
                semaforo = any(),
                motivosBloqueo = any(),
                advertencias = any(),
                buzoEvaluadoId = any(),
                edadBuzo = any(),
                testEsfuerzoVigente = any(),
                examenPreocupacionalVigente = any(),
                puntajeCalbuco = any(),
                compresorCumple = any(),
                buzoEmergenciaCumple = any()
            )
        } returns Result.success(
            EncuestaSalud(
                faenaId = "F-102",
                momento = MomentoSalud.ANTES,
                esApto = false,
                presionArterial = "120/80",
                frecuenciaCardiaca = 72,
                registradaPor = "Cristóbal Loncón",
                registradaEn = "24/11/2026 10:15",
                semaforo = ColorSemaforo.ROJO
            )
        )
        every { repositoryMock.registrarChequeoMedico(any()) } just runs

        val viewModel = SaludViewModel(repository = repositoryMock)

        assertEquals("47", viewModel.uiState.value.edadBuzo)
        assertFalse(viewModel.uiState.value.testEsfuerzo)
        assertTrue(viewModel.uiState.value.bloqueoMayor45SinTest)
        assertEquals(ColorSemaforo.ROJO, viewModel.uiState.value.semaforo)

        viewModel.guardarDictamen()

        verify(exactly = 1) {
            repositoryMock.registrarEncuestaSalud(
                faenaId = "F-102",
                momento = MomentoSalud.ANTES,
                esApto = false,
                presionArterial = "120/80",
                frecuenciaCardiaca = 72,
                semaforo = ColorSemaforo.ROJO,
                motivosBloqueo = any(),
                advertencias = any(),
                buzoEvaluadoId = "2",
                edadBuzo = 47,
                testEsfuerzoVigente = false,
                examenPreocupacionalVigente = true,
                puntajeCalbuco = 65,
                compresorCumple = true,
                buzoEmergenciaCumple = true
            )
        }
        verify(exactly = 1) { repositoryMock.registrarChequeoMedico(any()) }
    }

    @Test
    fun busquedaDeFaenas_filtraPorCamposYSeLimpiaAlSeleccionar() {
        val repositoryMock = mockk<AquaRepository>(relaxed = true)
        val melinka = RegistroFaena(
            id = "F-401",
            buzoId = "B-1",
            buzoNombre = "José Muñoz",
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
            id = "F-402",
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
        every { repositoryMock.buzos } returns MutableStateFlow(emptyList<Buzo>())
        every { repositoryMock.encuestas } returns MutableStateFlow(emptyMap())
        every { repositoryMock.revisiones } returns MutableStateFlow(emptyMap())
        every { repositoryMock.etapasConfirmadas } returns MutableStateFlow(emptySet())
        every { repositoryMock.historialMedico } returns MutableStateFlow(emptyList<Chequeo>())
        every { repositoryMock.solicitudRevision } returns MutableStateFlow<SolicitudRevision?>(null)
        every { repositoryMock.avanceDe(any()) } returns AvanceFaena()

        val viewModel = SaludViewModel(repository = repositoryMock)

        assertEquals(listOf("F-401", "F-402"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("MUNOZ")
        assertEquals(listOf("F-401"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("lopez")
        assertEquals(listOf("F-401"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("CHILOE")
        assertEquals(listOf("F-402"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("redes")
        assertEquals(listOf("F-402"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("F-401")
        assertEquals(listOf("F-401"), viewModel.uiState.value.faenasFiltradas.map { it.id })

        viewModel.onBusquedaFaenaChange("no existe")
        assertTrue(viewModel.uiState.value.faenasFiltradas.isEmpty())

        viewModel.seleccionarFaena("F-402")
        assertEquals("", viewModel.uiState.value.busquedaFaena)
        assertEquals("F-402", viewModel.uiState.value.faenaSeleccionadaId)
    }
}
