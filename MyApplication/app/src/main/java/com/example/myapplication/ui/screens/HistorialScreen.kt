package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.RegistroFaena
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }

    val faenasFiltradas = remember(searchQuery, selectedFilter, AppData.faenas.size) {
        AppData.faenas.filter { faena ->
            val coincideTexto = searchQuery.isBlank() ||
                faena.buzoNombre.contains(searchQuery, ignoreCase = true) ||
                faena.centroCultivo.contains(searchQuery, ignoreCase = true) ||
                faena.tipoFaena.contains(searchQuery, ignoreCase = true) ||
                faena.empresa.contains(searchQuery, ignoreCase = true)

            val coincideFiltro = when (selectedFilter) {
                "En Curso" -> faena.estado.contains("Curso", ignoreCase = true)
                "Completada" -> faena.estado.contains("Completada", ignoreCase = true)
                else -> true
            }

            coincideTexto && coincideFiltro
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Inmersiones", color = Color.White, fontWeight = FontWeight.Bold) },
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
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por buzo, centro o tipo de faena") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chips de Filtro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todas", "En Curso", "Completada").forEach { filtro ->
                    FilterChip(
                        selected = selectedFilter == filtro,
                        onClick = { selectedFilter = filtro },
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
                text = "Registros de Bitácora (${faenasFiltradas.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (faenasFiltradas.isEmpty()) {
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
                    items(faenasFiltradas) { faena ->
                        FaenaCard(faena = faena)
                    }
                }
            }
        }
    }
}

@Composable
fun FaenaCard(faena: RegistroFaena) {
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

                val enCurso = faena.estado.contains("Curso", ignoreCase = true)
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

            Text(
                text = "🤿 ${faena.buzoNombre}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )
            Text(
                text = "🏢 ${faena.empresa}",
                fontSize = 12.sp,
                color = AquaTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🛠️ ${faena.tipoFaena}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AquaTextPrimary
            )
            Text(
                text = "📍 ${faena.centroCultivo}",
                fontSize = 12.sp,
                color = AquaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
            }
        }
    }
}
