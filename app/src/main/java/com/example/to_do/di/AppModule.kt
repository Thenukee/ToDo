package com.example.to_do.di

// di/AppModule.kt

import android.content.Context
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.to_do.data.local.TaskDao
import com.example.to_do.data.local.TodoDatabase
import com.example.to_do.data.repository.TaskRepository
import com.example.to_do.data.worker.BackupWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            "todo_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: TodoDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun scheduleBackup(@ApplicationContext ctx: Context): WorkManager {
        val wm = WorkManager.getInstance(ctx)
        val req = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork(
            "daily_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
        return wm
    }

}