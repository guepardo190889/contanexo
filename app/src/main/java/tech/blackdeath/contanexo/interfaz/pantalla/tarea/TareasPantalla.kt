package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.tarea.TareaEstado
import tech.blackdeath.contanexo.dato.tarea.TareaModel
import tech.blackdeath.contanexo.dato.tarea.TareaRepository
import tech.blackdeath.contanexo.dato.tarea.TareaRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.DueChip
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar
import tech.blackdeath.contanexo.utileria.formatDateCompat

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
    t: TareaModel, onOpen: () -> Unit, onCompletar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors()
    ) {
        // Layout 3 zonas: izquierda (estado), centro (título+sub), derecha (DueChip + ⋮)
        Row(modifier = Modifier
            .clickable(onClickLabel = "Abrir detalle de ${t.titulo}") { onOpen() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            // IZQUIERDA: pill de estado
            EstadoPill(t.estado)

            // CENTRO
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    t.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subt = buildString {
                    t.obligacionTag?.let { append("$it • ") }
                    t.descripcionCorta?.let { append(it) }
                    t.venceElUtc?.let { append(" • Vence: ${formatDateCompat(it)}") }
                }
                Text(
                    subt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // DERECHA: DueChip + menú
            Row(verticalAlignment = Alignment.CenterVertically) {
                DueChip(t.venceElUtc)
                Box {
                    IconButton(onClick = { menu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "Más opciones de ${t.titulo}"
                            }) { Icon(Icons.Outlined.MoreVert, contentDescription = null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        if (t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA) {
                            DropdownMenuItem(text = { Text("Marcar completada") },
                                onClick = { menu = false; onCompletar() })
                        }
                        DropdownMenuItem(text = { Text("Ver detalle") },
                            onClick = { menu = false; onOpen() })
                    }
                }
            }
        }
    }
}

/**
 * Pill de estado.
 */
@Composable
private fun EstadoPill(estado: TareaEstado) {
    val (txt, container, on) = when (estado) {
        TareaEstado.PENDIENTE -> Triple(
            "Pendiente",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )

        TareaEstado.VENCIDA -> Triple(
            "Vencida",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )

        TareaEstado.COMPLETADA -> Triple(
            "Completada",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        TareaEstado.CANCELADA -> Triple(
            "Cancelada",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Surface(color = container, contentColor = on, shape = MaterialTheme.shapes.large) {
        Text(
            txt,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
