package com.example.to_do.data.repository

// data/repository/TaskRepository.kt

import com.example.to_do.data.entity.AttachmentEntity
import com.example.to_do.data.entity.SubTaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest





import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskList
import com.example.to_do.data.local.TaskDao
import com.example.to_do.data.local.TaskWithDetails
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao

) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getMyDayTasks(): Flow<List<TaskEntity>> = taskDao.getMyDayTasks()

    fun getImportantTasks(): Flow<List<TaskEntity>> = taskDao.getImportantTasks()

    fun getPlannedTasks(): Flow<List<TaskEntity>> = taskDao.getPlannedTasks()



    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    fun getSubTasksForTask(taskId: String): Flow<List<SubTaskEntity>> = taskDao.getSubTasksForTask(taskId)

    suspend fun insertSubTask(subTask: SubTaskEntity) = taskDao.insertSubTask(subTask)

    suspend fun updateSubTask(subTask: SubTaskEntity) = taskDao.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTaskEntity) = taskDao.deleteSubTask(subTask)

    /* lists ------------------------------------------------------ */
    fun getAllLists() = taskDao.getAllLists()

    fun getList(id: String) = taskDao.getList(id)

    /* tasks ------------------------------------------------------ */
    fun getTasksByList(listId: String) = taskDao.getTasksByList(listId)

    suspend fun swapTaskPositions(listId: String, from: Int, to: Int) =
        taskDao.swapPositions(listId, from, to)


    suspend fun insertList(taskList: TaskList) = taskDao.insertList(taskList)

    suspend fun updateList(taskList: TaskList) = taskDao.updateList(taskList)

    suspend fun deleteList(taskList: TaskList) = taskDao.deleteList(taskList)

    fun searchTasks(q: String): Flow<List<TaskEntity>> =
        flowOf(q)
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { taskDao.search("%$it%") }


    fun getAttachmentsForTask(taskId: String): Flow<List<AttachmentEntity>> =
        taskDao.getAttachmentsForTask(taskId)

    suspend fun insertAttachment(attachment: AttachmentEntity) = taskDao.insertAttachment(attachment)

    suspend fun deleteAttachment(attachment: AttachmentEntity) = taskDao.deleteAttachment(attachment)

    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails> =
        taskDao.getTaskWithDetails(taskId)
}
