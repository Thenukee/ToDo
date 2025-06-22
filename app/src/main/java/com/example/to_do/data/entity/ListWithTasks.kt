package com.example.to_do.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ListWithTasks(
    @Embedded val list: TaskListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId",
        entity = TaskEntity::class
    )
    val tasks: List<TaskEntity>
)
