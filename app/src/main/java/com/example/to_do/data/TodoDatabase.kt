package com.example.to_do.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.to_do.data.dao.TodoDao
import com.example.to_do.data.entity.*
import com.example.to_do.data.local.TaskDao


@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        TaskListEntity::class,
        AttachmentEntity::class
    ],
    version = 2,              // ← was 1, bump because the entity set changed
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

