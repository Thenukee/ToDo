package com.example.to_do.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.to_do.data.model.TaskList
import java.util.*

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int,
    val emoji: String? = null
)
// Converts a Room entity to the domain model
fun TaskListEntity.toDomain() = TaskList(
    id    = id,
    name  = name,
    color = color,
    emoji = emoji
)