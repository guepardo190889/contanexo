package tech.blackdeath.contanexo.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Clase que representa las pantallas de la aplicación.
 */
sealed class Pantalla(val ruta: String) {
    data object Ingreso : Pantalla("ingreso")
    data object Inicio : Pantalla("inicio")
    data object Obligaciones : Pantalla("obligaciones")
    data object Expediente : Pantalla("expediente")
    data object Notificaciones : Pantalla("notificaciones")
}

/**
 * Clase que representa un elemento del menú lateral.
 */
data class DrawerItem(
    val pantalla: Pantalla,
    val label: String,
    val icon: ImageVector
)

/**
 * Lista de elementos del menú lateral.
 */
val menu = listOf(
    DrawerItem(Pantalla.Inicio, "Panel", Icons.Outlined.Home),
    DrawerItem(Pantalla.Obligaciones, "Obligaciones", Icons.AutoMirrored.Outlined.List),
    DrawerItem(Pantalla.Expediente, "Expediente", Icons.Outlined.Folder),
    DrawerItem(Pantalla.Notificaciones, "Avisos", Icons.Outlined.Notifications),
)
