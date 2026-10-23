package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.SaludViewModel

private fun textoNumericoValido(texto: String, decimal: Boolean): Boolean =
    if (decimal) {
        texto.all { it.isDigit() || it == '.' || it == ',' } &&
                texto.count { it == '.' || it == ',' } <= 1
    } else {
        texto.all { it.isDigit() }
    }

@Composable
private fun CampoNumerico(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
    mostrarError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { nuevo ->
            if (textoNumericoValido(nuevo, decimal)) onValueChange(nuevo)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        isError = mostrarError,
        supportingText = { if (mostrarError) Text("Obligatorio") }
    )
}

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
        title = "Encuesta de Salud",
        subtitle = "Evaluación Biomédica y Aptitud",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
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
                text = "Faena a Evaluar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

            if (uiState.faenas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No hay faenas en curso. Autoriza una faena en «Registro de Faena» " +
                                "para registrar su encuesta de salud antes y después de la inmersión.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = AquaTextSecondary
                    )
                }
            } else {
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
                                text = uiState.faenaSeleccionada?.let { "🤿 ${it.id} · ${it.buzoNombre}" }
                                    ?: "Seleccionar faena",
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
                        uiState.faenas.forEach { f ->
                            DropdownMenuItem(
                                text = { Text("${f.id} · ${f.buzoNombre} - ${f.tipoFaena}") },
                                onClick = {
                                    viewModel.seleccionarFaena(f.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MomentoSalud.entries.forEach { m ->
                        FilterChip(
                            selected = uiState.momento == m,
                            onClick = { viewModel.seleccionarMomento(m) },
                            label = { Text(m.etiqueta, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaSecondary.copy(alpha = 0.2f),
                                selectedLabelColor = AquaPrimary
                            )
                        )
                    }
                }

                uiState.bloqueo?.let { motivo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SafetyCardBgDanger)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = SafetyDanger,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = motivo, fontSize = 13.sp, color = SafetyDanger)
                        }
                    }
                }

                uiState.encuestaRegistrada?.let { reg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (reg.esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Ya registrada (${reg.momento.etiqueta}): " +
                                        if (reg.esApto) "APTO" else "NO APTO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (reg.esApto) SafetySuccess else SafetyDanger
                            )
                            Text(
                                text = "PA ${reg.presionArterial} mmHg · ${reg.frecuenciaCardiaca} lpm · " +
                                        "${reg.registradaPor} · ${reg.registradaEn}",
                                fontSize = 11.sp,
                                color = AquaTextSecondary
                            )
                            if (uiState.bloqueo == null) {
                                Text(
                                    text = "Si registras una nueva, reemplaza a esta.",
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.faenaSeleccionada != null && uiState.bloqueo == null) {
                // Card de dictamen preventivo
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
                                    uiState.motivoNoApto,
                                fontSize = 12.sp,
                                color = AquaTextSecondary
                            )
                        }
                    }
                }

                // Sección 1: Signos vitales
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
                            text = "1. Signos Vitales",
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
                                singleLine = true,
                                isError = !uiState.presionValida,
                                supportingText = {
                                    if (!uiState.presionValida) Text("Ej: 120/80")
                                }
                            )
                            OutlinedTextField(
                                value = uiState.frecuenciaCardiaca,
                                onValueChange = { viewModel.onFrecuenciaChange(it) },
                                label = { Text("Pulso (lpm)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = !uiState.pulsoValido,
                                supportingText = {
                                    if (!uiState.pulsoValido) Text("50-100 lpm")
                                }
                            )
                        }
                    }
                }

                // Sección 2: Cuestionario de Declaración de Salud
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
                            pregunta = "¿Cuenta con Test de Esfuerzo vigente?",
                            checked = uiState.testEsfuerzo,
                            onCheckedChange = { viewModel.onTestEsfuerzoChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Libre de molestias o trastornos gastrointestinales?",
                            checked = uiState.gastroIntestinal,
                            onCheckedChange = { viewModel.onGastroIntestinalChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Posee evaluación cardiovascular aprobada y vigente?",
                            checked = uiState.cardiovascular,
                            onCheckedChange = { viewModel.onCardiovascularChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con examen de Audiometría vigente?",
                            checked = uiState.audiometria,
                            onCheckedChange = { viewModel.onAudiometriaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con examen de Optometría vigente?",
                            checked = uiState.optometria,
                            onCheckedChange = { viewModel.onOptometriaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con Electrocardiograma (ECG) al día?",
                            checked = uiState.electrocardiograma,
                            onCheckedChange = { viewModel.onElectrocardiogramaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
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
                            pregunta = "¿Se encuentra en condición física y anímica óptima para el buceo?",
                            checked = uiState.condicionOptima,
                            onCheckedChange = { viewModel.onCondicionOptimaChange(it) }
                        )
                    }
                }

                // Sección 3: Parámetros Antropométricos y Laboratorio
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
                            text = "3. Parámetros Antropométricos y Laboratorio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaPrimary
                        )

                        // Peso y talla
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampoNumerico(
                                value = uiState.pesoKg,
                                onValueChange = { viewModel.onPesoChange(it) },
                                label = "Peso (kg)",
                                modifier = Modifier.weight(1f),
                                decimal = true,
                                mostrarError = uiState.mostrarErrores && uiState.pesoVal == null
                            )
                            CampoNumerico(
                                value = uiState.talla,
                                onValueChange = { viewModel.onTallaChange(it) },
                                label = "Talla (m)",
                                modifier = Modifier.weight(1f),
                                decimal = true,
                                mostrarError = uiState.mostrarErrores && uiState.tallaVal == null
                            )
                        }

                        Text(
                            text = "Perfil Lipídico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )

                        CampoNumerico(
                            value = uiState.colesterol,
                            onValueChange = { viewModel.onColesterolChange(it) },
                            label = "Colesterol Total (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.colesterolVal == null
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampoNumerico(
                                value = uiState.hdl,
                                onValueChange = { viewModel.onHdlChange(it) },
                                label = "HDL (mg/dl)",
                                modifier = Modifier.weight(1f),
                                mostrarError = uiState.mostrarErrores && uiState.hdlVal == null
                            )
                            CampoNumerico(
                                value = uiState.ldl,
                                onValueChange = { viewModel.onLdlChange(it) },
                                label = "LDL (mg/dl)",
                                modifier = Modifier.weight(1f),
                                mostrarError = uiState.mostrarErrores && uiState.ldlVal == null
                            )
                        }

                        CampoNumerico(
                            value = uiState.trigliceridos,
                            onValueChange = { viewModel.onTrigliceridosChange(it) },
                            label = "Triglicéridos (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.trigliceridosVal == null
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        PreguntaSaludSwitch(
                            pregunta = "¿Hemograma dentro de parámetros normales?",
                            checked = uiState.hemograma,
                            onCheckedChange = { viewModel.onHemogramaChange(it) }
                        )

                        CampoNumerico(
                            value = uiState.glicemiaBasal,
                            onValueChange = { viewModel.onGlicemiaChange(it) },
                            label = "Glicemia basal (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.glicemiaVal == null
                        )
                    }
                }

                // Botón de emisión de dictamen
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