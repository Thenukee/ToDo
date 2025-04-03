package com.example.to_do.data.repository

// data/repository/TaskRepository.kt

import com.example.to_do.data.local.TaskDao
import com.example.to_do.data.local.TaskWithDetails
import com.example.to_do.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getMyDayTasks(): Flow<List<Task>> = taskDao.getMyDayTasks()

    fun getImportantTasks(): Flow<List<Task>> = taskDao.getImportantTasks()

    fun getPlannedTasks(): Flow<List<Task>> = taskDao.getPlannedTasks()

    fun getTasksByList(listId: String): Flow<List<Task>> = taskDao.getTasksByList(listId)

    suspend fun getTaskById(taskId: String): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    fun getSubTasksForTask(taskId: String): Flow<List<SubTask>> = taskDao.getSubTasksForTask(taskId)

    suspend fun insertSubTask(subTask: SubTask) = taskDao.insertSubTask(subTask)

    suspend fun updateSubTask(subTask: SubTask) = taskDao.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTask) = taskDao.deleteSubTask(subTask)

    fun getAllLists(): Flow<List<TaskList>> = taskDao.getAllLists()

    suspend fun insertList(taskList: TaskList) = taskDao.insertList(taskList)

    suspend fun updateList(taskList: TaskList) = taskDao.updateList(taskList)

    suspend fun deleteList(taskList: TaskList) = taskDao.deleteList(taskList)

    fun getAttachmentsForTask(taskId: String): Flow<List<Attachment>> =
        taskDao.getAttachmentsForTask(taskId)

    suspend fun insertAttachment(attachment: Attachment) = taskDao.insertAttachment(attachment)

    suspend fun deleteAttachment(attachment: Attachment) = taskDao.deleteAttachment(attachment)

    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails> =
        taskDao.getTaskWithDetails(taskId)
}
