package com.example.to_do.data.local

// data/local/TaskDao.kt

import androidx.room.*
import com.example.to_do.data.entity.AttachmentEntity
import com.example.to_do.data.entity.ListWithTasks
import com.example.to_do.data.entity.SubTaskEntity
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Task operations
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isInMyDay = 1 ORDER BY createdAt DESC")
    fun getMyDayTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isImportant = 1 ORDER BY createdAt DESC")
    fun getImportantTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun getPlannedTasks(): Flow<List<TaskEntity>>

   // @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY createdAt DESC")
    //fun getTasksByList(listId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // SubTask operations
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    fun getSubTasksForTask(taskId: String): Flow<List<SubTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Delete
    suspend fun deleteSubTask(subTask: SubTaskEntity)

    // List operations
   // @Query("SELECT * FROM lists ORDER BY name ASC")
    //fun getAllLists(): Flow<List<TaskList>>

  //  @Insert(onConflict = OnConflictStrategy.REPLACE)
   // suspend fun insertList(taskList: TaskList)

    //@Update
    //suspend fun updateList(taskList: TaskList)

   // @Delete
    //suspend fun deleteList(taskList: TaskList)

    @Insert suspend fun insertList(list: TaskListEntity)
    @Update suspend fun updateList(list: TaskListEntity)
    @Delete suspend fun deleteList(list: TaskListEntity)

    // Attachment operations
    @Query("SELECT * FROM attachments WHERE taskId = :taskId")
    fun getAttachmentsForTask(taskId: String): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)




    @Query("SELECT * FROM task_lists ORDER BY position ASC")
    fun getAllLists(): Flow<List<TaskListEntity>>

    @Transaction
    suspend fun swapListPositions(fromIndex: Int, toIndex: Int) {
        val current = getAllListsSync()
        val mutable = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        mutable.forEachIndexed { idx, lst ->
            if (lst.position != idx) {
                updateList(lst.copy(position = idx))
            }
        }
    }

    @Query("SELECT * FROM task_lists")
    suspend fun getAllListsSync(): List<TaskListEntity>

    @Transaction
    @Query("SELECT * FROM task_lists")
    fun getListsWithTasks(): Flow<List<ListWithTasks>>





    @Query("SELECT * FROM task_lists WHERE id = :id LIMIT 1")
    suspend fun getListSync(id: String): TaskListEntity?

    @Query("DELETE FROM task_lists WHERE id = :id")
    suspend fun deleteListById(id: String)


    // ---- Tasks by list ----
    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY position ASC")
    fun getTasksByList(listId: String): Flow<List<TaskEntity>>

    // One-shot version for backup
    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY position ASC")
    suspend fun getTasksByListSync(listId: String): List<TaskEntity>

    // Search
    @Query("SELECT * FROM tasks WHERE title LIKE :query ORDER BY modifiedAt DESC")
    fun search(query: String): Flow<List<TaskEntity>>

    // --- single list by id ---
    @Query("SELECT * FROM task_lists WHERE id = :id LIMIT 1")
    fun getList(id: String): kotlinx.coroutines.flow.Flow<TaskListEntity>

    // --- helper used by BackupWorker (one-shot) ---
    @Query("SELECT * FROM task_lists")
    suspend fun getListsWithTasksSync(): List<ListWithTasks>



    // Task with related data
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails>




    // --- atomic reorder inside a list ---
    @Transaction
    suspend fun swapPositions(listId: String, from: Int, to: Int) {
        val current = getTasksByListSync(listId)          // ordered by position
        val mutable = current.toMutableList().apply {
            add(to, removeAt(from))
        }
        mutable.forEachIndexed { idx, task ->
            updateTask(task.copy(position = idx))
        }
    }
}




// Task with all related data
data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubTaskEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<AttachmentEntity>
)

