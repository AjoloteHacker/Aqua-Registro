package com.example.myapplication.ui.viewmodels

import org.junit.Assert.*
import org.junit.Test

class SaludViewModelTest {

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
        val state = SaludUiState(
            pesoKg = "82,5",
            talla = "1,78"
        )

        assertEquals(82.5, state.pesoVal ?: 0.0, 0.01)
        assertEquals(1.78, state.tallaVal ?: 0.0, 0.01)

        val imc = state.pesoVal!! / (state.tallaVal!! * state.tallaVal!!)
        assertTrue(imc in 25.0..27.0) // Sobrepeso leve
    }
}
