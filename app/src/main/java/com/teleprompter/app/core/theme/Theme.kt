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

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = ElectricBlueLight,
    secondary = CyanGradient,
    onSecondary = TextWhite,
    secondaryContainer = Color(0xFF08909D),
    onSecondaryContainer = TextWhite,
    tertiary = PurpleGradient,
    onTertiary = TextWhite,
    tertiaryContainer = Color(0xFF6D44B6),
    onTertiaryContainer = ElectricBlueLight,
    background = DeepNavyDark,
    onBackground = TextDarkHeading,
    surface = DeepNavySurface,
    onSurface = TextDarkHeading,
    surfaceVariant = DeepNavyCard,
    onSurfaceVariant = TextLightSlate,
    surfaceTint = ElectricBlue,
    inverseSurface = TextDarkHeading,
    inverseOnSurface = DeepNavyDark,
    outline = DarkBorderSubtle,
    outlineVariant = DarkBorderElevated,
    scrim = Color(0x80000000),
    surfaceBright = DeepNavySurface2,
    surfaceContainer = DeepNavyCard,
    surfaceContainerHigh = DeepNavyCardElevated,
    surfaceContainerHighest = Color(0xFF2D2D3D)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = ElectricBlueDark,
    secondary = CyanGradient,
    onSecondary = TextWhite,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = TextDarkBody,
    tertiary = PurpleGradient,
    onTertiary = TextWhite,
    tertiaryContainer = Color(0xFFE1BEE7),
    onTertiaryContainer = TextDarkBody,
    background = LightBackground,
    onBackground = TextDarkHeading,
    surface = LightSurface,
    onSurface = TextDarkHeading,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextDarkBodySubtle,
    surfaceTint = ElectricBlue,
    inverseSurface = TextLightSlate,
    inverseOnSurface = LightBackground,
    outline = LightBorderSubtle,
    outlineVariant = LightBorderElevated,
    scrim = Color(0x80000000),
    surfaceBright = LightSurfaceElevated,
    surfaceContainer = LightCard,
    surfaceContainerHigh = LightCardElevated,
    surfaceContainerHighest = Color(0xFFEAEAEA)
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
