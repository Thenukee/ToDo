package com.example.to_do.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.firebase.FirestoreManager
import com.example.to_do.datastore.PreferencesManager
import com.example.to_do.data.repository.TaskRepository
import com.example.to_do.model.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val firestoreManager: FirestoreManager,
    private val taskRepository: TaskRepository
) : ViewModel() {

    // expose each preference as a StateFlow the UI can collect
    val darkTheme: StateFlow<Boolean> = prefs.darkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val sortAsc: StateFlow<Boolean> = prefs.sortAsc
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val autoBackup: StateFlow<Boolean> = prefs.autoBackup
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
        
    // State for export operation
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState
        
    // State for restore operation
    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState

    /** Toggle the Material-3 dark theme */
    fun setDarkTheme(on: Boolean) = viewModelScope.launch {
        prefs.setDarkTheme(on)
    }

    /** Change default sort order */
    fun setSortAsc(on: Boolean) = viewModelScope.launch {
        prefs.setSortAsc(on)
    }

    /** Toggle auto backup */
    fun setAutoBackup(on: Boolean) = viewModelScope.launch {
        prefs.setAutoBackup(on)
    }

    /** Send test data to Firestore */
    fun sendTestDataToFirestore() = viewModelScope.launch {
        try {
            val success = firestoreManager.sendTestDataToFirestore()
            if (success) {
                // Record backup time on success
                prefs.setLastBackupTime(System.currentTimeMillis())
                Timber.d("Test data sent to Firestore successfully")
            } else {
                Timber.e("Failed to send test data to Firestore")
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception sending test data to Firestore")
        }
    }
    
    /** Export tasks as JSON to Firestore */
    fun exportTasksAsJsonToFirestore() = viewModelScope.launch {
        try {
            // Get all tasks
            val tasks = taskRepository.getAllTasksSync()
            
            // Convert to TodoItem model
            val todoItems = tasks.map { taskEntity ->
                TodoItem(
                    id = taskEntity.id,
                    title = taskEntity.title,
                    description = taskEntity.description,
                    isCompleted = taskEntity.isCompleted,
                    listId = taskEntity.listId ?: "",
                    dueDate = taskEntity.dueDate,
                    isImportant = taskEntity.isImportant,
                    isInMyDay = taskEntity.isInMyDay,
                    position = taskEntity.position,
                    createdAt = taskEntity.createdAt
                )
            }
            
            // Send to Firestore
            val success = firestoreManager.sendTasksAsJson(todoItems)
            
            if (success) {
                Timber.d("Tasks exported as JSON to Firestore successfully")
            } else {
                Timber.e("Failed to export tasks as JSON to Firestore")
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception exporting tasks as JSON to Firestore: ${e.message}")
        }
    }
    
    /** Export tasks to JSON file */
    fun exportTasksToJson(context: Context, uri: Uri) = viewModelScope.launch {
        _exportState.value = ExportState.InProgress
        try {
            val tasks = taskRepository.getAllTasksSync()
            
            // Fix: Use first() to collect the Flow once
            val allLists = taskRepository.allLists.first()
            val listsMap = allLists.associateBy { listEntity -> listEntity.id }
            
            // Create JSON structure
            val jsonArray = JSONArray()
            
            tasks.forEach { task ->
                val jsonTask = JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("description", task.description)
                    put("isCompleted", task.isCompleted)
                    put("isImportant", task.isImportant)
                    put("isInMyDay", task.isInMyDay)
                    put("createdAt", task.createdAt)
                    put("position", task.position)
                    
                    // Handle nullable fields
                    task.listId?.let { listId ->
                        put("listId", listId)
                        put("listName", listsMap[listId]?.name ?: "Unknown List")
                    }
                    task.dueDate?.let { put("dueDate", it) }
                    task.reminderTime?.let { put("reminderTime", it) }
                }
                jsonArray.put(jsonTask)
            }
            
            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonArray.toString(2).toByteArray())
            }
            
            _exportState.value = ExportState.Success
            Timber.d("Tasks exported to JSON file successfully")
            
        } catch (e: Exception) {
            _exportState.value = ExportState.Error(e.message ?: "Unknown error")
            Timber.e(e, "Error exporting tasks to JSON file: ${e.message}")
        }
    }
    
    /** Get default JSON export filename */
    fun getDefaultJsonExportFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "todo_export_$timestamp.json"
    }
    
    /** Reset export state */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
    
    /**
     * Restore tasks from the latest JSON backup in Firestore
     */
    fun restoreFromJsonFirestore() = viewModelScope.launch {
        _restoreState.value = RestoreState.InProgress
        
        try {
            // Get the latest JSON backup
            val jsonBackup = firestoreManager.getLatestJsonBackup()
            
            if (jsonBackup == null) {
                _restoreState.value = RestoreState.Error("No JSON backup found")
                return@launch
            }
            
            // Convert JSON to TaskEntity objects
            val tasks = convertJsonToTasks(jsonBackup)
            
            if (tasks.isEmpty()) {
                _restoreState.value = RestoreState.Error("Backup contains no valid tasks")
                return@launch
            }
            
            // Insert tasks into the database
            var successCount = 0
            tasks.forEach { taskEntity ->
                try {
                    taskRepository.insertTask(taskEntity)
                    successCount++
                } catch (e: Exception) {
                    Timber.e(e, "Failed to insert task: ${taskEntity.title}")
                }
            }
            
            _restoreState.value = if (successCount > 0) {
                RestoreState.Success("Restored $successCount tasks from JSON backup")
            } else {
                RestoreState.Error("Failed to restore any tasks")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error restoring from JSON: ${e.message}")
            _restoreState.value = RestoreState.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Convert JSON maps to TaskEntity objects
     */
    @Suppress("UNCHECKED_CAST")
    private fun convertJsonToTasks(jsonTasks: List<Map<String, Any?>>): List<TaskEntity> {
        return jsonTasks.mapNotNull { json ->
            try {
                TaskEntity(
                    id = (json["id"] as? String) ?: UUID.randomUUID().toString(),
                    title = (json["title"] as? String) ?: "",
                    description = (json["description"] as? String) ?: "",
                    isCompleted = (json["isCompleted"] as? Boolean) ?: false,
                    isImportant = (json["isImportant"] as? Boolean) ?: false,
                    isInMyDay = (json["isInMyDay"] as? Boolean) ?: false,
                    listId = (json["listId"] as? String),
                    dueDate = json["dueDate"] as? Long,
                    position = (json["position"] as? Number)?.toInt() ?: 0,
                    createdAt = (json["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to convert JSON to TaskEntity: $json")
                null
            }
        }
    }
    
    /**
     * Reset restore state
     */
    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }
}

/** States for the export operation */
sealed class ExportState {
    object Idle : ExportState()
    object InProgress : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}

/** States for the restore operation */
sealed class RestoreState {
    object Idle : RestoreState()
    object InProgress : RestoreState()
    data class Success(val message: String) : RestoreState()
    data class Error(val message: String) : RestoreState()
}
