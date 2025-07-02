package com.example.to_do.data.export

import android.content.Context
import android.net.Uri
import com.example.to_do.data.repository.TaskRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileExportManager @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Export all tasks to a CSV file
     * @param context Android context
     * @param uri The URI where to save the file, typically from Storage Access Framework
     * @return true if export was successful, false otherwise
     */
    suspend fun exportTasksToCsv(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val allTasks = taskRepository.getAllTasksSync()
            val allLists = taskRepository.allLists.first() // Gets the current snapshot of lists
            
            // Create a map of list IDs to list names for quick lookup
            val listIdToName = allLists.associateBy { it.id }
            
            // Get output stream for the URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    // Write CSV header
                    writer.append("ID,Title,Description,List,Completed,Important,InMyDay,DueDate,CreatedAt\n")
                    
                    // Write each task
                    allTasks.forEach { task ->
                        val listName = task.listId?.let { listIdToName[it]?.name } ?: "No List"
                        val dueDate = task.dueDate?.let { formatDate(it) } ?: ""
                        val createdAt = formatDate(task.createdAt)
                        
                        writer.append(escapeCsvField(task.id)).append(",")
                        writer.append(escapeCsvField(task.title)).append(",")
                        writer.append(escapeCsvField(task.description)).append(",")
                        writer.append(escapeCsvField(listName)).append(",")
                        writer.append(if (task.isCompleted) "Yes" else "No").append(",")
                        writer.append(if (task.isImportant) "Yes" else "No").append(",")
                        writer.append(if (task.isInMyDay) "Yes" else "No").append(",")
                        writer.append(dueDate).append(",")
                        writer.append(createdAt).append("\n")
                    }
                }
            }
            
            Timber.d("Successfully exported ${allTasks.size} tasks to CSV")
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error exporting tasks to CSV: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Export all tasks to a JSON file
     * @param context Android context
     * @param uri The URI where to save the file, typically from Storage Access Framework
     * @return true if export was successful, false otherwise
     */
    suspend fun exportTasksToJson(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val allTasks = taskRepository.getAllTasksSync()
            val allLists = taskRepository.allLists.first()
            
            // Create a map of list IDs to list names for quick lookup
            val listIdToName = allLists.associateBy { it.id }
            
            // Transform tasks to JSON-friendly format
            val tasksJson = allTasks.map { task ->
                val listName = task.listId?.let { listIdToName[it]?.name } ?: "No List"
                val dueDate = task.dueDate?.let { formatDate(it) } ?: ""
                val createdAt = formatDate(task.createdAt)
                
                mapOf(
                    "id" to task.id,
                    "title" to task.title,
                    "description" to task.description,
                    "list" to listName,
                    "isCompleted" to task.isCompleted,
                    "isImportant" to task.isImportant,
                    "isInMyDay" to task.isInMyDay,
                    "dueDate" to dueDate,
                    "createdAt" to createdAt
                )
            }
            
            // Convert to JSON string with pretty printing
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(tasksJson)
            
            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.write(jsonString)
                }
            }
            
            Timber.d("Successfully exported ${tasksJson.size} tasks to JSON")
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error exporting tasks to JSON: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Format a timestamp as a date string
     */
    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Escape special characters for CSV format
     */
    private fun escapeCsvField(field: String): String {
        // If the field contains commas, newlines, or quotes, wrap it in quotes
        return if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
    }
    
    /**
     * Get a default export filename based on current date
     */
    fun getDefaultExportFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "todo_export_$timestamp.csv"
    }
    
    /**
     * Get a default export filename for JSON
     */
    fun getDefaultJsonExportFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "todo_export_$timestamp.json"
    }
}
