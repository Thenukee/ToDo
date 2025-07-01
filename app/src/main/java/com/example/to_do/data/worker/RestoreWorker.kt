package com.example.to_do.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.to_do.data.TodoDatabase
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.*

@HiltWorker
class RestoreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val db: TodoDatabase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val uid = Firebase.auth.currentUser?.uid ?: return Result.failure()
            val fs = Firebase.firestore
            
            // Get all lists from Firestore
            val listsSnapshot = fs.collection("users").document(uid)
                .collection("lists")
                .get()
                .await()
            
            // First clear existing data
            withContext(Dispatchers.IO) {
                // Clear existing data before restore
                db.clearAllTables()
            }
            
            // Restore each list and its tasks
            for (listDoc in listsSnapshot.documents) {
                // Convert Firestore document to TaskListEntity
                val taskList = listDoc.toTaskListEntity() ?: continue
                
                // Insert list to local database
                db.taskDao().insertList(taskList)
                
                // Get tasks for this list
                val tasksSnapshot = fs.collection("users").document(uid)
                    .collection("lists").document(taskList.id)
                    .collection("tasks")
                    .get()
                    .await()
                
                // Insert each task
                for (taskDoc in tasksSnapshot.documents) {
                    // Convert Firestore document to TaskEntity
                    val task = taskDoc.toTaskEntity(taskList.id) ?: continue
                    db.taskDao().insertTask(task)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during restore")
            Result.retry()
        }
    }
    
    // Convert Firestore document to TaskListEntity
    private fun DocumentSnapshot.toTaskListEntity(): TaskListEntity? {
        val id = getString("id") ?: UUID.randomUUID().toString()
        val name = getString("name") ?: return null
        val color = getLong("color")?.toInt() ?: 0
        val emoji = getString("emoji")
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        val position = getLong("position")?.toInt() ?: 0
        
        return TaskListEntity(
            id = id,
            name = name,
            color = color,
            emoji = emoji,
            createdAt = createdAt,
            position = position
        )
    }
    
    // Convert Firestore document to TaskEntity
    private fun DocumentSnapshot.toTaskEntity(listId: String): TaskEntity? {
        val id = getString("id") ?: UUID.randomUUID().toString()
        val title = getString("title") ?: return null
        val description = getString("description") ?: ""
        val isCompleted = getBoolean("isCompleted") ?: false
        val isImportant = getBoolean("isImportant") ?: false
        val isInMyDay = getBoolean("isInMyDay") ?: false
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        val modifiedAt = getLong("modifiedAt") ?: createdAt
        val dueDate = getLong("dueDate")
        val reminderTime = getLong("reminderTime")
        val position = getLong("position")?.toInt() ?: 0
        
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            isImportant = isImportant,
            isInMyDay = isInMyDay,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            dueDate = dueDate,
            reminderTime = reminderTime,
            position = position,
            listId = listId
        )
    }
}
