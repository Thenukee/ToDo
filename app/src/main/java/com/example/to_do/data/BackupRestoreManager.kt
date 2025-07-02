package com.example.to_do.data

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.to_do.data.worker.BackupWorker
import com.example.to_do.data.worker.RestoreWorker
import com.example.to_do.datastore.PreferencesManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.example.to_do.data.firebase.FirestoreManager

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val preferencesManager: PreferencesManager,
    private val firestoreManager: FirestoreManager
) {
    
    /**
     * Check if user is signed in
     */
    fun isUserSignedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }
    
    /**
     * Trigger an immediate backup to Firebase
     */
    fun backupNow() {
        if (!isUserSignedIn()) {
            Timber.w("Cannot backup: User not signed in")
            return
        }
        
        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(workDataOf("manual" to true))
            .build()
            
        workManager.enqueue(backupRequest)
        Timber.d("Manual backup enqueued")
    }
    
    /**
     * Restore data from Firebase
     */
    fun restoreFromCloud() {
        if (!isUserSignedIn()) {
            Timber.w("Cannot restore: User not signed in")
            return
        }
        
        val restoreRequest = OneTimeWorkRequestBuilder<RestoreWorker>().build()
        workManager.enqueue(restoreRequest)
        Timber.d("Restore operation enqueued")
    }
    
    /**
     * Get WorkManager to observe work status
     */
    fun getWorkManager(): WorkManager = workManager
    
    /**
     * Toggle auto-backup setting
     */
    suspend fun setAutoBackup(enabled: Boolean) {
        preferencesManager.setAutoBackup(enabled)
    }
    
    /**
     * Get auto-backup setting
     */
    suspend fun isAutoBackupEnabled(): Boolean {
        return preferencesManager.autoBackup.first()
    }
    
    /**
     * Update last backup time
     */
    suspend fun updateLastBackupTime() {
        preferencesManager.setLastBackupTime(System.currentTimeMillis())
    }
    
    /**
     * Send test data to Firestore to verify connectivity
     * @return true if test was successful, false otherwise
     */
    suspend fun sendTestDataToFirestore(): Boolean {
        if (!isUserSignedIn()) {
            Timber.w("BackupRestoreManager: Cannot test Firestore: User not signed in")
            // Try to sign in
            try {
                val signInResult = Firebase.auth.signInAnonymously().await()
                if (signInResult.user == null) {
                    Timber.e("BackupRestoreManager: Failed to sign in anonymously")
                    return false
                } else {
                    Timber.d("BackupRestoreManager: Successfully signed in anonymously with ID: ${signInResult.user?.uid}")
                }
            } catch (e: Exception) {
                Timber.e(e, "BackupRestoreManager: Error signing in anonymously: ${e.message}")
                return false
            }
        }
        
        Timber.d("BackupRestoreManager: Sending test data to Firestore")
        return firestoreManager.sendTestDataToFirestore()
    }
}
