package com.example.cashroyale.Services

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthService {
    private val auth: FirebaseAuth = Firebase.auth

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Result.success(result.user!!)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    companion object {
        @Volatile
        private var INSTANCE: AuthService? = null

        fun getInstance(): AuthService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthService().also { INSTANCE = it }
            }
        }
    }
}