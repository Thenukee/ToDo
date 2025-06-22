package com.example.to_do.data.repository

// data/repository/TaskRepository.kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest


import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import com.example.to_do.data.local.TaskDao
import com.example.to_do.data.local.TaskWithDetails
import com.example.to_do.data.model.*
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getMyDayTasks(): Flow<List<TaskEntity>> = taskDao.getMyDayTasks()

    fun getImportantTasks(): Flow<List<TaskEntity>> = taskDao.getImportantTasks()

    fun getPlannedTasks(): Flow<List<TaskEntity>> = taskDao.getPlannedTasks()

    fun getTasksByList(listId: String): Flow<List<TaskEntity>> = taskDao.getTasksByList(listId)

    suspend fun getTaskById(taskId: String): TaskEntity? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    fun getSubTasksForTask(taskId: String): Flow<List<SubTask>> = taskDao.getSubTasksForTask(taskId)

    suspend fun insertSubTask(subTask: SubTask) = taskDao.insertSubTask(subTask)

    suspend fun updateSubTask(subTask: SubTask) = taskDao.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTask) = taskDao.deleteSubTask(subTask)

    fun getAllLists(): Flow<List<TaskListEntity>> = taskDao.getAllLists()

    suspend fun insertList(taskList: TaskList) = taskDao.insertList(taskList)

    suspend fun updateList(taskList: TaskList) = taskDao.updateList(taskList)

    suspend fun deleteList(taskList: TaskList) = taskDao.deleteList(taskList)

    fun searchTasks(q: String): Flow<List<TaskEntity>> =
        flowOf(q)
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { dao.search("%$it%") }


    fun getAttachmentsForTask(taskId: String): Flow<List<Attachment>> =
        taskDao.getAttachmentsForTask(taskId)

    suspend fun insertAttachment(attachment: Attachment) = taskDao.insertAttachment(attachment)

    suspend fun deleteAttachment(attachment: Attachment) = taskDao.deleteAttachment(attachment)

    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails> =
        taskDao.getTaskWithDetails(taskId)
}
