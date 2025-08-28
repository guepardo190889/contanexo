package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

enum class CategoriaDoc { FISCAL, LEGAL, NOMINA, BANCARIO, OTROS }

data class DocumentoUi(
    val id: String,
    val nombre: String,
    val categoria: CategoriaDoc,
    val actualizado: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpedienteScreen(
    onSubir: () -> Unit = {},
    onAbrir: (String) -> Unit = {}
) {
    val docs = remember {
        listOf(
            DocumentoUi("csf", "Constancia de Situación Fiscal.pdf", CategoriaDoc.FISCAL, "10 Ago 2025"),
            DocumentoUi("rfc", "RFC.pdf", CategoriaDoc.FISCAL, "01 Dic 2024"),
            DocumentoUi("acta", "Acta constitutiva.pdf", CategoriaDoc.LEGAL, "20 Jun 2018"),
            DocumentoUi("opin", "Opinión de cumplimiento.pdf", CategoriaDoc.FISCAL, "05 Ago 2025")
        )
    }
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var categoria by remember { mutableStateOf<CategoriaDoc?>(null) }

    val filtradas = remember(query, categoria, docs) {
        docs.filter { d ->
            (query.text.isBlank() || d.nombre.contains(query.text, ignoreCase = true)) &&
                    (categoria == null || d.categoria == categoria)
        }
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
                leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) }
            )

            // Categorías
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoria == null,
                    onClick = { categoria = null },
                    label = { Text("Todas") }
                )
                CategoriaDoc.values().forEach { c ->
                    FilterChip(
                        selected = categoria == c,
                        onClick = { categoria = if (categoria == c) null else c },
                        label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Lista por documento (agrupado visualmente por categoría)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtradas, key = { it.id }) { d ->
                    ElevatedCard(
                        onClick = { onAbrir(d.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.Description, null) },
                            headlineContent = { Text(d.nombre) },
                            supportingContent = { Text("Categoría: ${d.categoria} • Actualizado: ${d.actualizado}") }
                        )
                    }
                }
                if (filtradas.isEmpty()) {
                    item {
                        Text(
                            "No hay documentos con esos filtros.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // FAB para subir
        ExtendedFloatingActionButton(
            onClick = onSubir,
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text("Subir") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
