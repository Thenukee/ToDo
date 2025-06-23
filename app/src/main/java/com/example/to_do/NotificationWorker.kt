package com.example.to_do.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.*
import com.example.to_do.MainActivity
import com.example.to_do.R
import com.example.to_do.data.entity.TaskEntity
import java.util.concurrent.TimeUnit


class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Task reminder"

        // Show the notification
        showNotification(taskId, taskTitle)

        return Result.success()
    }

    private fun showNotification(taskId: String, taskTitle: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task reminders"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent for when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }

        // Create a back stack so the user can navigate back to the app's main screen
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                taskId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists in your drawable resources
            .setContentTitle("Reminder: $taskTitle")
            .setContentText("Tap to view task details")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(taskId.hashCode(), notification)
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val CHANNEL_ID = "todo_reminders"
        const val ACTION_OPEN_TASK = "com.example.to_do.OPEN_TASK"
        const val EXTRA_TASK_ID = "task_id"

        /**
         * Schedule a reminder notification for a task
         */
        fun scheduleReminder(
            context: Context,
            task: TaskEntity
        ) {
            // Only schedule if there's a reminder time
            val reminderTime = task.reminderTime ?: return

            val workManager = WorkManager.getInstance(context)

            // Create input data with task info
            val inputData = workDataOf(
                KEY_TASK_ID to task.id,
                KEY_TASK_TITLE to task.title
            )

            // Calculate delay until reminder time
            val currentTime = System.currentTimeMillis()
            val delay = reminderTime - currentTime

            // Only schedule if the reminder is in the future
            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(inputData)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()

                // Use unique work name based on task ID to replace existing reminders
                workManager.enqueueUniqueWork(
                    "reminder_${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }

        /**
         * Cancel a scheduled reminder for a task
         */
        fun cancelReminder(context: Context, taskId: String) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("reminder_${taskId}")
        }
    }
}