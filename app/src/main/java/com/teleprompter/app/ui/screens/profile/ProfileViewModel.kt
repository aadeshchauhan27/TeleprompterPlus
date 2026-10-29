package com.teleprompter.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.auth.AuthRepository
import com.teleprompter.app.core.auth.AuthUser
import com.teleprompter.app.core.model.AppThemeMode
import com.teleprompter.app.data.repository.SettingsRepository
import com.teleprompter.app.data.sync.SyncRepository
import com.teleprompter.app.data.sync.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: AuthUser? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isSigningOut: Boolean = false,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.currentUser,
        syncRepository.syncStatus,
        syncRepository.isCloudSyncEnabled,
        syncRepository.lastSyncTime,
        settingsRepository.settingsFlow
    ) { user, syncStatus, isSyncEnabled, lastSyncTime, settings ->
        ProfileUiState(
            user = user,
            syncStatus = syncStatus,
            isCloudSyncEnabled = isSyncEnabled,
            lastSyncTime = lastSyncTime,
            appThemeMode = settings.appThemeMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.setCloudSyncEnabled(enabled)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncRepository.syncNow()
        }
    }

    fun setAppThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateAppThemeMode(themeMode)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }
}
