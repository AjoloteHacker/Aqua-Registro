package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var centroDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AquaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AquaPrimary, AquaSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Logo Aqua-Registro",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Aqua-Registro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AquaPrimary
            )

            Text(
                text = "Seguridad y Control de Buceo · AquaChile",
                style = MaterialTheme.typography.bodyMedium,
                color = AquaTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            @Suppress("DEPRECATION")
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.White,
                contentColor = AquaPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = AquaPrimary,
                    unselectedContentColor = AquaTextSecondary
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registrar Usuario", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = AquaPrimary,
                    unselectedContentColor = AquaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.selectedTab == 0) {
                        Text(
                            text = "Acceso Prevencionista",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.rutLogin,
                            onValueChange = { viewModel.onRutLoginChange(it) },
                            label = { Text("RUT o Correo del Prevencionista") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = "RUT", tint = AquaPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.passwordLogin,
                            onValueChange = { viewModel.onPasswordLoginChange(it) },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordLoginVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.passwordLoginVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar Visibilidad"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.passwordLoginVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SafetyCardBgDanger,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = SafetyDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.login {
                                    navController.navigate(Screen.Menu.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            Text(
                                text = "INGRESAR AL SISTEMA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.clickable {
                                viewModel.selectTab(1)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¿No tienes cuenta? ",
                                fontSize = 12.sp,
                                color = AquaTextSecondary
                            )
                            Text(
                                text = "Regístrate aquí",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = AquaBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AquaSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Demo: 12.345.678-9 / admin123",
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary
                                )
                            }
                        }

                    } else {
                        Text(
                            text = "Alta de Prevencionista",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaPrimary
                        )

                        Text(
                            text = "Acreditación y datos institucionales",
                            style = MaterialTheme.typography.bodySmall,
                            color = AquaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.regNombre,
                            onValueChange = { viewModel.onRegNombreChange(it) },
                            label = { Text("Nombre Completo *") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Nombre", tint = AquaPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.regRut,
                                onValueChange = { viewModel.onRegRutChange(it) },
                                label = { Text("RUT *") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = uiState.regSeremi,
                                onValueChange = { viewModel.onRegSeremiChange(it) },
                                label = { Text("N° Reg. SEREMI *") },
                                placeholder = { Text("PR-12345-X", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.regCorreo,
                            onValueChange = { viewModel.onRegCorreoChange(it) },
                            label = { Text("Correo Institucional *") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Correo", tint = AquaPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { centroDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Base / Centro Operativo", fontSize = 10.sp, color = AquaTextSecondary)
                                        Text(uiState.regCentro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AquaTextPrimary)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                }
                            }
                            DropdownMenu(
                                expanded = centroDropdownExpanded,
                                onDismissRequest = { centroDropdownExpanded = false }
                            ) {
                                uiState.centrosDisponibles.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.onRegCentroChange(c)
                                            centroDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.regPassword,
                            onValueChange = { viewModel.onRegPasswordChange(it) },
                            label = { Text("Contraseña (mínimo 4 caracteres) *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleRegPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar Visibilidad Contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.regPasswordConfirm,
                            onValueChange = { viewModel.onRegPasswordConfirmChange(it) },
                            label = { Text("Confirmar Contraseña *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Confirmar Contraseña", tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleRegPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar Visibilidad Confirmación"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onRegAceptaTerminosChange(!uiState.regAceptaTerminos) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.regAceptaTerminos,
                                onCheckedChange = { viewModel.onRegAceptaTerminosChange(it) },
                                colors = CheckboxDefaults.colors(checkedColor = AquaPrimary)
                            )
                            Text(
                                text = "Acepto el compromiso de confidencialidad de datos, respaldo digital y protocolos DIRECTEMAR.",
                                fontSize = 11.sp,
                                color = AquaTextSecondary,
                                lineHeight = 14.sp
                            )
                        }

                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SafetyCardBgDanger,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = SafetyDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.registrar {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CREAR CUENTA DE PREVENCIONISTA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.clickable {
                                viewModel.selectTab(0)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¿Ya tienes cuenta? ",
                                fontSize = 12.sp,
                                color = AquaTextSecondary
                            )
                            Text(
                                text = "Inicia sesión aquí",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Versión 2.0 MVVM · Grupo Smiling Friends",
                style = MaterialTheme.typography.labelSmall,
                color = AquaTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.successDialogMessage != null) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.clearMessages()
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                title = { Text("¡Registro Exitoso!", fontWeight = FontWeight.Bold, color = SafetySuccess) },
                text = { Text(uiState.successDialogMessage ?: "") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearMessages()
                            navController.navigate(Screen.Menu.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text("Ingresar al Menú Principal")
                    }
                }
            )
        }
    }
}
