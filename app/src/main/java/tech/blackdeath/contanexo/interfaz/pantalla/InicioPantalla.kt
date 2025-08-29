package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.aviso.AvisoModel
import tech.blackdeath.contanexo.dato.aviso.AvisoRepositoryMock
import tech.blackdeath.contanexo.dato.tarea.TareaModel
import tech.blackdeath.contanexo.dato.tarea.TareaRepositoryMock
import tech.blackdeath.contanexo.interfaz.comun.EmptyHint
import tech.blackdeath.contanexo.interfaz.comun.ErrorBar
import tech.blackdeath.contanexo.navegacion.LocalAppNavigator
import tech.blackdeath.contanexo.navegacion.Pantalla

/**
 * Inicio: SOLO dos cards (Próximas obligaciones, Avisos).
 * Se autoabastece desde dos repos (mock por defecto). No recibe datos por parámetro.
 */
@Composable
fun InicioPantalla(
    modifier: Modifier = Modifier,
) {
    val nav = LocalAppNavigator.current

    // Inyección simple (luego cámbialo por tu DI)
    val tareasRepository = remember { TareaRepositoryMock()}
    val avisoRepo  = remember { AvisoRepositoryMock() }

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var proximas by remember { mutableStateOf<List<TareaModel>>(emptyList()) }
    var avisos by remember { mutableStateOf<List<AvisoModel>>(emptyList()) }

    val scope = rememberCoroutineScope()

    /**
     * Carga de datos.
     */
    fun cargar() {
        cargando = true
        error = null
        proximas = emptyList()
        avisos = emptyList()

        scope.launch {
            runCatching {
                coroutineScope {
                    val p = async { tareasRepository.listar() }
                    val a = async { avisoRepo.recientes() }
                    proximas = p.await()
                    avisos = a.await()
                }
            }.onFailure {
                error = it.message ?: "Error al cargar inicio"
            }
            cargando = false
        }
    }


    LaunchedEffect(Unit) { cargar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
        error?.let {
            ErrorBar(
                texto = it,
                onReintentar = { cargar() })
            Spacer(Modifier.height(12.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 360.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Card: Próximas obligaciones
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "Próximas obligaciones",
                        actionText = "Ver todo",
                        onAction = { nav.go(Pantalla.Tareas) }
                    )
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                        if (proximas.isEmpty() && !cargando && error == null) {
                            EmptyHint("Sin obligaciones próximas.")
                        } else {
                            proximas.forEach { o ->
                                LineItem(
                                    title = o.titulo,
                                    subtitle = o.descripcionCorta.orEmpty(),
                                    onClick = { nav.go(Pantalla.Tareas) }
                                )
                            }
                        }
                    }
                }
            }

            // Card: Avisos
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        icon = Icons.Filled.Notifications,
                        title = "Avisos",
                        actionText = "Ver todo",
                        onAction = { nav.go(Pantalla.Notificaciones) }
                    )
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                        if (avisos.isEmpty() && !cargando && error == null) {
                            EmptyHint("No tienes avisos nuevos.")
                        } else {
                            avisos.forEach { n ->
                                LineItem(
                                    title = n.titulo,
                                    subtitle = n.detalle,
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) },
                                    onClick = { nav.go(Pantalla.Notificaciones) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------- helpers UI --------------------

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    actionText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(actionText) }
    }
}

@Composable
private fun LineItem(
    title: String,
    subtitle: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

