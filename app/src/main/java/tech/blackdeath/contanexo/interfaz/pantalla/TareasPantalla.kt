package tech.blackdeath.contanexo.interfaz.pantalla

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.tarea.TareaEstado
import tech.blackdeath.contanexo.dato.tarea.TareaModel
import tech.blackdeath.contanexo.dato.tarea.TareaRepository
import tech.blackdeath.contanexo.dato.tarea.TareaRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar

private enum class Filtro { PENDIENTES, VENCIDAS, COMPLETADAS, TODAS }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TareasPantalla(
    modifier: Modifier = Modifier,
    repo: TareaRepository = TareaRepositoryMock()
) {
    var filtro by remember { mutableStateOf(Filtro.PENDIENTES) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tareas by remember { mutableStateOf<List<TareaModel>>(emptyList()) }

    // Detalle (modal)
    var seleccionada by remember { mutableStateOf<TareaModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    fun cargar() {
        cargando = true; error = null
        scope.launch {
            runCatching { repo.listar() }
                .onSuccess { tareas = it }
                .onFailure { error = it.message ?: "Error al cargar tareas" }
            cargando = false
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filtros (single-select)
        FlowRow (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = filtro == Filtro.PENDIENTES, onClick = { filtro = Filtro.PENDIENTES }, label = { Text("Pendientes") })
            FilterChip(selected = filtro == Filtro.TODAS,      onClick = { filtro = Filtro.TODAS },      label = { Text("Todas") })
            FilterChip(selected = filtro == Filtro.VENCIDAS,   onClick = { filtro = Filtro.VENCIDAS },   label = { Text("Vencidas") })
            FilterChip(selected = filtro == Filtro.COMPLETADAS,onClick = { filtro = Filtro.COMPLETADAS },label = { Text("Completadas") })

            if (cargando) {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        error?.let {
            ErrorBar (texto = it, onReintentar = { cargar() })
        }

        val visibles = remember(filtro, tareas) {
            when (filtro) {
                Filtro.PENDIENTES -> tareas.filter { it.estado == TareaEstado.PENDIENTE }
                Filtro.VENCIDAS   -> tareas.filter { it.estado == TareaEstado.VENCIDA }
                Filtro.COMPLETADAS-> tareas.filter { it.estado == TareaEstado.COMPLETADA }
                Filtro.TODAS      -> tareas
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibles, key = { it.id }) { t ->
                TareaItem(
                    t = t,
                    onOpen = { seleccionada = t },
                    onCompletar = {
                        scope.launch {
                            val upd = repo.marcarCompletada(t.id)
                            if (upd != null) {
                                tareas = tareas.map { if (it.id == t.id) upd else it }
                            }
                        }
                    },
                    onAdjuntar = { openPicker ->
                        openPicker()
                    }
                )
            }

            if (!cargando && error == null && visibles.isEmpty()) {
                item { EmptyHint("No hay tareas para este filtro.") }
            }
        }
    }

    // Modal de detalle
    if (seleccionada != null) {
        val t = seleccionada!!
        ModalBottomSheet(
            onDismissRequest = { seleccionada = null },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(t.titulo, style = MaterialTheme.typography.titleLarge)

                // chips informativos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(t.tipo.name.replace('_', ' ')) })
                    AssistChip(onClick = {}, label = { Text("Prioridad: ${t.prioridad}") })
                    t.obligacionTag?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }

                t.venceElUtc?.let { epoch ->
                    Text("Vence: ${formatDate(epoch)}", style = MaterialTheme.typography.titleSmall)
                }


                t.descripcionLarga?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }

                // Enlaces relacionados
                if (t.enlaces.isNotEmpty()) {
                    Text("Enlaces", style = MaterialTheme.typography.titleSmall)
                    t.enlaces.forEach { url ->
                        OutlinedButton(onClick = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }) { Text(url.take(48) + if (url.length > 48) "…" else "") }
                    }
                }

                // Adjuntos
                if (t.adjuntos.isNotEmpty()) {
                    Text("Adjuntos", style = MaterialTheme.typography.titleSmall)
                    t.adjuntos.forEach { u ->
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
                            headlineContent = { Text(nombreDeArchivo(u)) },
                            supportingContent = { Text(u, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.clickable {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))
                            }
                        )
                    }
                }

                // Acciones
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pickLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            ctx.contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            scope.launch {
                                val upd = repo.adjuntar(t.id, uri.toString())
                                if (upd != null) {
                                    // Refrescar lista y seleccionado
                                    tareas = tareas.map { if (it.id == t.id) upd else it }
                                    seleccionada = upd
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            pickLauncher.launch(arrayOf("application/pdf", "image/*"))
                        }
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adjuntar")
                    }

                    if (t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val upd = repo.marcarCompletada(t.id)
                                    if (upd != null) {
                                        tareas = tareas.map { if (it.id == t.id) upd else it }
                                        seleccionada = upd
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Marcar completada")
                        }
                    }

                    Spacer(Modifier.weight(1f))
                }

                TextButton(modifier = Modifier.align(Alignment.End), onClick = { seleccionada = null }) { Text("Cerrar") }
            }
        }
    }
}

@Composable
private fun TareaItem(
    t: TareaModel,
    onOpen: () -> Unit,
    onCompletar: () -> Unit,
    onAdjuntar: (openPicker: () -> Unit) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // El repo se actualiza desde el modal; aquí solo abrimos el picker y devolvemos control
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        ListItem(
            modifier = Modifier.clickable(onClick = onOpen),
            leadingContent = {
                val estadoText = when (t.estado) {
                    TareaEstado.PENDIENTE -> "Pendiente"
                    TareaEstado.VENCIDA -> "Vencida"
                    TareaEstado.COMPLETADA -> "Completada"
                    TareaEstado.CANCELADA -> "Cancelada"
                }
                AssistChip(onClick = {}, label = { Text(estadoText) })
            },
            headlineContent = {
                Text(t.titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                val subt = buildString {
                    t.obligacionTag?.let { append("$it • ") }
                    t.descripcionCorta?.let { append(it) }
                    if (t.venceElUtc != null) append(" • Vence: ${formatDate(t.venceElUtc)}")
                }
                Text(subt, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { menu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Más")
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text("Adjuntar archivo") },
                                onClick = {
                                    menu = false
                                    onAdjuntar {
                                        pickLauncher.launch(arrayOf("application/pdf", "image/*"))
                                    }
                                }
                            )
                            if (t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA) {
                                DropdownMenuItem(
                                    text = { Text("Marcar completada") },
                                    onClick = { menu = false; onCompletar() }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

private fun formatDate(epochUtc: Long): String {
    // formato simple YYYY-MM-DD
    return java.time.Instant.ofEpochMilli(epochUtc)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

private fun nombreDeArchivo(uriOrUrl: String): String {
    return uriOrUrl.substringAfterLast('/').substringAfterLast(':')
}
