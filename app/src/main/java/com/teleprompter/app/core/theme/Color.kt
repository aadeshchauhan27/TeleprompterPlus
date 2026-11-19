package com.teleprompter.app.core.theme

import androidx.compose.ui.graphics.Color

// Primary Monochrome Brand Colors (Clean Black & White aesthetic)
val PureBlack = Color(0xFF0F172A) // Deep Obsidian Pitch Black
val PitchBlack = Color(0xFF000000)
val DarkCharcoal = Color(0xFF1E293B)
val MediumSlate = Color(0xFF475569)
val LightSlate = Color(0xFF94A3B8)
val HairlineBorder = Color(0xFFE2E8F0)

// Brand accent: a flat, grounded cobalt (no gradients or glow). One accent for primary actions
// and active states; red stays reserved for recording.
val AccentCobalt = Color(0xFF2F63D6)       // light-theme primary / prompter controls
val AccentCobaltDark = Color(0xFF1F4BB0)   // pressed / on-light emphasis
val AccentCobaltSoft = Color(0xFF7FA2EC)   // primary on dark surfaces (better contrast than the base)
val AccentCobaltContainerLight = Color(0xFFDCE6FA)
val AccentCobaltContainerDark = Color(0xFF1B3570)

// Backward-compatibility aliases now map to the accent (they were black, which made the Play
// button and progress bar invisible on the dark prompter)
val ElectricBlue = AccentCobalt
val ElectricBlueDark = AccentCobaltDark
val ElectricBlueLight = AccentCobaltSoft
val ElectricBlueLighter = AccentCobaltContainerLight

// Subtle Monochrome Accents
val CyanGradient = Color(0xFF334155)
val PurpleGradient = Color(0xFF1E293B)
val PinkGradient = Color(0xFF475569)
val OrangeGradient = Color(0xFF64748B)

// Dark Theme Surfaces (clean obsidian dark with depth)
val DeepNavyDark = Color(0xFF0E1626)
val DeepNavySurface = Color(0xFF152036)
val DeepNavySurface2 = Color(0xFF1C2A45)
val DeepNavyCard = Color(0xFF1B2842)
val DeepNavyCardElevated = Color(0xFF22314F)
val DarkBorderSubtle = Color(0xFF2B3B5C)
val DarkBorderElevated = Color(0xFF3A4C72)
val OverlayBackgroundDark = Color(0xE60E1626)

// Light Theme Surfaces (pure minimalist monochrome)
val LightBackground = Color(0xFFF5EFE4)
val LightSurface = Color(0xFFFBF7EF)
val LightSurfaceElevated = Color(0xFFEDE5D6)
val LightCard = Color(0xFFFBF7EF)
val LightCardElevated = Color(0xFFF0E9DB)
val LightBorderSubtle = Color(0xFFDDD3BF)
val LightBorderElevated = Color(0xFFCBBFA6)

// Typography Colors
val TextWhite = Color(0xFFFFFFFF)
val TextLightSlate = Color(0xFFE2E8F0)
val TextLightMuted = Color(0xFF94A3B8)
val TextDarkHeading = Color(0xFF0F172A)
val TextDarkHeadingSubtle = Color(0xFF1E293B)
val TextDarkBody = Color(0xFF334155)
val TextDarkBodySubtle = Color(0xFF475569)
val TextDarkMuted = Color(0xFF64748B)

// Recording & Camera Status Colors
val RecordRed = Color(0xFFEF4444)
val RecordRedPulse = Color(0xFFDC2626)
val SuccessGreen = Color(0xFF10B981)
val WarningOrange = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF3B82F6)
val ReadingGuideHighlight = Color(0x332F63D6)   // accent tint: visible on dark and light prompter backgrounds
val ReadingGuideBorder = Color(0xFF2F63D6)

// Gold / Accent tones
val ProGold = Color(0xFFFBBF24)
val ProGoldLight = Color(0xFFFCD34D)

// Theme Swatches
val ThemeLightBg = Color(0xFFFFFFFF)
val ThemeDarkBg = Color(0xFF090A0F)
val ThemeStudioBg = Color(0xFF000000)
val ThemeMinimalBg = Color(0xFFF8FAFC)
val ThemeYellowBg = Color(0xFFFEF3C7)
val ThemeGreenBg = Color(0xFFD1FAE5)

// Script Text Colors
val TextColorWhite = Color(0xFFFFFFFF)
val TextColorBlack = Color(0xFF0F172A)
val TextColorYellow = Color(0xFFFFD600)
val TextColorSkyBlue = Color(0xFF38BDF8)
val TextColorCobaltBlue = Color(0xFF2563EB)
val TextColorPurple = Color(0xFF8B5CF6)
val TextColorPink = Color(0xFFEC4899)
val TextColorCyan = Color(0xFF06B6D4)

// Folder Accent Colors
val FolderBlue = Color(0xFF2F63D6)
val FolderPurple = Color(0xFF7E5BA6)
val FolderPink = Color(0xFFC2476B)
val FolderOrange = Color(0xFFD9822B)
val FolderTeal = Color(0xFF16897F)
val FolderYellow = Color(0xFFC9A227)

// Monochrome Sets
val GradientPrimary = listOf(
    Color(0xFF0F172A),
    Color(0xFF1E293B),
    Color(0xFF334155)
)

val GradientFunky = listOf(
    Color(0xFF0F172A),
    Color(0xFF1E293B),
    Color(0xFF000000)
)

val GradientPremium = listOf(
    Color(0xFF0F172A),
    Color(0xFF1E293B),
    Color(0xFF334155)
)

val GradientDark = listOf(
    DeepNavyDark,
    DeepNavySurface,
    DeepNavyCard
)
