package tech.blackdeath.contanexo.interfaz.pantalla

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.aviso.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar

private enum class AvisoFiltro { TODOS, NO_LEIDOS, LEIDOS }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AvisosPantalla(
    modifier: Modifier = Modifier,
    repo: AvisoRepository = AvisoRepositoryMock()
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var filtro by remember { mutableStateOf(AvisoFiltro.TODOS) }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var avisos by remember { mutableStateOf<List<AvisoModel>>(emptyList()) }

    var seleccionado by remember { mutableStateOf<AvisoModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargar() {
        cargando = true; error = null; avisos = emptyList()
        scope.launch {
            runCatching { repo.recientes() }
                .onSuccess { avisos = it }
                .onFailure { error = it.message ?: "Error al cargar avisos" }
            cargando = false
        }
    }

    fun marcarLeidoLocal(id: Long, leido: Boolean) {
        avisos = avisos.map { if (it.id == id) it.copy(leido = leido) else it }
        if (seleccionado?.id == id) {
            seleccionado = seleccionado?.copy(leido = leido)
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Avisos", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar avisos…") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) }
        )

        // Filtros claros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filtro == AvisoFiltro.TODOS,
                onClick = { filtro = AvisoFiltro.TODOS },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = filtro == AvisoFiltro.NO_LEIDOS,
                onClick = { filtro = AvisoFiltro.NO_LEIDOS },
                label = { Text("No leídos") }
            )
            FilterChip(
                selected = filtro == AvisoFiltro.LEIDOS,
                onClick = { filtro = AvisoFiltro.LEIDOS },
                label = { Text("Leídos") }
            )
            if (cargando) {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        error?.let {
            ErrorBar (texto = it, onReintentar = { cargar() })
        }

        val filtrados = remember(query, filtro, avisos) {
            val q = query.text.trim()
            avisos
                .filter {
                    when (filtro) {
                        AvisoFiltro.TODOS -> true
                        AvisoFiltro.NO_LEIDOS -> !it.leido
                        AvisoFiltro.LEIDOS -> it.leido
                    }
                }
                .filter {
                    if (q.isEmpty()) true
                    else it.titulo.contains(q, true) || it.detalle.contains(q, true)
                }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtrados, key = { it.id }) { a ->
                AvisoItem(
                    a = a,
                    onOpen = {
                        // Abrir detalle y marcar como leído localmente
                        if (!a.leido) marcarLeidoLocal(a.id, true)
                        // Usa el objeto ya actualizado de la lista si quieres mostrar el estado correcto
                        seleccionado = avisos.firstOrNull { it.id == a.id } ?: a.copy(leido = true)
                    },
                    onMarcarLeido = {
                        marcarLeidoLocal(a.id, true)
                    },
                    onMarcarNoLeido = {
                        marcarLeidoLocal(a.id, false)
                    }
                )
            }
            if (!cargando && error == null && filtrados.isEmpty()) {
                item { EmptyHint("No hay avisos con ese filtro.") }
            }
        }
    }

    // Detalle en Modal Bottom Sheet
    if (seleccionado != null) {
        val aviso = seleccionado!!
        ModalBottomSheet(
            onDismissRequest = { seleccionado = null },
            sheetState = sheetState
        ) {
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(aviso.titulo, style = MaterialTheme.typography.titleLarge)

                // Chips informativos (solo modal)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aviso.categoria?.let {
                        AssistChip(onClick = {}, label = { Text(it) })
                    }
                    AssistChip(onClick = {}, label = { Text("Prioridad: ${aviso.prioridad}") })
                    AssistChip(onClick = {}, label = { Text("Origen: ${aviso.origen}") })
                    AssistChip(onClick = {}, label = { Text("Fecha: ${aviso.fechaIso}") })
                }

                Text(aviso.descripcionLarga, style = MaterialTheme.typography.bodyMedium)

                aviso.enlace?.let { url ->
                    OutlinedButton(
                        onClick = {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(i)
                        }
                    ) { Text("Abrir enlace relacionado") }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { seleccionado = null }) { Text("Cerrar") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AvisoItem(
    a: AvisoModel,
    onOpen: () -> Unit,
    onMarcarLeido: () -> Unit,
    onMarcarNoLeido: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onOpen),
            leadingContent = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null)
                    if (!a.leido) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            headlineContent = {
                Text(a.titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Text("${a.detalle} • ${a.fechaIso}", maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            trailingContent = {
                // Siempre mostramos ⋮ y la acción cambia según el estado
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Más")
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        if (a.leido) {
                            DropdownMenuItem(
                                text = { Text("Marcar como no leído") },
                                onClick = {
                                    menuAbierto = false
                                    onMarcarNoLeido()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Marcar como leído") },
                                onClick = {
                                    menuAbierto = false
                                    onMarcarLeido()
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}
