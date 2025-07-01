// TodoListWithItems.kt
package com.example.to_do.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TodoListWithItems(
    @Embedded val list: TodoListEntity,
    @Relation(
        parentColumn = "listId",
        entityColumn = "parentListId"
    )
    val items: List<ItemEntity>
)
