package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rutas de navegación de la aplicación.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val badge: String? = null
) {
    object Login : Screen("login", "Inicio de Sesión")
    object Menu : Screen("menu", "Panel Principal", Icons.Default.Dashboard)
    object Buzo : Screen("buzo", "Registro de Buzos", Icons.Default.People)
    object Equipos : Screen("equipos", "Control de Equipos", Icons.Default.CheckCircle)
    object Salud : Screen("salud", "Encuesta de Salud", Icons.Default.Favorite)
    object Trabajo : Screen("trabajo", "Registro de Faena", Icons.AutoMirrored.Filled.Assignment)
    object Historial : Screen("historial", "Historial de Inmersiones", Icons.Default.History)
    object Empresas : Screen("empresas", "Empresas Contratistas", Icons.Default.Business)

    companion object {
        val drawerScreens = listOf(Menu, Buzo, Equipos, Salud, Trabajo, Historial, Empresas)
    }
}