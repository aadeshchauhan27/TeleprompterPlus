package com.teleprompter.app.core.auth

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)
