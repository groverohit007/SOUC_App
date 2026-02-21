package com.GR8Studios.souc.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.GR8Studios.souc.R
import com.GR8Studios.souc.data.AppDefaults
import com.google.android.gms.auth.api.signin.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleAuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val tokenStorage = SecureTokenStorage(context)

    fun signInAndSyncProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                val credential = runCatching {
                    requestWithGoogleIdOption()
                }.recoverCatching { throwable ->
                    if (throwable is NoCredentialException) {
                        requestWithGoogleButtonFlow()
                    } else {
                        throw throwable
                    }
                }.getOrThrow()

                val tokenCredential = try {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } catch (e: GoogleIdTokenParsingException) {
                    throw IllegalStateException("Unable to parse Google credential", e)
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = signInWithFirebase(firebaseCredential)
                val firebaseUser = authResult.user ?: error("No user returned from Firebase")

                val isAdmin = firebaseUser.email.equals(AppDefaults.MASTER_EMAIL, ignoreCase = true)
                val tier = if (isAdmin) "PREMIUM" else "FREE"
                val userData = hashMapOf(
                    "email" to (firebaseUser.email ?: ""),
                    "tier" to tier,
                    "isAdmin" to isAdmin,
                    "postsUsed" to 0,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
                    .set(userData, SetOptions.merge())

                AuthSession.setUser(AuthUser(firebaseUser.uid, firebaseUser.email, firebaseUser.displayName))
                tokenStorage.saveGoogleProfile(firebaseUser.uid, firebaseUser.email, firebaseUser.displayName)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.toUserMessage())
            }
        }
    }

    private suspend fun requestWithGoogleIdOption() = credentialManager
        .getCredential(
            context,
            GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(false)
                        .build()
                )
                .build()
        ).credential

    private suspend fun requestWithGoogleButtonFlow() = credentialManager
        .getCredential(
            context,
            GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
                )
                .build()
        ).credential

    private suspend fun signInWithFirebase(credential: AuthCredential): AuthResult =
        suspendCancellableCoroutine { cont ->
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    fun restoreSession() {
        FirebaseAuth.getInstance().currentUser?.let {
            AuthSession.setUser(AuthUser(it.uid, it.email, it.displayName))
            return
        }
        AuthSession.setUser(tokenStorage.readGoogleProfile())
    }

    private fun Throwable.toUserMessage(): String {
        return when (this) {
            is NoCredentialException -> "No Google account found on this device. Add an account and try again."
            is GetCredentialException -> "Google sign-in is unavailable right now. Check Play Services and try again."
            else -> message ?: "Google sign-in failed"
        }
    }
}
