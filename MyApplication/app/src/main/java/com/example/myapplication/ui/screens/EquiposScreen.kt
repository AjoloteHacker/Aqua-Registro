package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.RevisionEquipo
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.EquiposUiState
import com.example.myapplication.ui.viewmodels.EquiposViewModel

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

// ───────────────────────── Colores de estado ─────────────────────────

private val AmbarRiesgo = Color(0xFFF59E0B) // reemplaza por tu token si ya existe

private fun EstadoEquipo.color(): Color = when (this) {
    EstadoEquipo.ACEPTABLE -> SafetySuccess
    EstadoEquipo.UTILIZABLE_RIESGOSO -> AmbarRiesgo
    EstadoEquipo.INUTILIZABLE -> SafetyDanger
    EstadoEquipo.SIN_REVISAR -> AquaTextSecondary
}

// ───────────────────────── Pantalla Principal ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: EquiposViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var currentEquipoFotoId by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val equipoId = currentEquipoFotoId
            val uri = tempPhotoUri
            if (equipoId != null && uri != null) {
                viewModel.registrarFoto(equipoId, uri.toString())
            }
        }
        currentEquipoFotoId = null
        tempPhotoUri = null
    }

    fun tomarFoto(equipoId: String) {
        currentEquipoFotoId = equipoId
        val photosDir = File(context.cacheDir, "photos").apply { mkdirs() }
        val photoFile = File.createTempFile("equipo_${equipoId}_", ".jpg", photosDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        tempPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Control de Equipos",
        subtitle = "Verificación y estado",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { paddingValues ->
        ChecklistEquiposContent(
            uiState = uiState,
            onSeleccionarBuzo = viewModel::seleccionarBuzo,
            onSeleccionarEtapa = viewModel::seleccionarEtapa,
            onEstadoChange = viewModel::actualizarEstado,
            onDescripcionChange = viewModel::actualizarDescripcion,
            onTomarFoto = { equipoId -> tomarFoto(equipoId) },
            onGuardarAvance = viewModel::guardarAvance,
            onConfirmarEtapa = viewModel::confirmarEtapa,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ───────────────────────── Contenido reutilizable ─────────────────────────
// Sin dependencia de la faena: sirve también para post-faena o comprobaciones sueltas.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEquiposContent(
    uiState: EquiposUiState,
    onSeleccionarBuzo: (String) -> Unit,
    onSeleccionarEtapa: (EtapaComprobacion) -> Unit,
    onEstadoChange: (equipoId: String, EstadoEquipo) -> Unit,
    onDescripcionChange: (equipoId: String, String) -> Unit,
    onTomarFoto: (equipoId: String) -> Unit,
    onGuardarAvance: () -> Unit,
    onConfirmarEtapa: () -> Unit,
    modifier: Modifier = Modifier,
    miniatura: @Composable (uri: String, modifier: Modifier) -> Unit = { uriString, m ->
        val context = LocalContext.current
        val bitmap = remember(uriString) {
            runCatching {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto del equipo",
                modifier = m,
                contentScale = ContentScale.Crop
            )
        } else {
            Box(m.background(AquaBackground), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Image, contentDescription = "Foto adjunta", tint = AquaPrimary)
            }
        }
    }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AquaBackground)
            .padding(16.dp)
    ) {
        // Etapa
        TabRow(selectedTabIndex = uiState.etapa.ordinal) {
            EtapaComprobacion.values().forEach { etapa ->
                Tab(
                    selected = etapa == uiState.etapa,
                    onClick = { onSeleccionarEtapa(etapa) },
                    text = { Text(etapa.etiqueta, fontSize = 13.sp) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Buzos de la faena
        Text("Buzo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AquaTextPrimary)
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.buzos, key = { it.id }) { buzo ->
                FilterChip(
                    selected = buzo.id == uiState.buzoSeleccionadoId,
                    onClick = { onSeleccionarBuzo(buzo.id) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ScubaDiving,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("${buzo.nombre} (${buzo.equiposCompletos}/${buzo.totalEquipos})") }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        ResumenCard(uiState)

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            uiState.equipos.groupBy { it.categoria }.forEach { (categoria, items) ->
                item(key = "cat_$categoria") {
                    Text(
                        text = categoria.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AquaPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(items, key = { it.equipoId }) { equipo ->
                    EquipoCard(
                        equipo = equipo,
                        onEstadoChange = { onEstadoChange(equipo.equipoId, it) },
                        onDescripcionChange = { onDescripcionChange(equipo.equipoId, it) },
                        onTomarFoto = { onTomarFoto(equipo.equipoId) },
                        miniatura = miniatura
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (!uiState.todoCompleto) {
            Text(
                text = "Lo registrado con estado y foto queda guardado como evidencia. " +
                        "Debes completar el resto más tarde.",
                fontSize = 11.sp,
                color = AquaTextSecondary
            )
            Spacer(Modifier.height(6.dp))
        }

        Button(
            onClick = if (uiState.todoCompleto) onConfirmarEtapa else onGuardarAvance,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.todoCompleto) AquaPrimary else AquaSecondary
            )
        ) {
            Text(
                text = if (uiState.todoCompleto) "CONFIRMAR ${uiState.etapa.etiqueta.uppercase()}"
                else "GUARDAR AVANCE (${uiState.completos}/${uiState.equipos.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ───────────────────────── Resumen ─────────────────────────

@Composable
private fun ResumenCard(uiState: EquiposUiState) {
    val hayCritico = uiState.inutilizables > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hayCritico) SafetyCardBgDanger else Color.White
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            if (hayCritico) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = SafetyDanger, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${uiState.inutilizables} equipo(s) inutilizable(s)",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SafetyDanger
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConteoItem("Aceptables", uiState.aceptables, SafetySuccess)
                ConteoItem("Riesgosos", uiState.riesgosos, AmbarRiesgo)
                ConteoItem("Inutiliz.", uiState.inutilizables, SafetyDanger)
                ConteoItem("Sin revisar", uiState.sinRevisar, AquaTextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${uiState.corroborables} de ${uiState.equipos.size} con evidencia (estado + foto)" +
                        if (uiState.sinFoto > 0) " · ${uiState.sinFoto} sin foto" else "",
                fontSize = 12.sp,
                color = if (uiState.sinFoto > 0) SafetyDanger else AquaTextSecondary
            )
        }
    }
}

@Composable
private fun ConteoItem(etiqueta: String, valor: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$valor", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(etiqueta, fontSize = 10.sp, color = AquaTextSecondary)
    }
}

// ───────────────────────── Tarjeta de equipo ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipoCard(
    equipo: RevisionEquipo,
    onEstadoChange: (EstadoEquipo) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFoto: () -> Unit,
    miniatura: @Composable (String, Modifier) -> Unit
) {
    val opciones = listOf(
        EstadoEquipo.ACEPTABLE,
        EstadoEquipo.UTILIZABLE_RIESGOSO,
        EstadoEquipo.INUTILIZABLE
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (equipo.faltaFoto || equipo.faltaDescripcion)
            BorderStroke(1.dp, SafetyDanger) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(equipo.nombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AquaTextPrimary)
                    Text(
                        "ID ${equipo.equipoId} · ${equipo.descripcionUso}",
                        fontSize = 11.sp, color = AquaTextSecondary, lineHeight = 14.sp
                    )
                }
                EstadoBadge(equipo)
            }

            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                opciones.forEachIndexed { index, estado ->
                    SegmentedButton(
                        selected = equipo.estado == estado,
                        onClick = { onEstadoChange(estado) },
                        shape = SegmentedButtonDefaults.itemShape(index, opciones.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = estado.color().copy(alpha = 0.18f),
                            activeContentColor = estado.color()
                        )
                    ) {
                        Text(estado.etiqueta, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = equipo.descripcionEstado,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (equipo.estado == EstadoEquipo.ACEPTABLE || !equipo.tieneEstado)
                            "Descripción del estado (opcional)"
                        else "Descripción del estado (obligatoria)",
                        fontSize = 12.sp
                    )
                },
                isError = equipo.faltaDescripcion,
                minLines = 1,
                maxLines = 3,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                equipo.fotoUri?.let { uri ->
                    miniatura(uri, Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.width(10.dp))
                }
                OutlinedButton(
                    onClick = onTomarFoto,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (equipo.faltaFoto) SafetyDanger else AquaPrimary)
                ) {
                    Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (equipo.fotoUri == null) "Tomar foto (obligatoria)" else "Repetir foto", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(equipo: RevisionEquipo) {
    val (texto, color) = when {
        equipo.estaCompleto -> "Completo" to SafetySuccess
        equipo.faltaFoto -> "Falta foto" to SafetyDanger
        equipo.faltaDescripcion -> "Falta descripción" to AmbarRiesgo
        else -> "Pendiente" to AquaTextSecondary
    }
    Text(
        text = texto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}