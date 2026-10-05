package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.SaludViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaludScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: SaludViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Encuesta de Salud Pre-Buceo",
        subtitle = "Evaluación Biomédica y Aptitud",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AquaBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Buzo a Evaluar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.buzoSeleccionado?.let { "🤿 ${it.nombre} (${it.rut})" } ?: "Seleccionar buzo",
                            color = AquaTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                    }
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    uiState.buzos.forEach { bz ->
                        DropdownMenuItem(
                            text = { Text("${bz.nombre} - ${bz.matricula}") },
                            onClick = {
                                viewModel.seleccionarBuzo(bz.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.esApto) Icons.Default.VerifiedUser else Icons.Default.WarningAmber,
                        contentDescription = "Estado Apto",
                        tint = if (uiState.esApto) SafetySuccess else SafetyDanger,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.esApto) "ESTADO: APTO PARA INMERSIÓN" else "ESTADO: NO APTO (ALERTA)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (uiState.esApto) SafetySuccess else SafetyDanger
                        )
                        Text(
                            text = if (uiState.esApto)
                                "El buzo cumple con todos los parámetros fisiológicos para faena."
                            else
                                "Existen factores de riesgo físico. Se prohíbe el buceo hasta normalización.",
                            fontSize = 12.sp,
                            color = AquaTextSecondary
                        )
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Signos Vitales Previos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.presionArterial,
                            onValueChange = { viewModel.onPresionChange(it) },
                            label = { Text("Presión (mmHg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.frecuenciaCardiaca,
                            onValueChange = { viewModel.onFrecuenciaChange(it) },
                            label = { Text("Pulso (lpm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "2. Cuestionario de Declaración de Salud",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

                    PreguntaSaludSwitch(
                        pregunta = "¿Durmió mínimo 7 a 8 horas continuas de descanso?",
                        checked = uiState.descansoAdecuado,
                        onCheckedChange = { viewModel.onDescansoChange(it) }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de consumo de alcohol o medicamentos las últimas 12 hrs?",
                        checked = uiState.sinSustancias,
                        onCheckedChange = { viewModel.onSinSustanciasChange(it) }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de congestión nasal, dolor de oídos o mareos?",
                        checked = uiState.sinCongestion,
                        onCheckedChange = { viewModel.onSinCongestionChange(it) }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Se siente en condiciones físicas y anímicas óptimas?",
                        checked = uiState.condicionOptima,
                        onCheckedChange = { viewModel.onCondicionOptimaChange(it) }
                    )
                }
            }

            Button(
                onClick = { viewModel.guardarDictamen() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.esApto) AquaPrimary else SafetyDanger
                )
            ) {
                Text(
                    text = if (uiState.esApto) "GUARDAR Y EMITIR DICTAMEN: APTO" else "GUARDAR DICTAMEN: NO APTO",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun PreguntaSaludSwitch(
    pregunta: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = pregunta,
            fontSize = 13.sp,
            color = AquaTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AquaSecondary
            )
        )
    }
}
