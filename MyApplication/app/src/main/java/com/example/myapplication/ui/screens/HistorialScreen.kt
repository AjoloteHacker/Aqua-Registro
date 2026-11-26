package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import com.example.myapplication.data.model.BitacoraPostInmersion
import com.example.myapplication.data.model.CategoriaPendiente
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.MotivoCierreSinTerminar
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
            @Suppress("DEPRECATION")
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.White,
                contentColor = AquaPrimary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabChange(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ScubaDiving, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Bitácora de Faenas (${uiState.faenas.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabChange(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalInformation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Historial Médico (${uiState.historialMedico.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Todo el contenido (buscador, filtros horizontales, alertas y registros) se mueve fluidamente en LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        label = {
                            Text(
                                if (uiState.selectedTab == 0)
                                    "Buscar por folio, buzo, supervisor o centro"
                                else
                                    "Buscar por buzo, RUT, folio o evaluador"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                item {
                    val filtrosActuales = if (uiState.selectedTab == 0) {
                        listOf("Todas", "En Curso", "Completada", "Sin Terminar")
                    } else {
                        listOf("Todas", "Aptos", "No Aptos")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filtrosActuales.forEach { filtro ->
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
                }

                if (uiState.selectedTab == 0) {
                    if (uiState.alertasVelocidad.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SafetyCardBgDanger)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = SafetyDanger,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ALERTAS JEFE DE CENTRO · VELOCIDAD 24/9 m/min (${uiState.alertasVelocidad.size})",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = SafetyDanger
                                        )
                                    }
                                    uiState.alertasVelocidad.take(3).forEach { alerta ->
                                        Text(
                                            text = "• ${alerta.faenaId} (${alerta.buzoNombre}): Descenso ${alerta.velocidadDescensoMpm} m/min / Ascenso ${alerta.velocidadAscensoMpm} m/min · ${alerta.estadoRevision}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SafetyDanger
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Registros de Bitácora (${uiState.faenasFiltradas.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaTextPrimary
                        )
                    }

                    if (uiState.faenasFiltradas.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, contentDescription = "Sin resultados", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No se encontraron registros de faena", color = AquaTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(uiState.faenasFiltradas, key = { it.id }) { faena ->
                            val bitacorasFaena = uiState.bitacorasPostInmersion.filter { it.faenaId == faena.id }
                            FaenaCard(
                                faena = faena,
                                semaforo = uiState.semaforoPorFaena[faena.id] ?: ColorSemaforo.AMARILLO,
                                buzoMatriculaVencida = faena.buzoId in uiState.buzosConMatriculaVencidaIds,
                                socorristaMatriculaVencida = faena.socorristaId in uiState.buzosConMatriculaVencidaIds,
                                pendienteSincronizar = faena.id in uiState.pendientesSincronizarIds || !uiState.isNetworkConnected,
                                cantidadPendientes = uiState.pendientesPorFaena[faena.id]?.size ?: 0,
                                bitacorasPostInmersion = bitacorasFaena,
                                onRevisar = { viewModel.abrirRevision(faena.id) },
                                onAbrirBitacoraPost = { viewModel.abrirBitacoraPostInmersion(faena.id) },
                                onCerrarSinTerminar = { viewModel.abrirCierreSinTerminar(faena.id) },
                                onEditarComentarioCierre = { viewModel.abrirEditarComentarioCierre(faena.id) }
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Evaluaciones Médicas Registradas (${uiState.chequeosFiltrados.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaTextPrimary
                        )
                    }

                    if (uiState.chequeosFiltrados.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, contentDescription = "Sin resultados", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No se encontraron registros médicos", color = AquaTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(uiState.chequeosFiltrados, key = { it.id }) { chequeo ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    RegistroMedicoItem(
                                        chequeo = chequeo,
                                        pendienteSincronizar = chequeo.id in uiState.pendientesSincronizarIds || !uiState.isNetworkConnected
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    uiState.faenaEnRevision?.let { faena ->
        RevisionFaenaDialog(
            faena = faena,
            semaforo = uiState.semaforoPorFaena[faena.id] ?: ColorSemaforo.AMARILLO,
            buzoMatriculaVencida = faena.buzoId in uiState.buzosConMatriculaVencidaIds,
            socorristaMatriculaVencida = faena.socorristaId in uiState.buzosConMatriculaVencidaIds,
            esSupervisor = uiState.esUsuarioSupervisor,
            rolEtiqueta = uiState.rolUsuarioEtiqueta,
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
            onCerrarSinTerminar = { viewModel.abrirCierreSinTerminar(faena.id) },
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

    uiState.faenaBitacoraActiva?.let { faena ->
        RegistrarBitacoraPostInmersionDialog(
            faena = faena,
            bitacorasPrevias = uiState.bitacorasPostInmersion.filter { it.faenaId == faena.id },
            error = uiState.errorBitacora,
            onGuardar = { prof, mins, velDesc, velAsc, origen, obs ->
                viewModel.registrarBitacoraPostInmersion(
                    faenaId = faena.id,
                    profundidad = prof,
                    tiempoFondo = mins,
                    velocidadDescenso = velDesc,
                    velocidadAscenso = velAsc,
                    origenDato = origen,
                    observaciones = obs
                )
            },
            onDismiss = { viewModel.cerrarBitacoraPostInmersion() }
        )
    }

    uiState.faenaCerrandoSinTerminar?.let { faena ->
        CerrarFaenaSinTerminarDialog(
            faena = faena,
            motivosAutomaticos = uiState.motivosAutomaticosCierre,
            error = uiState.errorCierreSinTerminar,
            onConfirmar = { motivos, otros, comentario ->
                viewModel.confirmarCierreSinTerminar(
                    faenaId = faena.id,
                    motivosSeleccionados = motivos,
                    otrosMotivos = otros,
                    comentario = comentario
                )
            },
            onDismiss = { viewModel.cerrarDialogoSinTerminar() }
        )
    }

    uiState.faenaEditandoComentario?.let { faena ->
        EditarComentarioCierreDialog(
            faena = faena,
            onGuardar = { nuevoComentario ->
                viewModel.guardarComentarioCierre(faena.id, nuevoComentario)
            },
            onDismiss = { viewModel.cerrarEditarComentarioCierre() }
        )
    }
}

@Composable
fun FaenaCard(
    faena: RegistroFaena,
    semaforo: ColorSemaforo = ColorSemaforo.VERDE,
    buzoMatriculaVencida: Boolean = false,
    socorristaMatriculaVencida: Boolean = false,
    pendienteSincronizar: Boolean = false,
    cantidadPendientes: Int,
    bitacorasPostInmersion: List<BitacoraPostInmersion> = emptyList(),
    onRevisar: () -> Unit,
    onAbrirBitacoraPost: () -> Unit = {},
    onCerrarSinTerminar: () -> Unit = {},
    onEditarComentarioCierre: () -> Unit = {}
) {
    val enCurso = faena.enCurso
    val (semColor, semBg) = when (semaforo) {
        ColorSemaforo.VERDE -> SafetySuccess to SafetyCardBgSuccess
        ColorSemaforo.AMARILLO -> Color(0xFFD97706) to Color(0xFFFEF3C7)
        ColorSemaforo.ROJO -> SafetyDanger to SafetyCardBgDanger
    }

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = semBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (semaforo) {
                                    ColorSemaforo.VERDE -> Icons.Default.CheckCircle
                                    ColorSemaforo.AMARILLO -> Icons.Default.WarningAmber
                                    ColorSemaforo.ROJO -> Icons.Default.Block
                                },
                                contentDescription = null,
                                tint = semColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Semáforo ${semaforo.codigo}",
                                color = semColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

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
                    text = buildString {
                        append(faena.buzoNombre)
                        if (faena.socorristaNombre.isNotBlank()) {
                            append(" · Socorrista: ${faena.socorristaNombre}")
                        }
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AquaTextPrimary
                )
            }
            if (buzoMatriculaVencida) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = SafetyDanger,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Advertencia (${faena.buzoNombre}): Matrícula marítima vencida",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyDanger
                    )
                }
            }
            if (socorristaMatriculaVencida && faena.socorristaNombre.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = SafetyDanger,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Advertencia (${faena.socorristaNombre}): Matrícula marítima vencida",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyDanger
                    )
                }
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
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = AquaPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Autorizó: ${faena.autorizadoPor}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaPrimary
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Prof: ${faena.profundidadMetros}m · ${faena.tiempoMinutos} min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AquaTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = faena.fechaHora,
                            fontSize = 11.sp,
                            color = AquaTextSecondary
                        )
                    }
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
                    if (pendienteSincronizar) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pendiente de sincronizar (Room)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción homogéneos (mismo alto 38.dp y distribución equilibrada)
            if (enCurso) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRevisar,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetySuccess)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revisar / Finalizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCerrarSinTerminar,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyDanger)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cerrar Faena", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            OutlinedButton(
                onClick = onAbrirBitacoraPost,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AquaPrimary)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bitácora Post-Inmersión (24/9 m/min)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            if (faena.cierreSinTerminar != null) {
                val cierre = faena.cierreSinTerminar
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SafetyCardBgDanger,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = SafetyDanger,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Faena cerrada sin terminar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafetyDanger
                                )
                            }
                            TextButton(
                                onClick = onEditarComentarioCierre,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = AquaPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar comentario", fontSize = 11.sp, color = AquaPrimary)
                            }
                        }
                        Text(
                            text = "Motivos: ${cierre.resumenMotivos()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaTextPrimary
                        )
                        Text(
                            text = "Comentario: ${cierre.comentario.ifBlank { "Sin comentario adicional" }}",
                            fontSize = 11.sp,
                            color = AquaTextSecondary
                        )
                        Text(
                            text = "Cerrada por: ${cierre.cerradoPor} · ${cierre.fechaHora}",
                            fontSize = 10.sp,
                            color = AquaTextSecondary
                        )
                    }
                }
            }

            if (bitacorasPostInmersion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                bitacorasPostInmersion.forEach { bit ->
                    Surface(
                        color = if (bit.alertaVelocidadSuperada) SafetyCardBgDanger else SafetyCardBgSuccess,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (bit.alertaVelocidadSuperada) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (bit.alertaVelocidadSuperada) SafetyDanger else SafetySuccess,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${bit.id} · Descenso: ${bit.velocidadDescensoMpm} m/min · Ascenso: ${bit.velocidadAscensoMpm} m/min",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bit.alertaVelocidadSuperada) SafetyDanger else SafetySuccess
                                    )
                                }
                            }
                            Text(
                                text = "${bit.detalleAlerta} · Origen: ${bit.origenDato} · ${bit.estadoRevision}",
                                fontSize = 10.sp,
                                color = if (bit.alertaVelocidadSuperada) SafetyDanger else AquaTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevisionFaenaDialog(
    faena: RegistroFaena,
    semaforo: ColorSemaforo = ColorSemaforo.AMARILLO,
    buzoMatriculaVencida: Boolean = false,
    socorristaMatriculaVencida: Boolean = false,
    esSupervisor: Boolean = true,
    rolEtiqueta: String = "Supervisor / Prevencionista SEREMI",
    pendientes: List<Pendiente>,
    novedades: List<String>,
    onEditarDatos: () -> Unit,
    onAprobarDatos: () -> Unit,
    onRevocarAprobacion: () -> Unit,
    onIrAEquipos: () -> Unit,
    onIrASalud: () -> Unit,
    onCerrarSinTerminar: () -> Unit = {},
    onFinalizar: () -> Unit,
    onDismiss: () -> Unit
) {
    val aprobacion = faena.aprobacion
    val hayPendientesEquipos = pendientes.any { it.categoria == CategoriaPendiente.EQUIPOS }
    val hayPendientesSalud = pendientes.any { it.categoria == CategoriaPendiente.SALUD }
    val (semColor, semBg) = when (semaforo) {
        ColorSemaforo.VERDE -> SafetySuccess to SafetyCardBgSuccess
        ColorSemaforo.AMARILLO -> Color(0xFFD97706) to Color(0xFFFEF3C7)
        ColorSemaforo.ROJO -> SafetyDanger to SafetyCardBgDanger
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Revisión de inmersión ${faena.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    color = semBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (semaforo) {
                                ColorSemaforo.VERDE -> Icons.Default.VerifiedUser
                                ColorSemaforo.AMARILLO -> Icons.Default.WarningAmber
                                ColorSemaforo.ROJO -> Icons.Default.Block
                            },
                            contentDescription = null,
                            tint = semColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = semaforo.etiqueta,
                            color = semColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
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
                DatoFila("Buzo ejecutor", faena.buzoNombre)
                if (buzoMatriculaVencida) {
                    Text(
                        text = "Advertencia: Matrícula marítima vencida (${faena.buzoNombre})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyDanger
                    )
                }
                DatoFila(
                    "Socorrista",
                    faena.socorristaNombre.takeIf { it.isNotBlank() } ?: "Sin asignar"
                )
                if (socorristaMatriculaVencida && faena.socorristaNombre.isNotBlank()) {
                    Text(
                        text = "Advertencia: Matrícula marítima vencida (${faena.socorristaNombre})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyDanger
                    )
                }
                DatoFila("Empresa", faena.empresa)
                DatoFila("Centro", faena.centroCultivo)
                DatoFila("Faena", faena.tipoFaena)
                DatoFila("Profundidad", "${faena.profundidadMetros} m")
                DatoFila("Tiempo máx.", "${faena.tiempoMinutos} min")
                DatoFila("Autorizada", faena.fechaHora)
                DatoFila("Autorizó", faena.autorizadoPor)

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

                if (!esSupervisor) {
                    Surface(
                        color = SafetyCardBgDanger,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SafetyDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Restricción por Rol ($rolEtiqueta): Solo un Supervisor puede aprobar o cerrar la faena.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyDanger
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditarDatos,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar datos", fontSize = 12.sp)
                    }
                    if (aprobacion == null) {
                        Button(
                            onClick = onAprobarDatos,
                            enabled = esSupervisor,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Aprobar datos", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onRevocarAprobacion,
                            enabled = esSupervisor,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
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
                            text = "Todo verificado. La faena puede finalizarse (la encuesta DESPUÉS es informativa).",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
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

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                OutlinedButton(
                    onClick = onCerrarSinTerminar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyDanger)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cerrar faena sin terminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinalizar,
                enabled = pendientes.isEmpty() && esSupervisor,
                modifier = Modifier.height(40.dp)
            ) {
                Text(if (esSupervisor) "Finalizar faena" else "Requiere Supervisor")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) { Text("Cerrar") }
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
            Button(
                onClick = { onGuardar(centro, tipo, profundidad, minutos) },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) { Text("Cancelar") }
        }
    )
}

@Composable
fun RegistrarBitacoraPostInmersionDialog(
    faena: RegistroFaena,
    bitacorasPrevias: List<BitacoraPostInmersion>,
    error: String?,
    onGuardar: (
        profundidad: String,
        tiempoFondo: String,
        velocidadDescenso: String,
        velocidadAscenso: String,
        origenDato: String,
        observaciones: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var profundidad by remember(faena.id) { mutableStateOf(faena.profundidadMetros.toString()) }
    var tiempoFondo by remember(faena.id) { mutableStateOf(faena.tiempoMinutos.toString()) }
    var velocidadDescenso by remember(faena.id) { mutableStateOf("18.0") }
    var velocidadAscenso by remember(faena.id) { mutableStateOf("8.0") }
    var origenDato by remember(faena.id) { mutableStateOf("Dispositivo Census / Simulación") }
    var observaciones by remember(faena.id) { mutableStateOf("") }

    val descVal = velocidadDescenso.replace(',', '.').toDoubleOrNull() ?: 0.0
    val ascVal = velocidadAscenso.replace(',', '.').toDoubleOrNull() ?: 0.0
    val superaDesc = descVal > BitacoraPostInmersion.LIMITE_DESCENSO_MPM
    val superaAsc = ascVal > BitacoraPostInmersion.LIMITE_ASCENSO_MPM
    val hayAlerta = superaDesc || superaAsc

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = if (hayAlerta) SafetyDanger else AquaPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bitácora Post-Inmersión · ${faena.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (hayAlerta) SafetyDanger else AquaPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Buzo: ${faena.buzoNombre} · Límites normativos: Descenso máx. 24 m/min | Ascenso máx. 9 m/min",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )

                Surface(
                    color = if (hayAlerta) SafetyCardBgDanger else SafetyCardBgSuccess,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hayAlerta) Icons.Default.WarningAmber else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (hayAlerta) SafetyDanger else SafetySuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                superaDesc && superaAsc ->
                                    "ALERTA JEFE DE CENTRO: Velocidad de descenso ($descVal > 24 m/min) y ascenso ($ascVal > 9 m/min) excedidas."
                                superaDesc ->
                                    "ALERTA JEFE DE CENTRO: Velocidad de descenso ($descVal m/min) supera el límite de 24 m/min."
                                superaAsc ->
                                    "ALERTA JEFE DE CENTRO: Velocidad de ascenso ($ascVal m/min) supera el límite de 9 m/min."
                                else ->
                                    "Velocidades dentro de rango seguro (<= 24 m/min descenso y <= 9 m/min ascenso)."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hayAlerta) SafetyDanger else SafetySuccess
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = profundidad,
                        onValueChange = { if (it.all { c -> c.isDigit() }) profundidad = it },
                        label = { Text("Profundidad (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = tiempoFondo,
                        onValueChange = { if (it.all { c -> c.isDigit() }) tiempoFondo = it },
                        label = { Text("Tiempo fondo (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = velocidadDescenso,
                        onValueChange = { velocidadDescenso = it },
                        label = { Text("Descenso (m/min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = superaDesc,
                        supportingText = { Text(if (superaDesc) "> 24 m/min: ALERTA" else "Máx: 24 m/min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = velocidadAscenso,
                        onValueChange = { velocidadAscenso = it },
                        label = { Text("Ascenso (m/min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = superaAsc,
                        supportingText = { Text(if (superaAsc) "> 9 m/min: ALERTA" else "Máx: 9 m/min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = origenDato,
                    onValueChange = { origenDato = it },
                    label = { Text("Origen del dato (Census / Simulación)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones post-inmersión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                if (bitacorasPrevias.isNotEmpty()) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    Text(
                        text = "Registros históricos de esta faena (${bitacorasPrevias.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaTextPrimary
                    )
                    bitacorasPrevias.forEach { bit ->
                        Text(
                            text = "• ${bit.id} (${bit.fechaHora}): Desc ${bit.velocidadDescensoMpm} / Asc ${bit.velocidadAscensoMpm} m/min — ${bit.estadoRevision}",
                            fontSize = 11.sp,
                            color = if (bit.alertaVelocidadSuperada) SafetyDanger else AquaTextSecondary
                        )
                    }
                }

                if (error != null) {
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafetyDanger
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(profundidad, tiempoFondo, velocidadDescenso, velocidadAscenso, origenDato, observaciones)
                },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hayAlerta) SafetyDanger else AquaPrimary
                )
            ) {
                Text(if (hayAlerta) "Registrar y Notificar Alerta" else "Guardar Bitácora")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) { Text("Cancelar") }
        }
    )
}

@Composable
fun CerrarFaenaSinTerminarDialog(
    faena: RegistroFaena,
    motivosAutomaticos: Set<MotivoCierreSinTerminar>,
    error: String?,
    onConfirmar: (
        motivosSeleccionados: Set<MotivoCierreSinTerminar>,
        otrosMotivos: List<String>,
        comentario: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var seleccionados by remember(faena.id, motivosAutomaticos) {
        mutableStateOf(motivosAutomaticos)
    }
    var otrosMotivos by remember(faena.id) { mutableStateOf(listOf("")) }
    var comentario by remember(faena.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = SafetyDanger,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Faena sin Terminar · ${faena.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SafetyDanger
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Por qué se cierra la faena sin terminar? (Puede marcar más de una opción):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaTextPrimary
                )

                MotivoCierreSinTerminar.entries.forEach { motivo ->
                    val esAutomatico = motivo in motivosAutomaticos
                    val estaMarcado = motivo in seleccionados || esAutomatico

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = estaMarcado,
                            onCheckedChange = { checked ->
                                if (!esAutomatico) {
                                    seleccionados = if (checked) seleccionados + motivo else seleccionados - motivo
                                }
                            },
                            enabled = !esAutomatico
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = motivo.etiqueta,
                                fontSize = 13.sp,
                                fontWeight = if (estaMarcado) FontWeight.Bold else FontWeight.Normal,
                                color = if (esAutomatico) SafetyDanger else AquaTextPrimary
                            )
                            if (esAutomatico) {
                                Text(
                                    text = "Marcado automático por estado de equipos/salud en la faena",
                                    fontSize = 10.sp,
                                    color = SafetyDanger
                                )
                            }
                        }
                    }
                }

                if (MotivoCierreSinTerminar.OTRO in seleccionados) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    Text(
                        text = "Especifique otro(s) motivo(s) no considerado(s):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    otrosMotivos.forEachIndexed { idx, texto ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = texto,
                                onValueChange = { nuevo ->
                                    otrosMotivos = otrosMotivos.toMutableList().also { it[idx] = nuevo }
                                },
                                label = { Text("Otro motivo #${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (otrosMotivos.size > 1) {
                                IconButton(
                                    onClick = {
                                        otrosMotivos = otrosMotivos.filterIndexed { i, _ -> i != idx }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar motivo", tint = SafetyDanger)
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { otrosMotivos = otrosMotivos + "" }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar otra opción en 'Otro'", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario de cierre (editable incluso tras cerrar)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                if (error != null) {
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafetyDanger
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(seleccionados, otrosMotivos, comentario) },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyDanger)
            ) {
                Text("Confirmar Cierre")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditarComentarioCierreDialog(
    faena: RegistroFaena,
    onGuardar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var comentario by remember(faena.id) {
        mutableStateOf(faena.cierreSinTerminar?.comentario.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar comentario de cierre · ${faena.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Motivos registrados: ${faena.cierreSinTerminar?.resumenMotivos().orEmpty()}",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario de cierre") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(comentario) },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Guardar comentario")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) { Text("Cancelar") }
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
