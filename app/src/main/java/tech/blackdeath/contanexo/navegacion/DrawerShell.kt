package tech.blackdeath.contanexo.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

/**
 * Función que representa el shell del menú lateral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerShell(
    nav: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val rutaActual = nav.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "ContaNexo",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                menu.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        selected = rutaActual == item.pantalla.ruta,
                        onClick = {
                            nav.navigate(item.pantalla.ruta) {
                                launchSingleTop = true
                                popUpTo(nav.graph.startDestinationId) { inclusive = false }
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(menu.find { it.pantalla.ruta == rutaActual }?.label ?: "ContaNexo") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            content(Modifier.padding(padding))
        }
    }
}
