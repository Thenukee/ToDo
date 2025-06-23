package com.example.to_do.data.repository

import com.example.to_do.data.entity.*
import com.example.to_do.data.local.TaskDao
import com.example.to_do.data.local.TaskWithDetails
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val dao: TaskDao
) {

    /* ───────────────────────  Lists  ──────────────────────────── */

    /** Stream of all lists ordered by `position` (lazy-collected in UI). */
    val allLists: Flow<List<TaskListEntity>> = dao.getAllLists()

    /** Re-orders two rows in the `task_lists` table (UI drag-and-drop). */
    suspend fun swapListPositions(from: Int, to: Int) =
        dao.swapListPositions(from, to)

    /** Renames a list by id. Requires `getListSync(id)` in your DAO. */
    suspend fun renameList(id: String, newName: String) {
        val current = dao.getListSync(id)      // ‼️ helper in DAO
            ?: return
        dao.updateList(current.copy(name = newName.trim()))
    }

    /** Deletes a list by id (convenience wrapper). */
    suspend fun deleteList(id: String) =
        dao.deleteListById(id)                 // ‼️ helper in DAO

    suspend fun insertList(list: TaskListEntity)  = dao.insertList(list)
    suspend fun updateList(list: TaskListEntity)  = dao.updateList(list)

    /** One-shot fetch used by ListTasksScreen & rename dialog */
    fun getList(id: String): Flow<TaskListEntity?> = dao.getList(id)

    /* ───────────────────────  Tasks  ──────────────────────────── */

    fun getAllTasks()       = dao.getAllTasks()
    fun getMyDayTasks()     = dao.getMyDayTasks()
    fun getImportantTasks() = dao.getImportantTasks()
    fun getPlannedTasks()   = dao.getPlannedTasks()

    fun getTasksByList(listId: String) = dao.getTasksByList(listId)

    suspend fun swapTaskPositions(listId: String, from: Int, to: Int) =
        dao.swapPositions(listId, from, to)

    suspend fun insertTask(task: TaskEntity)  = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity)  = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity)  = dao.deleteTask(task)

    /* ───────────────────────  Sub-tasks  ──────────────────────── */

    fun getSubTasksForTask(taskId: String)        = dao.getSubTasksForTask(taskId)
    suspend fun insertSubTask(sub: SubTaskEntity) = dao.insertSubTask(sub)
    suspend fun updateSubTask(sub: SubTaskEntity) = dao.updateSubTask(sub)
    suspend fun deleteSubTask(sub: SubTaskEntity) = dao.deleteSubTask(sub)

    /* ───────────────────────  Attachments  ────────────────────── */

    fun getAttachmentsForTask(taskId: String)         = dao.getAttachmentsForTask(taskId)
    suspend fun insertAttachment(att: AttachmentEntity) = dao.insertAttachment(att)
    suspend fun deleteAttachment(att: AttachmentEntity) = dao.deleteAttachment(att)

    /* ───────────────────────  Search  ─────────────────────────── */

    fun searchTasks(q: String): Flow<List<TaskEntity>> =
        flowOf(q)
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                dao.search("%${query.trim()}%")
            }

    /* ───────────────────────  Details  ────────────────────────── */

    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails> =
        dao.getTaskWithDetails(taskId)
}
