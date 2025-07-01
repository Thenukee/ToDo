package com.example.to_do.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: Int = 0,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firestore
    constructor() : this(
        id = UUID.randomUUID().toString(),
        name = "",
        color = 0,
        position = 0
    )
}
