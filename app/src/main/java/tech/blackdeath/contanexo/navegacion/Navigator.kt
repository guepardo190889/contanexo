package tech.blackdeath.contanexo.navegacion

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

/**
 * Interfaz para navegar entre pantallas.
 */
interface AppNavigator {
    fun go(pantalla: Pantalla)
    fun back(): Boolean
}

/**
 * Proveedor del navigator para TODA la app.
 */
val LocalAppNavigator = staticCompositionLocalOf <AppNavigator> {
    error("AppNavigator no inicializado")
}

/**
 * Permite navegar a una pantalla sin repetir la actual.
 */
fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}