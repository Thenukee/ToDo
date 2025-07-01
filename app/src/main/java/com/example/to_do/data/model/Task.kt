package com.example.to_do.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val listId: String = "",
    val dueDate: Long? = null,
    val priority: Int = 0,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firestore
    constructor() : this(
        id = UUID.randomUUID().toString(),
        title = "",
        description = "",
        isCompleted = false,
        listId = "",
        dueDate = null,
        priority = 0,
        position = 0
    )
}
