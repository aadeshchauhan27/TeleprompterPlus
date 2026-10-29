package com.teleprompter.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.auth.AuthRepository
import com.teleprompter.app.core.auth.AuthUser
import com.teleprompter.app.data.sync.SyncRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
    val isUserSignedIn: StateFlow<Boolean> = authRepository.isUserSignedIn

    fun signInWithGoogle(
        idToken: String = "",
        email: String? = null,
        name: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            authRepository.signInWithGoogleToken(idToken, email, name)
            // Trigger background sync after sign-in
            syncRepository.syncNow()
            onSuccess()
        }
    }

    fun signInWithEmail(email: String, name: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithEmail(email, name)
            syncRepository.syncNow()
            onSuccess()
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.continueAsGuest()
            onSuccess()
        }
    }
}
