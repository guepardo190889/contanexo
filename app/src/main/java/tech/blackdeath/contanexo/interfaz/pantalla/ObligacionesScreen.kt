package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

enum class EstadoObligacion { PENDIENTE, EN_CURSO, ENTREGADA, APROBADA }
enum class TipoObligacion { IVA, ISR, NOMINA, TRAMITE, OTRA }

data class ObligacionUi(
    val id: String,
    val titulo: String,
    val tipo: TipoObligacion,
    val vence: String,           // ISO o formato amigable
    val estado: EstadoObligacion,
    val monto: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ObligacionesScreen(
    onMarcarEntregada: (String) -> Unit = {}
) {
    // MOCK: reemplaza con ViewModel + Flow más adelante
    val data = remember {
        listOf(
            ObligacionUi(
                "1",
                "Declaración mensual IVA",
                TipoObligacion.IVA,
                "17 Sep 2025",
                EstadoObligacion.PENDIENTE,
                "$12,340"
            ), ObligacionUi(
                "2", "Nómina Q2", TipoObligacion.NOMINA, "15 Sep 2025", EstadoObligacion.EN_CURSO
            ), ObligacionUi(
                "3", "ISR mensual", TipoObligacion.ISR, "17 Sep 2025", EstadoObligacion.PENDIENTE
            ), ObligacionUi(
                "4",
                "Actualizar domicilio fiscal",
                TipoObligacion.TRAMITE,
                "01 Oct 2025",
                EstadoObligacion.PENDIENTE
            )
        )
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }
    var estado by remember { mutableStateOf<EstadoObligacion?>(null) }
    var tipo by remember { mutableStateOf<TipoObligacion?>(null) }
    var mesMenu by remember { mutableStateOf(false) }
    var mesSeleccionado by remember { mutableStateOf("Septiembre 2025") }
    val meses = listOf("Septiembre 2025", "Octubre 2025", "Noviembre 2025")

    val filtradas = remember(query, estado, tipo, mesSeleccionado, data) {
        data.filter { o ->
            (query.text.isBlank() || o.titulo.contains(
                query.text,
                ignoreCase = true
            )) && (estado == null || o.estado == estado) && (tipo == null || o.tipo == tipo)
            // mesSeleccionado: aquí harías match real por fecha (LocalDate)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fila de búsqueda + mes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar obligación…") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null) })

            // Mes (Dropdown simple, más adelante un date picker)
            ExposedDropdownMenuBox(expanded = mesMenu, onExpandedChange = { mesMenu = !mesMenu }) {
                OutlinedTextField(readOnly = true,
                    value = mesSeleccionado,
                    onValueChange = {},
                    label = { Text("Mes") },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable, enabled = true
                        )
                        .widthIn(min = 180.dp)
                )
                ExposedDropdownMenu(expanded = mesMenu, onDismissRequest = { mesMenu = false }) {
                    meses.forEach { mes ->
                        DropdownMenuItem(text = { Text(mes) },
                            onClick = { mesSeleccionado = mes; mesMenu = false })
                    }
                }
            }
        }

        // Chips de estado
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = estado == null,
                onClick = { estado = null },
                label = { Text("Todos") })
            EstadoObligacion.entries.forEach { e ->
                FilterChip(selected = estado == e,
                    onClick = { estado = if (estado == e) null else e },
                    label = {
                        Text(e.name.replace('_', ' ').lowercase()
                            .replaceFirstChar { it.uppercase() })
                    })
            }
        }

        // Chips de tipo
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(onClick = { tipo = null }, label = { Text("Cualquier tipo") })
            TipoObligacion.entries.forEach { t ->
                AssistChip(onClick = { tipo = if (tipo == t) null else t },
                    label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    leadingIcon = {
                        if (tipo == t) Icon(
                            Icons.Outlined.AssignmentTurnedIn, contentDescription = null
                        )
                    })
            }
        }

        // Lista
        LazyColumn(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtradas, key = { it.id }) { o ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(headlineContent = { Text(o.titulo) },
                        supportingContent = {
                            Text("Vence: ${o.vence} • Tipo: ${o.tipo} ${o.monto?.let { "• $it" } ?: ""}")
                        },
                        overlineContent = { Text(o.estado.name.replace('_', ' ')) },
                        trailingContent = {
                            if (o.estado == EstadoObligacion.PENDIENTE || o.estado == EstadoObligacion.EN_CURSO) {
                                TextButton(onClick = { onMarcarEntregada(o.id) }) {
                                    Text("Marcar entregada")
                                }
                            }
                        })
                }
            }
            if (filtradas.isEmpty()) {
                item {
                    Text(
                        "No hay resultados con esos filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}