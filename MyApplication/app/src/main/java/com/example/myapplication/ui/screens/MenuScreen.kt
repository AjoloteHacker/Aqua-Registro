package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.RetrofitClient
import androidx.compose.material.icons.automirrored.filled.FactCheck
import com.example.myapplication.ui.viewmodels.MenuViewModel
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.BuzoViewModel

/**
 * Pantalla Principal adaptada fielmente al boceto de diseño de operaciones submarinas:
 * - Ítem 1 (Hero Action Central): Botón circular de embarcación para Registrar Faena (AST).
 * - Ítems 2, 3 y 4 (Tarjetas inferiores): Buzos (+), Historial de Faenas (Checklist/Aprobación) y Cuadrilla/Turno.
 * - Encuestas y control de equipos retirados como botones sueltos (se integran al flujo de faena).
 */
@Composable
fun MenuScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    menuViewModel: MenuViewModel = viewModel(),
    buzoViewModel: BuzoViewModel = viewModel()
) {
    val prevencionista by menuViewModel.prevencionistaActivo.collectAsState()
    val buzos by menuViewModel.buzos.collectAsState()
    val faenas by menuViewModel.faenas.collectAsState()
    val isConnected by menuViewModel.isNetworkConnected.collectAsState()
    val climaMaritimo by menuViewModel.climaMaritimo.collectAsState()
    val syncStatus by menuViewModel.syncStatus.collectAsState()
    val pendientesSync by menuViewModel.pendientesSincronizarIds.collectAsState()
    val serverUrl by menuViewModel.serverUrl.collectAsState()
    val servidorOnline by menuViewModel.servidorOnline.collectAsState()
    val ultimoPingDetalle by menuViewModel.ultimoPingDetalle.collectAsState()
    val buzoUiState by buzoViewModel.uiState.collectAsState()
    var mostrarDialogoServidor by remember { mutableStateOf(false) }

    val faenasPendientes = remember(faenas) { faenas.count { it.estado != "Completada con Éxito" } }

    val snackbarHostState = remember { SnackbarHostState() }

    var empresaDropdownExpanded by remember { mutableStateOf(false) }
    var categoriaDropdownExpanded by remember { mutableStateOf(false) }
    var vigenciaDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(buzoUiState.userMessage) {
        buzoUiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            buzoViewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = prevencionista?.nombre ?: "Aqua-Registro",
        subtitle = "${prevencionista?.rol?.etiqueta ?: "Supervisor"} · SEREMI: ${prevencionista?.registroSeremi ?: "S/R"}",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = {
                menuViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar Sesión",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta de estado operativo de la base
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AquaSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = AquaPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prevencionista?.centro ?: "Centro Operativo Melinka",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AquaChile · ${buzos.size} buzos registrados",
                            fontSize = 12.sp,
                            color = AquaTextSecondary
                        )
                    }

                    Surface(
                        color = SafetyCardBgSuccess,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "ACTIVO",
                            color = SafetySuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp), color = AquaBackground)

                // Clima Marítimo contextual para faenas (API Externa Open-Meteo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Clima Marítimo",
                        tint = AquaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = climaMaritimo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AquaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp), color = AquaBackground)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 10.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val hayPendientesSync = pendientesSync.isNotEmpty()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                menuViewModel.probarConexionServidor()
                                mostrarDialogoServidor = true
                            }
                    ) {
                        Icon(
                            imageVector = when {
                                !isConnected -> Icons.Default.WifiOff
                                hayPendientesSync -> Icons.Default.CloudUpload
                                else -> Icons.Default.Sync
                            },
                            contentDescription = "Estado de Sincronización",
                            tint = if (isConnected && !hayPendientesSync) SafetySuccess else Color(0xFFF59E0B),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                !isConnected && hayPendientesSync ->
                                    "Modo offline · Pendiente de sincronizar (${pendientesSync.size})"
                                !isConnected ->
                                    "Modo offline · Guardando en Room SQLite"
                                hayPendientesSync ->
                                    "Pendiente de sincronizar (${pendientesSync.size})"
                                else -> syncStatus
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isConnected && !hayPendientesSync) SafetySuccess else Color(0xFFB45309),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AquaPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .clickable {
                                    menuViewModel.probarConexionServidor()
                                    mostrarDialogoServidor = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = "Configurar Servidor",
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Servidor",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaPrimary
                                )
                            }
                        }
                        Surface(
                            color = if (isConnected) AquaBackground else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .clickable { menuViewModel.alternarModoOfflineDemo() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = if (isConnected) AquaTextSecondary else Color(0xFFB45309),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isConnected) "Modo Offline" else "Reconectar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) AquaTextSecondary else Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==============================================================
            // ÍTEM 1 DEL BOCETO: BOTÓN HERO CIRCULAR CONCÉNTRICO (EMBARCACIÓN +)
            // ==============================================================
            Text(
                text = "OPERACIÓN Y AUTORIZACIÓN SUBMARINA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AquaTextSecondary,
                letterSpacing = 0.8.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Anillo exterior concéntrico del boceto
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(AquaPrimary.copy(alpha = 0.08f))
                        .border(2.dp, AquaPrimary.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Anillo interior
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .clip(CircleShape)
                            .background(AquaPrimary.copy(alpha = 0.14f))
                            .border(2.dp, AquaSecondary.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Botón central de acción principal
                        ElevatedCard(
                            onClick = { navController.navigate(Screen.Trabajo.route) },
                            modifier = Modifier.size(134.dp),
                            shape = CircleShape,
                            colors = CardDefaults.elevatedCardColors(containerColor = AquaPrimary),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(AquaPrimary, Color(0xFF0073C6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBoat,
                                            contentDescription = "Iniciar Faena",
                                            tint = Color.White,
                                            modifier = Modifier.size(46.dp)
                                        )
                                        // Badge '+' del boceto
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 8.dp, y = (-4).dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(AquaSecondary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Agregar Faena",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "REGISTRAR",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "FAENA (AST)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==============================================================
            // ÍTEMS 2, 3 Y 4 DEL BOCETO: LAS 3 TARJETAS INFERIORES
            // ==============================================================
            Text(
                text = "GESTIÓN Y REGISTROS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AquaTextSecondary,
                modifier = Modifier.fillMaxWidth(),
                letterSpacing = 0.8.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TARJETA 2: AGREGAR BUZO (abre el diálogo directamente)
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "2",
                    titulo = "Agregar\nBuzo",
                    subtitulo = "${buzos.size} Registrados",
                    icono = Icons.Default.PersonAdd,
                    badgeIcon = Icons.Default.Add,
                    colorTema = AquaPrimary,
                    onClick = { buzoViewModel.openAddDialog() }
                )

                // TARJETA 3: HISTORIAL (Checklist / Faenas realizadas con revisión y aprobación)
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "3",
                    titulo = "Historial",
                    subtitulo = "${faenas.size} Faenas",
                    icono = Icons.AutoMirrored.Filled.FactCheck,
                    badgeIcon = if (faenasPendientes > 0) Icons.Default.PendingActions else Icons.Default.Check,
                    colorTema = if (faenasPendientes > 0) SafetyWarning else AquaSecondary,
                    onClick = { navController.navigate(Screen.Historial.route) }
                )

                // TARJETA 4: CUADRILLA (Buzos en turno / cuadrilla)
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "4",
                    titulo = "Cuadrilla",
                    subtitulo = "${buzos.size} Buzos",
                    icono = Icons.Default.Groups,
                    badgeIcon = Icons.Default.CheckCircle,
                    colorTema = SafetySuccess,
                    onClick = { navController.navigate(Screen.Buzo.route) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Diálogos unificados de Registro de Buzo y Nueva Empresa Contratista (compartidos con BuzoScreen)
        RegistrarEditarBuzoDialogs(
            uiState = buzoUiState,
            viewModel = buzoViewModel
        )

        // Pop-up al iniciar sesión cuando algún buzo tiene menos de 15 días de vigencia de matrícula o está vencida
        var mostrarPopUpMatriculas by remember { mutableStateOf(true) }
        val buzosAlertaCriticaMatricula = remember(buzos) {
            buzos.filter { it.matriculaCriticaMenos15Dias || it.matriculaVencida }
        }
        if (mostrarPopUpMatriculas && buzosAlertaCriticaMatricula.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { mostrarPopUpMatriculas = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = SafetyDanger,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Alerta de Vigencia de Matrículas (< 15 días)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SafetyDanger
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Los siguientes buzos tienen su matrícula DIRECTEMAR próxima a vencer (< 15 días) o vencida:",
                            fontSize = 12.sp,
                            color = AquaTextPrimary
                        )
                        buzosAlertaCriticaMatricula.forEach { b ->
                            Text(
                                text = if (b.matriculaVencida) {
                                    "• ${b.nombre} (${b.matricula}): MATRÍCULA VENCIDA"
                                } else {
                                    "• ${b.nombre} (${b.matricula}): Quedan ${b.diasRestantesMatricula} día(s)"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafetyDanger
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { mostrarPopUpMatriculas = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Entendido")
                    }
                }
            )
        }

        if (mostrarDialogoServidor) {
            ConfigurarServidorDialog(
                serverUrl = serverUrl,
                isConnected = isConnected,
                servidorOnline = servidorOnline,
                ultimoPingDetalle = ultimoPingDetalle,
                pendientesCount = pendientesSync.size,
                onProbarConexion = { menuViewModel.probarConexionServidor() },
                onCambiarUrl = { nuevaUrl ->
                    menuViewModel.cambiarUrlServidor(nuevaUrl)
                },
                onSincronizar = { menuViewModel.sincronizarAhora() },
                onDismiss = { mostrarDialogoServidor = false }
            )
        }
    }
}

@Composable
private fun ConfigurarServidorDialog(
    serverUrl: String,
    isConnected: Boolean,
    servidorOnline: Boolean,
    ultimoPingDetalle: String,
    pendientesCount: Int,
    onProbarConexion: () -> Unit,
    onCambiarUrl: (String) -> Unit,
    onSincronizar: () -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(
        "Servidor Central (Nube / 4G / Wi-Fi)" to RetrofitClient.URL_AJOLOSERVER_PUBLICA,
        "Servidor Local Wi-Fi" to RetrofitClient.URL_AJOLOSERVER_LAN,
        "Servidor Red Privada (Tailscale)" to RetrofitClient.URL_AJOLOSERVER_TAILSCALE,
        "Emulador Android Studio" to RetrofitClient.URL_EMULADOR_LOCAL
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = AquaPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conexión con el Servidor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = when {
                        !isConnected -> Color(0xFFFEF3C7)
                        servidorOnline -> SafetyCardBgSuccess
                        else -> SafetyCardBgDanger
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    !isConnected -> Icons.Default.WifiOff
                                    servidorOnline -> Icons.Default.CloudDone
                                    else -> Icons.Default.CloudQueue
                                },
                                contentDescription = null,
                                tint = when {
                                    !isConnected -> Color(0xFFB45309)
                                    servidorOnline -> SafetySuccess
                                    else -> SafetyDanger
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    !isConnected -> "SIN CONEXIÓN DE RED"
                                    servidorOnline -> "CONECTADO AL SERVIDOR"
                                    else -> "SERVIDOR NO DISPONIBLE"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    !isConnected -> Color(0xFFB45309)
                                    servidorOnline -> SafetySuccess
                                    else -> SafetyDanger
                                }
                            )
                        }
                        Text(
                            text = ultimoPingDetalle,
                            fontSize = 11.sp,
                            color = AquaTextPrimary
                        )
                        if (pendientesCount > 0) {
                            Text(
                                text = "Registros locales pendientes de sincronizar: $pendientesCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Text(
                    text = "Seleccionar Servidor:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaTextPrimary
                )

                presets.forEach { (etiqueta, urlPreset) ->
                    val seleccionado = serverUrl.trimEnd('/') == urlPreset.trimEnd('/')
                    Surface(
                        color = if (seleccionado) AquaPrimary.copy(alpha = 0.10f) else AquaBackground,
                        shape = RoundedCornerShape(10.dp),
                        border = if (seleccionado) androidx.compose.foundation.BorderStroke(1.5.dp, AquaPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCambiarUrl(urlPreset)
                                onProbarConexion()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = seleccionado,
                                onClick = {
                                    onCambiarUrl(urlPreset)
                                    onProbarConexion()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AquaPrimary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = etiqueta,
                                fontSize = 12.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                color = if (seleccionado) AquaPrimary else AquaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onProbarConexion,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hacer Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSincronizar,
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(38.dp)
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Tarjeta rectangular vertical correspondiente a los ítems 2, 3 y 4 del boceto a mano.
 */
@Composable
private fun BocetoCard(
    modifier: Modifier = Modifier,
    numeroBoceto: String,
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    badgeIcon: ImageVector? = null,
    colorTema: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .height(145.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera de la tarjeta con número de referencia del boceto y badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = colorTema.copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        text = numeroBoceto,
                        color = colorTema,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (badgeIcon != null) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = colorTema,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Ícono principal
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colorTema.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorTema,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Textos descriptivos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AquaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitulo,
                    fontSize = 10.sp,
                    color = AquaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
