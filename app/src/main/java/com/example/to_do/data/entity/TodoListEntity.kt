// TodoListEntity.kt
package com.example.to_do.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey(autoGenerate = true) val listId: Long = 0,
    val title: String
)
