package com.example.to_do.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.to_do.data.entity.*
import com.example.to_do.data.firebase.FirebaseBackupService
import com.example.to_do.data.firebase.FirestoreManager
import com.example.to_do.data.local.TaskWithDetails
import com.example.to_do.data.repository.TaskRepository
import com.example.to_do.data.worker.BackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val workManager: WorkManager,
    private val firestoreManager: FirestoreManager,
    private val firebaseBackupService: FirebaseBackupService
) : ViewModel() {

    /* ───────────── Streams ───────────── */

    val allTasks       = repo.getAllTasks()
    val myDayTasks     = repo.getMyDayTasks()
    val importantTasks = repo.getImportantTasks()
    val plannedTasks   = repo.getPlannedTasks()

    val allLists: Flow<List<TaskListEntity>> = repo.allLists

    /* ───────────── Lists ───────────── */

    fun createList(
        name: String,
        color: Int,
        emoji: String? = null,
        id: String = UUID.randomUUID().toString()
    ) = viewModelScope.launch {
        repo.insertList(TaskListEntity(id, name.trim(), color, emoji, position = Int.MAX_VALUE))
    }

    fun swapListPositions(from: Int, to: Int) = viewModelScope.launch {
        repo.swapListPositions(from, to)
    }

    fun renameList(id: String, newName: String) = viewModelScope.launch {
        repo.renameList(id, newName)
    }

    fun deleteList(id: String) = viewModelScope.launch {
        repo.deleteList(id)
    }

    fun getList(id: String): Flow<TaskListEntity?> = repo.getList(id)

    fun updateListColor(id: String, newColor: Int) = viewModelScope.launch {
        val list = repo.getListSync(id) ?: return@launch
        repo.updateList(list.copy(color = newColor))
    }

    fun updateListEmoji(id: String, newEmoji: String?) = viewModelScope.launch {
        val list = repo.getListSync(id) ?: return@launch
        repo.updateList(list.copy(emoji = newEmoji))
    }

    fun duplicateList(id: String) = viewModelScope.launch {
        // Get the list to duplicate
        val originalList = repo.getListSync(id) ?: return@launch
        val tasks = repo.getTasksByListSync(id)
        
        // Create a new list with the same properties but a new ID
        val newListId = UUID.randomUUID().toString()
        val newList = originalList.copy(
            id = newListId,
            name = "${originalList.name} (Copy)",
            position = Int.MAX_VALUE // Will be positioned last
        )
        
        // Insert the new list
        repo.insertList(newList)
        
        // Copy all tasks to the new list
        tasks.forEach { task ->
            val newTask = task.copy(
                id = UUID.randomUUID().toString(),
                listId = newListId
            )
            repo.insertTask(newTask)
        }
    }
    
    fun clearCompletedTasks(listId: String) = viewModelScope.launch {
        val tasks = repo.getTasksByListSync(listId)
        tasks.filter { it.isCompleted }.forEach { task ->
            repo.deleteTask(task)
        }
    }

    fun clearCompletedTasksInAllLists() = viewModelScope.launch {
        val allTasks = repo.getAllTasksSync()
        allTasks.filter { it.isCompleted }.forEach { task ->
            repo.deleteTask(task)
        }
    }

    /* ───────────── Tasks ───────────── */

    fun createTask(
        title: String,
        listId: String? = null,
        isImportant: Boolean = false,
        isInMyDay: Boolean = false
    ) = viewModelScope.launch {
        repo.insertTask(
            TaskEntity(
                title       = title.trim(),
                listId      = listId,
                isImportant = isImportant,
                isInMyDay   = isInMyDay
            )
        )
    }

    fun updateTask(task: TaskEntity) = viewModelScope.launch {
        repo.updateTask(task)
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        repo.deleteTask(task)
    }

//    fun swapTaskPositions(listId: String, from: Int, to: Int) = viewModelScope.launch {
//        repo.swapTaskPositions(listId, from, to)
//    }

    fun swapTaskPositions(listId: String, from: Int, to: Int) =
        viewModelScope.launch {
            repo.swapTaskPositions(listId, from, to)
        }


    fun getTasksByList(listId: String) = repo.getTasksByList(listId)

    /* Toggles & helpers */

    fun toggleTaskCompletion(t: TaskEntity) = updateTask(t.copy(isCompleted = !t.isCompleted))
    fun toggleImportant(t: TaskEntity)      = updateTask(t.copy(isImportant = !t.isImportant))
    fun toggleMyDay(t: TaskEntity)          = updateTask(t.copy(isInMyDay   = !t.isInMyDay))

    fun setDueDate(t: TaskEntity, due: Long?)   = updateTask(t.copy(dueDate     = due))
    fun setReminder(t: TaskEntity, at:  Long?)  = updateTask(t.copy(reminderTime = at))

    /* ───────────── Sub-tasks & Attachments ───────────── */

    fun getSubTasks(taskId: String) = repo.getSubTasksForTask(taskId)
    fun addSubTask(taskId: String, title: String, position: Int) = viewModelScope.launch {
        repo.insertSubTask(
            SubTaskEntity(
                taskId   = taskId,          // ✅ goes to the taskId column
                title    = title.trim(),    // ✅ now the title parameter is provided
                position = position         // ✅ any extra optional args keep defaults
            )
        )
    }

    fun toggleSubTaskCompletion(s: SubTaskEntity) =
        viewModelScope.launch { repo.updateSubTask(s.copy(isCompleted = !s.isCompleted)) }
    fun deleteSubTask(s: SubTaskEntity) = viewModelScope.launch { repo.deleteSubTask(s) }

    fun getAttachments(taskId: String) = repo.getAttachmentsForTask(taskId)
    fun addAttachment(
        taskId: String,
        uri: String,
        name: String,
        type: String,
        size: Long
    ) = viewModelScope.launch {
        repo.insertAttachment(
            AttachmentEntity(
                taskId = taskId,   // FK to the task
                uri    = uri,
                name   = name.trim(),
                type   = type,
                size   = size      // now the correct field gets a Long
                // id     = UUID.randomUUID().toString() // add only if your entity needs it
            )
        )
    }

    fun deleteAttachment(a: AttachmentEntity) = viewModelScope.launch { repo.deleteAttachment(a) }

    /* ───────────── Search & Details ───────────── */

    // Standard implementation of search
    fun searchTasks(q: String): Flow<List<TaskEntity>> = repo.searchTasks(q)
    
    fun getTaskWithDetails(id: String): Flow<TaskWithDetails> = repo.getTaskWithDetails(id)

    fun moveTaskToList(task: TaskEntity, targetListId: String) = viewModelScope.launch {
        if (task.listId != targetListId) {
            // Get the target list's tasks to determine the new position
            val tasksInTargetList = repo.getTasksByListSync(targetListId)
            val newPosition = if (tasksInTargetList.isEmpty()) 0 else tasksInTargetList.size
            
            // Update the task with the new list ID and position
            val updatedTask = task.copy(
                listId = targetListId,
                position = newPosition
            )
            repo.updateTask(updatedTask)
        }
    }

    /* ───────────── Backup ───────────── */
    
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState
    
    // Keep track of observers to avoid memory leaks
    private var currentBackupObserver: androidx.lifecycle.Observer<List<androidx.work.WorkInfo>>? = null
    
    fun backupToFirestore() = viewModelScope.launch {
        _backupState.value = BackupState.InProgress
        
        try {
            // Remove any existing observer to avoid leaks
            currentBackupObserver?.let { observer ->
                workManager.getWorkInfosByTagLiveData("backup_task").removeObserver(observer)
                currentBackupObserver = null
            }
            
            // Schedule a one-time backup work with network constraint
            val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .addTag("backup_task") // Add tag for easy reference
                .build()
                
            workManager.enqueue(backupRequest)
            
            // Log that backup has started
            Timber.d("Manual backup started via drawer")
            
            // Create new observer for this backup task
            val workObserver = androidx.lifecycle.Observer<List<androidx.work.WorkInfo>> { workInfoList ->
                if (workInfoList.isNotEmpty()) {
                    val workInfo = workInfoList[0]
                    Timber.d("Backup work state: ${workInfo.state}")
                    
                    when (workInfo.state) {
                        androidx.work.WorkInfo.State.SUCCEEDED -> {
                            _backupState.value = BackupState.Success
                            // Clean up observer
                            removeBackupObserver()
                        }
                        androidx.work.WorkInfo.State.FAILED -> {
                            // Get error details if available
                            val errorMsg = workInfo.outputData.getString("error_message") ?: "Backup failed"
                            _backupState.value = BackupState.Failed(errorMsg)
                            // Clean up observer
                            removeBackupObserver()
                        }
                        androidx.work.WorkInfo.State.RUNNING -> {
                            _backupState.value = BackupState.InProgress
                        }
                        androidx.work.WorkInfo.State.CANCELLED -> {
                            _backupState.value = BackupState.Failed("Backup was cancelled")
                            // Clean up observer
                            removeBackupObserver()
                        }
                        else -> {
                            // Keep as scheduled for other states
                            if (_backupState.value == BackupState.InProgress) {
                                _backupState.value = BackupState.Scheduled
                            }
                        }
                    }
                }
            }
            
            // Store reference to observer
            currentBackupObserver = workObserver
            
            // Start observing
            workManager.getWorkInfosByTagLiveData("backup_task").observeForever(workObserver)
            
            // Initially set to Scheduled state
            _backupState.value = BackupState.Scheduled
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling backup")
            _backupState.value = BackupState.Failed(e.message ?: "Unknown error")
        }
    }
    
    // Helper function to remove backup observer
    private fun removeBackupObserver() {
        currentBackupObserver?.let { observer ->
            workManager.getWorkInfosByTagLiveData("backup_task").removeObserver(observer)
            currentBackupObserver = null
        }
    }

    /**
     * Reset the backup state to Idle
     * Call this when you want to clear any previous backup state
     */
    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }

    /* ───────────── Firebase Test Methods ───────────── */

    // State for the Firebase test operation
    private val _firestoreTestState = MutableStateFlow<FirestoreTestState>(FirestoreTestState.Idle)
    val firestoreTestState: StateFlow<FirestoreTestState> = _firestoreTestState
    
    /**
     * Test Firestore connectivity by sending test data
     * Call this method from UI to test if Firestore is working
     */
    fun testFirestoreConnection() = viewModelScope.launch {
        _firestoreTestState.value = FirestoreTestState.InProgress
        
        Timber.d("TaskViewModel: Testing Firestore connection...")
        
        try {
            // First verify Firebase authentication
            if (!firebaseBackupService.verifyFirebaseConnection()) {
                Timber.e("TaskViewModel: Firebase connection verification failed")
                _firestoreTestState.value = FirestoreTestState.Failed("Firebase connection verification failed")
                return@launch
            }
            
            // Then try to send test data
            val result = firestoreManager.sendTestDataToFirestore()
            
            if (result) {
                Timber.d("TaskViewModel: Firestore test successful")
                _firestoreTestState.value = FirestoreTestState.Success
            } else {
                Timber.e("TaskViewModel: Firestore test failed")
                _firestoreTestState.value = FirestoreTestState.Failed("Failed to send test data to Firestore")
            }
        } catch (e: Exception) {
            Timber.e(e, "TaskViewModel: Error during Firestore test: ${e.message}")
            _firestoreTestState.value = FirestoreTestState.Failed(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Reset the Firestore test state
     */
    fun resetFirestoreTestState() {
        _firestoreTestState.value = FirestoreTestState.Idle
    }
    
    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState
    
    private val _searchResults = MutableStateFlow<List<TaskEntity>>(emptyList())
    val searchResults: StateFlow<List<TaskEntity>> = _searchResults
    
    /**
     * Update search query and trigger search if query is not empty
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            // Clear results if query is blank
            _searchResults.value = emptyList()
            _searchState.value = SearchState.Idle
        } else {
            // Perform search with debounce
            viewModelScope.launch {
                delay(300) // Debounce for better UX
                if (_searchQuery.value == query) { // Make sure this is still the latest query
                    performSearch(query)
                }
            }
        }
    }
    
    /**
     * Perform search and update state
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                _searchState.value = SearchState.Searching
                
                // Similar to how we get tasks in backup functionality - use synchronous approach
                val results = repo.searchTasks(query).first() // Get first emission
                
                _searchResults.value = results
                _searchState.value = if (results.isEmpty()) {
                    SearchState.NoResults
                } else {
                    SearchState.Success(results.size)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during search: ${e.message}")
                _searchState.value = SearchState.Error(e.message ?: "Unknown error")
                _searchResults.value = emptyList()
            }
        }
    }
    
    /**
     * Clear search query and results
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _searchState.value = SearchState.Idle
    }
}

/**
 * State class for Firestore test operations
 */
sealed class FirestoreTestState {
    object Idle : FirestoreTestState()
    object InProgress : FirestoreTestState()
    object Success : FirestoreTestState()
    data class Failed(val message: String) : FirestoreTestState()
}

sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    object Scheduled : BackupState()
    object Success : BackupState()
    data class Failed(val message: String) : BackupState()
}

/**
 * State for search operations
 */
sealed class SearchState {
    object Idle : SearchState()
    object Searching : SearchState()
    object NoResults : SearchState()
    data class Success(val count: Int) : SearchState()
    data class Error(val message: String) : SearchState()
}
