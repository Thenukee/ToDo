package com.example.to_do.data.repository

import com.example.to_do.data.dao.TodoDao
import com.example.to_do.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val dao: TodoDao
) {
    /* Lists */
    fun allLists(): Flow<List<TodoListEntity>> = dao.getAllLists()
    suspend fun addList(title: String) =
        dao.insertList(TodoListEntity(title = title))

    /* Items inside one list */
    fun items(listId: Long): Flow<List<ItemEntity>> =
        dao.itemsForList(listId)

    suspend fun addItem(listId: Long, desc: String, pos: Int) =
        dao.insertItem(ItemEntity(description = desc, parentListId = listId, position = pos))

    suspend fun reorder(list: List<ItemEntity>) = dao.updateItems(list)

    /* Search */
    fun search(q: String): Flow<List<ItemEntity>> = dao.globalSearch(q)
}
