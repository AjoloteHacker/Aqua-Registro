package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
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
import com.example.myapplication.data.model.CategoriaPendiente
import com.example.myapplication.data.model.Pendiente
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.HistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: HistorialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Historial de Inmersiones",
        subtitle = "Bitácora Digital Centralizada",
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
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Buscar por folio, buzo, centro o faena") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todas", "En Curso", "Completada").forEach { filtro ->
                    FilterChip(
                        selected = uiState.selectedFilter == filtro,
                        onClick = { viewModel.onFilterChange(filtro) },
                        label = { Text(filtro, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaSecondary.copy(alpha = 0.2f),
                            selectedLabelColor = AquaPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Registros de Bitácora (${uiState.faenasFiltradas.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.faenasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = "Sin resultados", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron registros de faena", color = AquaTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.faenasFiltradas, key = { it.id }) { faena ->
                        FaenaCard(
                            faena = faena,
                            cantidadPendientes = uiState.pendientesPorFaena[faena.id]?.size ?: 0,
                            onRevisar = { viewModel.abrirRevision(faena.id) }
                        )
                    }
                }
            }
        }
    }

    uiState.faenaEnRevision?.let { faena ->
        RevisionFaenaDialog(
            faena = faena,
            pendientes = uiState.pendientesPorFaena[faena.id].orEmpty(),
            novedades = uiState.novedadesPorFaena[faena.id].orEmpty(),
            onEditarDatos = { viewModel.abrirEdicion(faena.id) },
            onAprobarDatos = { viewModel.aprobarDatos(faena.id) },
            onRevocarAprobacion = { viewModel.revocarAprobacion(faena.id) },
            onIrAEquipos = {
                viewModel.irAEquipos(faena.id)
                navController.navigate(Screen.Equipos.route) { launchSingleTop = true }
            },
            onIrASalud = {
                viewModel.irASalud(faena.id)
                navController.navigate(Screen.Salud.route) { launchSingleTop = true }
            },
            onFinalizar = { viewModel.finalizarFaena(faena.id) },
            onDismiss = { viewModel.cerrarRevision() }
        )
    }

    uiState.editandoFaena?.let { faena ->
        EditarDatosFaenaDialog(
            faena = faena,
            centros = uiState.centrosDisponibles,
            tipos = uiState.tiposFaenaDisponibles,
            error = uiState.errorEdicion,
            onGuardar = { centro, tipo, profundidad, minutos ->
                viewModel.editarDatos(faena.id, centro, tipo, profundidad, minutos)
            },
            onDismiss = { viewModel.cerrarEdicion() }
        )
    }
}

