package com.example.to_do.ui.viewmodel

// ui/viewmodel/TaskViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do.data.model.SubTask
import com.example.to_do.data.model.TaskList
import com.example.to_do.data.model.Attachment
import com.example.to_do.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // States for different task views
    val allTasks = repository.getAllTasks()
    val myDayTasks = repository.getMyDayTasks()
    val importantTasks = repository.getImportantTasks()
    val plannedTasks = repository.getPlannedTasks()

    // Task lists
    val allLists = repository.getAllLists()

    // Task operations
    fun createTask(title: String, listId: String? = null, isImportant: Boolean = false, isInMyDay: Boolean = false) {
        val task = Task(
            title = title,
            listId = listId,
            isImportant = isImportant,
            isInMyDay = isInMyDay
        )
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isCompleted = !task.isCompleted,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleImportant(task: Task) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isImportant = !task.isImportant,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleMyDay(task: Task) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isInMyDay = !task.isInMyDay,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setDueDate(task: Task, dueDate: Long?) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    dueDate = dueDate,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setReminder(task: Task, reminderTime: Long?) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    reminderTime = reminderTime,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // SubTask operations
    fun getSubTasks(taskId: String) = repository.getSubTasksForTask(taskId)

    fun addSubTask(taskId: String, title: String, position: Int) {
        val subTask = SubTask(
            taskId = taskId,
            title = title,
            position = position
        )
        viewModelScope.launch {
            repository.insertSubTask(subTask)
        }
    }

    fun updateSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(subTask)
        }
    }

    fun toggleSubTaskCompletion(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(
                subTask.copy(isCompleted = !subTask.isCompleted)
            )
        }
    }

    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
        }
    }

    // List operations
    fun createList(name: String, color: Int, emoji: String? = null) {
        val taskList = TaskList(
            name = name,
            color = color,
            emoji = emoji
        )
        viewModelScope.launch {
            repository.insertList(taskList)
        }
    }

    fun updateList(taskList: TaskList) {
        viewModelScope.launch {
            repository.updateList(taskList)
        }
    }

    fun deleteList(taskList: TaskList) {
        viewModelScope.launch {
            repository.deleteList(taskList)
        }
    }

    // Attachment operations
    fun getAttachments(taskId: String) = repository.getAttachmentsForTask(taskId)

    fun addAttachment(taskId: String, uri: String, name: String, type: String, size: Long) {
        val attachment = Attachment(
            taskId = taskId,
            uri = uri,
            name = name,
            type = type,
            size = size
        )
        viewModelScope.launch {
            repository.insertAttachment(attachment)
        }
    }

    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
        }
    }
    // Add this method to your TaskViewModel.kt file
// to make the ListTasksScreen compile

    fun getTasksByList(listId: String): Flow<List<Task>> {
        return repository.getTasksByList(listId)
    }

    // Get task with all details
    fun getTaskWithDetails(taskId: String) = repository.getTaskWithDetails(taskId)
}

