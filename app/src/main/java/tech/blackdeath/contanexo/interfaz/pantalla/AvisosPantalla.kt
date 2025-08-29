package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.aviso.AvisoModel
import tech.blackdeath.contanexo.dato.aviso.AvisoRepository
import tech.blackdeath.contanexo.dato.aviso.AvisoRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar

private enum class AvisoFiltro { TODOS, NO_LEIDOS, LEIDOS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosPantalla(
    modifier: Modifier = Modifier,
    repo: AvisoRepository = AvisoRepositoryMock() // inyecta tu repo real cuando lo tengas
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var filtro by remember { mutableStateOf(AvisoFiltro.TODOS) }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var avisos by remember { mutableStateOf<List<AvisoModel>>(emptyList()) }

    // Estado del detalle
    var seleccionado by remember { mutableStateOf<AvisoModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun cargar() {
        cargando = true; error = null; avisos = emptyList()
        scope.launch {
            runCatching { repo.recientes() }
                .onSuccess { avisos = it }
                .onFailure { error = it.message ?: "Error al cargar avisos" }
            cargando = false
        }
    }

    LaunchedEffect(Unit) { cargar() }

    // ---- UI principal ----
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Avisos", style = MaterialTheme.typography.headlineSmall)

        // Búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar avisos…") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) }
        )

        // Filtros: Todos / No leídos / Leídos
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
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        error?.let {
            ErrorBar(texto = it, onReintentar = { cargar() })
        }

        val filtrados = remember(query, filtro, avisos) {
            val q = query.text.trim()
            avisos
                .filter { a ->
                    when (filtro) {
                        AvisoFiltro.TODOS -> true
                        AvisoFiltro.NO_LEIDOS -> !a.leido
                        AvisoFiltro.LEIDOS -> a.leido
                    }
                }
                .filter { a ->
                    if (q.isEmpty()) true
                    else a.titulo.contains(q, ignoreCase = true) || a.detalle.contains(q, ignoreCase = true)
                }
            // .sortedByDescending { it.fechaIso }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtrados, key = { it.id }) { a ->
                AvisoItem(
                    a = a,
                    onClick = {
                        // Al abrir detalle, marcar leído en memoria y mostrar el sheet
                        val marcado = a.copy(leido = true)
                        seleccionado = marcado
                        avisos = avisos.map { if (it.id == a.id) marcado else it }
                    }
                )
            }
            if (!cargando && error == null && filtrados.isEmpty()) {
                item { EmptyHint("No hay avisos con ese filtro.") }
            }
        }
    }

    // ---- Detalle en Modal Bottom Sheet ----
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
                Text(
                    text = "Fecha: ${aviso.fechaIso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // “Descripción larga”: por ahora usamos 'detalle' completo.
                // Si luego agregas un campo de cuerpo extenso, solo cámbialo aquí.
                Text(
                    text = aviso.detalle,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                // Acciones del detalle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val esLeido = aviso.leido
                    FilledTonalButton(
                        onClick = {
                            // Alternar leído/no leído en memoria
                            val actualizado = aviso.copy(leido = !esLeido)
                            seleccionado = actualizado
                            avisos = avisos.map { if (it.id == aviso.id) actualizado else it }
                        }
                    ) {
                        Text(if (esLeido) "Marcar como no leído" else "Marcar como leído")
                    }
                    OutlinedButton(onClick = { seleccionado = null }) {
                        Text("Cerrar")
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AvisoItem(a: AvisoModel, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        ListItem(
            leadingContent = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null)
                    if (!a.leido) {
                        // Puntito para indicar "no leído"
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(MaterialTheme.shapes.extraSmall as Shape)
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
            }
        )
    }
}
