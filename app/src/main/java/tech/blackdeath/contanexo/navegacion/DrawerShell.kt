package tech.blackdeath.contanexo.navegacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.empresa.EmpresaModel
import tech.blackdeath.contanexo.dato.empresa.EmpresaRepositoryMock
import tech.blackdeath.contanexo.dato.empresa.rememberEmpresaState

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

    val empresaRepo = remember { EmpresaRepositoryMock() }
    var empresas by remember { mutableStateOf<List<EmpresaModel>>(emptyList()) }
    val empresaState = rememberEmpresaState()

    var abrirPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        empresas = empresaRepo.listar()
        if (empresaState.actual == null) empresaState.actual = empresas.firstOrNull()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(
                    empresa = empresaState.actual,
                    onCambiar = {
                        scope.launch { drawerState.close() }
                        abrirPicker = true
                    }
                )
                HorizontalDivider()

//                Text(
//                    "ContaNexo",
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.padding(16.dp)
//                )
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
                    title = {
                        Text(
                            menu.find { it.pantalla.ruta == rutaActual }?.label ?: "ContaNexo"
                        )
                    },
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

    if (abrirPicker) {
        EmpresaPickerSheet(
            empresas = empresas,
            seleccionada = empresaState.actual,
            onSelect = { e ->
                empresaState.actual = e
                abrirPicker = false
            },
            onDismiss = { abrirPicker = false }
        )
    }
}

@Composable
fun DrawerHeader(empresa: EmpresaModel?, onCambiar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // avatar con iniciales
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    (empresa?.nombre?.firstOrNull() ?: 'E').toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .size(40.dp)
                        .wrapContentSize(Alignment.Center)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    empresa?.nombre ?: "Sin empresa",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    empresa?.rfc ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onCambiar) { Text("Cambiar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaPickerSheet(
    empresas: List<EmpresaModel>,
    seleccionada: EmpresaModel?,
    onSelect: (EmpresaModel) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
            Text("Cambiar de empresa", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text("Buscar por nombre o RFC") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            val favoritas = empresas.filter { it.favorita }
            val filtradas = empresas.filter {
                query.isBlank() ||
                        it.nombre.contains(query, true) || it.rfc.contains(query, true)
            }

            if (favoritas.isNotEmpty()) {
                Text("Favoritas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                favoritas.forEach { e ->
                    EmpresaItem(e, seleccionada, onSelect)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            filtradas.forEach { e ->
                EmpresaItem(e, seleccionada, onSelect)
            }
        }
    }
}

/**
 * Item de empresa.
 */
@Composable
private fun EmpresaItem(
    e: EmpresaModel,
    seleccionada: EmpresaModel?,
    onSelect: (EmpresaModel) -> Unit
) {
    ListItem(
        headlineContent   = { Text(e.nombre, maxLines = 1) },
        supportingContent = { Text(e.rfc) },
        leadingContent = {
            val isSel = e.id == seleccionada?.id
            Surface(shape = CircleShape, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    e.nombre.first().toString(),
                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp).wrapContentSize(Alignment.Center)
                )
            }
        },
        trailingContent = { if (e.id == seleccionada?.id) Icon(Icons.Outlined.Check, null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(e) }
    )
}
