package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/**
 * Muestra un BottomSheet con el detalle de una tarea.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TareaDetalleSheet(
    t: TareaModel,
    sheetState: androidx.compose.material3.SheetState,
    repo: TareaRepository,
    tareas: List<TareaModel>,
    onTareasUpdate: (List<TareaModel>) -> Unit,
    onClose: () -> Unit,
    onSeleccionadaUpdate: (TareaModel?) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    fun nombreDeArchivo(uriOrUrl: String): String =
        uriOrUrl.substringAfterLast('/').substringAfterLast(':')

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // header
            Text(
                t.titulo,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactTag(text = t.tipo.name.replace('_',' '))
                CompactTag(text = "Prioridad: ${t.prioridad}")
                t.obligacionTag?.let { CompactTag(text = it) }
                DueBadge(t.venceElUtc)
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    t.enlaces.forEach { url ->
                        val annotated = remember(url) {
                            buildAnnotatedString {
                                pushStringAnnotation(tag = "URL", annotation = url)
                                withStyle(
                                    SpanStyle(
                                        color = primaryBlue,
                                        textDecoration = underline
                                    )
                                ) {
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
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(url)
                                        )
                                    )
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
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = "Adjunto"
                            )
                        },
                        headlineContent = { Text(nombreDeArchivo(u)) },
                        supportingContent = {
                            Text(
                                u,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
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
                            onTareasUpdate(nueva)
                            onSeleccionadaUpdate(nueva.firstOrNull { it.id == t.id })
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
                        .semantics { contentDescription = "Adjuntar archivo" }
                ) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adjuntar")
                }

                val puedeCompletar =
                    t.estado != TareaEstado.COMPLETADA && t.estado != TareaEstado.CANCELADA
                Button(
                    onClick = {
                        scope.launch {
                            val upd = repo.marcarCompletada(t.id)
                            if (upd != null) {
                                onTareasUpdate(tareas.map { if (it.id == t.id) upd else it })
                                onSeleccionadaUpdate(upd)
                            }
                        }
                    },
                    enabled = puedeCompletar,
                    modifier = Modifier
                        .semantics { contentDescription = "Marcar como completada" }
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Marcar completada")
                }
            }

            TextButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "Cerrar" }
            ) { Text("Cerrar") }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/**
 * Tag compacto.
 */
@Composable
private fun CompactTag(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
) {
    val container = tint?.copy(alpha = 0.14f) ?: MaterialTheme.colorScheme.surface
    val chipShape = MaterialTheme.shapes.large

    SuggestionChip(
        onClick = { /* no-op */ },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = container,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = chipShape,
        modifier = modifier
            .heightIn(min = 24.dp)
    )
}
