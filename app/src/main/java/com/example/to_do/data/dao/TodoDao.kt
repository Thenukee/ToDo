package com.example.to_do.data.dao

import androidx.room.*
import com.example.to_do.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    /* ---------- LISTS ---------- */

    @Insert suspend fun insertList(list: TodoListEntity): Long
    @Update suspend fun updateList(list: TodoListEntity)
    @Delete suspend fun deleteList(list: TodoListEntity)

    @Query("SELECT * FROM todo_lists ORDER BY listId DESC")
    fun getAllLists(): Flow<List<TodoListEntity>>

    /* ---------- ITEMS ---------- */

    @Insert suspend fun insertItem(item: ItemEntity): Long
    @Update suspend fun updateItem(item: ItemEntity)
    @Update suspend fun updateItems(items: List<ItemEntity>)
    @Delete suspend fun deleteItem(item: ItemEntity)

    @Query(
        "SELECT * FROM items WHERE parentListId = :listId ORDER BY position ASC"
    )
    fun itemsForList(listId: Long): Flow<List<ItemEntity>>

    /* ---------- SEARCH ---------- */

    @Query(
        "SELECT * FROM items WHERE description LIKE '%' || :q || '%' ORDER BY parentListId, position"
    )
    fun globalSearch(q: String): Flow<List<ItemEntity>>
}
