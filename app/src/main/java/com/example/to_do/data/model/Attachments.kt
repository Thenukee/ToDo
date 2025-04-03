package com.example.to_do.data.model

// models/Attachment.kt

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Attachment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val uri: String,
    val name: String,
    val type: String,
    val size: Long,
    val createdAt: Long = System.currentTimeMillis()
)