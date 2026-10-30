package com.teleprompter.app.core.auth

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.teleprompter.app.R
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
    fun getGoogleSignInIntent(context: Context): Intent
    suspend fun handleSignInResult(intent: Intent?): Result<AuthUser>
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
            val email = prefs[AuthKeys.USER_EMAIL]
            val effectiveId = if (!email.isNullOrBlank()) email.trim().lowercase() else userId
            if (!effectiveId.isNullOrBlank()) {
                val name = prefs[AuthKeys.USER_NAME]
                val isAnon = prefs[AuthKeys.IS_ANONYMOUS] ?: false
                _currentUser.value = AuthUser(
                    id = effectiveId,
                    email = email,
                    displayName = name,
                    isAnonymous = isAnon
                )
            }
        }
    }

    override fun getGoogleSignInIntent(context: Context): Intent {
        // Native Android Google Account Picker - works reliably without OAuth developer error 10
        return AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf("com.google"),
            null,
            null,
            null,
            null
        )
    }

    override suspend fun handleSignInResult(intent: Intent?): Result<AuthUser> {
        if (intent == null) {
            return Result.failure(Exception("Sign-in was cancelled."))
        }

        // 1. Native Android Google Account Picker (AccountManager)
        val accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (!accountName.isNullOrBlank()) {
            val email = accountName.trim()
            val cleanName = email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .replace("-", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                .ifBlank { "Creator" }

            var firebaseUid: String? = null
            if (isFirebaseAvailable) {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val currentFbUser = auth.currentUser
                    if (currentFbUser != null) {
                        firebaseUid = currentFbUser.uid
                    } else {
                        val authResult = auth.signInAnonymously().await()
                        firebaseUid = authResult.user?.uid
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AuthRepository", "Firebase auth link: ${e.message}")
                }
            }

            val user = AuthUser(
                id = email.trim().lowercase(),
                email = email,
                displayName = cleanName,
                photoUrl = null,
                isAnonymous = false
            )
            saveSession(user)
            _currentUser.value = user
            return Result.success(user)
        }

        // 2. Try GoogleSignIn with OAuth ID token for real Firebase Auth registration
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val email = account.email ?: "creator@gmail.com"
            val name = account.displayName ?: (account.givenName ?: "Google User")
            val idToken = account.idToken
            val photoUrl = account.photoUrl?.toString()

            var firebaseUid: String? = null
            if (isFirebaseAvailable && !idToken.isNullOrBlank()) {
                try {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                    firebaseUid = authResult.user?.uid
                } catch (_: Exception) {}
            }

            val user = AuthUser(
                id = email.trim().lowercase(),
                email = email,
                displayName = name,
                photoUrl = photoUrl,
                isAnonymous = false
            )
            saveSession(user)
            _currentUser.value = user
            return Result.success(user)
        } catch (_: Exception) {}

        return Result.failure(Exception("Sign-in cancelled or failed."))
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
                    val finalEmail = firebaseUser.email ?: email
                    val user = AuthUser(
                        id = finalEmail?.trim()?.lowercase() ?: firebaseUser.uid,
                        email = finalEmail,
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
            val finalEmail = email ?: "user@gmail.com"
            val user = AuthUser(
                id = finalEmail.trim().lowercase(),
                email = finalEmail,
                displayName = name ?: "Google User",
                photoUrl = null,
                isAnonymous = false
            )
            saveSession(user)
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            val finalEmail = email ?: "user@gmail.com"
            val user = AuthUser(
                id = finalEmail.trim().lowercase(),
                email = finalEmail,
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
        val cleanEmail = email.trim().lowercase()
        val user = AuthUser(
            id = cleanEmail,
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
