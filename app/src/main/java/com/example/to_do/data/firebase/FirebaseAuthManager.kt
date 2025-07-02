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
            Timber.d("FirebaseAuthManager: User already signed in with ID: ${it.uid}")
            return true
        }
        
        // Attempt anonymous sign-in
        return try {
            Timber.d("FirebaseAuthManager: Attempting anonymous sign-in...")
            val result = auth.signInAnonymously().await()
            result.user?.let {
                Timber.d("FirebaseAuthManager: Anonymous sign-in successful, user ID: ${it.uid}")
                true
            } ?: run {
                Timber.e("FirebaseAuthManager: Anonymous sign-in succeeded but user is null")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthManager: Anonymous sign-in failed: ${e.javaClass.simpleName}: ${e.message}")
            
            // Check for specific Firebase Auth errors
            when {
                e.message?.contains("network error") == true -> {
                    Timber.e("FirebaseAuthManager: Network error during sign-in. Check internet connection.")
                }
                e.message?.contains("API_KEY_INVALID") == true -> {
                    Timber.e("FirebaseAuthManager: Invalid API key. Check your Firebase configuration.")
                }
                e.message?.contains("PROJECT_NOT_FOUND") == true -> {
                    Timber.e("FirebaseAuthManager: Project not found. Check your Firebase project ID.")
                }
            }
            
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
