package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.components.RuedaSelectorCard
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.TrabajoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: TrabajoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var buzoDropdownOpen by remember { mutableStateOf(false) }
    var socorristaDropdownOpen by remember { mutableStateOf(false) }
    var centroDropdownOpen by remember { mutableStateOf(false) }
    var faenaDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Registro de Faena Submarina",
        subtitle = "Planificación AST y Autorización",
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
                        text = "1. Tripulación: 2 buzos por faena",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

                    Text("Buzo ejecutor (inmersión):", fontSize = 12.sp, color = AquaTextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { buzoDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.buzoSeleccionado?.let { "${it.nombre} (${it.empresa})" } ?: "Seleccionar buzo ejecutor",
                                    color = AquaTextPrimary,
                                    fontSize = 13.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = buzoDropdownOpen,
                            onDismissRequest = { buzoDropdownOpen = false }
                        ) {
                            uiState.buzos.forEach { buzo ->
                                DropdownMenuItem(
                                    text = { Text("${buzo.nombre} - [${buzo.estadoSalud.label}]") },
                                    onClick = {
                                        viewModel.seleccionarBuzo(buzo.id)
                                        buzoDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.buzoSeleccionado?.matriculaVencida == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = SafetyDanger, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Advertencia: ${uiState.buzoSeleccionado?.nombre} tiene la matrícula vencida.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyDanger
                            )
                        }
                    }

                    Text("Buzo socorrista (secundario):", fontSize = 12.sp, color = AquaTextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { socorristaDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.socorristaSeleccionado?.let { "${it.nombre} (${it.empresa})" }
                                        ?: "Seleccionar socorrista",
                                    color = AquaTextPrimary,
                                    fontSize = 13.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = socorristaDropdownOpen,
                            onDismissRequest = { socorristaDropdownOpen = false }
                        ) {
                            uiState.socorristasDisponibles.forEach { buzo ->
                                DropdownMenuItem(
                                    text = { Text("${buzo.nombre} - [${buzo.estadoSalud.label}]") },
                                    onClick = {
                                        viewModel.seleccionarSocorrista(buzo.id)
                                        socorristaDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.socorristaSeleccionado?.matriculaVencida == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = SafetyDanger, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Advertencia: ${uiState.socorristaSeleccionado?.nombre} tiene la matrícula vencida.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyDanger
                            )
                        }
                    }

                    if (uiState.tripulacionCompleta) {
                        Surface(
                            color = if (uiState.esBuzoApto && uiState.esSocorristaApto) SafetyCardBgSuccess else SafetyCardBgDanger,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.esBuzoApto && uiState.esSocorristaApto) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.esBuzoApto && uiState.esSocorristaApto) SafetySuccess else SafetyDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.esBuzoApto && uiState.esSocorristaApto) {
                                        "Tripulación completa: ambos buzos con chequeo al día."
                                    } else {
                                        "Uno o ambos buzos no están aptos. La encuesta de salud de la faena definirá el dictamen."
                                    },
                                    fontSize = 12.sp,
                                    color = if (uiState.esBuzoApto && uiState.esSocorristaApto) SafetySuccess else SafetyDanger,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Debe asignar dos buzos distintos: el ejecutor y su socorrista.",
                            fontSize = 12.sp,
                            color = SafetyDanger,
                            fontWeight = FontWeight.SemiBold
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
                        text = "2. Parámetros Operativos de Inmersión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

                    Text("Centro de Operación / Cultivo:", fontSize = 12.sp, color = AquaTextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { centroDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(uiState.centroCultivo, fontSize = 13.sp, color = AquaTextPrimary)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = centroDropdownOpen,
                            onDismissRequest = { centroDropdownOpen = false }
                        ) {
                            uiState.centrosDisponibles.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        viewModel.onCentroChange(c)
                                        centroDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Tipo de Faena Submarina:", fontSize = 12.sp, color = AquaTextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { faenaDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(uiState.tipoFaena, fontSize = 13.sp, color = AquaTextPrimary)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = faenaDropdownOpen,
                            onDismissRequest = { faenaDropdownOpen = false }
                        ) {
                            uiState.tiposFaenaDisponibles.forEach { tf ->
                                DropdownMenuItem(
                                    text = { Text(tf) },
                                    onClick = {
                                        viewModel.onTipoFaenaChange(tf)
                                        faenaDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    val opcionesProfundidad = remember { (5..60).map { it.toString() } }
                    val opcionesTiempoMin = remember { (10..90).map { it.toString() } }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RuedaSelectorCard(
                            titulo = "Profundidad",
                            unidad = "m",
                            icono = Icons.Default.Water,
                            opciones = opcionesProfundidad,
                            valorSeleccionado = uiState.profundidad.ifBlank { "18" },
                            onValorChange = { viewModel.onProfundidadChange(it) },
                            modifier = Modifier.weight(1f)
                        )
                        RuedaSelectorCard(
                            titulo = "Tiempo máx.",
                            unidad = "min",
                            icono = Icons.Default.Timer,
                            opciones = opcionesTiempoMin,
                            valorSeleccionado = uiState.duracionMinutos.ifBlank { "40" },
                            onValorChange = { viewModel.onDuracionChange(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        color = if (uiState.supervisorActivo) Color.White else SafetyCardBgDanger,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.supervisorActivo) Icons.Default.Person else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uiState.supervisorActivo) AquaPrimary else SafetyDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Supervisor de la faena (según la sesión)",
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary
                                )
                                Text(
                                    text = if (uiState.supervisorActivo) uiState.supervisor
                                    else "Inicie sesión como prevencionista para autorizar.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.supervisorActivo) AquaTextPrimary else SafetyDanger
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Surface(
                    color = SafetyCardBgDanger,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = SafetyDanger,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.autorizarFaena() },
                enabled = uiState.puedeAutorizar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AquaPrimary,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        !uiState.supervisorActivo -> "INICIE SESIÓN COMO PREVENCIONISTA"
                        !uiState.tripulacionCompleta -> "SELECCIONE EJECUTOR Y SOCORRISTA DISTINTOS"
                        else -> "AUTORIZAR E INICIAR FAENA"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.successDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSuccessDialog() },
                title = { Text("¡Faena Autorizada!", fontWeight = FontWeight.Bold, color = SafetySuccess) },
                text = {
                    Text(
                        "Faena ${uiState.faenaAutorizadaId.orEmpty()} de ${uiState.buzoSeleccionado?.nombre} " +
                            "con socorrista ${uiState.socorristaSeleccionado?.nombre} registrada en la bitácora central. " +
                            "Continúe con el control de equipos de ambos buzos."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.irAControlDeEquipos()
                            viewModel.dismissSuccessDialog()
                            navController.navigate(Screen.Equipos.route) { launchSingleTop = true }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Ir a Control de Equipos")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissSuccessDialog()
                            navController.navigate(Screen.Historial.route)
                        }
                    ) {
                        Text("Ver Historial")
                    }
                }
            )
        }
    }
}
