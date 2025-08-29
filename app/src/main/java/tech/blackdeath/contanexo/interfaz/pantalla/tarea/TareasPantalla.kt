package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.tarea.TareaEstado
import tech.blackdeath.contanexo.dato.tarea.TareaModel
import tech.blackdeath.contanexo.dato.tarea.TareaRepository
import tech.blackdeath.contanexo.dato.tarea.TareaRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasPantalla(
    modifier: Modifier = Modifier, repo: TareaRepository = TareaRepositoryMock()
) {
    // ---- estado de UI ----
    var filtro by rememberSaveable { mutableStateOf(Filtro.PENDIENTES) }
    var orden by rememberSaveable { mutableStateOf(Orden.POR_FECHA) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tareas by remember { mutableStateOf<List<TareaModel>>(emptyList()) }

    // Detalle (modal)
    var seleccionada by remember { mutableStateOf<TareaModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    /**
     * Carga las tareas desde el repo.
     */
    fun inicializar() {
        cargando = true; error = null
        scope.launch {
            runCatching { repo.listar() }.onSuccess { tareas = it }
                .onFailure { error = it.message ?: "Error al cargar tareas" }
            cargando = false
        }
    }

    LaunchedEffect(Unit) { inicializar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            FiltroEstadoTareaSegmentedButton(
                filtro, modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                filtro = it
            }

            Spacer(Modifier.width(8.dp))

            OrdenamientoTareaButton(ordenSeleccionado = { orden = it })
        }

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        error?.let {
            ErrorBar(texto = it, onReintentar = { inicializar() })
        }

        // ---- aplicar filtro + orden ----
        val visibles by remember(filtro, orden, tareas) {
            mutableStateOf(tareas.filter {
                    when (filtro) {
                        Filtro.PENDIENTES -> it.estado == TareaEstado.PENDIENTE
                        Filtro.VENCIDAS -> it.estado == TareaEstado.VENCIDA
                        Filtro.COMPLETADAS -> it.estado == TareaEstado.COMPLETADA
                        Filtro.TODAS -> true
                    }
                }.let { lista ->
                    when (orden) {
                        Orden.POR_FECHA -> lista.sortedWith(compareBy<TareaModel> {
                            it.venceElUtc ?: Long.MAX_VALUE
                        }.thenByDescending { it.prioridad })

                        Orden.POR_PRIORIDAD -> lista.sortedByDescending { it.prioridad }

                        Orden.POR_TIPO -> lista.sortedWith(
                            compareBy(
                                { it.tipo.name },
                                { it.titulo })
                        )
                    }
                })
        }

        // ---- lista ----
        LazyColumn(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibles, key = { it.id }) { t ->
                TareaItem(t = t, onOpen = { seleccionada = t }, onCompletar = {
                    scope.launch {
                        val upd = repo.marcarCompletada(t.id)
                        if (upd != null) tareas = tareas.map { if (it.id == t.id) upd else it }
                    }
                })
            }

            if (!cargando && error == null && visibles.isEmpty()) {
                item { EmptyHint("No hay tareas para este filtro.") }
            }
        }
    }

    if (seleccionada != null) {
        TareaDetalleSheet(t = seleccionada!!,
            sheetState = sheetState,
            repo = repo,
            tareas = tareas,
            onTareasUpdate = { tareas = it },
            onClose = { seleccionada = null },
            onSeleccionadaUpdate = { seleccionada = it })
    }
}

/**
 * Item de tarea.
 */
@Composable
private fun TareaItem(
    t: TareaModel,
    onOpen: () -> Unit,
    onCompletar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = "Abrir detalle de ${t.titulo}") { onOpen() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.estado.accentColor())
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    t.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Detalle a la izquierda
                    val subt = buildString {
                        t.obligacionTag?.let { append("$it • ") }
                        t.descripcionCorta?.let { append(it) }
                    }
                    Text(
                        subt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge mínimo a la derecha (sólo si hay fecha)
                    DueBadge(t.venceElUtc)
                }
            }

            // DERECHA: menú ⋮
            Box {
                IconButton(
                    onClick = { menu = true },
                    modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Outlined.MoreVert, contentDescription = "Más opciones") }

                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA) {
                        DropdownMenuItem(text = { Text("Marcar completada") }, onClick = { menu = false; onCompletar() })
                    }
                    DropdownMenuItem(text = { Text("Ver detalle") }, onClick = { menu = false; onOpen() })
                }
            }
        }
    }
}

@Composable
private fun estadoColor(estado: TareaEstado): Color = when (estado) {
    TareaEstado.PENDIENTE   -> MaterialTheme.colorScheme.tertiary
    TareaEstado.VENCIDA     -> MaterialTheme.colorScheme.error
    TareaEstado.COMPLETADA  -> MaterialTheme.colorScheme.secondary
    TareaEstado.CANCELADA   -> MaterialTheme.colorScheme.outlineVariant
}

/** Texto de countdown (compat API 24). */
private fun dueLabel(utc: Long?): String? {
    utc ?: return null
    val diff = utc - System.currentTimeMillis()
    val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff).toInt()
    return when {
        days < 0  -> "hace ${-days}d"
        days == 0 -> "hoy"
        days == 1 -> "en 1d"
        else      -> "en ${days}d"
    }
}

/** Badge compacto para vencimiento (mucho más chico que el chip anterior). */
@Composable
private fun DueBadge(venceUtc: Long?, modifier: Modifier = Modifier) {
    val label = remember(venceUtc) { dueLabel(venceUtc) } ?: return
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            "Vence $label",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
