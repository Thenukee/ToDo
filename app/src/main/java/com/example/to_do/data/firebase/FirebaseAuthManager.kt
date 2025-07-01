package com.example.to_do.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for Firebase Authentication operations
 */
@Singleton
class FirebaseAuthManager @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Get the current user, or null if not authenticated
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Sign in anonymously if not already signed in
     * @return success status of the operation
     */
    suspend fun ensureSignedIn(): Boolean {
        // If already signed in, return true
        auth.currentUser?.let {
            Timber.d("User already signed in with ID: ${it.uid}")
            return true
        }
        
        // Attempt anonymous sign-in
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.let {
                Timber.d("Anonymous sign-in successful, user ID: ${it.uid}")
                true
            } ?: false
        } catch (e: Exception) {
            Timber.e(e, "Anonymous sign-in failed")
            false
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        auth.signOut()
        Timber.d("User signed out")
    }
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean = auth.currentUser != null
    
    /**
     * Get the current user ID or null
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
