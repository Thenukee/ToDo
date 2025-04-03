package com.example.to_do.data.model

// models/TaskList.kt

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "lists")
data class TaskList(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int,
    val emoji: String? = null,
    val isShared: Boolean = false,
    val ownerId: String? = null
)