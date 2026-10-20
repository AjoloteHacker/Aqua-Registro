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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*

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
    repository: AquaRepository = AquaRepository.getInstance()
) {
    val prevencionista by repository.prevencionistaActivo.collectAsState()
    val buzos by repository.buzos.collectAsState()
    val faenas by repository.faenas.collectAsState()

    val buzosAptos = remember(buzos) { buzos.count { it.estadoSalud == EstadoSalud.APTO } }
    val faenasPendientes = remember(faenas) { faenas.count { !it.aprobadaPorSupervisor } }

    val snackbarHostState = remember { SnackbarHostState() }

    AquaScaffold(
        title = prevencionista?.nombre ?: "Aqua-Registro",
        subtitle = "Base: ${prevencionista?.centro ?: "AquaChile"} · SEREMI: ${prevencionista?.registroSeremi ?: "S/R"}",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = {
                repository.logout()
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
                            text = "AquaChile · ${buzos.size} buzos registrados (${buzosAptos} aptos)",
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

            // Nota informativa sobre el flujo unificado
            Surface(
                color = AquaPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AquaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Flujo Unificado: La faena incluye el control de 2 buzos, chequeo técnico de equipos y evaluación médica.",
                        fontSize = 11.sp,
                        color = AquaTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                // TARJETA 2: BUZOS (Ícono buzo / persona con badge '+')
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "2",
                    titulo = "Buzos",
                    subtitulo = "${buzos.size} Registrados",
                    icono = Icons.Default.Person,
                    badgeIcon = Icons.Default.Add,
                    colorTema = AquaPrimary,
                    onClick = { navController.navigate(Screen.Buzo.route) }
                )

                // TARJETA 3: HISTORIAL (Checklist / Faenas realizadas con revisión y aprobación)
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "3",
                    titulo = "Historial",
                    subtitulo = "${faenas.size} Faenas",
                    icono = Icons.Default.FactCheck,
                    badgeIcon = if (faenasPendientes > 0) Icons.Default.PendingActions else Icons.Default.Check,
                    colorTema = if (faenasPendientes > 0) SafetyWarning else AquaSecondary,
                    onClick = { navController.navigate(Screen.Historial.route) }
                )

                // TARJETA 4: CUADRILLA (Buzos aptos en turno / cuadrilla)
                BocetoCard(
                    modifier = Modifier.weight(1f),
                    numeroBoceto = "4",
                    titulo = "Cuadrilla",
                    subtitulo = "${buzosAptos} Aptos",
                    icono = Icons.Default.Groups,
                    badgeIcon = Icons.Default.CheckCircle,
                    colorTema = SafetySuccess,
                    onClick = { navController.navigate(Screen.Buzo.route) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
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
