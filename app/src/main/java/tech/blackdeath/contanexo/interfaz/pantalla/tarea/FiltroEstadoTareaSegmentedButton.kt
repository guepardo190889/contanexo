package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import tech.blackdeath.contanexo.dato.tarea.TareaEstado

/**
 * Botones de filtro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroEstadoTareaSegmentedButton(
    filtro: Filtro,
    modifier: Modifier = Modifier,
    onFiltroSeleccionado: (Filtro) -> Unit
) {
    val items = listOf(
        Filtro.PENDIENTES to "Pendientes",
        Filtro.TODAS to "Todas",
        Filtro.VENCIDAS to "Vencidas",
        Filtro.COMPLETADAS to "Completadas"
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        items.forEachIndexed { index, (f, label) ->
            val estado = f.asEstadoOrNull()
            val colors = if (estado != null) {
                                SegmentedButtonDefaults.colors(
                    activeContainerColor   = estado.containerColor(),
                    activeContentColor     = estado.onContainerColor(),
                    activeBorderColor      = estado.accentColor(),
                    inactiveContainerColor = estado.containerColor().copy(alpha = 0.15f),
                    inactiveContentColor   = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor    = MaterialTheme.colorScheme.outline,
                )
            } else {
                // "Todas" neutro (ligero toque primario)
                SegmentedButtonDefaults.colors(
                    activeContainerColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    activeContentColor     = MaterialTheme.colorScheme.onSurface,
                    activeBorderColor      = MaterialTheme.colorScheme.primary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor   = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor    = MaterialTheme.colorScheme.outline
                )
            }

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, items.size),
                selected = filtro == f,
                onClick = { onFiltroSeleccionado(f) },
                colors = colors,
                label = {
                    Text(
                        label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
/**
 * Obtiene el estado asociado a un filtro.
 */
private fun Filtro.asEstadoOrNull(): TareaEstado? = when (this) {
    Filtro.PENDIENTES   -> TareaEstado.PENDIENTE
    Filtro.VENCIDAS     -> TareaEstado.VENCIDA
    Filtro.COMPLETADAS  -> TareaEstado.COMPLETADA
    Filtro.TODAS        -> null
}

/**
 * Filtros de estado de tareas.
 */
enum class Filtro { PENDIENTES, VENCIDAS, COMPLETADAS, TODAS }