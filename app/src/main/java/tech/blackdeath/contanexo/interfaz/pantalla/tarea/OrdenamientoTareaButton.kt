package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón de ordenamiento.
 */
@Composable
fun OrdenamientoTareaButton(
    ordenSeleccionado: (Orden) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Ordenar")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Ordenar por fecha") },
                onClick = { expanded = false; ordenSeleccionado(Orden.POR_FECHA) })
            DropdownMenuItem(text = { Text("Ordenar por prioridad") },
                onClick = { expanded = false; ordenSeleccionado(Orden.POR_PRIORIDAD) })
            DropdownMenuItem(text = { Text("Ordenar por tipo") },
                onClick = { expanded = false; ordenSeleccionado(Orden.POR_TIPO) })
        }
    }
}

/**
 * Tipos de ordenamiento.
 */
enum class Orden { POR_FECHA, POR_PRIORIDAD, POR_TIPO }