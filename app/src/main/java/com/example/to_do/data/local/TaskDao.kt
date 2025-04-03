package com.example.to_do.data.local

// data/local/TaskDao.kt

import androidx.room.*
import com.example.to_do.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Task operations
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isInMyDay = 1 ORDER BY createdAt DESC")
    fun getMyDayTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isImportant = 1 ORDER BY createdAt DESC")
    fun getImportantTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun getPlannedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY createdAt DESC")
    fun getTasksByList(listId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // SubTask operations
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    fun getSubTasksForTask(taskId: String): Flow<List<SubTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask)

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    // List operations
    @Query("SELECT * FROM lists ORDER BY name ASC")
    fun getAllLists(): Flow<List<TaskList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(taskList: TaskList)

    @Update
    suspend fun updateList(taskList: TaskList)

    @Delete
    suspend fun deleteList(taskList: TaskList)

    // Attachment operations
    @Query("SELECT * FROM attachments WHERE taskId = :taskId")
    fun getAttachmentsForTask(taskId: String): Flow<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: Attachment)

    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    // Task with related data
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails>
}

// Task with all related data
data class TaskWithDetails(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubTask>,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<Attachment>
)

