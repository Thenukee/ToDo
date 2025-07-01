// ItemEntity.kt  (REPLACE)
package com.example.to_do.data.entity

import androidx.room.*

@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(
        entity = TodoListEntity::class,
        parentColumns = ["listId"],
        childColumns = ["parentListId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("parentListId")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val description: String,
    val position: Int,
    val parentListId: Long
)
