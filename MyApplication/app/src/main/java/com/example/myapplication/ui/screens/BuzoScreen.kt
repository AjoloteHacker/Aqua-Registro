
package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.BuzoViewModel

// Autor: Amaro Villalobos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzoScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: BuzoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var empresaDropdownExpanded by remember { mutableStateOf(false) }
    var categoriaDropdownExpanded by remember { mutableStateOf(false) }
    var vigenciaDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Registro de Buzos",
        subtitle = "${uiState.buzos.size} buzos enrolados",
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
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Buzo")
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
            // Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Buscar buzo por nombre, RUT, matrícula o empresa") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros rápidos por estado de salud
            Text(
                text = "Filtrar por estado:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AquaTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.filtroSalud == null,
                        onClick = { viewModel.onFiltroSaludChange(null) },
                        label = { Text("Todos (${uiState.buzos.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = AquaPrimary
                        )
                    )
                }
                item {
                    val count = uiState.buzos.count { it.estadoSalud == EstadoSalud.APTO }
                    FilterChip(
                        selected = uiState.filtroSalud == EstadoSalud.APTO,
                        onClick = { viewModel.onFiltroSaludChange(EstadoSalud.APTO) },
                        label = { Text("Aptos ($count)") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafetySuccess, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyCardBgSuccess,
                            selectedLabelColor = SafetySuccess
                        )
                    )
                }
                item {
                    val count = uiState.buzos.count { it.estadoSalud == EstadoSalud.PENDIENTE }
                    FilterChip(
                        selected = uiState.filtroSalud == EstadoSalud.PENDIENTE,
                        onClick = { viewModel.onFiltroSaludChange(EstadoSalud.PENDIENTE) },
                        label = { Text("Pendientes ($count)") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = AquaPrimary
                        )
                    )
                }
                item {
                    val count = uiState.buzos.count { it.estadoSalud == EstadoSalud.NO_APTO }
                    FilterChip(
                        selected = uiState.filtroSalud == EstadoSalud.NO_APTO,
                        onClick = { viewModel.onFiltroSaludChange(EstadoSalud.NO_APTO) },
                        label = { Text("No Aptos ($count)") },
                        leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = SafetyDanger, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyCardBgDanger,
                            selectedLabelColor = SafetyDanger
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Encabezado de la lista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Buzos Registrados (${uiState.buzosFiltrados.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AquaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de buzos
            if (uiState.buzosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.filtroSalud != null)
                                "No se encontraron buzos con los filtros aplicados"
                            else "Aún no hay buzos registrados. ¡Agrega el primero!",
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
                    items(uiState.buzosFiltrados, key = { it.id }) { buzo ->
                        BuzoCard(
                            buzo = buzo,
                            onClick = { viewModel.openDetalle(buzo) },
                            onEdit = { viewModel.openEditDialog(buzo) },
                            onDelete = { viewModel.solicitarEliminar(buzo) }
                        )
                    }
                }
            }
        }

        // Diálogo para Registrar / Editar Buzo
        if (uiState.showAddDialog) {
            val esModoEdicion = uiState.buzoAEditar != null
            AlertDialog(
                onDismissRequest = { viewModel.closeAddDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (esModoEdicion) Icons.Default.Edit else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (esModoEdicion) "Editar Buzo" else "Registrar Nuevo Buzo",
                            fontWeight = FontWeight.Bold,
                            color = AquaPrimary
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = uiState.nuevoNombre,
                            onValueChange = { viewModel.onNuevoNombreChange(it) },
                            label = { Text("Nombre y Apellidos *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.nuevoRut,
                            onValueChange = { viewModel.onNuevoRutChange(it) },
                            label = { Text("RUT (ej: 18.234.567-8) *") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector de Categoría DIRECTEMAR
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { categoriaDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Categoría: ${uiState.nuevaCategoria}", fontSize = 13.sp, color = AquaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                }
                            }
                            DropdownMenu(
                                expanded = categoriaDropdownExpanded,
                                onDismissRequest = { categoriaDropdownExpanded = false }
                            ) {
                                viewModel.categoriasDisponibles.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            viewModel.onNuevaCategoriaChange(cat)
                                            categoriaDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.nuevaMatricula,
                            onValueChange = { viewModel.onNuevaMatriculaChange(it) },
                            label = { Text("Matrícula Marítima DIRECTEMAR") },
                            placeholder = { Text("ej: Matrícula N° 2394") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.nuevoTelefono,
                            onValueChange = { viewModel.onNuevoTelefonoChange(it) },
                            label = { Text("Teléfono de Contacto") },
                            placeholder = { Text("+56 9 8451 2290") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector de Empresa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { empresaDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Empresa: ${uiState.empresaSeleccionada.take(20)}${if (uiState.empresaSeleccionada.length > 20) "..." else ""}",
                                            fontSize = 13.sp,
                                            color = AquaTextPrimary
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                    }
                                }
                                DropdownMenu(
                                    expanded = empresaDropdownExpanded,
                                    onDismissRequest = { empresaDropdownExpanded = false }
                                ) {
                                    uiState.empresasDisponibles.forEach { emp ->
                                        DropdownMenuItem(
                                            text = { Text(emp) },
                                            onClick = {
                                                viewModel.onEmpresaSeleccionadaChange(emp)
                                                empresaDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.openNuevaEmpresaDialog() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AquaSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Nueva Empresa", tint = AquaPrimary)
                            }
                        }

                        // Selector de Vigencia de Matrícula
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { vigenciaDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Vigencia Matrícula: ${uiState.nuevaVigencia}", fontSize = 13.sp, color = AquaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                }
                            }
                            DropdownMenu(
                                expanded = vigenciaDropdownExpanded,
                                onDismissRequest = { vigenciaDropdownExpanded = false }
                            ) {
                                viewModel.vigenciasDisponibles.forEach { vig ->
                                    DropdownMenuItem(
                                        text = { Text(vig) },
                                        onClick = {
                                            viewModel.onNuevaVigenciaChange(vig)
                                            vigenciaDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (uiState.error != null) {
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
                        onClick = { viewModel.guardarBuzo() },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Text(if (esModoEdicion) "Actualizar Buzo" else "Guardar Buzo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeAddDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para Registrar Nueva Empresa
        if (uiState.showNuevaEmpresaDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeNuevaEmpresaDialog() },
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
                    TextButton(onClick = { viewModel.closeNuevaEmpresaDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo Ficha Técnica / Detalle del Buzo
        uiState.buzoDetalle?.let { buzo ->
            AlertDialog(
                onDismissRequest = { viewModel.closeDetalle() },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AquaPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AquaPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(buzo.nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AquaTextPrimary)
                            Text("RUT: ${buzo.rut}", fontSize = 12.sp, color = AquaTextSecondary)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                        FichaItem(label = "Estado de Salud", valor = buzo.estadoSalud.label)
                        FichaItem(label = "Último Chequeo", valor = buzo.ultimoChequeo)
                        FichaItem(label = "Categoría", valor = buzo.categoria)
                        FichaItem(label = "Matrícula Marítima", valor = buzo.matricula)
                        FichaItem(label = "Vigencia Matrícula", valor = buzo.vigenciaMatricula)
                        FichaItem(label = "Empresa Contratista", valor = buzo.empresa)
                        FichaItem(label = "Teléfono de Contacto", valor = buzo.telefono)

                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = "Acciones Operativas Rápidas:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AquaTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.closeDetalle()
                                    navController.navigate(Screen.Equipos.route)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Equipos", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.closeDetalle()
                                    navController.navigate(Screen.Salud.route)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Salud", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.closeDetalle()
                            viewModel.openEditDialog(buzo)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDetalle() }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        // Diálogo de Confirmación para Eliminar
        uiState.buzoAEliminar?.let { buzo ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelarEliminar() },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = SafetyDanger, modifier = Modifier.size(36.dp)) },
                title = { Text("Confirmar Eliminación", fontWeight = FontWeight.Bold, color = SafetyDanger) },
                text = {
                    Text(
                        text = "¿Está seguro de que desea eliminar al buzo '${buzo.nombre}' (RUT: ${buzo.rut})? Sus registros y revisiones asociadas serán eliminados del sistema.",
                        fontSize = 14.sp,
                        color = AquaTextPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmarEliminar() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyDanger)
                    ) {
                        Text("Sí, Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelarEliminar() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun FichaItem(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = AquaTextSecondary, fontWeight = FontWeight.Medium)
        Text(text = valor, fontSize = 12.sp, color = AquaTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BuzoCard(
    buzo: Buzo,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AquaPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Buzo",
                        tint = AquaPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buzo.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AquaTextPrimary
                    )
                    Text(
                        text = "RUT: ${buzo.rut} · ${buzo.matricula}",
                        fontSize = 12.sp,
                        color = AquaTextSecondary
                    )
                }

                val (badgeColor, badgeBg) = when (buzo.estadoSalud) {
                    EstadoSalud.APTO -> Pair(SafetySuccess, SafetyCardBgSuccess)
                    EstadoSalud.NO_APTO -> Pair(SafetyDanger, SafetyCardBgDanger)
                    EstadoSalud.PENDIENTE -> Pair(AquaPrimary, AquaBackground)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = buzo.estadoSalud.label.take(12),
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Menú contextual 3 puntos
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver Ficha Detallada") },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar Buzo") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar Buzo", color = SafetyDanger) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SafetyDanger) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chips informativos de categoría y vigencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = AquaSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = buzo.categoria,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AquaPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = if (buzo.vigenciaMatricula.contains("Expirada", ignoreCase = true)) SafetyCardBgDanger else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Vigencia: ${buzo.vigenciaMatricula}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (buzo.vigenciaMatricula.contains("Expirada", ignoreCase = true)) SafetyDanger else AquaTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🏢 ${buzo.empresa}",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
                Text(
                    text = "📞 ${buzo.telefono}",
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
            }
        }
    }
}
