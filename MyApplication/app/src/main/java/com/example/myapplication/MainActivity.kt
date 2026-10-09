package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.ui.components.AquaDrawerContent
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación.
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
 * Composable raíz con la navegación y menú lateral.
 */
@Composable
fun AquaRegistroApp(
    repository: AquaRepository = AquaRepository.getInstance()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    val usuarioActivo by repository.prevencionistaActivo.collectAsState()
    val isDrawerEnabled = currentRoute != Screen.Login.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerEnabled,
        drawerContent = {
            if (isDrawerEnabled) {
                AquaDrawerContent(
                    currentRoute = currentRoute,
                    usuario = usuarioActivo,
                    onNavigate = { screen ->
                        coroutineScope.launch { drawerState.close() }
                        if (currentRoute != screen.route) {
                            if (screen.route == Screen.Menu.route) {
                                navController.popBackStack(Screen.Menu.route, inclusive = false)
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Menu.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    onLogout = {
                        coroutineScope.launch { drawerState.close() }
                        repository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }
            composable(Screen.Menu.route) {
                MenuScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Buzo.route) {
                BuzoScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Equipos.route) {
                EquiposScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Salud.route) {
                SaludScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Trabajo.route) {
                TrabajoScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Historial.route) {
                HistorialScreen(
                    navController = navController,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
    }
}