package com.GR8Studios.souc.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.GR8Studios.souc.R

class GoogleAuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val tokenStorage = SecureTokenStorage(context)

    suspend fun signIn(): Result<AuthUser> {
        return runCatching {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(context, request)
            val credential = credentialResponse.credential
            val googleIdTokenCredential = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                throw IllegalStateException("Unable to parse Google credential", e)
            }

            val user = AuthUser(
                googleId = googleIdTokenCredential.id,
                email = googleIdTokenCredential.id,
                displayName = googleIdTokenCredential.displayName
            )
            tokenStorage.saveGoogleProfile(user.googleId, user.email, user.displayName)
            AuthSession.setUser(user)
            user
        }
    }

    fun restoreSession() {
        AuthSession.setUser(tokenStorage.readGoogleProfile())
    }
}
