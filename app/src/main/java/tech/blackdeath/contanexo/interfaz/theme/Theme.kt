package tech.blackdeath.contanexo.interfaz.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5AA6),
    onPrimary = Color.White,
    secondary = Color(0xFF4D7CC3),
    tertiary = Color(0xFF8AB6F2),
    background = Color(0xFFF7FAFF),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB6F2),
    onPrimary = Color(0xFF06264A),
    secondary = Color(0xFFBCD4FF),
    tertiary = Color(0xFFD0E3FF),
    background = Color(0xFF0F1720),
    surface = Color(0xFF101418)
)

@Composable
fun ContaNexoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}