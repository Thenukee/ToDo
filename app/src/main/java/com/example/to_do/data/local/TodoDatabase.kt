package com.example.to_do.data.local

// data/local/TodoDatabase.kt

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.to_do.data.model.*

@Database(
    entities = [Task::class, SubTask::class, TaskList::class, Attachment::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}