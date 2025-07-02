package com.example.to_do.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.to_do.data.firebase.FirebaseBackupService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class RestoreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val firebaseBackupService: FirebaseBackupService
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting restore process via FirebaseBackupService...")
            
            val success = firebaseBackupService.restoreFromLatestBackup()
            
            if (success) {
                Timber.d("Restore completed successfully")
                Result.success()
            } else {
                Timber.e("Restore failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Restore failed with exception: ${e.message}")
            Result.retry()
        }
    }
}
