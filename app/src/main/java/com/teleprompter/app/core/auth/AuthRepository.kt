package com.teleprompter.app.core.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.teleprompter.app.data.repository.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    val isUserSignedIn: StateFlow<Boolean>
    suspend fun signInWithGoogleToken(idToken: String, email: String? = null, name: String? = null): Result<AuthUser>
    suspend fun signInWithEmail(email: String, name: String? = null): Result<AuthUser>
    suspend fun continueAsGuest(): AuthUser
    suspend fun signOut()
}

class AuthRepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AuthRepository {

    private object AuthKeys {
        val USER_ID = stringPreferencesKey("auth_user_id")
        val USER_EMAIL = stringPreferencesKey("auth_user_email")
        val USER_NAME = stringPreferencesKey("auth_user_name")
        val IS_ANONYMOUS = booleanPreferencesKey("auth_is_anonymous")
    }

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Exception) {
            false
        }

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override val isUserSignedIn: StateFlow<Boolean> = _currentUser.map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    init {
        // Restore session from DataStore / Firebase
        scope.launch {
            if (isFirebaseAvailable) {
                try {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        _currentUser.value = AuthUser(
                            id = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            isAnonymous = firebaseUser.isAnonymous
                        )
                        return@launch
                    }
                } catch (_: Exception) {}
            }

            // Restore from local DataStore session
            val prefs = context.dataStore.data.first()
            val userId = prefs[AuthKeys.USER_ID]
            if (!userId.isNullOrBlank()) {
                val email = prefs[AuthKeys.USER_EMAIL]
                val name = prefs[AuthKeys.USER_NAME]
                val isAnon = prefs[AuthKeys.IS_ANONYMOUS] ?: false
                _currentUser.value = AuthUser(
                    id = userId,
                    email = email,
                    displayName = name,
                    isAnonymous = isAnon
                )
            }
        }
    }

    override suspend fun signInWithGoogleToken(
        idToken: String,
        email: String?,
        name: String?
    ): Result<AuthUser> {
        return try {
            if (isFirebaseAvailable && idToken.isNotBlank()) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = AuthUser(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        displayName = firebaseUser.displayName ?: name,
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isAnonymous = false
                    )
                    saveSession(user)
                    _currentUser.value = user
                    return Result.success(user)
                }
            }

            // Fallback / direct account sign-in
            val user = AuthUser(
                id = "google_${UUID.randomUUID().toString().take(8)}",
                email = email ?: "user@gmail.com",
                displayName = name ?: "Google User",
                photoUrl = null,
                isAnonymous = false
            )
            saveSession(user)
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            // Graceful fallback for offline mode
            val user = AuthUser(
                id = "google_offline_${UUID.randomUUID().toString().take(8)}",
                email = email ?: "user@gmail.com",
                displayName = name ?: "Google User",
                photoUrl = null,
                isAnonymous = false
            )
            saveSession(user)
            _currentUser.value = user
            Result.success(user)
        }
    }

    override suspend fun signInWithEmail(email: String, name: String?): Result<AuthUser> {
        val user = AuthUser(
            id = "email_${UUID.randomUUID().toString().take(8)}",
            email = email,
            displayName = name ?: email.substringBefore("@"),
            isAnonymous = false
        )
        saveSession(user)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun continueAsGuest(): AuthUser {
        val guestUser = AuthUser(
            id = "guest_${UUID.randomUUID().toString().take(8)}",
            email = null,
            displayName = "Guest Creator",
            isAnonymous = true
        )
        saveSession(guestUser)
        _currentUser.value = guestUser
        return guestUser
    }

    override suspend fun signOut() {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}
        }

        context.dataStore.edit { prefs ->
            prefs.remove(AuthKeys.USER_ID)
            prefs.remove(AuthKeys.USER_EMAIL)
            prefs.remove(AuthKeys.USER_NAME)
            prefs.remove(AuthKeys.IS_ANONYMOUS)
        }
        _currentUser.value = null
        // Crucial: Local scripts and recordings are NOT deleted on sign out!
    }

    private suspend fun saveSession(user: AuthUser) {
        context.dataStore.edit { prefs ->
            prefs[AuthKeys.USER_ID] = user.id
            if (user.email != null) prefs[AuthKeys.USER_EMAIL] = user.email else prefs.remove(AuthKeys.USER_EMAIL)
            if (user.displayName != null) prefs[AuthKeys.USER_NAME] = user.displayName else prefs.remove(AuthKeys.USER_NAME)
            prefs[AuthKeys.IS_ANONYMOUS] = user.isAnonymous
        }
    }
}
