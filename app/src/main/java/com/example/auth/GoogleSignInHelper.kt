package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

private const val TAG = "GoogleSignInHelper"

object GoogleSignInHelper {

    suspend fun signInWithGoogle(
        context: Context,
        onLoadingChange: (Boolean) -> Unit = {}
    ): Result<FirebaseUser> {
        val credentialManager = CredentialManager.create(context)
        val serverClientId = context.getString(R.string.default_web_client_id)

        // Credential Manager Option Exclusivity: exactly one credential option per request
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        onLoadingChange(true)
        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to retrieve authenticated user"))
                }
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled Google Sign-In bottom sheet", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "GetCredentialException during Google Sign-In: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed unexpectedly", e)
            Result.failure(e)
        } finally {
            onLoadingChange(false)
        }
    }

    suspend fun trySilentSignIn(context: Context): FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            return auth.currentUser
        }
        val credentialManager = CredentialManager.create(context)
        val serverClientId = context.getString(R.string.default_web_client_id)

        // Silent auto sign-in carries only GetGoogleIdOption
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                auth.signInWithCredential(authCredential).await().user
            } else null
        } catch (e: Exception) {
            Log.d(TAG, "Silent sign in not available: ${e.message}")
            null
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
