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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.tarea.TareaEstado
import tech.blackdeath.contanexo.dato.tarea.TareaModel
import tech.blackdeath.contanexo.dato.tarea.TareaRepository
import tech.blackdeath.contanexo.dato.tarea.TareaRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private enum class Filtro { PENDIENTES, VENCIDAS, COMPLETADAS, TODAS }
private enum class Orden { POR_FECHA, POR_PRIORIDAD, POR_TIPO }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TareasPantalla(
    modifier: Modifier = Modifier,
    repo: TareaRepository = TareaRepositoryMock()
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

    // ---- “toolbar” local con acciones (ordenar) ----
    var menuOrden by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tareas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(
                onClick = { menuOrden = true },
                modifier = Modifier.size(48.dp).semantics { contentDescription = "Ordenar" }
            ) {
                Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = null)
            }
            DropdownMenu(expanded = menuOrden, onDismissRequest = { menuOrden = false }) {
                DropdownMenuItem(
                    text = { Text("Ordenar por fecha") },
                    onClick = { menuOrden = false; orden = Orden.POR_FECHA }
                )
                DropdownMenuItem(
                    text = { Text("Ordenar por prioridad") },
                    onClick = { menuOrden = false; orden = Orden.POR_PRIORIDAD }
                )
                DropdownMenuItem(
                    text = { Text("Ordenar por tipo") },
                    onClick = { menuOrden = false; orden = Orden.POR_TIPO }
                )
            }
        }

        // ---- Segmented buttons para filtros ----
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(
                Filtro.PENDIENTES to "Pendientes",
                Filtro.TODAS to "Todas",
                Filtro.VENCIDAS to "Vencidas",
                Filtro.COMPLETADAS to "Completadas"
            ).forEachIndexed { index, pair ->
                val selected = filtro == pair.first
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                    selected = selected,
                    onClick = { filtro = pair.first },
                    label = { Text(pair.second) }
                )
            }
        }

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        error?.let {
            ErrorBar (texto = it, onReintentar = { cargar() })
        }

        // ---- aplicar filtro + orden ----
        val visibles by remember(filtro, orden, tareas) {
            mutableStateOf(
                tareas
                    .filter {
                        when (filtro) {
                            Filtro.PENDIENTES -> it.estado == TareaEstado.PENDIENTE
                            Filtro.VENCIDAS -> it.estado == TareaEstado.VENCIDA
                            Filtro.COMPLETADAS -> it.estado == TareaEstado.COMPLETADA
                            Filtro.TODAS -> true
                        }
                    }
                    .let { lista ->
                        when (orden) {
                            Orden.POR_FECHA ->
                                lista.sortedWith(
                                    compareBy<TareaModel> { it.venceElUtc ?: Long.MAX_VALUE }
                                        .thenByDescending { it.prioridad }
                                )
                            Orden.POR_PRIORIDAD ->
                                lista.sortedByDescending { it.prioridad }
                            Orden.POR_TIPO ->
                                lista.sortedWith(compareBy({ it.tipo.name }, { it.titulo }))
                        }
                    }
            )
        }

        // ---- lista ----
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
                            if (upd != null) tareas = tareas.map { if (it.id == t.id) upd else it }
                        }
                    }
                )
            }

            if (!cargando && error == null && visibles.isEmpty()) {
                item { EmptyHint("No hay tareas para este filtro.") }
            }
        }
    }

    // ---- Modal de detalle ----
    if (seleccionada != null) {
        val t = seleccionada!!
        ModalBottomSheet(
            onDismissRequest = { seleccionada = null },
            sheetState = sheetState,
        ) {
            // estructura: contenido scrollable + pie fijo
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // header
                Text(t.titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))

                // chips informativos
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(onClick = {}, label = { Text(t.tipo.name.replace('_',' ')) })
                    AssistChip(onClick = {}, label = { Text("Prioridad: ${t.prioridad}") })
                    t.obligacionTag?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    DueChip(t.venceElUtc)
                }

                Spacer(Modifier.height(8.dp))

                // cuerpo
                t.descripcionLarga?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                }

                if (t.enlaces.isNotEmpty()) {
                    Text("Enlaces", style = MaterialTheme.typography.titleSmall)
                    val primaryBlue = MaterialTheme.colorScheme.primary
                    val underline = TextDecoration.Underline
                    val context = LocalContext.current
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                        t.enlaces.forEach { url ->
                            val annotated = remember(url) {
                                buildAnnotatedString {
                                    pushStringAnnotation(tag = "URL", annotation = url)
                                    withStyle(SpanStyle(color = primaryBlue, textDecoration = underline)) {
                                        append(url)
                                    }
                                    pop()
                                }
                            }
                            Text(
                                text = annotated,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClickLabel = "Abrir enlace $url"
                                    ) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (t.adjuntos.isNotEmpty()) {
                    Text("Adjuntos", style = MaterialTheme.typography.titleSmall)
                    t.adjuntos.forEach { u ->
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.Description, contentDescription = "Adjunto") },
                            headlineContent = { Text(nombreDeArchivo(u)) },
                            supportingContent = { Text(u, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier
                                .clickable(onClickLabel = "Abrir adjunto") {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))
                                }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // --- pie fijo ---
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
                                // refrescar lista y seleccionado
                                val nueva = tareas.map { if (it.id == t.id) upd else it }
                                tareas = nueva
                                seleccionada = nueva.firstOrNull { it.id == t.id }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { pickLauncher.launch(arrayOf("application/pdf", "image/*")) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .semantics { contentDescription = "Adjuntar archivo" }
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adjuntar")
                    }

                    val puedeCompletar = t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA
                    Button(
                        onClick = {
                            scope.launch {
                                val upd = repo.marcarCompletada(t.id)
                                if (upd != null) {
                                    tareas = tareas.map { if (it.id == t.id) upd else it }
                                    seleccionada = upd
                                }
                            }
                        },
                        enabled = puedeCompletar,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .semantics { contentDescription = "Marcar como completada" }
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar completada")
                    }

                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = { seleccionada = null },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .semantics { contentDescription = "Cerrar detalle" }
                    ) { Text("Cerrar") }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

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
        // Layout 3 zonas: izquierda (estado), centro (título+sub), derecha (DueChip + ⋮)
        Row(
            modifier = Modifier
                .clickable(onClickLabel = "Abrir detalle de ${t.titulo}") { onOpen() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IZQUIERDA: pill de estado
            EstadoPill(t.estado)

            // CENTRO
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(t.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    IconButton(
                        onClick = { menu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Más opciones de ${t.titulo}" }
                    ) { Icon(Icons.Outlined.MoreVert, contentDescription = null) }
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
}

/* ------------------- UI helpers ------------------- */

@Composable
private fun EstadoPill(estado: TareaEstado) {
    val (txt, container, on) = when (estado) {
        TareaEstado.PENDIENTE ->
            Triple("Pendiente", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        TareaEstado.VENCIDA  ->
            Triple("Vencida", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        TareaEstado.COMPLETADA ->
            Triple("Completada", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        TareaEstado.CANCELADA ->
            Triple("Cancelada", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(color = container, contentColor = on, shape = MaterialTheme.shapes.large) {
        Text(
            txt,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** Countdown chip accesible: hoy / en Nd / hace Nd */
@Composable
private fun DueChip(venceUtc: Long?) {
    val label = remember(venceUtc) { dueLabel(venceUtc) } ?: return
    AssistChip(
        onClick = {},
        label = { Text(label) },
        modifier = Modifier
            .padding(end = 4.dp)
            .semantics { contentDescription = "Vencimiento: $label" }
    )
}

/** Calcula el texto del countdown sin java.time (minSdk 24 safe). */
private fun dueLabel(utc: Long?): String? {
    utc ?: return null
    val now = System.currentTimeMillis()
    val diff = utc - now
    val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
    return when {
        days < 0  -> "Vencida hace ${-days}d"
        days == 0 -> "Vence hoy"
        days == 1 -> "Vence en 1 día"
        else      -> "Vence en ${days}d"
    }
}

/** Fecha simple local YYYY-MM-DD (compat API 24). */
private fun formatDateCompat(epochUtc: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // mostramos en zona local del usuario
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date(epochUtc))
}

private fun nombreDeArchivo(uriOrUrl: String): String =
    uriOrUrl.substringAfterLast('/').substringAfterLast(':')
