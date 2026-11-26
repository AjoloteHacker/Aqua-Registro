package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScubaDiving
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AquaPrimary
import com.example.myapplication.ui.theme.AquaTextPrimary
import com.example.myapplication.ui.theme.AquaTextSecondary

/** Opción genérica del selector de faenas, reutilizable en Equipos y Salud. */
data class OpcionSelectorFaena(
    val id: String,
    val titulo: String,
    val detalle: String = "",
    val meta: String? = null
)

/**
 * Selector de faena en curso con buscador integrado.
 *
 * El botón muestra siempre la faena seleccionada; al abrirlo se puede filtrar por
 * folio, buzo, tipo o centro sin depender del orden ni de la cantidad de faenas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFaena(
    opciones: List<OpcionSelectorFaena>,
    seleccionada: OpcionSelectorFaena?,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiquetaBusqueda: String = "Buscar por folio, buzo, tipo o centro"
) {
    var expandido by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScubaDiving,
                        contentDescription = null,
                        tint = AquaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = seleccionada?.titulo ?: "Seleccionar faena",
                            color = AquaTextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val resumen = listOfNotNull(
                            seleccionada?.detalle?.takeIf { it.isNotBlank() },
                            seleccionada?.meta
                        ).joinToString(" · ")
                        if (resumen.isNotBlank()) {
                            Text(
                                text = resumen,
                                color = AquaTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
                onBusquedaChange("")
            },
            modifier = Modifier.widthIn(min = 280.dp, max = 360.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                label = { Text(etiquetaBusqueda, fontSize = 11.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquaPrimary)
                },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { onBusquedaChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .focusRequester(focusRequester)
            )

            LaunchedEffect(expandido) {
                if (expandido) focusRequester.requestFocus()
            }

            Spacer(Modifier.height(4.dp))

            if (opciones.isEmpty()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = AquaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "No se encontraron faenas",
                        fontSize = 12.sp,
                        color = AquaTextSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    opciones.forEach { opcion ->
                        DropdownMenuItem(
                            leadingIcon = {
                                if (opcion.id == seleccionada?.id) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Faena seleccionada",
                                        tint = AquaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(18.dp))
                                }
                            },
                            text = {
                                Column {
                                    Text(
                                        text = opcion.titulo,
                                        fontSize = 12.sp,
                                        fontWeight = if (opcion.id == seleccionada?.id) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = AquaTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val detalle = listOfNotNull(
                                        opcion.detalle.takeIf { it.isNotBlank() },
                                        opcion.meta
                                    ).joinToString(" · ")
                                    if (detalle.isNotBlank()) {
                                        Text(
                                            text = detalle,
                                            fontSize = 11.sp,
                                            color = AquaTextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSeleccionar(opcion.id)
                                onBusquedaChange("")
                                expandido = false
                            }
                        )
                    }
                }
            }
        }
    }
}
