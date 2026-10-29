package com.teleprompter.app.core.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CameraLens {
    FRONT,
    BACK
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

data class CameraRecordingStatus(
    val state: RecordingState = RecordingState.IDLE,
    val durationSeconds: Long = 0,
    val formattedDuration: String = "00:00",
    val isTorchOn: Boolean = false,
    val currentLens: CameraLens = CameraLens.FRONT,
    val outputUri: Uri? = null,
    val errorMessage: String? = null
)

class CameraEngine(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentCamera: Camera? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private val _status = MutableStateFlow(CameraRecordingStatus())
    val status: StateFlow<CameraRecordingStatus> = _status.asStateFlow()

    private var currentTempFile: File? = null

    fun clearErrorMessage() {
        _status.value = _status.value.copy(errorMessage = null)
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lens: CameraLens = _status.value.currentLens,
        videoQuality: com.teleprompter.app.core.model.VideoQualityPreference = com.teleprompter.app.core.model.VideoQualityPreference.HIGH,
        defaultFlashOn: Boolean = false,
        onCameraReady: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val provider = cameraProvider ?: return@addListener

                val cameraSelector = if (lens == CameraLens.FRONT) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val targetQuality = when (videoQuality) {
                    com.teleprompter.app.core.model.VideoQualityPreference.ULTRA -> Quality.UHD
                    com.teleprompter.app.core.model.VideoQualityPreference.STANDARD -> Quality.HD
                    else -> Quality.FHD
                }
                val fallbackQuality = when (videoQuality) {
                    com.teleprompter.app.core.model.VideoQualityPreference.ULTRA -> Quality.FHD
                    else -> Quality.SD
                }

                val qualitySelector = QualitySelector.from(
                    targetQuality,
                    FallbackStrategy.lowerQualityOrHigherThan(fallbackQuality)
                )

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                provider.unbindAll()
                currentCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )

                _status.value = _status.value.copy(
                    currentLens = lens,
                    isTorchOn = false
                )

                if (defaultFlashOn && lens == CameraLens.BACK && currentCamera?.cameraInfo?.hasFlashUnit() == true) {
                    currentCamera?.cameraControl?.enableTorch(true)
                    _status.value = _status.value.copy(isTorchOn = true)
                }

                onCameraReady()
            } catch (e: Exception) {
                _status.value = _status.value.copy(errorMessage = e.localizedMessage)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun unbindCamera() {
        try {
            cameraProvider?.unbindAll()
            currentCamera = null
        } catch (_: Exception) {}
    }

    fun toggleCameraLens(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        // Prevent camera lens flip during active recording to prevent stream corruption
        if (_status.value.state == RecordingState.RECORDING || _status.value.state == RecordingState.PAUSED) {
            return
        }
        val nextLens = if (_status.value.currentLens == CameraLens.FRONT) {
            CameraLens.BACK
        } else {
            CameraLens.FRONT
        }
        bindCamera(lifecycleOwner, previewView, nextLens)
    }

    fun toggleTorch() {
        val camera = currentCamera ?: return
        val currentTorch = _status.value.isTorchOn
        val targetTorch = !currentTorch
        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(targetTorch)
            _status.value = _status.value.copy(isTorchOn = targetTorch)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        recordAudio: Boolean = true,
        onFinalized: (Uri) -> Unit = {}
    ) {
        val capture = videoCapture ?: return
        if (activeRecording != null) return

        // Verify storage capacity before recording
        if (com.teleprompter.app.core.util.ThumbnailUtils.isStorageCriticallyLow()) {
            _status.value = _status.value.copy(
                errorMessage = "Insufficient storage space to record video"
            )
            return
        }

        val fileName = "TelePrompter_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
        val outputFile = File(context.cacheDir, fileName)
        currentTempFile = outputFile

        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        val pendingRecording = capture.output
            .prepareRecording(context, outputOptions)

        // Try to enable audio recording if preferred and permission granted
        if (recordAudio) {
            try {
                pendingRecording.withAudioEnabled()
            } catch (_: SecurityException) {
                // Audio permission might not be granted; record video without audio
            } catch (_: Exception) {
                // Camera/audio configuration fallback
            }
        }

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    _status.value = _status.value.copy(
                        state = RecordingState.RECORDING,
                        durationSeconds = 0,
                        formattedDuration = "00:00"
                    )
                }

                is VideoRecordEvent.Status -> {
                    val durationNanos = event.recordingStats.recordedDurationNanos
                    val seconds = durationNanos / 1_000_000_000L
                    val mins = seconds / 60
                    val secs = seconds % 60
                    val formatted = String.format(Locale.US, "%02d:%02d", mins, secs)
                    _status.value = _status.value.copy(
                        durationSeconds = seconds,
                        formattedDuration = formatted
                    )
                }

                is VideoRecordEvent.Pause -> {
                    _status.value = _status.value.copy(state = RecordingState.PAUSED)
                }

                is VideoRecordEvent.Resume -> {
                    _status.value = _status.value.copy(state = RecordingState.RECORDING)
                }

                is VideoRecordEvent.Finalize -> {
                    val fileUri = Uri.fromFile(outputFile)
                    _status.value = _status.value.copy(
                        state = RecordingState.STOPPED,
                        outputUri = fileUri
                    )
                    activeRecording = null
                    if (!event.hasError()) {
                        onFinalized(fileUri)
                    } else {
                        _status.value = _status.value.copy(
                            errorMessage = "Recording error: ${event.cause?.message}"
                        )
                    }
                }
            }
        }
    }

    fun pauseRecording() {
        activeRecording?.pause()
    }

    fun resumeRecording() {
        activeRecording?.resume()
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun saveVideoToGallery(videoUri: Uri): Uri? {
        val file = if (videoUri.scheme == "file") {
            File(videoUri.path ?: return null)
        } else {
            currentTempFile ?: return null
        }

        if (!file.exists()) return null

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TelePrompter")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val insertedUri = resolver.insert(collection, contentValues) ?: return null

        try {
            resolver.openOutputStream(insertedUri)?.use { out ->
                FileInputStream(file).use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(insertedUri, contentValues, null, null)
            }
            return insertedUri
        } catch (e: Exception) {
            resolver.delete(insertedUri, null, null)
            return null
        }
    }

    fun createShareIntent(videoUri: Uri): android.content.Intent {
        val file = if (videoUri.scheme == "file") {
            File(videoUri.path ?: "")
        } else {
            currentTempFile ?: File(videoUri.path ?: "")
        }

        val contentUri = if (file.exists()) {
            try {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (_: Exception) {
                videoUri
            }
        } else {
            videoUri
        }

        return android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
