package com.example.to_do.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.to_do.data.dao.TodoDao
import com.example.to_do.data.entity.*

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        TaskListEntity::class,
        AttachmentEntity::class
    ],
    version = 2,             // bump from 1 to 2
    exportSchema = true
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
