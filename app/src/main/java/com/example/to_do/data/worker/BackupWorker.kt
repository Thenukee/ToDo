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
import com.example.to_do.data.TodoDatabase
import com.example.to_do.data.firebase.FirebaseAuthManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val db: TodoDatabase,
    private val authManager: FirebaseAuthManager
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val CHANNEL_ID = "backup_channel"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting backup process...")
            
            // Ensure user is signed in anonymously
            if (!authManager.ensureSignedIn()) {
                Timber.e("Backup failed: Could not sign in anonymously")
                return Result.retry()
            }
            
            // Get user ID
            val uid = authManager.getCurrentUserId()
            if (uid == null) {
                Timber.e("Backup failed: No user ID available even after sign-in")
                return Result.failure()
            }
            
            Timber.d("Authenticated with user ID: $uid")
            
            val fs = Firebase.firestore
            var totalItems = 0

            // Get all data from the database
            val allLists = withContext(Dispatchers.IO) {
                db.taskDao().getAllListsSync()
            }
            
            Timber.d("Found ${allLists.size} lists to backup")
            
            // First backup metadata about the backup itself
            try {
                val backupMetadata = hashMapOf(
                    "lastBackupTime" to System.currentTimeMillis(),
                    "listCount" to allLists.size
                )
                
                fs.collection("users").document(uid)
                    .collection("backup_metadata")
                    .document("latest")
                    .set(backupMetadata)
                    .await()
                
                Timber.d("Backup metadata uploaded")
            } catch (e: Exception) {
                Timber.e(e, "Failed to upload backup metadata")
            }

            // Process each list and its tasks
            allLists.forEach { list ->
                try {
                    // Convert list to a HashMap for Firestore
                    val listData = convertListToMap(list)
                    
                    // Upload list data
                    fs.collection("users").document(uid)
                        .collection("lists").document(list.id)
                        .set(listData)
                        .await()
                    
                    Timber.d("Uploaded list: ${list.name}")
                    
                    // Get all tasks for this list
                    val tasks = withContext(Dispatchers.IO) {
                        db.taskDao().getTasksByListSync(list.id)
                    }
                    
                    // Upload each task
                    tasks.forEach { task ->
                        try {
                            val taskData = convertTaskToMap(task)
                            
                            fs.collection("users").document(uid)
                                .collection("lists").document(list.id)
                                .collection("tasks").document(task.id)
                                .set(taskData)
                                .await()
                            
                            totalItems++
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to upload task: ${task.id}")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to upload list: ${list.id}")
                }
            }
            
            // Show success notification only if items were backed up
            if (totalItems > 0) {
                Timber.d("Backup completed successfully. Backed up $totalItems items.")
                showBackupSuccessNotification(totalItems)
                Result.success()
            } else {
                Timber.w("Backup completed but no items were backed up")
                Result.success() // Even with 0 items, consider it successful (might be first run)
            }
        } catch (e: Exception) {
            Timber.e(e, "Backup failed with exception")
            Result.retry()
        }
    }
    
    // Convert TaskListEntity to a Map that Firestore can store
    private fun convertListToMap(list: TaskListEntity): Map<String, Any?> {
        return hashMapOf(
            "id" to list.id,
            "name" to list.name,
            "color" to list.color,
            "emoji" to list.emoji,
            "createdAt" to list.createdAt,
            "position" to list.position
        )
    }
    
    // Convert TaskEntity to a Map that Firestore can store
    private fun convertTaskToMap(task: TaskEntity): Map<String, Any?> {
        val taskMap = HashMap<String, Any?>()
        taskMap["id"] = task.id
        taskMap["title"] = task.title
        taskMap["description"] = task.description
        taskMap["isCompleted"] = task.isCompleted
        taskMap["isImportant"] = task.isImportant
        taskMap["isInMyDay"] = task.isInMyDay
        taskMap["createdAt"] = task.createdAt
        taskMap["modifiedAt"] = task.modifiedAt
        taskMap["position"] = task.position
        taskMap["listId"] = task.listId
        
        // Handle nullable fields
        task.dueDate?.let { taskMap["dueDate"] = it }
        task.reminderTime?.let { taskMap["reminderTime"] = it }
        
        return taskMap
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
