package com.example.to_do.data.firebase

import com.example.to_do.data.entity.SubTaskEntity
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for Firebase Firestore operations including backup functionality
 */
@Singleton
class FirestoreManager @Inject constructor(
    private val authManager: FirebaseAuthManager
) {
    private val firestoreInstance by lazy { 
        val db = FirebaseFirestore.getInstance()
        // Enable Firestore debug logging and persistence
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        
        Timber.d("Firestore instance initialized with persistence enabled")
        db
    }
    
    /**
     * Expose Firestore instance for use in restore functionality
     */
    fun getFirestore(): FirebaseFirestore {
        return firestoreInstance
    }
    
    /**
     * Backs up a collection of todo items for the current user
     * @param todoItems List of todo items to backup
     * @return success status of the operation
     */
    suspend fun backupTodoItems(todoItems: List<Map<String, Any>>): Boolean {
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot backup todoItems - user ID is null")
            return false
        }
        
        return try {
            // Create a backup document with timestamp
            val backupData = hashMapOf(
                "timestamp" to System.currentTimeMillis(),
                "items" to todoItems
            )
            
            Timber.d("FirestoreManager: Backing up ${todoItems.size} todo items for user $userId")
            
            // Store in users/{userId}/backups/{auto-id}
            firestoreInstance.collection("users")
                .document(userId)
                .collection("backups")
                .document()
                .set(backupData)
                .await()
                
            Timber.d("FirestoreManager: Backup successful for user $userId with ${todoItems.size} items")
            true
        } catch (e: Exception) {
            Timber.e(e, "FirestoreManager: Backup failed: ${e.javaClass.simpleName}: ${e.message}")
            
            // Check for common Firestore errors
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules.")
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate.")
                e.message?.contains("UNAVAILABLE") == true -> 
                    Timber.e("FirestoreManager: Service unavailable. Check internet connection.")
            }
            
            false
        }
    }
    
    /**
     * Retrieves the latest backup for the current user
     * @return The backup data or null if no backup exists or on error
     */
    suspend fun getLatestBackup(): Map<String, Any>? {
        val userId = authManager.getCurrentUserId() ?: return null
        
        return try {
            val snapshot = firestoreInstance.collection("users")
                .document(userId)
                .collection("backups")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                
            if (snapshot.isEmpty) {
                Timber.d("No backup found for user $userId")
                null
            } else {
                val backup = snapshot.documents[0].data
                Timber.d("Retrieved latest backup for user $userId")
                backup
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve backup: ${e.message}")
            null
        }
    }
    
    /**
     * Lists all available backups for the current user
     * @return List of backup metadata or empty list if none exist or on error
     */
    suspend fun listBackups(): List<Map<String, Any>> {
        val userId = authManager.getCurrentUserId() ?: return emptyList()
        
        return try {
            val snapshot = firestoreInstance.collection("users")
                .document(userId)
                .collection("backups")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                
            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    mapOf(
                        "id" to doc.id,
                        "timestamp" to (data["timestamp"] as? Long ?: 0),
                        "itemCount" to ((data["items"] as? List<*>)?.size ?: 0)
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to list backups: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Backs up a list to Firestore
     * @param list The list to back up
     * @return Success status of the operation
     */
    suspend fun backupList(list: TaskListEntity): Boolean {
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot backup list - user ID is null")
            return false
        }
        
        return try {
            val listData = convertListToMap(list)
            
            Timber.d("FirestoreManager: Backing up list ${list.id} to Firestore for user $userId")
            
            // Log the data being written
            Timber.d("FirestoreManager: Writing data: $listData")
            
            // Write to Firestore with merge option to avoid overwriting existing data
            firestoreInstance.collection("users")
                .document(userId)
                .collection("lists")
                .document(list.id)
                .set(listData, SetOptions.merge())
                .await()
                
            Timber.d("FirestoreManager: List backup successful: ${list.name}")
            true
        } catch (e: Exception) {
            Timber.e(e, "FirestoreManager: List backup failed for list ${list.id}: ${e.javaClass.simpleName}: ${e.message}")
            
            // Check for common Firestore errors
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules.")
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate.")
                e.message?.contains("NOT_FOUND") == true -> 
                    Timber.e("FirestoreManager: Document path or collection not found.")
            }
            
            false
        }
    }
    
    /**
     * Backs up a task to Firestore
     * @param listId The ID of the parent list
     * @param task The task to back up
     * @return Success status of the operation
     */
    suspend fun backupTask(listId: String, task: TaskEntity): Boolean {
        val userId = authManager.getCurrentUserId() 
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot backup task - user ID is null")
            return false
        }
        
        return try {
            val taskData = convertTaskToMap(task)
            
            Timber.d("FirestoreManager: Backing up task ${task.id} to Firestore for list $listId")
            Timber.d("FirestoreManager: Task data: $taskData")
            
            firestoreInstance.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(task.id)
                .set(taskData, SetOptions.merge())
                .await()
                
            Timber.d("Task backup successful: ${task.title}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Task backup failed for task ${task.id}: ${e.javaClass.simpleName}: ${e.message}")
            
            // Check for common Firestore errors
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules.")
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate.")
                e.message?.contains("NOT_FOUND") == true -> 
                    Timber.e("FirestoreManager: Document path or collection not found.")
                e.message?.contains("INVALID_ARGUMENT") == true ->
                    Timber.e("FirestoreManager: Invalid argument. Check if data is valid.")
            }
            
            false
        }
    }
    
    /**
     * Backs up a subtask to Firestore
     * @param listId The ID of the parent list
     * @param taskId The ID of the parent task
     * @param subtask The subtask to back up
     * @return Success status of the operation
     */
    suspend fun backupSubtask(listId: String, taskId: String, subtask: SubTaskEntity): Boolean {
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot backup subtask - user ID is null")
            return false
        }
        
        return try {
            val subtaskData = hashMapOf(
                "id" to subtask.id,
                "title" to subtask.title,
                "isCompleted" to subtask.isCompleted,
                "position" to subtask.position
            )
            
            Timber.d("FirestoreManager: Backing up subtask ${subtask.id} to Firestore for task $taskId")
            Timber.d("FirestoreManager: Subtask data: $subtaskData")
            
            firestoreInstance.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(taskId)
                .collection("subtasks")
                .document(subtask.id)
                .set(subtaskData, SetOptions.merge())
                .await()
            
            Timber.d("Subtask backup successful: ${subtask.title}")    
            true
        } catch (e: Exception) {
            Timber.e(e, "Subtask backup failed for subtask ${subtask.id}: ${e.javaClass.simpleName}: ${e.message}")
            
            // Check for common Firestore errors
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules.")
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate.")
                e.message?.contains("NOT_FOUND") == true -> 
                    Timber.e("FirestoreManager: Document path or collection not found.")
            }
            
            false
        }
    }
    
    /**
     * Verify Firestore connectivity and database rules
     * @return true if connection was successful, false otherwise
     */
    suspend fun verifyFirestoreConnectivity(): Boolean {
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot verify connectivity - user ID is null")
            return false
        }
        
        return try {
            // Try to write a test document
            val testData = hashMapOf(
                "timestamp" to System.currentTimeMillis(),
                "connectionTest" to true
            )
            
            Timber.d("FirestoreManager: Testing Firestore connectivity for user $userId")
            Timber.d("FirestoreManager: Attempting to write test data to connectivity_tests collection")
            
            // Write to a test collection
            firestoreInstance.collection("users")
                .document(userId)
                .collection("connectivity_tests")
                .document("latest_test")
                .set(testData)
                .await()
                
            Timber.d("FirestoreManager: Firestore connectivity test successful - document written to users/$userId/connectivity_tests/latest_test")
            true
        } catch (e: Exception) {
            Timber.e(e, "FirestoreManager: Firestore connectivity test failed: ${e.javaClass.simpleName}: ${e.message}")
            
            // Log detailed error information
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules. " +
                        "Make sure rules allow write to users/$userId/connectivity_tests")
                }
                e.message?.contains("UNAUTHENTICATED") == true -> {
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate. " +
                        "User ID: $userId, isAuthStateValid: ${authManager.isSignedIn()}")
                }
                e.message?.contains("UNAVAILABLE") == true -> {
                    Timber.e("FirestoreManager: Service unavailable. Check internet connection and Firebase project configuration.")
                }
                e.message?.contains("FAILED_PRECONDITION") == true -> {
                    Timber.e("FirestoreManager: Failed precondition. This usually means the app is offline and you're trying to perform a write operation.")
                }
            }
            
            false
        }
    }
    
    /**
     * Sends test data to Firestore to verify if backup functionality is working
     * This method can be called directly from UI for testing purposes
     * @return true if test data was successfully sent, false otherwise
     */
    suspend fun sendTestDataToFirestore(): Boolean {
        Timber.d("FirestoreManager: Starting test data send to Firestore")
        
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot send test data - user ID is null")
            
            // Try to sign in anonymously
            if (!authManager.ensureSignedIn()) {
                Timber.e("FirestoreManager: Failed to sign in anonymously")
                return false
            }
            
            // Get the user ID again after sign-in attempt
            val newUserId = authManager.getCurrentUserId()
            if (newUserId == null) {
                Timber.e("FirestoreManager: Still cannot get user ID after sign-in attempt")
                return false
            }
            
            Timber.d("FirestoreManager: Successfully signed in with ID: $newUserId")
        } else {
            Timber.d("FirestoreManager: User already signed in with ID: $userId")
        }
        
        // Create test data
        val testTimestamp = System.currentTimeMillis()
        val testData = hashMapOf(
            "timestamp" to testTimestamp,
            "testId" to "test-${testTimestamp}",
            "message" to "This is a test backup from the ToDo app",
            "appVersion" to "1.0",
            "testItems" to listOf(
                hashMapOf(
                    "id" to "test-item-1",
                    "title" to "Test Task 1",
                    "isCompleted" to false
                ),
                hashMapOf(
                    "id" to "test-item-2",
                    "title" to "Test Task 2",
                    "isCompleted" to true
                )
            )
        )
        
        return try {
            // Ensure we have the current user ID
            val currentUserId = authManager.getCurrentUserId() ?: return false
            
            Timber.d("FirestoreManager: Sending test data to Firestore for user $currentUserId")
            
            // Write to a test collection
            firestoreInstance.collection("users")
                .document(currentUserId)
                .collection("test_backups")
                .document("latest_test")
                .set(testData)
                .await()
                
            Timber.d("FirestoreManager: Test data successfully sent to Firestore at users/$currentUserId/test_backups/latest_test")
            Timber.d("FirestoreManager: Test data: $testData")
            true
        } catch (e: Exception) {
            Timber.e(e, "FirestoreManager: Failed to send test data: ${e.javaClass.simpleName}: ${e.message}")
            
            // Log detailed error information
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    Timber.e("FirestoreManager: Permission denied. Check Firestore rules. " +
                        "Make sure rules allow write to users/{userId}/test_backups")
                }
                e.message?.contains("UNAUTHENTICATED") == true -> {
                    Timber.e("FirestoreManager: Authentication issue. User may need to re-authenticate.")
                }
                e.message?.contains("UNAVAILABLE") == true -> {
                    Timber.e("FirestoreManager: Service unavailable. Check internet connection and Firebase project configuration.")
                }
                e.message?.contains("FAILED_PRECONDITION") == true -> {
                    Timber.e("FirestoreManager: Failed precondition. This usually means the app is offline.")
                }
            }
            
            false
        }
    }
    
    /**
     * Send tasks as JSON to Firestore
     * @param tasks List of task items to send
     * @return true if successfully sent, false otherwise
     */
    suspend fun sendTasksAsJson(tasks: List<com.example.to_do.model.TodoItem>): Boolean {
        val userId = authManager.getCurrentUserId()
        if (userId == null) {
            Timber.e("FirestoreManager: Cannot send JSON - user ID is null")
            return false
        }
        
        return try {
            // Convert tasks to Map objects
            val tasksAsMap = tasks.map { it.toMap() }
            
            // Create a document with timestamp and tasks array
            val jsonData = hashMapOf(
                "timestamp" to System.currentTimeMillis(),
                "format" to "json",
                "tasks" to tasksAsMap
            )
            
            Timber.d("FirestoreManager: Sending ${tasks.size} tasks as JSON to Firestore")
            
            // Store in users/{userId}/json_backups/{auto-id}
            firestoreInstance.collection("users")
                .document(userId)
                .collection("json_backups")
                .document()
                .set(jsonData)
                .await()
                
            Timber.d("FirestoreManager: JSON backup successful with ${tasks.size} tasks")
            true
        } catch (e: Exception) {
            Timber.e(e, "FirestoreManager: JSON backup failed: ${e.message}")
            false
        }
    }
    
    /**
     * Retrieves the latest JSON backup for the current user
     * @return The backup data as a list of task maps or null if no backup exists
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getLatestJsonBackup(): List<Map<String, Any?>>? {
        val userId = authManager.getCurrentUserId() ?: return null
        
        return try {
            val snapshot = firestoreInstance.collection("users")
                .document(userId)
                .collection("json_backups")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                
            if (snapshot.isEmpty) {
                Timber.d("No JSON backup found for user $userId")
                null
            } else {
                val backup = snapshot.documents[0].data
                val tasks = backup?.get("tasks") as? List<Map<String, Any?>>
                
                if (tasks.isNullOrEmpty()) {
                    Timber.d("JSON backup found but no tasks in it")
                    null
                } else {
                    Timber.d("Retrieved latest JSON backup with ${tasks.size} tasks")
                    tasks
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve JSON backup: ${e.message}")
            null
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
        val taskMap = hashMapOf<String, Any?>(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to task.isCompleted,
            "isImportant" to task.isImportant,
            "isInMyDay" to task.isInMyDay,
            "createdAt" to task.createdAt,
            "modifiedAt" to task.modifiedAt,
            "position" to task.position,
            "listId" to task.listId
        )
        
        // Add nullable fields
        task.dueDate?.let { taskMap["dueDate"] = it }
        task.reminderTime?.let { taskMap["reminderTime"] = it }
        
        return taskMap
    }
}
