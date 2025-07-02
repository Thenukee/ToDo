package com.example.to_do.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.to_do.R
import com.example.to_do.data.firebase.FirebaseBackupService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val firebaseBackupService: FirebaseBackupService
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val CHANNEL_ID = "backup_channel"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting backup process via FirebaseBackupService...")
            
            val success = firebaseBackupService.performFullBackup()
            
            if (success) {
                // Get count of backed up items
                val itemCount = firebaseBackupService.getBackupItemCount() ?: 0
                
                // Show success notification only if items were backed up
                if (itemCount > 0) {
                    Timber.d("Backup completed successfully. Backed up $itemCount items.")
                    showBackupSuccessNotification(itemCount)
                    Result.success()
                } else {
                    Timber.w("Backup completed but no items were backed up")
                    Result.success() // Even with 0 items, consider it successful (might be first run)
                }
            } else {
                Timber.e("Backup failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Backup failed with exception")
            Result.retry()
        }
    }

            private fun showBackupSuccessNotification(itemCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Backup Notifications"
            val description = "Notifications about ToDo app backups"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup_success) // Using our custom icon
            .setContentTitle("ToDo Backup Complete")
            .setContentText("$itemCount items backed up to the cloud")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
