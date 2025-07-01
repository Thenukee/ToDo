package com.example.to_do.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBar(
    onAddTask: (String) -> Unit,
    placeholder: String = "Add a task",
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        TextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            placeholder = { 
                Text(text = placeholder, style = MaterialTheme.typography.bodyMedium) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            leadingIcon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.padding(start = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (taskTitle.isNotBlank()) {
                    IconButton(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                onAddTask(taskTitle)
                                taskTitle = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Add Task",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (taskTitle.isNotBlank()) {
                    onAddTask(taskTitle)
                    taskTitle = ""
                }
            }),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}