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
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.EstadoSalud
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaludScreen(navController: NavController) {
    var buzoIndex by remember { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val buzoActual = AppData.buzos.getOrNull(buzoIndex)

    var presionArterial by remember { mutableStateOf("120/80") }
    var frecuenciaCardiaca by remember { mutableStateOf("72") }

    var descansoAdecuado by remember { mutableStateOf(true) }
    var sinSustancias by remember { mutableStateOf(true) }
    var sinCongestion by remember { mutableStateOf(true) }
    var condicionOptima by remember { mutableStateOf(true) }

    val esApto = descansoAdecuado && sinSustancias && sinCongestion && condicionOptima

    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensajeResultado) {
        mensajeResultado?.let {
            snackbarHostState.showSnackbar(it)
            mensajeResultado = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuesta de Salud Pre-Buceo", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AquaPrimary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            // Selector de Buzo
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
                            text = buzoActual?.let { "🤿 ${it.nombre} (${it.rut})" } ?: "Seleccionar buzo",
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
                    AppData.buzos.forEachIndexed { idx, bz ->
                        DropdownMenuItem(
                            text = { Text("${bz.nombre} - ${bz.matricula}") },
                            onClick = {
                                buzoIndex = idx
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Banner Dinámico Apto / No Apto
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esApto) Icons.Default.VerifiedUser else Icons.Default.WarningAmber,
                        contentDescription = "Estado Apto",
                        tint = if (esApto) SafetySuccess else SafetyDanger,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (esApto) "ESTADO: APTO PARA INMERSIÓN" else "ESTADO: NO APTO (ALERTA)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (esApto) SafetySuccess else SafetyDanger
                        )
                        Text(
                            text = if (esApto)
                                "El buzo cumple con todos los parámetros fisiológicos para faena."
                            else
                                "Existen factores de riesgo físico. Se prohíbe el buceo hasta normalización.",
                            fontSize = 12.sp,
                            color = AquaTextSecondary
                        )
                    }
                }
            }

            // Sección 1: Parámetros Fisiológicos
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
                            value = presionArterial,
                            onValueChange = { presionArterial = it },
                            label = { Text("Presión (mmHg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = frecuenciaCardiaca,
                            onValueChange = { frecuenciaCardiaca = it },
                            label = { Text("Pulso (lpm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Sección 2: Cuestionario de Condición Pre-Buceo
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
                        checked = descansoAdecuado,
                        onCheckedChange = { descansoAdecuado = it }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de consumo de alcohol o medicamentos las últimas 12 hrs?",
                        checked = sinSustancias,
                        onCheckedChange = { sinSustancias = it }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de congestión nasal, dolor de oídos o mareos?",
                        checked = sinCongestion,
                        onCheckedChange = { sinCongestion = it }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    PreguntaSaludSwitch(
                        pregunta = "¿Se siente en condiciones físicas y anímicas óptimas?",
                        checked = condicionOptima,
                        onCheckedChange = { condicionOptima = it }
                    )
                }
            }

            // Botón de Registro
            Button(
                onClick = {
                    buzoActual?.let { buzo ->
                        val nuevoEstado = if (esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO
                        buzo.estadoSalud = nuevoEstado
                        buzo.ultimoChequeo = "Hoy - Presión: $presionArterial, Pulso: $frecuenciaCardiaca"
                        mensajeResultado = if (esApto)
                            "Chequeo registrado: ${buzo.nombre} declarado APTO"
                        else
                            "Alerta: ${buzo.nombre} registrado como NO APTO. Notificado a supervisor."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esApto) AquaPrimary else SafetyDanger
                )
            ) {
                Text(
                    text = if (esApto) "GUARDAR Y EMITIR DICTAMEN: APTO" else "GUARDAR DICTAMEN: NO APTO",
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
