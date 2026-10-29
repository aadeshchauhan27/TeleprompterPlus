package com.teleprompter.app.ui.screens.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.camera.CameraLens
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.PresetThemes
import com.teleprompter.app.core.model.PresetType
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.model.TextAlignmentType
import com.teleprompter.app.core.model.ThemeModel
import com.teleprompter.app.core.model.VideoQualityPreference
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import com.teleprompter.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScriptSettingsUiState(
    val scriptId: String? = null,
    val script: ScriptEntity? = null,
    val scriptTitle: String = "Script Settings",
    val settings: TeleprompterSettings = TeleprompterSettings(),
    val availableThemes: List<ThemeModel> = PresetThemes.all,
    val isSaved: Boolean = false
)

class ScriptSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    private val scriptId: String? = savedStateHandle["scriptId"]
    private val _localSettings = MutableStateFlow<TeleprompterSettings?>(null)
    private val _script = MutableStateFlow<ScriptEntity?>(null)

    val uiState: StateFlow<ScriptSettingsUiState> = combine(
        settingsRepository.settingsFlow,
        _localSettings,
        _script
    ) { globalSettings, local, script ->
        val activeSettings = local ?: globalSettings
        ScriptSettingsUiState(
            scriptId = scriptId,
            script = script,
            scriptTitle = script?.title?.ifBlank { "Script Settings" } ?: "Script Settings",
            settings = activeSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScriptSettingsUiState()
    )

    init {
        loadScript()
    }

    private fun loadScript() {
        if (!scriptId.isNullOrBlank()) {
            viewModelScope.launch {
                val script = scriptRepository.getScriptByIdDirect(scriptId)
                _script.value = script
                if (script != null && script.hasCustomSettings) {
                    val fontStyle = try {
                        FontStyleType.valueOf(script.fontStyle)
                    } catch (_: Exception) {
                        FontStyleType.SANS
                    }
                    val alignment = try {
                        TextAlignmentType.valueOf(script.textAlignment)
                    } catch (_: Exception) {
                        TextAlignmentType.CENTER
                    }
                    val preset = try {
                        PresetType.valueOf(script.presetType)
                    } catch (_: Exception) {
                        PresetType.CUSTOM
                    }
                    _localSettings.value = TeleprompterSettings(
                        scrollSpeed = script.scrollSpeed,
                        fontSize = script.fontSize,
                        fontStyle = fontStyle,
                        textColorHex = script.textColorHex,
                        bgOpacity = script.bgOpacity,
                        textOpacity = script.textOpacity,
                        readingWidthPercent = script.readingWidthPercent,
                        verticalPositionPercent = script.verticalPositionPercent,
                        lineSpacingMultiplier = script.lineSpacingMultiplier,
                        letterSpacingSp = script.letterSpacingSp,
                        textAlignment = alignment,
                        mirrorText = script.mirrorText,
                        showSafeAreaGuide = script.showSafeAreaGuide,
                        presetType = preset
                    )
                }
            }
        }
    }

    fun applyPreset(preset: PresetType) {
        val current = uiState.value.settings.applyPreset(preset)
        _localSettings.value = current
        viewModelScope.launch {
            settingsRepository.applyPreset(preset)
        }
    }

    fun setScrollSpeed(speed: Int) {
        val current = uiState.value.settings.copy(
            scrollSpeed = speed.coerceIn(10, 100),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateScrollSpeed(speed) }
    }

    fun incrementScrollSpeed() {
        setScrollSpeed(uiState.value.settings.scrollSpeed + 2)
    }

    fun decrementScrollSpeed() {
        setScrollSpeed(uiState.value.settings.scrollSpeed - 2)
    }

    fun setFontSize(size: Int) {
        val current = uiState.value.settings.copy(
            fontSize = size.coerceIn(16, 56),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateFontSize(size) }
    }

    fun incrementFontSize() {
        setFontSize(uiState.value.settings.fontSize + 2)
    }

    fun decrementFontSize() {
        setFontSize(uiState.value.settings.fontSize - 2)
    }

    fun setFontStyle(style: FontStyleType) {
        val current = uiState.value.settings.copy(
            fontStyle = style,
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateFontStyle(style) }
    }

    fun setTextColor(colorHex: String) {
        val current = uiState.value.settings.copy(textColorHex = colorHex)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateTextColor(colorHex) }
    }

    fun setTextOpacity(opacity: Float) {
        val current = uiState.value.settings.copy(
            textOpacity = opacity.coerceIn(0.2f, 1.0f),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateTextOpacity(opacity) }
    }

    fun setBgOpacity(opacity: Float) {
        val current = uiState.value.settings.copy(
            bgOpacity = opacity.coerceIn(0f, 1f),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateBgOpacity(opacity) }
    }

    fun setLineSpacing(spacing: Float) {
        val current = uiState.value.settings.copy(
            lineSpacingMultiplier = spacing.coerceIn(0.8f, 2.0f),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateLineSpacing(spacing) }
    }

    fun setLetterSpacing(spacingSp: Float) {
        val current = uiState.value.settings.copy(
            letterSpacingSp = spacingSp.coerceIn(-1.0f, 4.0f),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateLetterSpacing(spacingSp) }
    }

    fun setTextAlignment(alignment: TextAlignmentType) {
        val current = uiState.value.settings.copy(
            textAlignment = alignment,
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateTextAlignment(alignment) }
    }

    fun setReadingWidth(percent: Int) {
        val current = uiState.value.settings.copy(
            readingWidthPercent = percent.coerceIn(50, 100),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateReadingWidth(percent) }
    }

    fun setVerticalPosition(percent: Int) {
        val current = uiState.value.settings.copy(
            verticalPositionPercent = percent.coerceIn(10, 80),
            presetType = PresetType.CUSTOM
        )
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateVerticalPosition(percent) }
    }

    fun setMirrorText(mirror: Boolean) {
        val current = uiState.value.settings.copy(mirrorText = mirror)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateMirrorText(mirror) }
    }

    fun setShowSafeAreaGuide(show: Boolean) {
        val current = uiState.value.settings.copy(showSafeAreaGuide = show)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateShowSafeArea(show) }
    }

    fun setCountdown(seconds: Int) {
        val current = uiState.value.settings.copy(countdownSeconds = seconds)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateCountdown(seconds) }
    }

    fun setDefaultCameraLens(lens: CameraLens) {
        val current = uiState.value.settings.copy(defaultCameraLens = lens)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateDefaultCameraLens(lens) }
    }

    fun setVideoQuality(quality: VideoQualityPreference) {
        val current = uiState.value.settings.copy(videoQuality = quality)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateVideoQuality(quality) }
    }

    fun setRecordAudio(recordAudio: Boolean) {
        val current = uiState.value.settings.copy(recordAudio = recordAudio)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateRecordAudio(recordAudio) }
    }

    fun setDefaultFlashOn(flashOn: Boolean) {
        val current = uiState.value.settings.copy(defaultFlashOn = flashOn)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateDefaultFlashOn(flashOn) }
    }

    fun setAutoHideControls(autoHide: Boolean) {
        val current = uiState.value.settings.copy(autoHideControls = autoHide)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateAutoHideControls(autoHide) }
    }

    fun setKeepScreenAwake(keepAwake: Boolean) {
        val current = uiState.value.settings.copy(keepScreenAwake = keepAwake)
        _localSettings.value = current
        viewModelScope.launch { settingsRepository.updateKeepScreenAwake(keepAwake) }
    }

    fun setTheme(themeId: String) {
        val theme = PresetThemes.all.find { it.id == themeId } ?: return
        val current = uiState.value.settings.copy(
            activeThemeId = themeId,
            textColorHex = theme.textColorHex
        )
        _localSettings.value = current
        viewModelScope.launch {
            settingsRepository.updateTheme(themeId)
            settingsRepository.updateTextColor(theme.textColorHex)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            _localSettings.value = TeleprompterSettings()
        }
    }

    fun saveScriptSpecificSettings(onDone: () -> Unit) {
        if (!scriptId.isNullOrBlank()) {
            viewModelScope.launch {
                val current = uiState.value.settings
                val script = scriptRepository.getScriptByIdDirect(scriptId)
                if (script != null) {
                    val updated = script.copy(
                        scrollSpeed = current.scrollSpeed,
                        fontSize = current.fontSize,
                        fontStyle = current.fontStyle.name,
                        textColorHex = current.textColorHex,
                        bgOpacity = current.bgOpacity,
                        textOpacity = current.textOpacity,
                        readingWidthPercent = current.readingWidthPercent,
                        verticalPositionPercent = current.verticalPositionPercent,
                        lineSpacingMultiplier = current.lineSpacingMultiplier,
                        letterSpacingSp = current.letterSpacingSp,
                        textAlignment = current.textAlignment.name,
                        mirrorText = current.mirrorText,
                        showSafeAreaGuide = current.showSafeAreaGuide,
                        presetType = current.presetType.name,
                        hasCustomSettings = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    scriptRepository.updateScript(updated)
                }
                onDone()
            }
        } else {
            onDone()
        }
    }
}
