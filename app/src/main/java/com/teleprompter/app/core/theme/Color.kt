package com.teleprompter.app.core.theme

import androidx.compose.ui.graphics.Color

// Primary Monochrome Brand Colors (Clean Black & White aesthetic)
val PureBlack = Color(0xFF0F172A) // Deep Obsidian Pitch Black
val PitchBlack = Color(0xFF000000)
val DarkCharcoal = Color(0xFF1E293B)
val MediumSlate = Color(0xFF475569)
val LightSlate = Color(0xFF94A3B8)
val HairlineBorder = Color(0xFFE2E8F0)

// Backward-compatibility aliases remapped to clean monochrome
val ElectricBlue = PureBlack
val ElectricBlueDark = PitchBlack
val ElectricBlueLight = MediumSlate
val ElectricBlueLighter = LightSlate

// Subtle Monochrome Accents
val CyanGradient = Color(0xFF334155)
val PurpleGradient = Color(0xFF1E293B)
val PinkGradient = Color(0xFF475569)
val OrangeGradient = Color(0xFF64748B)

// Dark Theme Surfaces (clean obsidian dark with depth)
val DeepNavyDark = Color(0xFF090A0F)
val DeepNavySurface = Color(0xFF12131A)
val DeepNavySurface2 = Color(0xFF181922)
val DeepNavyCard = Color(0xFF1E1F2A)
val DeepNavyCardElevated = Color(0xFF272937)
val DarkBorderSubtle = Color(0xFF27272A)
val DarkBorderElevated = Color(0xFF3F3F46)
val OverlayBackgroundDark = Color(0xE6090A0F)

// Light Theme Surfaces (pure minimalist monochrome)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1F5F9)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFF8FAFC)
val LightBorderSubtle = Color(0xFFE2E8F0)
val LightBorderElevated = Color(0xFFCBD5E1)

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
val ReadingGuideHighlight = Color(0x260F172A)
val ReadingGuideBorder = Color(0xFF0F172A)

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
val FolderBlue = Color(0xFF0F172A)
val FolderPurple = Color(0xFF334155)
val FolderPink = Color(0xFF475569)
val FolderOrange = Color(0xFF64748B)
val FolderTeal = Color(0xFF0F172A)
val FolderYellow = Color(0xFF94A3B8)

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
