package tech.blackdeath.contanexo.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val rutaActual by nav.currentBackStackEntryAsState()

    val isIngreso = rutaActual?.destination?.route == Pantalla.Ingreso.ruta || rutaActual == null

    if (isIngreso) {
        AppNavHost(nav)
    } else {
        DrawerShell(nav) { modifier: Modifier ->
            AppNavHost(nav, modifier)
        }
    }
}
