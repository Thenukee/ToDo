package com.example.to_do.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


// Converts a Room entity to the domain model
fun TaskListEntity.toDomain() = TaskListEntity(
    id    = id,
    name  = name,
    color = color,
    emoji = emoji
)
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),   // default id
    val name: String,
    val color: Int,
    val emoji: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val position: Int =0
)