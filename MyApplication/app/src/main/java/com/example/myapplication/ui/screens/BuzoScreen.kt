package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.Buzo
import com.example.myapplication.data.EstadoSalud
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzoScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoRut by remember { mutableStateOf("") }
    var nuevaMatricula by remember { mutableStateOf("") }
    var nuevoTelefono by remember { mutableStateOf("") }
    var empresaSeleccionada by remember { mutableStateOf(AppData.empresas.first()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val buzosFiltrados = remember(searchQuery, AppData.buzos.size) {
        if (searchQuery.isBlank()) AppData.buzos
        else AppData.buzos.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            it.rut.contains(searchQuery, ignoreCase = true) ||
            it.empresa.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Buzos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AquaPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = AquaSecondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Buzo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AquaBackground)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar buzo por nombre, RUT o empresa") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Buzos Registrados (${buzosFiltrados.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AquaTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(buzosFiltrados) { buzo ->
                    BuzoCard(buzo = buzo)
                }
            }
        }

        // Modal para agregar Buzo
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Registrar Nuevo Buzo", fontWeight = FontWeight.Bold, color = AquaPrimary) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = { Text("Nombre y Apellidos") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nuevoRut,
                            onValueChange = { nuevoRut = it },
                            label = { Text("RUT (ej: 18.234.567-8)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nuevaMatricula,
                            onValueChange = { nuevaMatricula = it },
                            label = { Text("Matrícula Marítima") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nuevoTelefono,
                            onValueChange = { nuevoTelefono = it },
                            label = { Text("Teléfono de Contacto") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector de Empresa Contratista
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Empresa: $empresaSeleccionada", fontSize = 13.sp, color = AquaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                }
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                AppData.empresas.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text(emp) },
                                        onClick = {
                                            empresaSeleccionada = emp
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nuevoNombre.isNotBlank() && nuevoRut.isNotBlank()) {
                                AppData.buzos.add(
                                    Buzo(
                                        id = (AppData.buzos.size + 1).toString(),
                                        rut = nuevoRut,
                                        nombre = nuevoNombre,
                                        empresa = empresaSeleccionada,
                                        matricula = if (nuevaMatricula.isBlank()) "Buzo Comercial N° ${1000 + AppData.buzos.size}" else nuevaMatricula,
                                        telefono = if (nuevoTelefono.isBlank()) "+56 9 9000 0000" else nuevoTelefono,
                                        estadoSalud = EstadoSalud.PENDIENTE
                                    )
                                )
                                nuevoNombre = ""
                                nuevoRut = ""
                                nuevaMatricula = ""
                                nuevoTelefono = ""
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Guardar Buzo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun BuzoCard(buzo: Buzo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AquaPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Buzo",
                        tint = AquaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buzo.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AquaTextPrimary
                    )
                    Text(
                        text = "RUT: ${buzo.rut} · ${buzo.matricula}",
                        fontSize = 12.sp,
                        color = AquaTextSecondary
                    )
                }

                // Badge de Estado de Salud
                val (badgeColor, badgeBg) = when (buzo.estadoSalud) {
                    EstadoSalud.APTO -> Pair(SafetySuccess, SafetyCardBgSuccess)
                    EstadoSalud.NO_APTO -> Pair(SafetyDanger, SafetyCardBgDanger)
                    EstadoSalud.PENDIENTE -> Pair(AquaPrimary, AquaBackground)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = buzo.estadoSalud.label.take(12),
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🏢 ${buzo.empresa}",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
                Text(
                    text = "📞 ${buzo.telefono}",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
            }
        }
    }
}