@Composable
fun FaenaCard(
    faena: RegistroFaena,
    cantidadPendientes: Int,
    onRevisar: () -> Unit
) {
    val enCurso = faena.enCurso

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inmersión ${faena.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AquaPrimary
                )

                Surface(
                    color = if (enCurso) AquaPrimary.copy(alpha = 0.12f) else SafetyCardBgSuccess,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = faena.estado,
                        color = if (enCurso) AquaPrimary else SafetySuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ScubaDiving,
                    contentDescription = null,
                    tint = AquaPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = faena.buzoNombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AquaTextPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CorporateFare,
                    contentDescription = null,
                    tint = AquaTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = faena.empresa,
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = AquaPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = faena.tipoFaena,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AquaTextPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AquaTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = faena.centroCultivo,
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prof: ${faena.profundidadMetros}m · ${faena.tiempoMinutos} min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaSecondary
                    )
                    Text(
                        text = "🕒 ${faena.fechaHora}",
                        fontSize = 11.sp,
                        color = AquaTextSecondary
                    )
                    if (enCurso) {
                        Text(
                            text = if (cantidadPendientes == 0) {
                                "Lista para finalizar"
                            } else {
                                "$cantidadPendientes pendiente(s)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cantidadPendientes == 0) {
                                SafetySuccess
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                if (enCurso) {
                    OutlinedButton(
                        onClick = onRevisar,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetySuccess)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revisar y finalizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RevisionFaenaDialog(
    faena: RegistroFaena,
    pendientes: List<Pendiente>,
    novedades: List<String>,
    onEditarDatos: () -> Unit,
    onAprobarDatos: () -> Unit,
    onRevocarAprobacion: () -> Unit,
    onIrAEquipos: () -> Unit,
    onIrASalud: () -> Unit,
    onFinalizar: () -> Unit,
    onDismiss: () -> Unit
) {
    val aprobacion = faena.aprobacion
    val hayPendientesEquipos = pendientes.any { it.categoria == CategoriaPendiente.EQUIPOS }
    val hayPendientesSalud = pendientes.any { it.categoria == CategoriaPendiente.SALUD }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Revisión de inmersión ${faena.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Datos de la faena: se aprueban o se corrigen
                Text(
                    text = "Datos de la faena",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaTextPrimary
                )
                DatoFila("Buzo", faena.buzoNombre)
                DatoFila("Empresa", faena.empresa)
                DatoFila("Centro", faena.centroCultivo)
                DatoFila("Faena", faena.tipoFaena)
                DatoFila("Profundidad", "${faena.profundidadMetros} m")
                DatoFila("Tiempo máx.", "${faena.tiempoMinutos} min")
                DatoFila("Autorizada", faena.fechaHora)

                if (aprobacion != null) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafetySuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Datos aprobados por ${aprobacion.aprobadoPor} · ${aprobacion.fechaHora}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetySuccess
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Datos pendientes de aprobación",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditarDatos,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar datos", fontSize = 12.sp)
                    }
                    if (aprobacion == null) {
                        Button(
                            onClick = onAprobarDatos,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Aprobar datos", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onRevocarAprobacion,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Quitar aprobación", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                // Pendientes del cierre
                if (pendientes.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafetySuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Todo verificado. La faena puede finalizarse.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetySuccess
                        )
                    }
                } else {
                    Text(
                        text = "Pendientes (${pendientes.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    pendientes.forEach { pendiente ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = pendiente.detalle, fontSize = 12.sp, color = AquaTextPrimary)
                        }
                    }
                }

                if (hayPendientesEquipos) {
                    OutlinedButton(
                        onClick = onIrAEquipos,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir a Control de Equipos", fontSize = 12.sp)
                    }
                }
                if (hayPendientesSalud) {
                    OutlinedButton(
                        onClick = onIrASalud,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir a Encuesta de Salud", fontSize = 12.sp)
                    }
                }

                if (novedades.isNotEmpty()) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    Text(
                        text = "Novedades (se reportan, no bloquean)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaTextPrimary
                    )
                    novedades.forEach { novedad ->
                        Text(text = "• $novedad", fontSize = 12.sp, color = AquaTextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onFinalizar, enabled = pendientes.isEmpty()) {
                Text("Finalizar faena")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$etiqueta:",
            fontSize = 12.sp,
            color = AquaTextSecondary,
            modifier = Modifier.width(92.dp)
        )
        Text(
            text = valor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AquaTextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EditarDatosFaenaDialog(
    faena: RegistroFaena,
    centros: List<String>,
    tipos: List<String>,
    error: String?,
    onGuardar: (centro: String, tipo: String, profundidad: String, minutos: String) -> Unit,
    onDismiss: () -> Unit
) {
    var centro by remember(faena.id) { mutableStateOf(faena.centroCultivo) }
    var tipo by remember(faena.id) { mutableStateOf(faena.tipoFaena) }
    var profundidad by remember(faena.id) { mutableStateOf(faena.profundidadMetros.toString()) }
    var minutos by remember(faena.id) { mutableStateOf(faena.tiempoMinutos.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar datos de ${faena.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectorDeTexto(
                    etiqueta = "Centro de operación / cultivo",
                    valor = centro,
                    opciones = centros,
                    onSeleccion = { centro = it }
                )
                SelectorDeTexto(
                    etiqueta = "Tipo de faena submarina",
                    valor = tipo,
                    opciones = tipos,
                    onSeleccion = { tipo = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = profundidad,
                        onValueChange = { nuevo -> if (nuevo.all { c -> c.isDigit() }) profundidad = nuevo },
                        label = { Text("Profundidad (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minutos,
                        onValueChange = { nuevo -> if (nuevo.all { c -> c.isDigit() }) minutos = nuevo },
                        label = { Text("Tiempo máx. (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Text(
                    text = "Si cambias algún dato, la aprobación se anula y debe aprobarse de nuevo.",
                    fontSize = 11.sp,
                    color = AquaTextSecondary
                )
                if (error != null) {
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onGuardar(centro, tipo, profundidad, minutos) }) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun SelectorDeTexto(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Column {
        Text(text = etiqueta, fontSize = 11.sp, color = AquaTextSecondary)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandido = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = valor,
                        fontSize = 12.sp,
                        color = AquaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                }
            }
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, fontSize = 12.sp) },
                        onClick = {
                            onSeleccion(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}
