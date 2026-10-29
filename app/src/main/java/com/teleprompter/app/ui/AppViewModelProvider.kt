package com.teleprompter.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.teleprompter.app.TelePrompterApp
import com.teleprompter.app.ui.screens.auth.SignInViewModel
import com.teleprompter.app.ui.screens.camera.CameraRecordingViewModel
import com.teleprompter.app.ui.screens.detail.ScriptDetailViewModel
import com.teleprompter.app.ui.screens.editor.EditorViewModel
import com.teleprompter.app.ui.screens.folders.FolderViewModel
import com.teleprompter.app.ui.screens.home.HomeViewModel
import com.teleprompter.app.ui.screens.profile.ProfileViewModel
import com.teleprompter.app.ui.screens.recordings.RecordingsViewModel
import com.teleprompter.app.ui.screens.search.SearchViewModel
import com.teleprompter.app.ui.screens.settings.ScriptSettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                scriptRepository = telePrompterApp().container.scriptRepository
            )
        }
        initializer {
            EditorViewModel(
                savedStateHandle = createSavedStateHandle(),
                scriptRepository = telePrompterApp().container.scriptRepository
            )
        }
        initializer {
            ScriptDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                scriptRepository = telePrompterApp().container.scriptRepository,
                folderRepository = telePrompterApp().container.folderRepository
            )
        }
        initializer {
            FolderViewModel(
                savedStateHandle = createSavedStateHandle(),
                folderRepository = telePrompterApp().container.folderRepository,
                scriptRepository = telePrompterApp().container.scriptRepository
            )
        }
        initializer {
            SearchViewModel(
                scriptRepository = telePrompterApp().container.scriptRepository
            )
        }
        initializer {
            ScriptSettingsViewModel(
                savedStateHandle = createSavedStateHandle(),
                settingsRepository = telePrompterApp().container.settingsRepository,
                scriptRepository = telePrompterApp().container.scriptRepository
            )
        }
        initializer {
            CameraRecordingViewModel(
                savedStateHandle = createSavedStateHandle(),
                scriptRepository = telePrompterApp().container.scriptRepository,
                settingsRepository = telePrompterApp().container.settingsRepository,
                recordingRepository = telePrompterApp().container.recordingRepository,
                cameraEngine = telePrompterApp().container.cameraEngine
            )
        }
        initializer {
            RecordingsViewModel(
                recordingRepository = telePrompterApp().container.recordingRepository,
                cameraEngine = telePrompterApp().container.cameraEngine
            )
        }
        initializer {
            SignInViewModel(
                authRepository = telePrompterApp().container.authRepository,
                syncRepository = telePrompterApp().container.syncRepository
            )
        }
        initializer {
            ProfileViewModel(
                authRepository = telePrompterApp().container.authRepository,
                syncRepository = telePrompterApp().container.syncRepository
            )
        }
    }
}

fun CreationExtras.telePrompterApp(): TelePrompterApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TelePrompterApp)
