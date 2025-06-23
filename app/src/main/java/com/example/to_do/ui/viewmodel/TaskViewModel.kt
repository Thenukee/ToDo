package com.example.to_do.ui.viewmodel

// ui/viewmodel/TaskViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.SubTaskEntity
import com.example.to_do.data.entity.AttachmentEntity
import com.example.to_do.data.entity.TaskListEntity
import com.example.to_do.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
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
        val task = TaskEntity(
            title = title,
            listId = listId,
            isImportant = isImportant,
            isInMyDay = isInMyDay
        )
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun getList(id: String) = repository.getList(id)             // Flow<TaskListEntity?>

    fun swapPositions(listId: String, from: Int, to: Int) =
        viewModelScope.launch { repository.swapTaskPositions(listId, from, to) }



    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isCompleted = !task.isCompleted,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleImportant(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isImportant = !task.isImportant,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleMyDay(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isInMyDay = !task.isInMyDay,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setDueDate(task: TaskEntity, dueDate: Long?) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    dueDate = dueDate,
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun searchTasks(q: String) = repository.searchTasks(q)


    fun setReminder(task: TaskEntity, reminderTime: Long?) {
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
        val subTask = SubTaskEntity(
            taskId = taskId,
            title = title,
            position = position
        )
        viewModelScope.launch {
            repository.insertSubTask(subTask)
        }
    }

    fun updateSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch {
            repository.updateSubTask(subTask)
        }
    }

    fun toggleSubTaskCompletion(subTask: SubTaskEntity) {
        viewModelScope.launch {
            repository.updateSubTask(
                subTask.copy(isCompleted = !subTask.isCompleted)
            )
        }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
        }
    }

    // List operations
    fun createList(name: String, color: Int, emoji: String? = null) {
        val taskList = TaskListEntity(

            name  = name,
            color = color,
            emoji = emoji
        )
        viewModelScope.launch { repository.insertList(taskList) }
    }

    fun updateList(taskList: TaskListEntity) {
        viewModelScope.launch {
            repository.updateList(taskList)
        }
    }

    fun deleteList(taskList: TaskListEntity) {
        viewModelScope.launch {
            repository.deleteList(taskList)
        }
    }

    // Attachment operations
    fun getAttachments(taskId: String) = repository.getAttachmentsForTask(taskId)

    fun addAttachment(taskId: String, uri: String, name: String, type: String, size: Long) {
        val attachment = AttachmentEntity(
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

    fun deleteAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
        }
    }
    // Add this method to your TaskViewModel.kt file
// to make the ListTasksScreen compile

    fun getTasksByList(listId: String): Flow<List<TaskEntity>> {
        return repository.getTasksByList(listId)
    }

    // Get task with all details
    fun getTaskWithDetails(taskId: String) = repository.getTaskWithDetails(taskId)
}

