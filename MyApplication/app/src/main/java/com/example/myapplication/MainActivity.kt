package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * =============================================================================
 * MainActivity
 * =============================================================================
 * Actividad principal y punto de entrada de la aplicación Aqua-Registro.
 *
 * Responsabilidades:
 * - Inicializar el entorno de Compose mediante [setContent].
 * - Aplicar el tema global de la aplicación ([MyApplicationTheme]).
 * - Alojar el contenedor principal [Surface] y lanzar la navegación con [AquaRegistroApp].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AquaRegistroApp()
                }
            }
        }
    }
}

/**
 * =============================================================================
 * AquaRegistroApp
 * =============================================================================
 * Composable raíz que gestiona el grafo de navegación de la aplicación mediante
 * [NavHost] y [rememberNavController].
 *
 * Rutas configuradas en el flujo del sistema:
 * - "login"     : Pantalla de autenticación y alta de prevencionista ([LoginScreen]).
 * - "menu"      : Menú principal / Dashboard operativo en terreno ([MenuScreen]).
 * - "buzo"      : Ficha técnica, búsqueda y alta de buzos ([BuzoScreen]).
 * - "equipos"   : Checklist técnico de 9 implementos de seguridad ([EquiposScreen]).
 * - "salud"     : Encuesta biomédica pre-inmersión y dictamen de aptitud ([SaludScreen]).
 * - "trabajo"   : Planificación AST, asignación de buzo y autorización ([TrabajoScreen]).
 * - "historial" : Bitácora digitalizada centralizada de faenas ([HistorialScreen]).
 */
@Composable
fun AquaRegistroApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("menu") { MenuScreen(navController) }
        composable("buzo") { BuzoScreen(navController) }
        composable("equipos") { EquiposScreen(navController) }
        composable("salud") { SaludScreen(navController) }
        composable("trabajo") { TrabajoScreen(navController) }
        composable("historial") { HistorialScreen(navController) }
    }
}