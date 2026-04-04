package dev.amharictranslator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Ethiopian-inspired warm palette ─────────────────────────────────
// Primary:   Warm gold — honey, injera warmth, Aksumite gold
// Secondary: Highland teal — Ethiopian highlands verdure
// Tertiary:  Terracotta — East African earth, pottery, warm spice
// Backgrounds: Deep charcoal-navy — night sky over the Simien Mountains

object AppColors {
    val Gold = Color(0xFFD4A54A)
    val GoldLight = Color(0xFFE8C97A)
    val GoldDark = Color(0xFF9C7830)
    val Teal = Color(0xFF5BBFB2)
    val TealLight = Color(0xFF89D9CF)
    val Terracotta = Color(0xFFE07C52)
    val TerracottaLight = Color(0xFFF0A882)

    val DeepNavy = Color(0xFF0A1220)
    val CardDark = Color(0xFF101D2D)
    val CardMedium = Color(0xFF162538)
    val TextPrimary = Color(0xFFF5F0E6)
    val TextSecondary = Color(0xFFB8C3CF)
    val TextMuted = Color(0xFF7E8D9E)
    val Border = Color(0xFF253545)
    val BorderLight = Color(0xFF2E4055)
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Gold,
    onPrimary = Color(0xFF1A1006),
    primaryContainer = AppColors.GoldDark.copy(alpha = 0.3f),
    onPrimaryContainer = AppColors.GoldLight,
    secondary = AppColors.Teal,
    onSecondary = Color(0xFF04211C),
    secondaryContainer = AppColors.Teal.copy(alpha = 0.15f),
    onSecondaryContainer = AppColors.TealLight,
    tertiary = AppColors.Terracotta,
    onTertiary = Color(0xFF1C0E07),
    tertiaryContainer = AppColors.Terracotta.copy(alpha = 0.15f),
    onTertiaryContainer = AppColors.TerracottaLight,
    background = AppColors.DeepNavy,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.CardDark,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.CardMedium,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    outlineVariant = AppColors.BorderLight,
    error = Color(0xFFE8524A),
    onError = Color(0xFF1C0707)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun AmharicTranslatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
