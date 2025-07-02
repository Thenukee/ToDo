package com.example.to_do.data.firebase

import com.example.to_do.data.entity.AttachmentEntity
import com.example.to_do.data.entity.SubTaskEntity
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import com.example.to_do.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to coordinate backup operations between Room and Firestore
 */
@Singleton
class FirebaseBackupService @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager,
    private val taskRepository: TaskRepository
) {
    /**
     * Performs a complete backup of all user data to Firebase Firestore
     * @return true if backup was successful, false otherwise
     */
    suspend fun performFullBackup(): Boolean {
        Timber.d("FirebaseBackupService: Starting performFullBackup process")
        
        try {
            // Verify Firebase connection and user authentication
            if (!verifyFirebaseConnection()) {
                Timber.e("FirebaseBackupService: Cannot backup: Firebase connection verification failed")
                return false
            }

            val userId = firebaseAuthManager.getCurrentUserId()
            if (userId == null) {
                Timber.e("FirebaseBackupService: Cannot backup: User ID is null despite being signed in")
                return false
            }
            
            Timber.d("FirebaseBackupService: Starting backup for user: $userId")

            try {
                // Get all lists
                val allLists = taskRepository.allLists.first()
                
                if (allLists.isEmpty()) {
                    Timber.d("No lists to backup")
                    return true // Successfully backed up nothing
                }
                
                Timber.d("Retrieved ${allLists.size} lists to backup: ${allLists.joinToString { it.name }}")

                // Upload each list and its tasks
                var successCount = 0
                var failCount = 0
                
                for (list in allLists) {
                    // Backup list
                    Timber.d("Attempting to backup list: ${list.id} - ${list.name}")
                    val listResult = firestoreManager.backupList(list)
                    if (!listResult) {
                        Timber.w("Failed to backup list: ${list.id}")
                        failCount++
                        continue
                    } else {
                        successCount++
                    }

                    // Get tasks for this list
                    val tasks = withContext(Dispatchers.IO) {
                        taskRepository.getTasksByListSync(list.id)
                    }
                    
                    Timber.d("Found ${tasks.size} tasks for list ${list.name}")

                    // Backup each task
                    for (task in tasks) {
                        val taskResult = firestoreManager.backupTask(list.id, task)
                        if (!taskResult) {
                            Timber.w("Failed to backup task: ${task.id} - ${task.title}")
                            failCount++
                        } else {
                            successCount++
                        }

                        // Get subtasks for this task
                        val subtasks = withContext(Dispatchers.IO) {
                            taskRepository.getSubTasksForTask(task.id).first()
                        }
                        
                        Timber.d("Found ${subtasks.size} subtasks for task ${task.title}")

                        // Backup each subtask
                        for (subtask in subtasks) {
                            val subtaskResult = firestoreManager.backupSubtask(list.id, task.id, subtask)
                            if (!subtaskResult) {
                                Timber.w("Failed to backup subtask: ${subtask.id} - ${subtask.title}")
                                failCount++
                            } else {
                                successCount++
                            }
                        }
                    }
                }

                Timber.d("FirebaseBackupService: Backup completed with $successCount successful items and $failCount failed items")
                return failCount == 0 || successCount > 0 // Consider successful if at least some items backed up
            } catch (e: Exception) {
                Timber.e(e, "FirebaseBackupService: Error during backup: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                
                // Check for common Firestore errors
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    Timber.e("FirebaseBackupService: Permission denied error. Check Firestore rules.")
                } else if (e.message?.contains("UNAUTHENTICATED") == true) {
                    Timber.e("FirebaseBackupService: Authentication failed. User may need to re-authenticate.")
                } else if (e.message?.contains("UNAVAILABLE") == true) {
                    Timber.e("FirebaseBackupService: Service unavailable. Network connectivity issues.")
                }
                
                return false
            }
        } catch (e: Exception) {
            Timber.e(e, "FirebaseBackupService: Authentication error: ${e.message}")
            return false
        }
    }

    /**
     * Returns the count of items that were backed up in the last backup operation
     * @return Number of backed up items, or null if no backup was performed
     */
    suspend fun getBackupItemCount(): Int? {
        Timber.d("FirebaseBackupService: Getting backup item count")
        
        if (!firebaseAuthManager.isSignedIn()) {
            Timber.w("FirebaseBackupService: Cannot get item count, user not signed in")
            return null
        }

        try {
            var totalCount = 0

            // Get all lists
            val allLists = taskRepository.allLists.first()
            Timber.d("FirebaseBackupService: Found ${allLists.size} lists")
            totalCount += allLists.size

            // Count all tasks and subtasks
            for (list in allLists) {
                // Get tasks for this list
                val tasks = withContext(Dispatchers.IO) {
                    taskRepository.getTasksByListSync(list.id)
                }
                totalCount += tasks.size

                // Count subtasks for each task
                for (task in tasks) {
                    val subtasks = withContext(Dispatchers.IO) {
                        taskRepository.getSubTasksForTask(task.id).first()
                    }
                    totalCount += subtasks.size
                }
            }

            Timber.d("FirebaseBackupService: Total items counted: $totalCount")
            return totalCount
        } catch (e: Exception) {
            Timber.e(e, "FirebaseBackupService: Error getting backup item count: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * Restore data from the latest backup
     * @return true if restore was successful, false otherwise
     */
    suspend fun restoreFromLatestBackup(): Boolean {
        Timber.d("FirebaseBackupService: Starting restore from latest backup")
        
        // Verify Firebase connection and user authentication
        if (!verifyFirebaseConnection()) {
            Timber.e("FirebaseBackupService: Cannot restore: Firebase connection verification failed")
            return false
        }
        
        try {
            // Get the latest backup data from Firestore
            val backupData = firestoreManager.getLatestBackup()
            if (backupData == null) {
                Timber.w("FirebaseBackupService: No backup found to restore")
                return false
            }
            
            // Clear existing data before restore
            clearExistingData()
            
            // Restore lists first
            val userId = firebaseAuthManager.getCurrentUserId() ?: return false
            val listsCollection = withContext(Dispatchers.IO) {
                firestoreManager.getFirestore().collection("users")
                    .document(userId)
                    .collection("lists")
                    .get()
                    .await()
            }
            
            if (listsCollection.isEmpty) {
                Timber.w("FirebaseBackupService: No lists to restore")
                return false
            }
            
            // Restore each list and its tasks
            for (listDoc in listsCollection.documents) {
                val listData = listDoc.data ?: continue
                
                // Create and save list entity
                val list = createListFromMap(listData, listDoc.id)
                taskRepository.insertList(list)
                
                // Restore tasks for this list
                val tasksCollection = withContext(Dispatchers.IO) {
                    firestoreManager.getFirestore().collection("users")
                        .document(userId)
                        .collection("lists")
                        .document(listDoc.id)
                        .collection("tasks")
                        .get()
                        .await()
                }
                
                // Restore each task
                for (taskDoc in tasksCollection.documents) {
                    val taskData = taskDoc.data ?: continue
                    
                    // Create and save task entity
                    val task = createTaskFromMap(taskData, taskDoc.id, listDoc.id)
                    taskRepository.insertTask(task)
                    
                    // Restore subtasks for this task
                    val subtasksCollection = withContext(Dispatchers.IO) {
                        firestoreManager.getFirestore().collection("users")
                            .document(userId)
                            .collection("lists")
                            .document(listDoc.id)
                            .collection("tasks")
                            .document(taskDoc.id)
                            .collection("subtasks")
                            .get()
                            .await()
                    }
                    
                    // Restore each subtask
                    for (subtaskDoc in subtasksCollection.documents) {
                        val subtaskData = subtaskDoc.data ?: continue
                        
                        // Create and save subtask entity
                        val subtask = createSubtaskFromMap(subtaskData, subtaskDoc.id, taskDoc.id)
                        taskRepository.insertSubTask(subtask)
                    }
                }
            }
            
            Timber.d("FirebaseBackupService: Restore completed successfully")
            return true
        } catch (e: Exception) {
            Timber.e(e, "FirebaseBackupService: Error during restore: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Clear all existing data before restore
     */
    private suspend fun clearExistingData() {
        try {
            // Get all existing lists
            val existingLists = taskRepository.allLists.first()
            
            // Delete each list (cascade delete should handle tasks and subtasks)
            for (list in existingLists) {
                taskRepository.deleteList(list.id)
            }
            
            Timber.d("FirebaseBackupService: Cleared existing data before restore")
        } catch (e: Exception) {
            Timber.e(e, "FirebaseBackupService: Error clearing existing data: ${e.message}")
            throw e  // Re-throw to handle in the calling method
        }
    }
    
    /**
     * Create a TaskListEntity from a Firestore map
     */
    private fun createListFromMap(data: Map<String, Any>, id: String): TaskListEntity {
        return TaskListEntity(
            id = id,
            name = data["name"] as? String ?: "Unnamed List",
            color = data["color"] as? Int ?: 0,
            emoji = data["emoji"] as? String ?: "",
            position = (data["position"] as? Long)?.toInt() ?: 0,
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
    
    /**
     * Create a TaskEntity from a Firestore map
     */
    private fun createTaskFromMap(data: Map<String, Any>, id: String, listId: String): TaskEntity {
        return TaskEntity(
            id = id,
            title = data["title"] as? String ?: "Unnamed Task",
            description = data["description"] as? String ?: "",
            isCompleted = data["isCompleted"] as? Boolean ?: false,
            isImportant = data["isImportant"] as? Boolean ?: false,
            isInMyDay = data["isInMyDay"] as? Boolean ?: false,
            dueDate = data["dueDate"] as? Long,
            reminderTime = data["reminderTime"] as? Long,
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
            modifiedAt = data["modifiedAt"] as? Long ?: System.currentTimeMillis(),
            position = (data["position"] as? Long)?.toInt() ?: 0,
            listId = listId
        )
    }
    
    /**
     * Create a SubTaskEntity from a Firestore map
     */
    private fun createSubtaskFromMap(data: Map<String, Any>, id: String, taskId: String): SubTaskEntity {
        return SubTaskEntity(
            id = id,
            taskId = taskId,
            title = data["title"] as? String ?: "Unnamed Subtask",
            isCompleted = data["isCompleted"] as? Boolean ?: false,
            position = (data["position"] as? Long)?.toInt() ?: 0
        )
    }

    /**
     * Try to establish Firebase connectivity and ensure user is authenticated
     * @return true if connection was successful, false otherwise
     */
    suspend fun verifyFirebaseConnection(): Boolean {
        try {
            Timber.d("FirebaseBackupService: Verifying Firebase connection...")

            // Ensure user is signed in
            if (!firebaseAuthManager.isSignedIn()) {
                Timber.e("FirebaseBackupService: User is not signed in")

                // Try to sign in anonymously
                if (!firebaseAuthManager.ensureSignedIn()) {
                    Timber.e("FirebaseBackupService: Failed to sign in anonymously")
                    return false
                } else {
                    Timber.d("FirebaseBackupService: Successfully signed in anonymously with ID: ${firebaseAuthManager.getCurrentUserId()}")
                }
            } else {
                Timber.d("FirebaseBackupService: User is already signed in: ${firebaseAuthManager.getCurrentUserId()}")
            }

            // Test Firestore connectivity
            val connectivityResult = firestoreManager.verifyFirestoreConnectivity()
            Timber.d("FirebaseBackupService: Firestore connectivity test result: $connectivityResult")
            
            if (!connectivityResult) {
                Timber.e("FirebaseBackupService: Failed to connect to Firestore. Please check internet connection and Firebase project configuration.")
            }
            
            return connectivityResult
        } catch (e: Exception) {
            Timber.e(e, "FirebaseBackupService: Error verifying Firebase connection: ${e.message}")
            return false
        }
    }}

    