package tech.blackdeath.contanexo.interfaz.comun

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

/**
 * Countdown chip accesible: hoy / en Nd / hace Nd
 */
@Composable
fun DueChip(venceUtc: Long?) {
    val label = remember(venceUtc) { dueLabel(venceUtc) } ?: return
    AssistChip(
        onClick = {},
        label = { Text(label) },
        modifier = Modifier
            .padding(end = 4.dp)
            .semantics { contentDescription = "Vencimiento: $label" }
    )
}


/**
 * Calcula el texto del countdown sin java.time (minSdk 24 safe).
 */
fun dueLabel(utc: Long?): String? {
    utc ?: return null
    val now = System.currentTimeMillis()
    val diff = utc - now
    val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
    return when {
        days < 0  -> "Vencida hace ${-days}d"
        days == 0 -> "Vence hoy"
        days == 1 -> "Vence en 1 día"
        else      -> "Vence en ${days}d"
    }
}