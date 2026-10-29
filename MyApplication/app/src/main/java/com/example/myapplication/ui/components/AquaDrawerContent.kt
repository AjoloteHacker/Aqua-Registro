package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.*

/**
 * Menú lateral con perfil de usuario y accesos directos.
 */
@Composable
fun AquaDrawerContent(
    currentRoute: String,
    usuario: Prevencionista?,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AquaPrimary, AquaSecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aquachile_emblem),
                        contentDescription = "AquaChile Emblem",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Aqua-Registro",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Image(
                            painter = painterResource(id = R.drawable.logo_aquachile_white),
                            contentDescription = "Logo AquaChile",
                            modifier = Modifier.height(15.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = usuario?.fotoUrl ?: "https://randomuser.me/api/portraits/men/75.jpg",
                        contentDescription = "Foto ${usuario?.nombre}",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = usuario?.nombre ?: "Prevencionista de Turno",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "SEREMI: ${usuario?.registroSeremi ?: "S/R"} · ${usuario?.centro ?: "Base AquaChile"}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MÓDULOS DE SEGURIDAD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AquaTextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Screen.drawerScreens.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = screen.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    icon = {
                        screen.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) AquaPrimary else AquaTextSecondary
                            )
                        }
                    },
                    selected = isSelected,
                    onClick = { onNavigate(screen) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AquaPrimary.copy(alpha = 0.12f),
                        selectedTextColor = AquaPrimary,
                        unselectedTextColor = AquaTextPrimary,
                        unselectedIconColor = AquaTextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        NavigationDrawerItem(
            label = {
                Text(
                    text = "Cerrar Sesión",
                    color = SafetyDanger,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar Sesión",
                    tint = SafetyDanger
                )
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}
