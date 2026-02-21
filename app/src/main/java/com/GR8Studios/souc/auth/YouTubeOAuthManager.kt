package com.GR8Studios.souc.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.GR8Studios.souc.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

class YouTubeOAuthManager(private val context: Context) {
    private val tokenStorage = SecureTokenStorage(context)

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        // Request server auth code so backend can exchange for refresh/access tokens.
        // Replace default_web_client_id with your OAuth Web Client ID from Google Cloud console.
        .requestServerAuthCode(context.getString(R.string.default_web_client_id), true)
        .requestScopes(SCOPE_YOUTUBE_UPLOAD, SCOPE_YOUTUBE_READONLY)
        .build()

    fun getConnectIntent(): Intent = GoogleSignIn.getClient(context, gso).signInIntent

    fun handleResult(data: Intent?): Result<GoogleSignInAccount> {
        return runCatching {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
                ?: error("No Google account returned from YouTube OAuth")
        }
    }

    fun persistAuthCodeAsScaffold(authCode: String) {
        // Scaffold mode: store auth code in secure storage placeholder until backend token exchange is added.
        // In production, exchange authCode server-side and store real access/refresh token pair.
        val accessTokenPlaceholder = "auth_code:$authCode"
        val refreshTokenPlaceholder = "pending_server_exchange"
        val expiry = System.currentTimeMillis() + 55 * 60 * 1000L
        tokenStorage.saveYouTubeTokens(accessTokenPlaceholder, refreshTokenPlaceholder, expiry)
    }

    fun hasValidConnection(): Boolean = tokenStorage.isYouTubeConnectedAndValid()

    fun clearConnection() = tokenStorage.clearYouTubeTokens()

    companion object {
        private val SCOPE_YOUTUBE_UPLOAD = Scope("https://www.googleapis.com/auth/youtube.upload")
        private val SCOPE_YOUTUBE_READONLY = Scope("https://www.googleapis.com/auth/youtube.readonly")

        const val REQ_CODE_YOUTUBE_OAUTH = 7001
    }
}
