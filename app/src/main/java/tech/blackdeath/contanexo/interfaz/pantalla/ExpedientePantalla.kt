package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.dato.expediente.CategoriaDocumento
import tech.blackdeath.contanexo.dato.expediente.DocumentoModel
import tech.blackdeath.contanexo.dato.expediente.ExpedienteRepository
import tech.blackdeath.contanexo.dato.expediente.ExpedienteRepositoryS3Public
import tech.blackdeath.contanexo.utileria.enqueueDownload
import tech.blackdeath.contanexo.utileria.shareLink
import tech.blackdeath.contanexo.utileria.viewPdfFromUrl

/**
 * Vista de Expediente.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpedientePantalla(
    repo: ExpedienteRepository = ExpedienteRepositoryS3Public(),
    onSubir: () -> Unit = {},
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sheetDoc by remember { mutableStateOf<DocumentoModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var query by remember { mutableStateOf(TextFieldValue("")) }
    var categoriaSel by remember { mutableStateOf<CategoriaDocumento?>(null) }

    var docs by remember { mutableStateOf<List<DocumentoModel>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        cargando = true; error = null
        runCatching { repo.listar() }
            .onSuccess { docs = it }
            .onFailure { error = it.message }
        cargando = false
    }

    // Filtro en memoria (texto + categoría)
    val filtradas = remember(query, categoriaSel, docs) {
        val q = query.text.trim()
        docs
            .filter { d -> q.isEmpty() || d.nombre.contains(q, ignoreCase = true) }
            .filter { d -> categoriaSel == null || d.categoria == categoriaSel }
        // (si quieres ordenar, p.ej. por actualizado desc)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar documento…") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Description, null) }
            )

            // Chips de categoría (Todas + cada enum)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoriaSel == null,
                    onClick = { categoriaSel = null },
                    label = { Text("Todas") }
                )
                CategoriaDocumento.entries.forEach { c ->
                    FilterChip(
                        selected = categoriaSel == c,
                        onClick = { categoriaSel = if (categoriaSel == c) null else c },
                        label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }

            // Lista
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtradas, key = { it.id }) { d ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.Description, null) },
                            headlineContent = { Text(d.nombre) },
                            supportingContent = {
                                Text("Categoría: ${d.categoria} • Actualizado: ${d.actualizado}")
                            },
                            trailingContent = {
                                IconButton(onClick = { sheetDoc = d }) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = "Más")
                                }
                            }
                        )
                    }
                }

                if (!cargando && filtradas.isEmpty()) {
                    item { Text("No hay documentos con ese filtro.", modifier = Modifier.padding(8.dp)) }
                }
            }

            // Sheet con acciones (Ver / Descargar / Compartir)
            if (sheetDoc != null) {
                val doc = sheetDoc!!
                ModalBottomSheet(
                    onDismissRequest = { sheetDoc = null },
                    sheetState = sheetState
                ) {
                    // Acción: Ver
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, null) },
                        headlineContent = { Text("Ver") },
                        modifier = Modifier.clickable {
                            scope.launch {
                                val url = repo.urlDescarga(doc.key, doc.urlDirecta)
                                viewPdfFromUrl(context, url, doc.nombre)  // <-- AQUÍ
                                sheetDoc = null
                            }
                        }
                    )

                    // Acción: Descargar
                    ListItem(
                        leadingContent = { Icon(Icons.Outlined.Download, null) },
                        headlineContent = { Text("Descargar") },
                        modifier = Modifier.clickable {
                            scope.launch {
                                val url = repo.urlDescarga(doc.key, doc.urlDirecta)
                                enqueueDownload(context, url, doc.nombre, doc.mimeType)
                                sheetDoc = null
                            }
                        }
                    )
                    // Acción: Compartir (enlace)
                    ListItem(
                        leadingContent = { Icon(Icons.Outlined.Share, null) },
                        headlineContent = { Text("Compartir") },
                        supportingContent = { Text("Enviar por correo o WhatsApp") },
                        modifier = Modifier.clickable {
                            scope.launch {
                                val url = repo.urlDescarga(doc.key, doc.urlDirecta)
                                shareLink(context, url, subject = doc.nombre)
                                sheetDoc = null
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        }

         ExtendedFloatingActionButton(
             onClick = onSubir,
             icon = { Icon(Icons.Outlined.Add, null) },
             text = { Text("Subir") },
             modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
         )
    }
}