package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Rueda selectora vertical ("Wheel Picker") para elegir un valor sin usar teclado.
 */
@Composable
fun RuedaSelectorCard(
    titulo: String,
    unidad: String,
    icono: ImageVector,
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
