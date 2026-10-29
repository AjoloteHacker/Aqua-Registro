package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.SaludViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

private fun textoNumericoValido(texto: String, decimal: Boolean): Boolean =
    if (decimal) {
        texto.all { it.isDigit() || it == '.' || it == ',' } &&
                texto.count { it == '.' || it == ',' } <= 1
    } else {
        texto.all { it.isDigit() }
    }

@Composable
private fun CampoNumerico(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
    mostrarError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { nuevo ->
            if (textoNumericoValido(nuevo, decimal)) onValueChange(nuevo)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        isError = mostrarError,
        supportingText = { if (mostrarError) Text("Obligatorio") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaludScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: SaludViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    AquaScaffold(
        title = "Encuesta de Salud",
        subtitle = "Evaluación Biomédica y Aptitud",
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Faena a Evaluar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

            if (uiState.faenas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No hay faenas en curso. Autoriza una faena en «Registro de Faena» " +
                                "para registrar su encuesta de salud antes y después de la inmersión.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = AquaTextSecondary
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ScubaDiving,
                                    contentDescription = null,
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.faenaSeleccionada?.let { "${it.id} · ${it.buzoNombre}" }
                                        ?: "Seleccionar faena",
                                    color = AquaTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                        }
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        uiState.faenas.forEach { f ->
                            DropdownMenuItem(
                                text = { Text("${f.id} · ${f.buzoNombre} - ${f.tipoFaena}") },
                                onClick = {
                                    viewModel.seleccionarFaena(f.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MomentoSalud.entries.forEach { m ->
                        FilterChip(
                            selected = uiState.momento == m,
                            onClick = { viewModel.seleccionarMomento(m) },
                            label = { Text(m.etiqueta, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaSecondary.copy(alpha = 0.2f),
                                selectedLabelColor = AquaPrimary
                            )
                        )
                    }
                }

                uiState.bloqueo?.let { motivo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SafetyCardBgDanger)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = SafetyDanger,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = motivo, fontSize = 13.sp, color = SafetyDanger)
                        }
                    }
                }

                uiState.encuestaRegistrada?.let { reg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (reg.esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Ya registrada (${reg.momento.etiqueta}): " +
                                        if (reg.esApto) "APTO" else "NO APTO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (reg.esApto) SafetySuccess else SafetyDanger
                            )
                            Text(
                                text = "PA ${reg.presionArterial} mmHg · ${reg.frecuenciaCardiaca} lpm · " +
                                        "${reg.registradaPor} · ${reg.registradaEn}",
                                fontSize = 11.sp,
                                color = AquaTextSecondary
                            )
                            if (uiState.bloqueo == null) {
                                Text(
                                    text = "Si registras una nueva, reemplaza a esta.",
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.faenaSeleccionada != null && uiState.bloqueo == null) {
                // Card de dictamen preventivo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.esApto) Icons.Default.VerifiedUser else Icons.Default.WarningAmber,
                            contentDescription = "Estado Apto",
                            tint = if (uiState.esApto) SafetySuccess else SafetyDanger,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.esApto) "ESTADO: APTO PARA INMERSIÓN" else "ESTADO: NO APTO (ALERTA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (uiState.esApto) SafetySuccess else SafetyDanger
                            )
                            Text(
                                text = if (uiState.esApto)
                                    "El buzo cumple con todos los parámetros fisiológicos para faena."
                                else
                                    uiState.motivoNoApto,
                                fontSize = 12.sp,
                                color = AquaTextSecondary
                            )
                        }
                    }
                }

                // Sección 1: Signos vitales
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "1. Signos Vitales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.presionArterial,
                                onValueChange = { viewModel.onPresionChange(it) },
                                label = { Text("Presión (mmHg)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = !uiState.presionValida,
                                supportingText = {
                                    if (!uiState.presionValida) Text("Ej: 120/80")
                                }
                            )
                            OutlinedTextField(
                                value = uiState.frecuenciaCardiaca,
                                onValueChange = { viewModel.onFrecuenciaChange(it) },
                                label = { Text("Pulso (lpm)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = !uiState.pulsoValido,
                                supportingText = {
                                    if (!uiState.pulsoValido) Text("50-100 lpm")
                                }
                            )
                        }
                    }
                }

                // Sección 2: Cuestionario de Declaración de Salud
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "2. Cuestionario de Declaración de Salud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaPrimary
                        )

                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con Test de Esfuerzo vigente?",
                            checked = uiState.testEsfuerzo,
                            onCheckedChange = { viewModel.onTestEsfuerzoChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Libre de molestias o trastornos gastrointestinales?",
                            checked = uiState.gastroIntestinal,
                            onCheckedChange = { viewModel.onGastroIntestinalChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Posee evaluación cardiovascular aprobada y vigente?",
                            checked = uiState.cardiovascular,
                            onCheckedChange = { viewModel.onCardiovascularChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con examen de Audiometría vigente?",
                            checked = uiState.audiometria,
                            onCheckedChange = { viewModel.onAudiometriaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con examen de Optometría vigente?",
                            checked = uiState.optometria,
                            onCheckedChange = { viewModel.onOptometriaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con Electrocardiograma (ECG) al día?",
                            checked = uiState.electrocardiograma,
                            onCheckedChange = { viewModel.onElectrocardiogramaChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Durmió mínimo 7 a 8 horas continuas de descanso?",
                            checked = uiState.descansoAdecuado,
                            onCheckedChange = { viewModel.onDescansoChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Libre de consumo de alcohol o medicamentos las últimas 12 hrs?",
                            checked = uiState.sinSustancias,
                            onCheckedChange = { viewModel.onSinSustanciasChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Libre de congestión nasal, dolor de oídos o mareos?",
                            checked = uiState.sinCongestion,
                            onCheckedChange = { viewModel.onSinCongestionChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Se encuentra en condición física y anímica óptima para el buceo?",
                            checked = uiState.condicionOptima,
                            onCheckedChange = { viewModel.onCondicionOptimaChange(it) }
                        )
                    }
                }

                // Sección 3: Parámetros Antropométricos y Laboratorio
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "3. Parámetros Antropométricos y Laboratorio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaPrimary
                        )

                        // Peso y estatura con Selector de Rueda (sin teclado)
                        val opcionesPeso = remember { (45..140).map { it.toString() } }
                        val opcionesTalla = remember {
                            (145..210).map { String.format(Locale.US, "%.2f", it / 100.0) }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RuedaSelectorCard(
                                titulo = "Peso Corporal",
                                unidad = "kg",
                                icono = Icons.Default.MonitorWeight,
                                opciones = opcionesPeso,
                                valorSeleccionado = uiState.pesoKg.ifBlank { "78" },
                                onValorChange = { viewModel.onPesoChange(it) },
                                modifier = Modifier.weight(1f)
                            )
                            RuedaSelectorCard(
                                titulo = "Estatura (Talla)",
                                unidad = "m",
                                icono = Icons.Default.Height,
                                opciones = opcionesTalla,
                                valorSeleccionado = uiState.talla.ifBlank { "1.75" },
                                onValorChange = { viewModel.onTallaChange(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Resumen automático de IMC
                        val imc = remember(uiState.pesoVal, uiState.tallaVal) {
                            val p = uiState.pesoVal
                            val t = uiState.tallaVal
                            if (p != null && t != null && t > 0) p / (t * t) else null
                        }
                        if (imc != null) {
                            Surface(
                                color = AquaPrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "IMC Calculado: ${String.format(Locale.US, "%.1f", imc)} kg/m²",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AquaPrimary
                                    )
                                    Text(
                                        text = "${uiState.pesoKg} kg · ${uiState.talla} m",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AquaTextSecondary
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Perfil Lipídico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )

                        CampoNumerico(
                            value = uiState.colesterol,
                            onValueChange = { viewModel.onColesterolChange(it) },
                            label = "Colesterol Total (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.colesterolVal == null
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampoNumerico(
                                value = uiState.hdl,
                                onValueChange = { viewModel.onHdlChange(it) },
                                label = "HDL (mg/dl)",
                                modifier = Modifier.weight(1f),
                                mostrarError = uiState.mostrarErrores && uiState.hdlVal == null
                            )
                            CampoNumerico(
                                value = uiState.ldl,
                                onValueChange = { viewModel.onLdlChange(it) },
                                label = "LDL (mg/dl)",
                                modifier = Modifier.weight(1f),
                                mostrarError = uiState.mostrarErrores && uiState.ldlVal == null
                            )
                        }

                        CampoNumerico(
                            value = uiState.trigliceridos,
                            onValueChange = { viewModel.onTrigliceridosChange(it) },
                            label = "Triglicéridos (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.trigliceridosVal == null
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        PreguntaSaludSwitch(
                            pregunta = "¿Hemograma dentro de parámetros normales?",
                            checked = uiState.hemograma,
                            onCheckedChange = { viewModel.onHemogramaChange(it) }
                        )

                        CampoNumerico(
                            value = uiState.glicemiaBasal,
                            onValueChange = { viewModel.onGlicemiaChange(it) },
                            label = "Glicemia basal (mg/dl)",
                            modifier = Modifier.fillMaxWidth(),
                            mostrarError = uiState.mostrarErrores && uiState.glicemiaVal == null
                        )
                    }
                }

                // Botón de emisión de dictamen
                Button(
                    onClick = { viewModel.guardarDictamen() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.esApto) AquaPrimary else SafetyDanger
                    )
                ) {
                    Text(
                        text = if (uiState.esApto) "GUARDAR Y EMITIR DICTAMEN: APTO" else "GUARDAR DICTAMEN: NO APTO",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

            }

            // Sección 4: Historial de Registro Médico
            if (uiState.historialMedico.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalInformation,
                                    contentDescription = null,
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Historial de Registro Médico (${uiState.historialMedico.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AquaPrimary
                                )
                            }
                        }

                        uiState.historialMedico.forEachIndexed { index, chequeo ->
                            if (index > 0) {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))
                            }
                            RegistroMedicoItem(chequeo = chequeo)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Rueda selectora vertical ("Wheel Picker") para elegir Peso y Estatura sin usar teclado.
 */
@Composable
fun RuedaSelectorCard(
    titulo: String,
    unidad: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    opciones: List<String>,
    valorSeleccionado: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialIndex = remember(opciones) {
        opciones.indexOf(valorSeleccionado).coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // Sincronizar cuando cambia el elemento central al desplazarse la rueda
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                initialIndex
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index
                    ?: initialIndex
            }
        }
    }

    LaunchedEffect(centeredIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && centeredIndex in opciones.indices) {
            val nuevoValor = opciones[centeredIndex]
            if (nuevoValor != valorSeleccionado) {
                onValorChange(nuevoValor)
            }
        }
    }

    // Si el valor cambia desde afuera (ej. al reiniciar el formulario), centrar la rueda
    LaunchedEffect(valorSeleccionado) {
        val targetIdx = opciones.indexOf(valorSeleccionado)
        if (targetIdx >= 0 && targetIdx != centeredIndex && !listState.isScrollInProgress) {
            listState.scrollToItem(targetIdx)
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = AquaBackground,
        border = BorderStroke(1.dp, AquaPrimary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = AquaPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = titulo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp),
                contentAlignment = Alignment.Center
            ) {
                // Franja central indicadora de la rueda
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = AquaPrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, AquaPrimary)
                ) {}

                LazyColumn(
                    state = listState,
                    flingBehavior = flingBehavior,
                    contentPadding = PaddingValues(vertical = 43.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(opciones) { index, item ->
                        val isSelected = index == centeredIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    onValorChange(item)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$item $unidad",
                                fontSize = if (isSelected) 16.sp else 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) AquaPrimary else AquaTextSecondary,
                                modifier = Modifier.alpha(if (isSelected) 1f else 0.55f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Desliza la rueda",
                fontSize = 10.sp,
                color = AquaTextSecondary
            )
        }
    }
}

@Composable
fun RegistroMedicoItem(chequeo: Chequeo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AquaBackground)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${chequeo.id} · ${chequeo.buzoNombre.ifBlank { chequeo.buzoRut }}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AquaTextPrimary
            )
            Surface(
                color = if (chequeo.esApto) SafetyCardBgSuccess else SafetyCardBgDanger,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (chequeo.esApto) "APTO" else "NO APTO",
                    color = if (chequeo.esApto) SafetySuccess else SafetyDanger,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = "Faena: ${chequeo.faenaId.ifBlank { "General" }} (${chequeo.momento}) · ${chequeo.fechaHora.ifBlank { chequeo.fecha.toString() }}",
            fontSize = 11.sp,
            color = AquaTextSecondary
        )
        Text(
            text = "PA: ${chequeo.presionArterial} mmHg · Pulso: ${chequeo.frecuenciaCardiaca} lpm · Peso: ${chequeo.pesoKg} kg · Talla: ${chequeo.tallaM} m",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AquaTextPrimary
        )
        Text(
            text = "Colesterol: ${chequeo.colesterolTotal} (HDL ${chequeo.hdl} / LDL ${chequeo.ldl}) · Trig: ${chequeo.trigliceridos} · Glicemia: ${chequeo.glicemiaBasal} mg/dl",
            fontSize = 11.sp,
            color = AquaTextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = AquaPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Evaluado por: ${chequeo.evaluadoPor}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AquaPrimary
            )
        }
    }
}

@Composable
fun PreguntaSaludSwitch(
    pregunta: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = pregunta,
            fontSize = 13.sp,
            color = AquaTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AquaSecondary
            )
        )
    }
}