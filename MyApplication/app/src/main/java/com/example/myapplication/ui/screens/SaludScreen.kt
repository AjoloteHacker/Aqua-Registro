package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.model.ColorSemaforo
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.ui.components.AquaScaffold
import com.example.myapplication.ui.components.OpcionSelectorFaena
import com.example.myapplication.ui.components.RuedaSelectorCard
import com.example.myapplication.ui.components.SelectorFaena
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodels.SaludUiState
import com.example.myapplication.ui.viewmodels.SaludViewModel
import java.util.Locale


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

private fun RegistroFaena.aOpcionSelector(): OpcionSelectorFaena = OpcionSelectorFaena(
    id = id,
    titulo = "$id · $buzoNombre",
    detalle = buildString {
        if (socorristaNombre.isNotBlank()) append("Soc: ").append(socorristaNombre)
        if (tipoFaena.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(tipoFaena)
        }
        if (centroCultivo.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(centroCultivo)
        }
    },
    meta = fechaHora
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaludScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: SaludViewModel = viewModel()
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
                SelectorFaena(
                    opciones = uiState.faenasFiltradas.map { it.aOpcionSelector() },
                    seleccionada = uiState.faenaSeleccionada?.aOpcionSelector(),
                    busqueda = uiState.busquedaFaena,
                    onBusquedaChange = viewModel::onBusquedaFaenaChange,
                    onSeleccionar = viewModel::seleccionarFaena
                )

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

                if (uiState.tripulacionFaena.size > 1) {
                    Text(
                        text = "Buzo de la tripulación a evaluar en Semáforo:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AquaTextSecondary
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.tripulacionFaena.forEach { asig ->
                            val buzoItem = uiState.buzos.firstOrNull { it.id == asig.buzoId }
                            val tieneAlertaEdad = buzoItem?.bloqueadoPorEdadSinTestEsfuerzo == true
                            FilterChip(
                                selected = uiState.buzoSeleccionado?.id == asig.buzoId,
                                onClick = { viewModel.seleccionarBuzoEvaluado(asig.buzoId) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (tieneAlertaEdad) Icons.Default.WarningAmber else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (tieneAlertaEdad) SafetyDanger else AquaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    val edadTxt = buzoItem?.edadAnios?.let { " ($it años)" }.orEmpty()
                                    Text(
                                        text = "${asig.rol.corto}: ${asig.buzoNombre}$edadTxt",
                                        fontSize = 12.sp,
                                        fontWeight = if (uiState.buzoSeleccionado?.id == asig.buzoId) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (tieneAlertaEdad) SafetyCardBgDanger else AquaPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = if (tieneAlertaEdad) SafetyDanger else AquaPrimary
                                )
                            )
                        }
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
                // Tarjeta de Semáforo Industrial (Verde / Amarillo / Rojo) - Anexo A EA4 / EA5
                SemaforoIndustrialCard(uiState = uiState)

                // Sección 0: Validación Cruzada y Equipos Críticos (Semáforo Industrial)
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "0. Validación Cruzada · Semáforo Industrial",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AquaPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.buzoSeleccionado?.edadAnios?.let { edad ->
                                    Surface(
                                        color = if (edad > 45 && !uiState.testEsfuerzo) SafetyCardBgDanger else AquaBackground,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$edad años",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (edad > 45 && !uiState.testEsfuerzo) SafetyDanger else AquaTextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                uiState.buzoSeleccionado?.let { buzo ->
                                    Surface(
                                        color = if (buzo.puntajeCalbuco >= 70) SafetyCardBgSuccess else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Escuela: ${buzo.puntajeCalbuco}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (buzo.puntajeCalbuco >= 70) SafetySuccess else Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                uiState.imcBuzo?.let { imc ->
                                    Surface(
                                        color = if (imc in 19.0..30.0) SafetyCardBgSuccess else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "IMC: ${String.format(Locale.US, "%.1f", imc)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (imc in 19.0..30.0) SafetySuccess else Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con Test de Esfuerzo vigente? (Obligatorio si >45 años)",
                            checked = uiState.testEsfuerzo,
                            onCheckedChange = { viewModel.onTestEsfuerzoChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Cuenta con Examen Médico Preocupacional vigente?",
                            checked = uiState.examenPreocupacional,
                            onCheckedChange = { viewModel.onExamenPreocupacionalChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Compresor principal de aire operativo («Cumple»)?",
                            checked = uiState.compresorCumple,
                            onCheckedChange = { viewModel.onCompresorCumpleChange(it) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        PreguntaSaludSwitch(
                            pregunta = "¿Buzo de emergencia / socorrista en superficie («Cumple»)?",
                            checked = uiState.buzoEmergenciaCumple,
                            onCheckedChange = { viewModel.onBuzoEmergenciaCumpleChange(it) }
                        )
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

                        val opcionesPulso = remember { (45..130).map { it.toString() } }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.presionArterial,
                                onValueChange = { viewModel.onPresionChange(it) },
                                label = { Text("Presión (mmHg)") },
                                leadingIcon = { Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = AquaPrimary) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = !uiState.presionValida,
                                supportingText = {
                                    if (!uiState.presionValida) Text("Ej: 120/80")
                                }
                            )
                            RuedaSelectorCard(
                                titulo = "Pulso",
                                unidad = "lpm",
                                icono = Icons.Default.Favorite,
                                opciones = opcionesPulso,
                                valorSeleccionado = uiState.frecuenciaCardiaca.ifBlank { "72" },
                                onValorChange = { viewModel.onFrecuenciaChange(it) },
                                modifier = Modifier.weight(1f)
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

                // Sección 3: Perfil Lipídico y Laboratorio
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
                            text = "3. Perfil Lipídico y Laboratorio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AquaPrimary
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

                // Botón de emisión de dictamen con color de Semáforo
                val colorBotonDictamen = when (uiState.semaforo) {
                    ColorSemaforo.VERDE -> SafetySuccess
                    ColorSemaforo.AMARILLO -> Color(0xFFD97706)
                    ColorSemaforo.ROJO -> SafetyDanger
                }
                Button(
                    onClick = { viewModel.guardarDictamen() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorBotonDictamen)
                ) {
                    Icon(
                        imageVector = when (uiState.semaforo) {
                            ColorSemaforo.VERDE -> Icons.Default.CheckCircle
                            ColorSemaforo.AMARILLO -> Icons.Default.WarningAmber
                            ColorSemaforo.ROJO -> Icons.Default.Block
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (uiState.semaforo) {
                            ColorSemaforo.VERDE -> "GUARDAR PRE-CHEQUEO · SEMÁFORO VERDE"
                            ColorSemaforo.AMARILLO -> "GUARDAR PRE-CHEQUEO · SEMÁFORO AMARILLO"
                            ColorSemaforo.ROJO -> "REGISTRAR BLOQUEO · SEMÁFORO ROJO"
                        },
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

@Composable
fun RegistroMedicoItem(
    chequeo: Chequeo,
    pendienteSincronizar: Boolean = false
) {
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
            text = "PA: ${chequeo.presionArterial} mmHg · Pulso: ${chequeo.frecuenciaCardiaca} lpm",
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
        if (pendienteSincronizar) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pendiente de sincronizar (Room)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
fun SemaforoIndustrialCard(uiState: SaludUiState) {
    val semaforo = uiState.semaforo
    val (colorPrincipal, colorFondo) = when (semaforo) {
        ColorSemaforo.VERDE -> SafetySuccess to SafetyCardBgSuccess
        ColorSemaforo.AMARILLO -> Color(0xFFD97706) to Color(0xFFFEF3C7)
        ColorSemaforo.ROJO -> SafetyDanger to SafetyCardBgDanger
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(1.5.dp, colorPrincipal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (semaforo) {
                            ColorSemaforo.VERDE -> Icons.Default.VerifiedUser
                            ColorSemaforo.AMARILLO -> Icons.Default.WarningAmber
                            ColorSemaforo.ROJO -> Icons.Default.Block
                        },
                        contentDescription = semaforo.etiqueta,
                        tint = colorPrincipal,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = semaforo.etiqueta,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = colorPrincipal
                        )
                        Text(
                            text = "Buzo: ${uiState.buzoSeleccionado?.nombre ?: "Sin seleccionar"} (${uiState.edadBuzo} años)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaTextPrimary
                        )
                    }
                }

                // 3 Luces del Semáforo Industrial (Rojo, Amarillo, Verde)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LuzSemaforo(color = Color(0xFFEF4444), activa = semaforo == ColorSemaforo.ROJO)
                    LuzSemaforo(color = Color(0xFFF59E0B), activa = semaforo == ColorSemaforo.AMARILLO)
                    LuzSemaforo(color = Color(0xFF22C55E), activa = semaforo == ColorSemaforo.VERDE)
                }
            }

            if (uiState.motivosBloqueo.isNotEmpty()) {
                uiState.motivosBloqueo.forEach { motivo ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = SafetyDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = motivo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetyDanger
                        )
                    }
                }
            } else if (uiState.advertenciasSemaforo.isNotEmpty()) {
                uiState.advertenciasSemaforo.forEach { adv ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = adv,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            } else {
                Text(
                    text = semaforo.resumen,
                    fontSize = 12.sp,
                    color = AquaTextSecondary
                )
            }
        }
    }
}

@Composable
private fun LuzSemaforo(color: Color, activa: Boolean) {
    Box(
        modifier = Modifier
            .size(if (activa) 18.dp else 14.dp)
            .clip(CircleShape)
            .background(if (activa) color else color.copy(alpha = 0.22f))
            .then(
                if (activa) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
            )
    )
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