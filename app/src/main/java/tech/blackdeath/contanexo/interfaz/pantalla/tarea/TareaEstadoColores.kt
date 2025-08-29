package tech.blackdeath.contanexo.interfaz.pantalla.tarea

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import tech.blackdeath.contanexo.dato.tarea.TareaEstado

/** Colores propios para estados (más diferenciados que el ColorScheme). */
data class EstadoColors(
    val pendiente: Color,
    val vencida: Color,
    val completada: Color,
    val cancelada: Color,
)

val LightEstadoColors = EstadoColors(
    pendiente  = Color(0xFFF59E0B), // Amber 500 (pendiente/atención)
    vencida    = Color(0xFFDC2626), // Red 600 (urgente/vencida)
    completada = Color(0xFF16A34A), // Green 600 (ok/completada)
    cancelada  = Color(0xFF64748B), // Slate 500 (neutral)
)

val DarkEstadoColors = EstadoColors(
    pendiente  = Color(0xFFFBBF24), // Amber 400
    vencida    = Color(0xFFF87171), // Red 400
    completada = Color(0xFF34D399), // Green 400
    cancelada  = Color(0xFF9CA3AF), // Gray 400
)

val LocalEstadoColors = staticCompositionLocalOf { LightEstadoColors }

/** Color “acentado” (sólido) para barras/indicadores. */
@Composable
fun TareaEstado.accentColor(): Color = when (this) {
    TareaEstado.PENDIENTE   -> LocalEstadoColors.current.pendiente
    TareaEstado.VENCIDA     -> LocalEstadoColors.current.vencida
    TareaEstado.COMPLETADA  -> LocalEstadoColors.current.completada
    TareaEstado.CANCELADA   -> LocalEstadoColors.current.cancelada
}

/** Fondo “pastel” para chips/segmentados basado en accent. */
@Composable
fun TareaEstado.containerColor(): Color = this.accentColor().copy(alpha = 0.18f)

/** Texto sobre el contenedor pastel. */
@Composable
fun TareaEstado.onContainerColor(): Color = MaterialTheme.colorScheme.onSurface