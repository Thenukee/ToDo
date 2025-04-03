package com.example.to_do.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBar(
    onAddTask: (String) -> Unit,
    placeholder: String = "Add a task",
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }

    OutlinedTextField(
        value = taskTitle,
        onValueChange = { taskTitle = it },
        placeholder = { Text(text = placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            IconButton(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onAddTask(taskTitle)
                        taskTitle = ""
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        },
        singleLine = true
    )
}