package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.myapplication.data.EquipoChequeo
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(navController: NavController) {
    var buzoSeleccionado by remember {
        mutableStateOf(AppData.buzos.firstOrNull()?.nombre ?: "Sin buzo")
    }
    var dropdownBuzoExpanded by remember { mutableStateOf(false) }

    val listaEquipos = remember {
        mutableStateListOf<EquipoChequeo>().apply {
            addAll(AppData.obtenerEquiposDefault())
        }
    }

    val totalEquipos = listaEquipos.size
    val equiposValidados = listaEquipos.count { it.verificado }
    val esConforme = equiposValidados == totalEquipos

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Equipos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AquaPrimary)
            )
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
            // Selector de Buzo
            Text(
                text = "Buzo Asignado para Chequeo",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dropdownBuzoExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🤿 $buzoSeleccionado", color = AquaTextPrimary, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar buzo")
                    }
                }
                DropdownMenu(
                    expanded = dropdownBuzoExpanded,
                    onDismissRequest = { dropdownBuzoExpanded = false }
                ) {
                    AppData.buzos.forEach { buzo ->
                        DropdownMenuItem(
                            text = { Text("${buzo.nombre} (${buzo.empresa})") },
                            onClick = {
                                buzoSeleccionado = buzo.nombre
                                dropdownBuzoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Banner de Estado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esConforme) SafetyCardBgSuccess else SafetyCardBgDanger
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esConforme) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Estado",
                        tint = if (esConforme) SafetySuccess else SafetyDanger,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (esConforme) "EQUIPAMIENTO 100% VERIFICADO" else "REVISIÓN INCOMPLETA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (esConforme) SafetySuccess else SafetyDanger
                        )
                        Text(
                            text = "$equiposValidados de $totalEquipos implementos verificados en terreno",
                            fontSize = 12.sp,
                            color = AquaTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de marcar todos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de Verificación Obligatoria",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AquaTextPrimary
                )
                TextButton(
                    onClick = {
                        val marcar = !esConforme
                        listaEquipos.forEachIndexed { i, eq ->
                            listaEquipos[i] = eq.copy(verificado = marcar)
                        }
                    }
                ) {
                    Text(
                        text = if (esConforme) "Desmarcar todos" else "Marcar todos",
                        fontSize = 12.sp,
                        color = AquaPrimary
                    )
                }
            }

            // Lista de Checkbox
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(listaEquipos) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.verificado,
                                onCheckedChange = { isChecked ->
                                    listaEquipos[index] = item.copy(verificado = isChecked)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AquaSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.nombre,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = AquaTextPrimary
                                )
                                Text(
                                    text = item.descripcion,
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (esConforme) {
                        snackbarMessage = "Equipos de $buzoSeleccionado aprobados exitosamente"
                    } else {
                        snackbarMessage = "Atención: Debe completar la verificación de todos los equipos antes de la inmersión"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esConforme) AquaPrimary else AquaSecondary
                )
            ) {
                Text(
                    text = if (esConforme) "GUARDAR CONFORMIDAD DE EQUIPOS" else "GUARDAR ESTADO PARCIAL",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
