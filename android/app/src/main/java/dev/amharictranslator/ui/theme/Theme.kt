package dev.amharictranslator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF0B35D),
    onPrimary = Color(0xFF2A1A05),
    secondary = Color(0xFF74D0C3),
    onSecondary = Color(0xFF04211C),
    tertiary = Color(0xFF8DB5FF),
    onTertiary = Color(0xFF07111C),
    background = Color(0xFF07111C),
    onBackground = Color(0xFFF4EFE6),
    surface = Color(0xFF0D1A29),
    onSurface = Color(0xFFF4EFE6),
    surfaceVariant = Color(0xFF152434),
    onSurfaceVariant = Color(0xFFB4BEC9),
    outline = Color(0x334F5E6F)
)

@Composable
fun AmharicTranslatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
