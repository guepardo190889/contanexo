package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            ErrorBar (texto = it, onReintentar = { cargar() })
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
            // .sortedByDescending { it.fechaIso }  // si quieres ordenar por fecha
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtrados, key = { it.id }) { a ->
                AvisoItem(a)
            }
            if (!cargando && error == null && filtrados.isEmpty()) {
                item { EmptyHint("No hay avisos con ese filtro.") }
            }
        }
    }
}

@Composable
private fun AvisoItem(a: AvisoModel) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                                .padding(0.dp)
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