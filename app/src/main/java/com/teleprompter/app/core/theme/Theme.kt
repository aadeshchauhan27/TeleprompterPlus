package com.teleprompter.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Dark: deep navy (not black) with cool, slightly blue-tinted greys.
private val DarkColorScheme = darkColorScheme(
    primary = AccentCobaltSoft,
    onPrimary = Color(0xFF0A1B45),
    primaryContainer = AccentCobaltContainerDark,
    onPrimaryContainer = AccentCobaltContainerLight,
    secondary = Color(0xFFC9D4EA),
    onSecondary = Color(0xFF0E1626),
    secondaryContainer = Color(0xFF22314F),
    onSecondaryContainer = Color(0xFFE8EEFA),
    tertiary = Color(0xFFE8A867),          // warm amber for small highlights
    onTertiary = Color(0xFF3A2410),
    tertiaryContainer = Color(0xFF4A3520),
    onTertiaryContainer = Color(0xFFF6DFC4),
    background = Color(0xFF0E1626),
    onBackground = Color(0xFFE8EEFA),
    surface = Color(0xFF152036),
    onSurface = Color(0xFFE8EEFA),
    surfaceVariant = Color(0xFF1C2A45),
    onSurfaceVariant = Color(0xFF9FADC8),
    surfaceTint = AccentCobaltSoft,
    inverseSurface = Color(0xFFE8EEFA),
    inverseOnSurface = Color(0xFF0E1626),
    outline = Color(0xFF2B3B5C),
    outlineVariant = Color(0xFF3A4C72),
    scrim = Color(0x99000000),
    surfaceBright = Color(0xFF273759),
    surfaceContainer = Color(0xFF131D32),
    surfaceContainerHigh = Color(0xFF1B2842),
    surfaceContainerHighest = Color(0xFF22314F)
)

// Light: warm paper / cream instead of stark white, with a navy-tinted ink for text.
private val LightColorScheme = lightColorScheme(
    primary = AccentCobalt,
    onPrimary = Color.White,
    primaryContainer = AccentCobaltContainerLight,
    onPrimaryContainer = Color(0xFF0B2A6B),
    secondary = Color(0xFF3A4660),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E2D3),
    onSecondaryContainer = Color(0xFF1F2638),
    tertiary = Color(0xFFB8651B),          // warm amber for small highlights
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6DFC4),
    onTertiaryContainer = Color(0xFF4A2A08),
    background = Color(0xFFF5EFE4),
    onBackground = Color(0xFF1F2638),
    surface = Color(0xFFFBF7EF),
    onSurface = Color(0xFF1F2638),
    surfaceVariant = Color(0xFFEDE5D6),
    onSurfaceVariant = Color(0xFF5C6275),
    surfaceTint = AccentCobalt,
    inverseSurface = Color(0xFF232C45),
    inverseOnSurface = Color(0xFFF5EFE4),
    outline = Color(0xFFDDD3BF),
    outlineVariant = Color(0xFFCBBFA6),
    scrim = Color(0x80000000),
    surfaceBright = Color(0xFFFDFAF4),
    surfaceContainer = Color(0xFFF0E9DB),
    surfaceContainerHigh = Color(0xFFEAE2D2),
    surfaceContainerHighest = Color(0xFFE4DBC9)
)

/** Rounder corners than the Material default for a friendlier, less corporate feel. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun TelePrompterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAbsoluteTonalElevation provides 0.dp
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
