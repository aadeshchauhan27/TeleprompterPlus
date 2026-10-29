package com.teleprompter.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teleprompter.app.core.camera.CameraLens
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.PresetType
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.model.TextAlignmentType
import com.teleprompter.app.core.model.VideoQualityPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "teleprompter_settings")

interface SettingsRepository {
    val settingsFlow: Flow<TeleprompterSettings>
    suspend fun updateScrollSpeed(speed: Int)
    suspend fun updateFontSize(size: Int)
    suspend fun updateFontStyle(style: FontStyleType)
    suspend fun updateTextColor(colorHex: String)
    suspend fun updateTextOpacity(opacity: Float)
    suspend fun updateBgOpacity(opacity: Float)
    suspend fun updateLineSpacing(spacing: Float)
    suspend fun updateLetterSpacing(spacingSp: Float)
    suspend fun updateTextAlignment(alignment: TextAlignmentType)
    suspend fun updateReadingWidth(percent: Int)
    suspend fun updateVerticalPosition(percent: Int)
    suspend fun updateMirrorText(mirror: Boolean)
    suspend fun updateShowSafeArea(show: Boolean)
    suspend fun updateCountdown(seconds: Int)
    suspend fun updateAutoHideControls(autoHide: Boolean)
    suspend fun updateTheme(themeId: String)
    suspend fun applyPreset(preset: PresetType)
    // Camera Preferences
    suspend fun updateDefaultCameraLens(lens: CameraLens)
    suspend fun updateVideoQuality(quality: VideoQualityPreference)
    suspend fun updateRecordAudio(recordAudio: Boolean)
    suspend fun updateDefaultFlashOn(flashOn: Boolean)
    // General Preferences
    suspend fun updateKeepScreenAwake(keepAwake: Boolean)
    // Script Settings Batch Apply
    suspend fun applyScriptSettings(
        scrollSpeed: Int,
        fontSize: Int,
        fontStyle: FontStyleType,
        textColorHex: String,
        bgOpacity: Float,
        textOpacity: Float,
        readingWidthPercent: Int,
        verticalPositionPercent: Int,
        lineSpacingMultiplier: Float,
        letterSpacingSp: Float,
        textAlignment: TextAlignmentType,
        mirrorText: Boolean,
        presetType: PresetType
    )
    suspend fun resetToDefaults()
}

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val SCROLL_SPEED = intPreferencesKey("scroll_speed")
        val FONT_SIZE = intPreferencesKey("font_size")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val TEXT_COLOR = stringPreferencesKey("text_color")
        val TEXT_OPACITY = floatPreferencesKey("text_opacity")
        val BG_OPACITY = floatPreferencesKey("bg_opacity")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val TEXT_ALIGNMENT = stringPreferencesKey("text_alignment")
        val READING_WIDTH = intPreferencesKey("reading_width")
        val VERTICAL_POSITION = intPreferencesKey("vertical_position")
        val MIRROR_TEXT = booleanPreferencesKey("mirror_text")
        val SHOW_SAFE_AREA = booleanPreferencesKey("show_safe_area")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val AUTO_HIDE_CONTROLS = booleanPreferencesKey("auto_hide_controls")
        val ACTIVE_THEME = stringPreferencesKey("active_theme")
        val PRESET_TYPE = stringPreferencesKey("preset_type")
        val DEFAULT_CAMERA_LENS = stringPreferencesKey("default_camera_lens")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val RECORD_AUDIO = booleanPreferencesKey("record_audio")
        val DEFAULT_FLASH_ON = booleanPreferencesKey("default_flash_on")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
    }

    override val settingsFlow: Flow<TeleprompterSettings> = context.dataStore.data.map { preferences ->
        val styleString = preferences[PreferencesKeys.FONT_STYLE] ?: FontStyleType.SANS.name
        val fontStyle = try {
            FontStyleType.valueOf(styleString)
        } catch (_: Exception) {
            FontStyleType.SANS
        }

        val alignmentString = preferences[PreferencesKeys.TEXT_ALIGNMENT] ?: TextAlignmentType.CENTER.name
        val alignment = try {
            TextAlignmentType.valueOf(alignmentString)
        } catch (_: Exception) {
            TextAlignmentType.CENTER
        }

        val presetString = preferences[PreferencesKeys.PRESET_TYPE] ?: PresetType.CREATOR.name
        val preset = try {
            PresetType.valueOf(presetString)
        } catch (_: Exception) {
            PresetType.CREATOR
        }

        val lensString = preferences[PreferencesKeys.DEFAULT_CAMERA_LENS] ?: CameraLens.FRONT.name
        val defaultLens = try {
            CameraLens.valueOf(lensString)
        } catch (_: Exception) {
            CameraLens.FRONT
        }

        val qualityString = preferences[PreferencesKeys.VIDEO_QUALITY] ?: VideoQualityPreference.HIGH.name
        val videoQuality = try {
            VideoQualityPreference.valueOf(qualityString)
        } catch (_: Exception) {
            VideoQualityPreference.HIGH
        }

        TeleprompterSettings(
            scrollSpeed = preferences[PreferencesKeys.SCROLL_SPEED] ?: 50,
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 28,
            fontStyle = fontStyle,
            textColorHex = preferences[PreferencesKeys.TEXT_COLOR] ?: "#FFFFFF",
            textOpacity = preferences[PreferencesKeys.TEXT_OPACITY] ?: 1.0f,
            bgOpacity = preferences[PreferencesKeys.BG_OPACITY] ?: 0.40f,
            lineSpacingMultiplier = preferences[PreferencesKeys.LINE_SPACING] ?: 1.2f,
            letterSpacingSp = preferences[PreferencesKeys.LETTER_SPACING] ?: 0.0f,
            textAlignment = alignment,
            readingWidthPercent = preferences[PreferencesKeys.READING_WIDTH] ?: 85,
            verticalPositionPercent = preferences[PreferencesKeys.VERTICAL_POSITION] ?: 25,
            mirrorText = preferences[PreferencesKeys.MIRROR_TEXT] ?: false,
            showSafeAreaGuide = preferences[PreferencesKeys.SHOW_SAFE_AREA] ?: true,
            countdownSeconds = preferences[PreferencesKeys.COUNTDOWN_SECONDS] ?: 3,
            autoHideControls = preferences[PreferencesKeys.AUTO_HIDE_CONTROLS] ?: true,
            activeThemeId = preferences[PreferencesKeys.ACTIVE_THEME] ?: "DARK",
            presetType = preset,
            defaultCameraLens = defaultLens,
            videoQuality = videoQuality,
            recordAudio = preferences[PreferencesKeys.RECORD_AUDIO] ?: true,
            defaultFlashOn = preferences[PreferencesKeys.DEFAULT_FLASH_ON] ?: false,
            keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: true
        )
    }

    override suspend fun updateScrollSpeed(speed: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLL_SPEED] = speed.coerceIn(10, 100)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size.coerceIn(16, 56)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateFontStyle(style: FontStyleType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_STYLE] = style.name
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateTextColor(colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_COLOR] = colorHex
        }
    }

    override suspend fun updateTextOpacity(opacity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_OPACITY] = opacity.coerceIn(0.2f, 1.0f)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateBgOpacity(opacity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BG_OPACITY] = opacity.coerceIn(0f, 1f)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateLineSpacing(spacing: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LINE_SPACING] = spacing.coerceIn(0.8f, 2.0f)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateLetterSpacing(spacingSp: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LETTER_SPACING] = spacingSp.coerceIn(-1.0f, 4.0f)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateTextAlignment(alignment: TextAlignmentType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = alignment.name
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateReadingWidth(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.READING_WIDTH] = percent.coerceIn(50, 100)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateVerticalPosition(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VERTICAL_POSITION] = percent.coerceIn(10, 80)
            preferences[PreferencesKeys.PRESET_TYPE] = PresetType.CUSTOM.name
        }
    }

    override suspend fun updateMirrorText(mirror: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIRROR_TEXT] = mirror
        }
    }

    override suspend fun updateShowSafeArea(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SAFE_AREA] = show
        }
    }

    override suspend fun updateCountdown(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COUNTDOWN_SECONDS] = seconds.coerceIn(0, 10)
        }
    }

    override suspend fun updateAutoHideControls(autoHide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_HIDE_CONTROLS] = autoHide
        }
    }

    override suspend fun updateTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_THEME] = themeId
        }
    }

    override suspend fun applyPreset(preset: PresetType) {
        val base = TeleprompterSettings().applyPreset(preset)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRESET_TYPE] = preset.name
            preferences[PreferencesKeys.SCROLL_SPEED] = base.scrollSpeed
            preferences[PreferencesKeys.FONT_SIZE] = base.fontSize
            preferences[PreferencesKeys.FONT_STYLE] = base.fontStyle.name
            preferences[PreferencesKeys.READING_WIDTH] = base.readingWidthPercent
            preferences[PreferencesKeys.VERTICAL_POSITION] = base.verticalPositionPercent
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = base.textAlignment.name
            preferences[PreferencesKeys.LINE_SPACING] = base.lineSpacingMultiplier
            preferences[PreferencesKeys.LETTER_SPACING] = base.letterSpacingSp
            preferences[PreferencesKeys.TEXT_OPACITY] = base.textOpacity
            preferences[PreferencesKeys.BG_OPACITY] = base.bgOpacity
            preferences[PreferencesKeys.COUNTDOWN_SECONDS] = base.countdownSeconds
        }
    }

    override suspend fun updateDefaultCameraLens(lens: CameraLens) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CAMERA_LENS] = lens.name
        }
    }

    override suspend fun updateVideoQuality(quality: VideoQualityPreference) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIDEO_QUALITY] = quality.name
        }
    }

    override suspend fun updateRecordAudio(recordAudio: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORD_AUDIO] = recordAudio
        }
    }

    override suspend fun updateDefaultFlashOn(flashOn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_FLASH_ON] = flashOn
        }
    }

    override suspend fun updateKeepScreenAwake(keepAwake: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = keepAwake
        }
    }

    override suspend fun applyScriptSettings(
        scrollSpeed: Int,
        fontSize: Int,
        fontStyle: FontStyleType,
        textColorHex: String,
        bgOpacity: Float,
        textOpacity: Float,
        readingWidthPercent: Int,
        verticalPositionPercent: Int,
        lineSpacingMultiplier: Float,
        letterSpacingSp: Float,
        textAlignment: TextAlignmentType,
        mirrorText: Boolean,
        presetType: PresetType
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLL_SPEED] = scrollSpeed
            preferences[PreferencesKeys.FONT_SIZE] = fontSize
            preferences[PreferencesKeys.FONT_STYLE] = fontStyle.name
            preferences[PreferencesKeys.TEXT_COLOR] = textColorHex
            preferences[PreferencesKeys.BG_OPACITY] = bgOpacity
            preferences[PreferencesKeys.TEXT_OPACITY] = textOpacity
            preferences[PreferencesKeys.READING_WIDTH] = readingWidthPercent
            preferences[PreferencesKeys.VERTICAL_POSITION] = verticalPositionPercent
            preferences[PreferencesKeys.LINE_SPACING] = lineSpacingMultiplier
            preferences[PreferencesKeys.LETTER_SPACING] = letterSpacingSp
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = textAlignment.name
            preferences[PreferencesKeys.MIRROR_TEXT] = mirrorText
            preferences[PreferencesKeys.PRESET_TYPE] = presetType.name
        }
    }

    override suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            val defaultSettings = TeleprompterSettings()
            preferences[PreferencesKeys.SCROLL_SPEED] = defaultSettings.scrollSpeed
            preferences[PreferencesKeys.FONT_SIZE] = defaultSettings.fontSize
            preferences[PreferencesKeys.FONT_STYLE] = defaultSettings.fontStyle.name
            preferences[PreferencesKeys.TEXT_COLOR] = defaultSettings.textColorHex
            preferences[PreferencesKeys.TEXT_OPACITY] = defaultSettings.textOpacity
            preferences[PreferencesKeys.BG_OPACITY] = defaultSettings.bgOpacity
            preferences[PreferencesKeys.LINE_SPACING] = defaultSettings.lineSpacingMultiplier
            preferences[PreferencesKeys.LETTER_SPACING] = defaultSettings.letterSpacingSp
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = defaultSettings.textAlignment.name
            preferences[PreferencesKeys.READING_WIDTH] = defaultSettings.readingWidthPercent
            preferences[PreferencesKeys.VERTICAL_POSITION] = defaultSettings.verticalPositionPercent
            preferences[PreferencesKeys.MIRROR_TEXT] = defaultSettings.mirrorText
            preferences[PreferencesKeys.SHOW_SAFE_AREA] = defaultSettings.showSafeAreaGuide
            preferences[PreferencesKeys.COUNTDOWN_SECONDS] = defaultSettings.countdownSeconds
            preferences[PreferencesKeys.AUTO_HIDE_CONTROLS] = defaultSettings.autoHideControls
            preferences[PreferencesKeys.ACTIVE_THEME] = defaultSettings.activeThemeId
            preferences[PreferencesKeys.PRESET_TYPE] = defaultSettings.presetType.name
            preferences[PreferencesKeys.DEFAULT_CAMERA_LENS] = defaultSettings.defaultCameraLens.name
            preferences[PreferencesKeys.VIDEO_QUALITY] = defaultSettings.videoQuality.name
            preferences[PreferencesKeys.RECORD_AUDIO] = defaultSettings.recordAudio
            preferences[PreferencesKeys.DEFAULT_FLASH_ON] = defaultSettings.defaultFlashOn
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = defaultSettings.keepScreenAwake
        }
    }
}
