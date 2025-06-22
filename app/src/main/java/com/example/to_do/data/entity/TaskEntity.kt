package com.example.to_do.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val isInMyDay: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = createdAt,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val position: Int = 0,
    val listId: String? = null            // NEW: nullable → “Tasks” root group
)
