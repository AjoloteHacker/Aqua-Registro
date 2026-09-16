package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aqua-Registro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Prevencionista: ${AppData.prevencionistaActivo.nombre}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AquaPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AquaPrimary,
                        selectedTextColor = AquaPrimary,
                        indicatorColor = AquaPrimary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("buzo")
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Buzos") },
                    label = { Text("Buzos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("trabajo")
                    },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = "Faena") },
                    label = { Text("Faena") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate("historial")
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta Informativa de Faena Activa
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AquaSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = AquaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppData.prevencionistaActivo.centro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AquaTextPrimary
                        )
                        Text(
                            text = "AquaChile · ${AppData.buzos.size} buzos registrados en turno",
                            fontSize = 13.sp,
                            color = AquaTextSecondary
                        )
                    }

                    Surface(
                        color = SafetyCardBgSuccess,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "OPERATIVO",
                            color = SafetySuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Módulos de Seguridad y Control",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AquaTextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            // Módulo 1: Registro de Buzos
            MenuOptionCard(
                titulo = "Ficha y Registro de Buzos",
                descripcion = "Administración de buzos, RUT, matrículas de la Armada y empresas contratistas.",
                icono = Icons.Default.PersonAdd,
                colorTema = AquaPrimary,
                badgeText = "${AppData.buzos.size} Buzos",
                onClick = { navController.navigate("buzo") }
            )

            // Módulo 2: Control de Equipos
            MenuOptionCard(
                titulo = "Control de Equipos y Seguridad",
                descripcion = "Checklist de tanques (200 BAR), regulador octopus, BCD, trajes y profundímetro.",
                icono = Icons.Default.CheckCircle,
                colorTema = AquaSecondary,
                badgeText = "9 Puntos",
                onClick = { navController.navigate("equipos") }
            )

            // Módulo 3: Encuesta Pre-Buceo
            MenuOptionCard(
                titulo = "Encuesta de Salud Pre-Buceo",
                descripcion = "Control de presión, pulsaciones y aptitud física/mental antes de la inmersión.",
                icono = Icons.Default.Favorite,
                colorTema = Color(0xFFE91E63),
                badgeText = "Médico",
                onClick = { navController.navigate("salud") }
            )

            // Módulo 4: Registro de Trabajo
            MenuOptionCard(
                titulo = "Registro de Trabajo y Faena",
                descripcion = "Asignación de buzo apto, tipo de maniobra (redes, peceras) y tiempo de inmersión.",
                icono = Icons.AutoMirrored.Filled.Assignment,
                colorTema = AquaPrimary,
                badgeText = "Autorización",
                onClick = { navController.navigate("trabajo") }
            )

            // Módulo 5: Historial
            MenuOptionCard(
                titulo = "Historial de Inmersiones",
                descripcion = "Bitácora digital centralizada de todas las faenas submarinas realizadas.",
                icono = Icons.Default.HistoryEdu,
                colorTema = AquaSecondary,
                badgeText = "${AppData.faenas.size} Registros",
                onClick = { navController.navigate("historial") }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuOptionCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    colorTema: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorTema.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorTema,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AquaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = colorTema.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = colorTema,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    fontSize = 12.sp,
                    color = AquaTextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = Color.LightGray
            )
        }
    }
}
