// app/src/main/java/com/example/to_do/di/DatabaseModule.kt
package com.example.to_do.di

import android.content.Context
import androidx.room.Room
import com.example.to_do.data.TodoDatabase
import com.example.to_do.data.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): TodoDatabase =
        Room.databaseBuilder(ctx, TodoDatabase::class.java, "todo_db")
            .fallbackToDestructiveMigration()   // dev-only wipe on schema change
            .build()

    @Provides
    fun provideTodoDao(db: TodoDatabase): TodoDao = db.todoDao()
}
