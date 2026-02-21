package com.GR8Studios.souc.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureTokenStorage(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveGoogleProfile(googleId: String, email: String?, displayName: String?) {
        prefs.edit()
            .putString(KEY_GOOGLE_ID, googleId)
            .putString(KEY_GOOGLE_EMAIL, email)
            .putString(KEY_GOOGLE_NAME, displayName)
            .apply()
    }

    fun readGoogleProfile(): AuthUser? {
        val id = prefs.getString(KEY_GOOGLE_ID, null) ?: return null
        return AuthUser(
            googleId = id,
            email = prefs.getString(KEY_GOOGLE_EMAIL, null),
            displayName = prefs.getString(KEY_GOOGLE_NAME, null)
        )
    }

    fun saveYouTubeTokens(accessToken: String, refreshToken: String, expiryEpochMillis: Long) {
        prefs.edit()
            .putString(KEY_YT_ACCESS, accessToken)
            .putString(KEY_YT_REFRESH, refreshToken)
            .putLong(KEY_YT_EXPIRY, expiryEpochMillis)
            .apply()
    }

    fun clearYouTubeTokens() {
        prefs.edit()
            .remove(KEY_YT_ACCESS)
            .remove(KEY_YT_REFRESH)
            .remove(KEY_YT_EXPIRY)
            .apply()
    }

    fun isYouTubeConnectedAndValid(): Boolean {
        val access = prefs.getString(KEY_YT_ACCESS, null)
        val expiry = prefs.getLong(KEY_YT_EXPIRY, 0L)
        if (access.isNullOrBlank()) return false
        if (expiry == 0L) return false
        return System.currentTimeMillis() < expiry
    }

    companion object {
        private const val PREF_NAME = "souc_secure_store"
        private const val KEY_GOOGLE_ID = "google_id"
        private const val KEY_GOOGLE_EMAIL = "google_email"
        private const val KEY_GOOGLE_NAME = "google_name"
        private const val KEY_YT_ACCESS = "youtube_access_token"
        private const val KEY_YT_REFRESH = "youtube_refresh_token"
        private const val KEY_YT_EXPIRY = "youtube_token_expiry"
    }
}
