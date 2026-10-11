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
                        text = "1. Buzo Asignado a la Inmersión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

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
                                    text = uiState.buzoSeleccionado?.let { "${it.nombre} (${it.empresa})" } ?: "Seleccionar buzo",
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

                    if (uiState.esBuzoApto) {
                        Surface(
                            color = SafetyCardBgSuccess,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Apto", tint = SafetySuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buzo Apto para inmersión (Chequeo de salud al día)",
                                    fontSize = 12.sp,
                                    color = SafetySuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = SafetyCardBgDanger,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = SafetyDanger, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Atención: El buzo no está apto. Debe validar la encuesta de salud antes de autorizar.",
                                    fontSize = 12.sp,
                                    color = SafetyDanger,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.profundidad,
                            onValueChange = { viewModel.onProfundidadChange(it) },
                            label = { Text("Profundidad (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.duracionMinutos,
                            onValueChange = { viewModel.onDuracionChange(it) },
                            label = { Text("Tiempo máx (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = uiState.supervisor,
                        onValueChange = { viewModel.onSupervisorChange(it) },
                        label = { Text("Supervisor / Prevencionista a cargo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
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
                enabled = uiState.esBuzoApto,
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
                    text = if (uiState.esBuzoApto) "AUTORIZAR E INICIAR FAENA" else "BUZO NO APTO (BLOQUEADO)",
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
                    Text("La inmersión para ${uiState.buzoSeleccionado?.nombre} ha sido registrada y documentada en la bitácora central.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissSuccessDialog()
                            navController.navigate(Screen.Historial.route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Ver Historial")
                    }
                }
            )
        }
    }
}
