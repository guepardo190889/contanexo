package tech.blackdeath.contanexo.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tech.blackdeath.contanexo.interfaz.pantalla.ExpedientePantalla
import tech.blackdeath.contanexo.interfaz.pantalla.InicioPantalla
import tech.blackdeath.contanexo.interfaz.pantalla.IngresoPantalla
import tech.blackdeath.contanexo.interfaz.pantalla.AvisosPantalla
import tech.blackdeath.contanexo.interfaz.pantalla.TareasPantalla

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
            IngresoPantalla(
                onLogin = {
                    nav.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Ingreso.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Pantalla.Inicio.ruta) { InicioPantalla() }
        composable(Pantalla.Tareas.ruta) { TareasPantalla() }
        composable(Pantalla.Expediente.ruta) { ExpedientePantalla() }
        composable(Pantalla.Notificaciones.ruta) { AvisosPantalla() }
    }
}