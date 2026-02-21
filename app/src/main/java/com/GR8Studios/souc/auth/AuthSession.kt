package com.GR8Studios.souc.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AuthUser(
    val googleId: String,
    val email: String?,
    val displayName: String?
)

object AuthSession {
    var currentUser: AuthUser? by mutableStateOf(null)
        private set

    fun setUser(user: AuthUser?) {
        currentUser = user
    }

    val isLoggedIn: Boolean
        get() = currentUser != null
}
