package tech.blackdeath.contanexo.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tech.blackdeath.contanexo.interfaz.pantalla.ExpedientePantalla
import tech.blackdeath.contanexo.interfaz.pantalla.InicioPantalla
import tech.blackdeath.contanexo.interfaz.pantalla.LoginScreen
import tech.blackdeath.contanexo.interfaz.pantalla.AvisosScreen
import tech.blackdeath.contanexo.interfaz.pantalla.ObligacionesScreen

/**
 * Función que representa la navegación de la aplicación.
 */
@Composable
fun AppNavHost(nav: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = nav,
        startDestination = Pantalla.Ingreso.ruta,
        modifier = modifier
    ) {
        composable(Pantalla.Ingreso.ruta) {
            LoginScreen(
                onLogin = {
                    nav.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Ingreso.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Pantalla.Inicio.ruta) { InicioPantalla() }
        composable(Pantalla.Obligaciones.ruta) { ObligacionesScreen() }
        composable(Pantalla.Expediente.ruta) { ExpedientePantalla() }
        composable(Pantalla.Notificaciones.ruta) { AvisosScreen() }
    }
}