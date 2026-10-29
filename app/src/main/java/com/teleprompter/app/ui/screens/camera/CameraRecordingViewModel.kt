package com.teleprompter.app.ui.screens.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.camera.CameraEngine
import com.teleprompter.app.core.camera.CameraLens
import com.teleprompter.app.core.camera.CameraRecordingStatus
import com.teleprompter.app.core.camera.RecordingState
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.PresetType
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.model.TextAlignmentType
import com.teleprompter.app.core.model.VideoQualityPreference
import com.teleprompter.app.data.local.entity.RecordingEntity
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.RecordingRepository
import com.teleprompter.app.data.repository.ScriptRepository
import com.teleprompter.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CameraScreenUiState(
    val script: ScriptEntity? = null,
    val settings: TeleprompterSettings = TeleprompterSettings(),
    val cameraStatus: CameraRecordingStatus = CameraRecordingStatus(),
    val isCustomizationSheetOpen: Boolean = false,
    val isCameraBound: Boolean = false,
    val savedVideoUri: Uri? = null,
    val targetTotalTimeSeconds: Int = 300 // default 5 min (05:00)
)

class CameraRecordingViewModel(
    savedStateHandle: SavedStateHandle,
    private val scriptRepository: ScriptRepository,
    private val settingsRepository: SettingsRepository,
    private val recordingRepository: RecordingRepository,
    val cameraEngine: CameraEngine
) : ViewModel() {

    private val scriptId: String? = savedStateHandle.get<String>("scriptId")

    private val _isCustomizationSheetOpen = MutableStateFlow(false)
    private val _isCameraBound = MutableStateFlow(false)
    private val _script = MutableStateFlow<ScriptEntity?>(null)

    val uiState: StateFlow<CameraScreenUiState> = combine(
        _script,
        settingsRepository.settingsFlow,
        cameraEngine.status,
        _isCustomizationSheetOpen,
        _isCameraBound
    ) { script, settings, cameraStatus, isSheetOpen, isBound ->
        val totalSec = if (script != null && script.estimatedReadingTime > 0) {
            script.estimatedReadingTime * 60
        } else {
            300
        }
        CameraScreenUiState(
            script = script,
            settings = settings,
            cameraStatus = cameraStatus,
            isCustomizationSheetOpen = isSheetOpen,
            isCameraBound = isBound,
            targetTotalTimeSeconds = totalSec
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CameraScreenUiState()
    )

    init {
        loadScript()
    }

    private fun loadScript() {
        if (scriptId == null) return
        viewModelScope.launch {
            scriptRepository.getScriptById(scriptId).collect { loadedScript ->
                _script.value = loadedScript
                // Restore script-specific settings if customized
                if (loadedScript != null && loadedScript.hasCustomSettings) {
                    val fontStyle = try {
                        FontStyleType.valueOf(loadedScript.fontStyle)
                    } catch (_: Exception) {
                        FontStyleType.SANS
                    }
                    val alignment = try {
                        TextAlignmentType.valueOf(loadedScript.textAlignment)
                    } catch (_: Exception) {
                        TextAlignmentType.CENTER
                    }
                    val preset = try {
                        PresetType.valueOf(loadedScript.presetType)
                    } catch (_: Exception) {
                        PresetType.CUSTOM
                    }
                    settingsRepository.applyScriptSettings(
                        scrollSpeed = loadedScript.scrollSpeed,
                        fontSize = loadedScript.fontSize,
                        fontStyle = fontStyle,
                        textColorHex = loadedScript.textColorHex,
                        bgOpacity = loadedScript.bgOpacity,
                        textOpacity = loadedScript.textOpacity,
                        readingWidthPercent = loadedScript.readingWidthPercent,
                        verticalPositionPercent = loadedScript.verticalPositionPercent,
                        lineSpacingMultiplier = loadedScript.lineSpacingMultiplier,
                        letterSpacingSp = loadedScript.letterSpacingSp,
                        textAlignment = alignment,
                        mirrorText = loadedScript.mirrorText,
                        presetType = preset
                    )
                }
            }
        }
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val currentSettings = uiState.value.settings
        cameraEngine.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            lens = currentSettings.defaultCameraLens,
            videoQuality = currentSettings.videoQuality,
            defaultFlashOn = currentSettings.defaultFlashOn,
            onCameraReady = {
                _isCameraBound.value = true
            }
        )
    }

    fun toggleCameraLens(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraEngine.toggleCameraLens(lifecycleOwner, previewView)
    }

    fun toggleTorch() {
        cameraEngine.toggleTorch()
    }

    fun startRecording(onFinalized: (Uri) -> Unit) {
        val recordAudio = uiState.value.settings.recordAudio
        cameraEngine.startRecording(recordAudio = recordAudio, onFinalized = onFinalized)
    }

    fun pauseRecording() {
        cameraEngine.pauseRecording()
    }

    fun resumeRecording() {
        cameraEngine.resumeRecording()
    }

    fun stopRecording() {
        cameraEngine.stopRecording()
    }

    fun clearCameraError() {
        cameraEngine.clearErrorMessage()
    }

    fun toggleCustomizationSheet() {
        _isCustomizationSheetOpen.value = !_isCustomizationSheetOpen.value
    }

    fun closeCustomizationSheet() {
        _isCustomizationSheetOpen.value = false
    }

    fun applyPreset(preset: PresetType) {
        viewModelScope.launch {
            settingsRepository.applyPreset(preset)
        }
    }

    fun updateFontSize(fontSizeSp: Int) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(fontSizeSp)
        }
    }

    fun updateScrollSpeed(speedWpm: Int) {
        viewModelScope.launch {
            settingsRepository.updateScrollSpeed(speedWpm)
        }
    }

    fun updateLineSpacing(spacingMultiplier: Float) {
        viewModelScope.launch {
            settingsRepository.updateLineSpacing(spacingMultiplier)
        }
    }

    fun updateLetterSpacing(spacingSp: Float) {
        viewModelScope.launch {
            settingsRepository.updateLetterSpacing(spacingSp)
        }
    }

    fun updateTextAlignment(alignment: TextAlignmentType) {
        viewModelScope.launch {
            settingsRepository.updateTextAlignment(alignment)
        }
    }

    fun updateBackgroundOpacity(opacityFraction: Float) {
        viewModelScope.launch {
            settingsRepository.updateBgOpacity(opacityFraction)
        }
    }

    fun updateTextOpacity(opacityFraction: Float) {
        viewModelScope.launch {
            settingsRepository.updateTextOpacity(opacityFraction)
        }
    }

    fun updateReadingWidth(percent: Int) {
        viewModelScope.launch {
            settingsRepository.updateReadingWidth(percent)
        }
    }

    fun updateVerticalPosition(percent: Int) {
        viewModelScope.launch {
            settingsRepository.updateVerticalPosition(percent)
        }
    }

    fun updateMirrorText(mirror: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateMirrorText(mirror)
        }
    }

    fun updateShowSafeArea(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowSafeArea(show)
        }
    }

    fun updateCountdown(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateCountdown(seconds)
        }
    }

    fun saveScriptSpecificSettings(onSaved: () -> Unit = {}) {
        val currentScript = _script.value ?: return
        val currentSettings = uiState.value.settings
        viewModelScope.launch {
            val updated = currentScript.copy(
                scrollSpeed = currentSettings.scrollSpeed,
                fontSize = currentSettings.fontSize,
                fontStyle = currentSettings.fontStyle.name,
                textColorHex = currentSettings.textColorHex,
                bgOpacity = currentSettings.bgOpacity,
                textOpacity = currentSettings.textOpacity,
                readingWidthPercent = currentSettings.readingWidthPercent,
                verticalPositionPercent = currentSettings.verticalPositionPercent,
                lineSpacingMultiplier = currentSettings.lineSpacingMultiplier,
                letterSpacingSp = currentSettings.letterSpacingSp,
                textAlignment = currentSettings.textAlignment.name,
                mirrorText = currentSettings.mirrorText,
                showSafeAreaGuide = currentSettings.showSafeAreaGuide,
                presetType = currentSettings.presetType.name,
                hasCustomSettings = true,
                updatedAt = System.currentTimeMillis()
            )
            scriptRepository.updateScript(updated)
            _script.value = updated
            onSaved()
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }

    suspend fun saveRecordedVideo(videoUri: Uri, durationSeconds: Long): RecordingEntity? {
        val currentScript = _script.value
        val title = currentScript?.title ?: "Recorded Video"
        return recordingRepository.saveRecording(
            scriptId = currentScript?.id,
            title = title,
            tempVideoUri = videoUri,
            durationSeconds = durationSeconds
        )
    }

    fun saveVideoToGallery(videoUri: Uri): Uri? {
        return cameraEngine.saveVideoToGallery(videoUri)
    }
}
