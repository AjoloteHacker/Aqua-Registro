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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.EmpresaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: EmpresaViewModel = viewModel()
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
        title = "Empresas Contratistas",
        subtitle = "${uiState.empresas.size} empresas registradas",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = AquaSecondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Empresa")
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
            // Encabezado de la lista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Empresas Registradas (${uiState.empresas.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AquaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.empresas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aún no hay empresas registradas. ¡Agrega la primera!",
                            color = AquaTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.empresas) { empresa ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = empresa,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = AquaTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para Registrar Nueva Empresa
        if (uiState.showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAddDialog() },
                title = { Text("Nueva Empresa Contratista", fontWeight = FontWeight.Bold, color = AquaPrimary) },
                text = {
                    Column {
                        Text("Ingresa el nombre de la empresa de buceo para añadirla a los registros:", fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.nuevaEmpresaNombre,
                            onValueChange = { viewModel.onNuevaEmpresaNombreChange(it) },
                            label = { Text("Nombre de la empresa *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (uiState.error != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = SafetyDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.guardarNuevaEmpresa() },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Añadir Empresa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeAddDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
