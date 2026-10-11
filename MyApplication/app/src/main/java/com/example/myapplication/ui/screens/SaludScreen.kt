package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.AppData
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.EstadoSalud
import com.example.myapplication.ui.theme.*
import java.time.LocalDate

// Rangos de ejemplo: ajústalos al protocolo médico/normativo que corresponda.
private val RANGO_PULSO = 50..100
private val RANGO_SISTOLICA = 90..139
private val RANGO_DIASTOLICA = 60..89

// Acepta solo dígitos y, si es decimal, un único separador (punto o coma).
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
fun SaludScreen(navController: NavController) {
    var buzoIndex by remember { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val buzoActual = AppData.buzos.getOrNull(buzoIndex)

    // Signos vitales
    var presionArterial by remember { mutableStateOf("120/80") }
    var frecuenciaCardiaca by remember { mutableStateOf("72") }

    // Antropometría y laboratorio (vacíos hasta que el prevencionista los ingrese)
    var pesoKg by remember { mutableStateOf("") }
    var talla by remember { mutableStateOf("") }
    var colesterol by remember { mutableStateOf("") }
    var hdl by remember { mutableStateOf("") }
    var ldl by remember { mutableStateOf("") }
    var trigliceridos by remember { mutableStateOf("") }
    var hemograma by remember { mutableStateOf(true) }
    var glicemiaBasal by remember { mutableStateOf("") }

    // Cuestionario
    var descansoAdecuado by remember { mutableStateOf(true) }
    var sinSustancias by remember { mutableStateOf(true) }
    var sinCongestion by remember { mutableStateOf(true) }
    var condicionOptima by remember { mutableStateOf(true) }
    var gastroIntestinal by remember { mutableStateOf(true) }
    var testEsfuerzo by remember { mutableStateOf(true) }
    var cardiovascular by remember { mutableStateOf(true) }
    var audiometria by remember { mutableStateOf(true) }
    var optometria by remember { mutableStateOf(true) }
    var electrocardiograma by remember { mutableStateOf(true) }

    // Se activa al intentar guardar con datos incompletos: marca los campos faltantes
    var mostrarErrores by remember { mutableStateOf(false) }

    // Validación de signos vitales
    val pulso = frecuenciaCardiaca.trim().toIntOrNull()
    val partesPresion = presionArterial.split("/")
    val sistolica = partesPresion.getOrNull(0)?.trim()?.toIntOrNull()
    val diastolica = if (partesPresion.size == 2) partesPresion[1].trim().toIntOrNull() else null

    val pulsoValido = pulso != null && pulso in RANGO_PULSO
    val presionValida = sistolica != null && diastolica != null &&
            sistolica in RANGO_SISTOLICA && diastolica in RANGO_DIASTOLICA
    val signosEnRango = pulsoValido && presionValida

    // Valores numéricos de los campos: null = vacío o inválido (deben ser mayores que 0)
    val pesoVal = pesoKg.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val tallaVal = talla.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val colesterolVal = colesterol.toIntOrNull()?.takeIf { it > 0 }
    val hdlVal = hdl.toIntOrNull()?.takeIf { it > 0 }
    val ldlVal = ldl.toIntOrNull()?.takeIf { it > 0 }
    val trigliceridosVal = trigliceridos.toIntOrNull()?.takeIf { it > 0 }
    val glicemiaVal = glicemiaBasal.toIntOrNull()?.takeIf { it > 0 }

    val cuestionarioOk = descansoAdecuado && sinSustancias && sinCongestion && condicionOptima &&
            gastroIntestinal && testEsfuerzo && cardiovascular && audiometria &&
            optometria && electrocardiograma

    val esApto = cuestionarioOk && signosEnRango

    val motivoNoApto = if (!cuestionarioOk) {
        "Existen factores de riesgo físico. Se prohíbe el buceo hasta normalización."
    } else {
        "Revise presión y pulso: están vacíos, mal escritos o fuera de rango."
    }

    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensajeResultado) {
        mensajeResultado?.let {
            snackbarHostState.showSnackbar(it)
            mensajeResultado = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuesta de Salud Pre-Buceo", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selector de Buzo
            Text(
                text = "Buzo a Evaluar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AquaTextPrimary
            )

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
                        Text(
                            text = buzoActual?.let { "${it.nombre} (${it.rut})" } ?: "Seleccionar buzo",
                            color = AquaTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                    }
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    AppData.buzos.forEachIndexed { idx, bz ->
                        DropdownMenuItem(
                            text = { Text("${bz.nombre} - ${bz.matricula}") },
                            onClick = {
                                buzoIndex = idx
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Banner Dinámico Apto / No Apto
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esApto) SafetyCardBgSuccess else SafetyCardBgDanger
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esApto) Icons.Default.VerifiedUser else Icons.Default.WarningAmber,
                        contentDescription = "Estado Apto",
                        tint = if (esApto) SafetySuccess else SafetyDanger,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (esApto) "ESTADO: APTO PARA INMERSIÓN" else "ESTADO: NO APTO (ALERTA)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (esApto) SafetySuccess else SafetyDanger
                        )
                        Text(
                            text = if (esApto)
                                "El buzo cumple con todos los parámetros fisiológicos para faena."
                            else
                                motivoNoApto,
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
                        text = "1. Signos Vitales Previos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AquaPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = presionArterial,
                            onValueChange = { presionArterial = it },
                            label = { Text("Presión (mmHg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = !presionValida,
                            supportingText = {
                                if (!presionValida) Text("Ej: 120/80")
                            }
                        )
                        OutlinedTextField(
                            value = frecuenciaCardiaca,
                            onValueChange = { nuevo ->
                                if (nuevo.all { it.isDigit() }) frecuenciaCardiaca = nuevo
                            },
                            label = { Text("Pulso (lpm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !pulsoValido,
                            supportingText = {
                                if (!pulsoValido) Text("${RANGO_PULSO.first}-${RANGO_PULSO.last} lpm")
                            }
                        )
                    }
                }
            }

            // Sección 2: Cuestionario de Condición Pre-Buceo
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
                        checked = testEsfuerzo,
                        onCheckedChange = { testEsfuerzo = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de molestias o trastornos gastrointestinales?",
                        checked = gastroIntestinal,
                        onCheckedChange = { gastroIntestinal = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Posee evaluación cardiovascular aprobada y vigente?",
                        checked = cardiovascular,
                        onCheckedChange = { cardiovascular = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Cuenta con examen de Audiometría vigente?",
                        checked = audiometria,
                        onCheckedChange = { audiometria = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Cuenta con examen de Optometría vigente?",
                        checked = optometria,
                        onCheckedChange = { optometria = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Cuenta con Electrocardiograma (ECG) al día?",
                        checked = electrocardiograma,
                        onCheckedChange = { electrocardiograma = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Asegura haber tenido descanso adecuado previo a la inmersión?",
                        checked = descansoAdecuado,
                        onCheckedChange = { descansoAdecuado = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Libre de consumo de alcohol u otras sustancias?",
                        checked = sinSustancias,
                        onCheckedChange = { sinSustancias = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Sin congestión nasal o cuadros respiratorios?",
                        checked = sinCongestion,
                        onCheckedChange = { sinCongestion = it }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    PreguntaSaludSwitch(
                        pregunta = "¿Se encuentra en condición física óptima para el buceo?",
                        checked = condicionOptima,
                        onCheckedChange = { condicionOptima = it }
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

                    // Peso y talla
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CampoNumerico(
                            value = pesoKg,
                            onValueChange = { pesoKg = it },
                            label = "Peso (kg)",
                            modifier = Modifier.weight(1f),
                            decimal = true,
                            mostrarError = mostrarErrores && pesoVal == null
                        )
                        CampoNumerico(
                            value = talla,
                            onValueChange = { talla = it },
                            label = "Talla (m)",
                            modifier = Modifier.weight(1f),
                            decimal = true,
                            mostrarError = mostrarErrores && tallaVal == null
                        )
                    }

                    Text(
                        text = "Perfil Lipídico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    // Colesterol total
                    CampoNumerico(
                        value = colesterol,
                        onValueChange = { colesterol = it },
                        label = "Colesterol Total (mg/dl)",
                        modifier = Modifier.fillMaxWidth(),
                        mostrarError = mostrarErrores && colesterolVal == null
                    )

                    // HDL y LDL juntos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CampoNumerico(
                            value = hdl,
                            onValueChange = { hdl = it },
                            label = "HDL (mg/dl)",
                            modifier = Modifier.weight(1f),
                            mostrarError = mostrarErrores && hdlVal == null
                        )
                        CampoNumerico(
                            value = ldl,
                            onValueChange = { ldl = it },
                            label = "LDL (mg/dl)",
                            modifier = Modifier.weight(1f),
                            mostrarError = mostrarErrores && ldlVal == null
                        )
                    }

                    // Triglicéridos
                    CampoNumerico(
                        value = trigliceridos,
                        onValueChange = { trigliceridos = it },
                        label = "Triglicéridos (mg/dl)",
                        modifier = Modifier.fillMaxWidth(),
                        mostrarError = mostrarErrores && trigliceridosVal == null
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // Hemograma
                    PreguntaSaludSwitch(
                        pregunta = "¿Hemograma dentro de parámetros normales?",
                        checked = hemograma,
                        onCheckedChange = { hemograma = it }
                    )

                    // Glicemia
                    CampoNumerico(
                        value = glicemiaBasal,
                        onValueChange = { glicemiaBasal = it },
                        label = "Glicemia basal (mg/dl)",
                        modifier = Modifier.fillMaxWidth(),
                        mostrarError = mostrarErrores && glicemiaVal == null
                    )
                }
            }

            // Botón de Registro
            Button(
                onClick = {
                    val buzo = buzoActual
                    if (buzo == null) {
                        mensajeResultado = "Seleccione un buzo antes de registrar el chequeo."
                        return@Button
                    }

                    // Si falta cualquier dato, no se guarda nada y se marcan los campos
                    if (pulso == null || sistolica == null || diastolica == null ||
                        pesoVal == null || tallaVal == null || colesterolVal == null ||
                        hdlVal == null || ldlVal == null || trigliceridosVal == null ||
                        glicemiaVal == null
                    ) {
                        mostrarErrores = true
                        mensajeResultado = "Complete todos los campos obligatorios antes de guardar."
                        return@Button
                    }

                    val fechaChequeo = LocalDate.now()

                    val nuevoChequeo = Chequeo(
                        id = "${AppData.chequeos.size + 1}",
                        buzoRut = buzo.rut,
                        fecha = fechaChequeo,
                        presionArterial = presionArterial.trim(),
                        frecuenciaCardiaca = pulso,
                        pesoKg = pesoVal,
                        tallaM = tallaVal,
                        colesterolTotal = colesterolVal,
                        hdl = hdlVal,
                        ldl = ldlVal,
                        trigliceridos = trigliceridosVal,
                        hemograma = hemograma,
                        glicemiaBasal = glicemiaVal,
                        esApto = esApto
                    )

                    AppData.chequeos.add(nuevoChequeo)

                    buzo.estadoSalud = if (esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO
                    buzo.ultimoChequeo = fechaChequeo

                    mensajeResultado = if (esApto)
                        "Chequeo registrado: ${buzo.nombre} declarado APTO"
                    else
                        "Alerta: ${buzo.nombre} registrado como NO APTO."

                    // Reiniciar el formulario para el siguiente chequeo
                    presionArterial = "120/80"
                    frecuenciaCardiaca = "72"
                    pesoKg = ""
                    talla = ""
                    colesterol = ""
                    hdl = ""
                    ldl = ""
                    trigliceridos = ""
                    glicemiaBasal = ""
                    mostrarErrores = false

                    hemograma = true
                    descansoAdecuado = true
                    sinSustancias = true
                    sinCongestion = true
                    condicionOptima = true
                    gastroIntestinal = true
                    testEsfuerzo = true
                    cardiovascular = true
                    audiometria = true
                    optometria = true
                    electrocardiograma = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esApto) AquaPrimary else SafetyDanger
                )
            ) {
                Text(
                    text = if (esApto) "GUARDAR Y EMITIR DICTAMEN: APTO" else "GUARDAR DICTAMEN: NO APTO",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
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