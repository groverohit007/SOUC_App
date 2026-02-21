package com.GR8Studios.souc.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.GR8Studios.souc.R
import com.GR8Studios.souc.data.AppDefaults
import com.google.android.gms.auth.api.signin.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credential = credentialManager.getCredential(context, request).credential
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
            }.onFailure {
                onError(it.message ?: "Google sign-in failed")
            }
        }

    private suspend fun getDocument(
        ref: com.google.firebase.firestore.DocumentReference
    ): com.google.firebase.firestore.DocumentSnapshot = suspendCancellableCoroutine { cont ->
        ref.get().addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun setDocument(
        ref: com.google.firebase.firestore.DocumentReference,
        data: Map<String, Any>
    ) = suspendCancellableCoroutine<Unit> { cont ->
        ref.set(data, SetOptions.merge())
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

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
}
