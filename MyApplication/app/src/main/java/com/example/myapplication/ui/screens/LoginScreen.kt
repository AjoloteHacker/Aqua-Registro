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
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.Prevencionista
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    // 0 = Iniciar Sesión, 1 = Registrar Usuario
    var selectedTab by remember { mutableIntStateOf(0) }

    // Estado Iniciar Sesión
    var rutLogin by remember { mutableStateOf("12.345.678-9") }
    var passwordLogin by remember { mutableStateOf("admin123") }
    var passwordLoginVisible by remember { mutableStateOf(false) }

    // Estado Registro de Usuario (Prevencionista)
    var regNombre by remember { mutableStateOf("") }
    var regRut by remember { mutableStateOf("") }
    var regSeremi by remember { mutableStateOf("") }
    var regCorreo by remember { mutableStateOf("") }
    var regCentro by remember { mutableStateOf(AppData.centrosCultivo.firstOrNull() ?: "Centro Melinka 1 · Fiordo Aisén") }
    var centroDropdownExpanded by remember { mutableStateOf(false) }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordConfirm by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regAceptaTerminos by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successDialogMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AquaBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header con Identidad Visual Aqua-Registro
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

            // Selector de Pestañas: Iniciar Sesión vs Registrar Usuario
            @Suppress("DEPRECATION")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = AquaPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                    },
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
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                    },
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

            // Tarjeta de Formulario Principal
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
                    if (selectedTab == 0) {
                        // ==========================================
                        // PESTAÑA 1: INICIAR SESIÓN
                        // ==========================================
                        Text(
                            text = "Acceso Prevencionista",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = rutLogin,
                            onValueChange = {
                                rutLogin = it
                                errorMessage = null
                            },
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
                            value = passwordLogin,
                            onValueChange = {
                                passwordLogin = it
                                errorMessage = null
                            },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordLoginVisible = !passwordLoginVisible }) {
                                    Icon(
                                        imageVector = if (passwordLoginVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar Visibilidad"
                                    )
                                }
                            },
                            visualTransformation = if (passwordLoginVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SafetyCardBgDanger,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = SafetyDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val usuarioEncontrado = AppData.usuarios.firstOrNull {
                                    (it.rut.trim().equals(rutLogin.trim(), ignoreCase = true) ||
                                     it.correo.trim().equals(rutLogin.trim(), ignoreCase = true)) &&
                                    it.contrasena == passwordLogin
                                }

                                if (usuarioEncontrado != null) {
                                    AppData.prevencionistaActivo = usuarioEncontrado
                                    navController.navigate("menu") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = "Credenciales inválidas. Verifique su RUT y contraseña o regístrese."
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

                        // Acceso rápido a registro
                        Row(
                            modifier = Modifier.clickable {
                                selectedTab = 1
                                errorMessage = null
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

                        // Recordatorio de usuario demo
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
                        // ==========================================
                        // PESTAÑA 2: REGISTRAR USUARIO (PREVENCIONISTA)
                        // ==========================================
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
                            value = regNombre,
                            onValueChange = {
                                regNombre = it
                                errorMessage = null
                            },
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
                                value = regRut,
                                onValueChange = {
                                    regRut = it
                                    errorMessage = null
                                },
                                label = { Text("RUT *") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = regSeremi,
                                onValueChange = {
                                    regSeremi = it
                                    errorMessage = null
                                },
                                label = { Text("N° Reg. SEREMI *") },
                                placeholder = { Text("PR-12345-X", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regCorreo,
                            onValueChange = {
                                regCorreo = it
                                errorMessage = null
                            },
                            label = { Text("Correo Institucional *") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Correo", tint = AquaPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selector de Centro de Cultivo / Base
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
                                        Text(regCentro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AquaTextPrimary)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                }
                            }
                            DropdownMenu(
                                expanded = centroDropdownExpanded,
                                onDismissRequest = { centroDropdownExpanded = false }
                            ) {
                                AppData.centrosCultivo.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontSize = 12.sp) },
                                        onClick = {
                                            regCentro = c
                                            centroDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                errorMessage = null
                            },
                            label = { Text("Contraseña (mínimo 4 caracteres) *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = AquaPrimary)
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPasswordConfirm,
                            onValueChange = {
                                regPasswordConfirm = it
                                errorMessage = null
                            },
                            label = { Text("Confirmar Contraseña *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Confirmar Contraseña", tint = AquaPrimary)
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Checkbox de términos y confidencialidad DIRECTEMAR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { regAceptaTerminos = !regAceptaTerminos },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = regAceptaTerminos,
                                onCheckedChange = { regAceptaTerminos = it },
                                colors = CheckboxDefaults.colors(checkedColor = AquaPrimary)
                            )
                            Text(
                                text = "Acepto el compromiso de confidencialidad de datos, respaldo digital y protocolos DIRECTEMAR.",
                                fontSize = 11.sp,
                                color = AquaTextSecondary,
                                lineHeight = 14.sp
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SafetyCardBgDanger,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = SafetyDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (regNombre.isBlank() || regRut.isBlank() || regSeremi.isBlank() || regCorreo.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Por favor complete todos los campos obligatorios (*)."
                                    return@Button
                                }
                                if (regPassword.length < 4) {
                                    errorMessage = "La contraseña debe tener al menos 4 caracteres."
                                    return@Button
                                }
                                if (regPassword != regPasswordConfirm) {
                                    errorMessage = "Las contraseñas no coinciden."
                                    return@Button
                                }
                                if (!regAceptaTerminos) {
                                    errorMessage = "Debe aceptar el compromiso de confidencialidad y protocolos DIRECTEMAR."
                                    return@Button
                                }
                                if (AppData.usuarios.any { it.rut.trim().equals(regRut.trim(), ignoreCase = true) }) {
                                    errorMessage = "Ya existe un prevencionista registrado con el RUT $regRut."
                                    return@Button
                                }

                                val nuevoPrev = Prevencionista(
                                    nombre = regNombre.trim(),
                                    rut = regRut.trim(),
                                    correo = regCorreo.trim(),
                                    registroSeremi = regSeremi.trim(),
                                    centro = regCentro,
                                    contrasena = regPassword
                                )
                                AppData.usuarios.add(nuevoPrev)
                                AppData.prevencionistaActivo = nuevoPrev
                                successDialogMessage = "Cuenta creada con éxito para ${nuevoPrev.nombre} (SEREMI: ${nuevoPrev.registroSeremi})"
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
                                selectedTab = 0
                                errorMessage = null
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
                text = "Versión 1.0 MVP · Grupo Smiling Friends",
                style = MaterialTheme.typography.labelSmall,
                color = AquaTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Diálogo de éxito al registrar usuario
        if (successDialogMessage != null) {
            AlertDialog(
                onDismissRequest = {
                    successDialogMessage = null
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                title = { Text("¡Registro Exitoso!", fontWeight = FontWeight.Bold, color = SafetySuccess) },
                text = { Text(successDialogMessage ?: "") },
                confirmButton = {
                    Button(
                        onClick = {
                            successDialogMessage = null
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
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
