package com.screenlog.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUserStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun signInWithEmail(email: String, javaPassword: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, javaPassword).awaitResult()
        return result.user ?: throw Exception("User is null")
    }

    suspend fun signUpWithEmail(email: String, javaPassword: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, javaPassword).awaitResult()
        return result.user ?: throw Exception("User creation failed")
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun updatePassword(newPassword: String) {
        val user = firebaseAuth.currentUser ?: throw Exception("No user signed in")
        user.updatePassword(newPassword).awaitResult()
    }

    // Helper extension to await task results using standard Coroutines
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result, null)
                } else {
                    continuation.resumeWith(Result.failure(task.exception ?: Exception("Task failed")))
                }
            }
        }
    }
}
