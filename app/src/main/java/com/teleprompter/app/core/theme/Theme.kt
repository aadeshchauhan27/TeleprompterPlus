package com.teleprompter.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Sleek Monochrome Dark Scheme (Deep Obsidian with Crisp White Primary)
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF090A0F),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFFE2E8F0),
    onSecondary = Color(0xFF090A0F),
    secondaryContainer = Color(0xFF1E202B),
    onSecondaryContainer = Color(0xFFF8FAFC),
    tertiary = Color(0xFF94A3B8),
    onTertiary = Color(0xFF090A0F),
    tertiaryContainer = Color(0xFF1E202B),
    onTertiaryContainer = Color(0xFFE2E8F0),
    background = Color(0xFF090A0F),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF12131A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF181922),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceTint = Color.White,
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF090A0F),
    outline = Color(0xFF27272A),
    outlineVariant = Color(0xFF3F3F46),
    scrim = Color(0x99000000),
    surfaceBright = Color(0xFF1E1F2A),
    surfaceContainer = Color(0xFF14151E),
    surfaceContainerHigh = Color(0xFF1E202B),
    surfaceContainerHighest = Color(0xFF272937)
)

// Minimalist Monochrome Light Scheme (Pure White with Deep Obsidian Pitch Black Primary)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8FAFC),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF64748B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1F5F9),
    onTertiaryContainer = Color(0xFF334155),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = Color(0xFF0F172A),
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFFFFFFF),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    scrim = Color(0x80000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF8FAFC),
    surfaceContainerHighest = Color(0xFFF1F5F9)
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
            content = content
        )
    }
}
