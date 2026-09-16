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
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.EstadoSalud
import com.example.myapplication.data.RegistroFaena
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoScreen(navController: NavController) {
    var buzoIndex by remember { mutableIntStateOf(0) }
    var buzoDropdownOpen by remember { mutableStateOf(false) }

    var centroCultivo by remember { mutableStateOf(AppData.centrosCultivo.first()) }
    var centroDropdownOpen by remember { mutableStateOf(false) }

    var tipoFaena by remember { mutableStateOf(AppData.tiposFaena.first()) }
    var faenaDropdownOpen by remember { mutableStateOf(false) }

    var profundidad by remember { mutableStateOf("18") }
    var duracionMinutos by remember { mutableStateOf("40") }
    var supervisor by remember { mutableStateOf("Cristóbal Loncón (Prevencionista)") }

    val buzoSeleccionado = AppData.buzos.getOrNull(buzoIndex)
    val esBuzoApto = buzoSeleccionado?.estadoSalud == EstadoSalud.APTO

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Faena Submarina", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AquaPrimary)
            )
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
            // Tarjeta de Selección de Buzo
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
                                    text = buzoSeleccionado?.let { "${it.nombre} (${it.empresa})" } ?: "Seleccionar",
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
                            AppData.buzos.forEachIndexed { idx, buzo ->
                                DropdownMenuItem(
                                    text = { Text("${buzo.nombre} - [${buzo.estadoSalud.label}]") },
                                    onClick = {
                                        buzoIndex = idx
                                        buzoDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Alerta de Estado del Buzo
                    if (esBuzoApto) {
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
                                    text = "Atención: Debe validar el examen de salud antes de autorizar",
                                    fontSize = 12.sp,
                                    color = SafetyDanger,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta de Parámetros de la Faena
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

                    // Centro de Cultivo
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
                                Text(centroCultivo, fontSize = 13.sp, color = AquaTextPrimary)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = centroDropdownOpen,
                            onDismissRequest = { centroDropdownOpen = false }
                        ) {
                            AppData.centrosCultivo.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        centroCultivo = c
                                        centroDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Tipo de Faena
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
                                Text(tipoFaena, fontSize = 13.sp, color = AquaTextPrimary)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                            }
                        }
                        DropdownMenu(
                            expanded = faenaDropdownOpen,
                            onDismissRequest = { faenaDropdownOpen = false }
                        ) {
                            AppData.tiposFaena.forEach { tf ->
                                DropdownMenuItem(
                                    text = { Text(tf) },
                                    onClick = {
                                        tipoFaena = tf
                                        faenaDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Profundidad y Tiempo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = profundidad,
                            onValueChange = { profundidad = it },
                            label = { Text("Profundidad (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = duracionMinutos,
                            onValueChange = { duracionMinutos = it },
                            label = { Text("Tiempo máx (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = supervisor,
                        onValueChange = { supervisor = it },
                        label = { Text("Supervisor / Prevencionista a cargo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            // Botón de Autorización
            Button(
                onClick = {
                    if (buzoSeleccionado == null) {
                        errorMessage = "Seleccione un buzo válido"
                        return@Button
                    }
                    val prof = profundidad.toIntOrNull() ?: 15
                    val mins = duracionMinutos.toIntOrNull() ?: 30

                    val nuevoRegistro = RegistroFaena(
                        id = "F-${100 + AppData.faenas.size + 1}",
                        buzoNombre = buzoSeleccionado.nombre,
                        empresa = buzoSeleccionado.empresa,
                        centroCultivo = centroCultivo,
                        tipoFaena = tipoFaena,
                        profundidadMetros = prof,
                        tiempoMinutos = mins,
                        fechaHora = "15/09/2026 11:15",
                        estado = "Autorizada y en Curso"
                    )
                    AppData.faenas.add(0, nuevoRegistro)
                    successDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AUTORIZAR E INICIAR FAENA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (successDialog) {
            AlertDialog(
                onDismissRequest = { successDialog = false },
                title = { Text("¡Faena Autorizada!", fontWeight = FontWeight.Bold, color = SafetySuccess) },
                text = {
                    Text("La inmersión para ${buzoSeleccionado?.nombre} ha sido registrada y documentada en la bitácora central.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            successDialog = false
                            navController.navigate("historial")
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
