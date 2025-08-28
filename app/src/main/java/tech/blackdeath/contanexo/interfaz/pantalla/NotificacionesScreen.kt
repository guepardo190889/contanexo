package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class TipoAviso { OBLIGACION, DOCUMENTO, MENSAJE }

data class NotificacionUi(
    val id: String,
    val titulo: String,
    val detalle: String,
    val fecha: String,
    val tipo: TipoAviso,
    val leida: Boolean = false
)

@Composable
fun NotificacionesScreen(
    onMarcarLeida: (String) -> Unit = {},
    onMarcarTodas: () -> Unit = {}
) {
    var avisos by remember {
        mutableStateOf(
            listOf(
                NotificacionUi("n1", "Nueva obligación", "Declaración mensual lista para revisión.", "24 Ago 2025", TipoAviso.OBLIGACION, leida = false),
                NotificacionUi("n2", "Documento subido", "Carta de contribuyente cumplido.", "20 Ago 2025", TipoAviso.DOCUMENTO, leida = true),
                NotificacionUi("n3", "Mensaje del contador", "Revisar CFDIs de agosto.", "19 Ago 2025", TipoAviso.MENSAJE, leida = false)
            )
        )
    }
    var soloNoLeidas by remember { mutableStateOf(false) }

    val lista = if (soloNoLeidas) avisos.filter { !it.leida } else avisos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Acciones y filtros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = soloNoLeidas,
                onClick = { soloNoLeidas = !soloNoLeidas },
                label = { Text(if (soloNoLeidas) "Sólo no leídas" else "Todas") },
                leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) }
            )
            TextButton(
                onClick = {
                    avisos = avisos.map { it.copy(leida = true) }
                    onMarcarTodas()
                }
            ) {
                Icon(Icons.Outlined.MarkEmailRead, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Marcar todas como leídas")
            }
        }

        // Lista
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lista, key = { it.id }) { n ->
                ElevatedCard(
                    onClick = {
                        avisos = avisos.map { if (it.id == n.id) it.copy(leida = true) else it }
                        onMarcarLeida(n.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        leadingContent = {
                            val icon = when (n.tipo) {
                                TipoAviso.OBLIGACION -> Icons.Outlined.Campaign
                                TipoAviso.DOCUMENTO -> Icons.Outlined.Description
                                TipoAviso.MENSAJE -> Icons.Outlined.Notifications
                            }
                            Box(Modifier.size(24.dp)) {
                                Icon(icon, contentDescription = null)
                                if (!n.leida) {
                                    // puntito de no leído
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(MaterialTheme.shapes.extraSmall)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        },
                        headlineContent = { Text(n.titulo) },
                        supportingContent = { Text(n.detalle) },
                        overlineContent = { Text(n.fecha) }
                    )
                }
            }
            if (lista.isEmpty()) {
                item {
                    Text(
                        if (soloNoLeidas) "No tienes avisos pendientes." else "No hay avisos.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
