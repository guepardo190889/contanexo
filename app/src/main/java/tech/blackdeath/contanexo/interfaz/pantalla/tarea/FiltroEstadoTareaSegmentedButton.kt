package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/**
 * Botones de filtro.
 */
@Composable
fun FiltroEstadoTareaSegmentedButton(
    filtro: Filtro,
    modifier: Modifier = Modifier,
    onFiltroSeleccionado: (Filtro) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        val items = listOf(
            Filtro.PENDIENTES to "Pendientes",
            Filtro.TODAS to "Todas",
            Filtro.VENCIDAS to "Vencidas",
            Filtro.COMPLETADAS to "Completadas"
        )
        items.forEachIndexed { index, (f, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, items.size),
                selected = filtro == f,
                onClick = { onFiltroSeleccionado(f) },
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


enum class Filtro { PENDIENTES, VENCIDAS, COMPLETADAS, TODAS }