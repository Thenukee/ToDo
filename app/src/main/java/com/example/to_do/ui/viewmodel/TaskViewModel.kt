package com.example.to_do.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do.data.entity.*
import com.example.to_do.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository
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

    fun searchTasks(q: String)            = repo.searchTasks(q)
    fun getTaskWithDetails(id: String)    = repo.getTaskWithDetails(id)
}
