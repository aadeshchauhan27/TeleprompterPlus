package com.teleprompter.app.core.model

import com.teleprompter.app.core.camera.CameraLens

enum class FontStyleType(val label: String) {
    SANS("Sans"),
    SERIF("Serif"),
    MONO("Mono")
}

enum class TextAlignmentType(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}

enum class PresetType(val label: String, val description: String) {
    CREATOR("Creator", "Balanced speed & 85% width for vloggers and creators"),
    INTERVIEW("Interview", "Relaxed pace, serif typography positioned close to the lens"),
    REELS("Reels", "Fast-paced, bold text with compact width for vertical video"),
    CUSTOM("Custom", "Customized to your specific preferences")
}

enum class VideoQualityPreference(val label: String, val description: String) {
    HIGH("High (1080p)", "Best balance of sharpness and storage"),
    STANDARD("Standard (720p)", "Smaller file size, faster sharing"),
    ULTRA("Ultra (4K)", "Maximum resolution on supported sensors")
}

data class TeleprompterSettings(
    val scrollSpeed: Int = 50,
    val fontSize: Int = 28,
    val fontStyle: FontStyleType = FontStyleType.SANS,
    val textColorHex: String = "#FFFFFF",
    val textOpacity: Float = 1.0f,
    val bgOpacity: Float = 0.5f,
    val lineSpacingMultiplier: Float = 1.0f,
    val letterSpacingSp: Float = 0.0f,
    val textAlignment: TextAlignmentType = TextAlignmentType.CENTER,
    val readingWidthPercent: Int = 85,
    val verticalPositionPercent: Int = 25,
    val mirrorText: Boolean = false,
    val showSafeAreaGuide: Boolean = true,
    val countdownSeconds: Int = 3,
    val autoHideControls: Boolean = true,
    val activeThemeId: String = "DARK",
    val presetType: PresetType = PresetType.CREATOR,
    // Camera Preferences
    val defaultCameraLens: CameraLens = CameraLens.FRONT,
    val videoQuality: VideoQualityPreference = VideoQualityPreference.HIGH,
    val recordAudio: Boolean = true,
    val defaultFlashOn: Boolean = false,
    // General Preferences
    val keepScreenAwake: Boolean = true
) {
    fun applyPreset(preset: PresetType): TeleprompterSettings {
        return when (preset) {
            PresetType.CREATOR -> this.copy(
                presetType = PresetType.CREATOR,
                scrollSpeed = 50,
                fontSize = 28,
                fontStyle = FontStyleType.SANS,
                readingWidthPercent = 85,
                verticalPositionPercent = 25,
                textAlignment = TextAlignmentType.CENTER,
                lineSpacingMultiplier = 1.2f,
                letterSpacingSp = 0.0f,
                textOpacity = 1.0f,
                bgOpacity = 0.40f,
                countdownSeconds = 3
            )
            PresetType.INTERVIEW -> this.copy(
                presetType = PresetType.INTERVIEW,
                scrollSpeed = 38,
                fontSize = 26,
                fontStyle = FontStyleType.SERIF,
                readingWidthPercent = 80,
                verticalPositionPercent = 20,
                textAlignment = TextAlignmentType.LEFT,
                lineSpacingMultiplier = 1.4f,
                letterSpacingSp = 0.5f,
                textOpacity = 0.95f,
                bgOpacity = 0.50f,
                countdownSeconds = 5
            )
            PresetType.REELS -> this.copy(
                presetType = PresetType.REELS,
                scrollSpeed = 62,
                fontSize = 32,
                fontStyle = FontStyleType.SANS,
                readingWidthPercent = 70,
                verticalPositionPercent = 28,
                textAlignment = TextAlignmentType.CENTER,
                lineSpacingMultiplier = 1.1f,
                letterSpacingSp = 0.0f,
                textOpacity = 1.0f,
                bgOpacity = 0.45f,
                countdownSeconds = 3
            )
            PresetType.CUSTOM -> this.copy(
                presetType = PresetType.CUSTOM
            )
        }
    }
}

data class ThemeModel(
    val id: String,
    val name: String,
    val backgroundColorHex: String,
    val textColorHex: String,
    val isDark: Boolean
)

object PresetThemes {
    val Light = ThemeModel("LIGHT", "Light", "#FFFFFF", "#0F172A", false)
    val Dark = ThemeModel("DARK", "Dark", "#0F172A", "#FFFFFF", true)
    val Studio = ThemeModel("STUDIO", "Studio", "#000000", "#FFFFFF", true)
    val Minimal = ThemeModel("MINIMAL", "Minimal", "#F1F5F9", "#0F172A", false)
    val Yellow = ThemeModel("YELLOW", "Yellow", "#FEF3C7", "#78350F", false)
    val Green = ThemeModel("GREEN", "Green", "#D1FAE5", "#065F46", false)

    val all = listOf(Light, Dark, Studio, Minimal, Yellow, Green)
}
