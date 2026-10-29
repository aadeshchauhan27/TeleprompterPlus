package com.teleprompter.app.ui.screens.recordings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.camera.CameraEngine
import com.teleprompter.app.data.local.entity.RecordingEntity
import com.teleprompter.app.data.repository.RecordingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordingsUiState(
    val recordings: List<RecordingEntity> = emptyList(),
    val isLoading: Boolean = false
)

class RecordingsViewModel(
    private val recordingRepository: RecordingRepository,
    private val cameraEngine: CameraEngine
) : ViewModel() {

    val recordings: StateFlow<List<RecordingEntity>> = recordingRepository
        .getAllRecordings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRecording(id: String) {
        viewModelScope.launch {
            recordingRepository.deleteRecording(id)
        }
    }

    fun renameRecording(id: String, newTitle: String) {
        viewModelScope.launch {
            recordingRepository.renameRecording(id, newTitle)
        }
    }

    fun createShareIntent(videoUriString: String): Intent {
        val uri = Uri.parse(videoUriString)
        return cameraEngine.createShareIntent(uri)
    }
}
