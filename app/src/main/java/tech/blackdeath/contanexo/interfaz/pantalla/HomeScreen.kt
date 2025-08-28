package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    // Datos mock por ahora
    val proximas = listOf(
        ItemUI("Pago IVA agosto", "Vence: 17 Sep 2025"),
        ItemUI("Nómina Q2", "Vence: 15 Sep 2025"),
        ItemUI("ISR mensual", "Vence: 17 Sep 2025"),
    )
    val docsRecientes = listOf(
        ItemUI("CSF_2025.pdf", "Actualizado: 10 Ago 2025"),
        ItemUI("RFC.pdf", "Actualizado: 01 Dic 2024")
    )
    val avisos = listOf(
        ItemUI("Nueva obligación", "Declaración mensual lista para revisión"),
        ItemUI("Documento subido", "Carta de contribuyente cumplido")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado simple
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Panel", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Resumen rápido de tus pendientes y documentos.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Acciones rápidas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { /* TODO: subir a expediente */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Subir documento")
            }
            OutlinedButton(
                onClick = { /* TODO: nueva entrega de obligación */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva entrega")
            }
        }

        // Próximas obligaciones
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Próximas obligaciones", style = MaterialTheme.typography.titleMedium) }
            )
            Column(Modifier.padding(bottom = 8.dp)) {
                proximas.forEach {
                    ListItem(
                        headlineContent = {
                            Text(it.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = { Text(it.subtitle) }
                    )
                }
            }
        }

        // Documentos recientes
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
                headlineContent = { Text("Documentos recientes", style = MaterialTheme.typography.titleMedium) }
            )
            Column(Modifier.padding(bottom = 8.dp)) {
                docsRecientes.forEach {
                    ListItem(
                        headlineContent = {
                            Text(it.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = { Text(it.subtitle) }
                    )
                }
            }
        }

        // Avisos
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                leadingContent = { Icon(Icons.Outlined.Message, contentDescription = null) },
                headlineContent = { Text("Avisos", style = MaterialTheme.typography.titleMedium) }
            )
            Column(Modifier.padding(bottom = 8.dp)) {
                avisos.forEach {
                    ListItem(
                        headlineContent = {
                            Text(it.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = { Text(it.subtitle) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private data class ItemUI(val title: String, val subtitle: String)
