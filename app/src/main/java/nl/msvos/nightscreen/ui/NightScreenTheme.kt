package nl.msvos.nightscreen.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightColors = darkColorScheme(
    primary = Color(0xFFD9E2FF),
    onPrimary = Color(0xFF263141),
    primaryContainer = Color(0xFF3C4858),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondaryContainer = Color(0xFF3E4759),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    surfaceVariant = Color(0xFF24262D),
)

@Composable
fun NightScreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightColors,
        content = content,
    )
}
