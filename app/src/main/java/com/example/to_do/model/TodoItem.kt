package com.example.to_do.model

/**
 * Simplified model class used for JSON export
 */
data class TodoItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val listId: String = "",
    val dueDate: Long? = null,
    val isImportant: Boolean = false,
    val isInMyDay: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Convert to Map for Firestore storage
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "title" to title,
            "description" to description,
            "isCompleted" to isCompleted,
            "listId" to listId,
            "isImportant" to isImportant,
            "isInMyDay" to isInMyDay,
            "position" to position,
            "createdAt" to createdAt
        )
        
        // Add nullable fields
        dueDate?.let { map["dueDate"] = it }
        
        return map
    }
}
