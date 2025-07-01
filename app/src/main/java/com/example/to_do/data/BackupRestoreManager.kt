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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val preferencesManager: PreferencesManager
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
}
