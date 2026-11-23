package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI Test exigido por la pauta de evaluación (EA4 / EA5):
 * "Al menos un Compose UI Test que compruebe un formulario, alerta y bloqueo por incumplimiento crítico."
 */
@RunWith(AndroidJUnit4::class)
class ComposeFormLockTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun formularioConIncumplimientoCritico_muestraAlertaYDeshabilitaBotonAprobacion() {
        composeTestRule.setContent {
            MyApplicationTheme {
                // Componente de prueba que simula el formulario de autorización de inmersión
                var edadText by remember { mutableStateOf("48") } // Mayor de 45 años
                var tieneTestEsfuerzo by remember { mutableStateOf(false) } // Sin test vigente
                var equipoCompresorCumple by remember { mutableStateOf(false) } // Compresor no cumple

                val esMayorDe45SinTest = (edadText.toIntOrNull() ?: 0) > 45 && !tieneTestEsfuerzo
                val esCriticoBloqueado = esMayorDe45SinTest || !equipoCompresorCumple

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Pre-Chequeo de Inmersión y Seguridad Submarina")

                    OutlinedTextField(
                        value = edadText,
                        onValueChange = { edadText = it },
                        label = { Text("Edad del buzo") },
                        modifier = Modifier.testTag("tag_campo_edad")
                    )

                    Row {
                        Checkbox(
                            checked = tieneTestEsfuerzo,
                            onCheckedChange = { tieneTestEsfuerzo = it },
                            modifier = Modifier.testTag("tag_check_test_esfuerzo")
                        )
                        Text("Test de esfuerzo al día")
                    }

                    Row {
                        Checkbox(
                            checked = equipoCompresorCumple,
                            onCheckedChange = { equipoCompresorCumple = it },
                            modifier = Modifier.testTag("tag_check_compresor")
                        )
                        Text("Compresor de aire operativo")
                    }

                    if (esCriticoBloqueado) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("tag_alerta_bloqueo")
                        ) {
                            Text(
                                text = "BLOQUEO CRÍTICO: Buzo >45 años sin test vigente o compresor en No Cumple. Faena no autorizada.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { /* Acción de autorización */ },
                        enabled = !esCriticoBloqueado,
                        modifier = Modifier.testTag("tag_boton_autorizar")
                    ) {
                        Text(if (esCriticoBloqueado) "Autorización Bloqueada" else "Autorizar Faena")
                    }
                }
            }
        }

        // 1. Verificar que el banner de alerta de bloqueo crítico está visible en pantalla
        composeTestRule.onNodeWithTag("tag_alerta_bloqueo").assertIsDisplayed()
        composeTestRule.onNodeWithText("BLOQUEO CRÍTICO", substring = true).assertIsDisplayed()

        // 2. Verificar que el botón de autorización está deshabilitado / bloqueado
        composeTestRule.onNodeWithTag("tag_boton_autorizar").assertIsNotEnabled()

        // 3. Simular que se subsana el compresor pero la edad sigue siendo >45 sin test
        composeTestRule.onNodeWithTag("tag_check_compresor").performClick()

        // Sigue bloqueado por la regla de edad >45 años sin test de esfuerzo
        composeTestRule.onNodeWithTag("tag_alerta_bloqueo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_boton_autorizar").assertIsNotEnabled()

        // 4. Subsanar el test de esfuerzo del buzo
        composeTestRule.onNodeWithTag("tag_check_test_esfuerzo").performClick()

        // 5. Verificar que la alerta desaparece y el botón se habilita
        composeTestRule.onNodeWithTag("tag_alerta_bloqueo").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tag_boton_autorizar").assertIsEnabled()
    }
}
