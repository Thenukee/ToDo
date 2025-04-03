package com.example.to_do.ui.screens.taskdetail


// ui/screens/taskdetail/TaskDetailScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.data.model.SubTask
import com.example.to_do.data.model.Task
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val taskWithDetails by viewModel.getTaskWithDetails(taskId).collectAsState(initial = null)
    val task = taskWithDetails?.task
    val subtasks = taskWithDetails?.subtasks ?: emptyList()
    val attachments = taskWithDetails?.attachments ?: emptyList()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var newSubtaskTitle by remember { mutableStateOf("") }

    task?.let {
        LaunchedEffect(it) {
            title = it.title
            description = it.description
        }
    }

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Task Details",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        task?.let { currentTask ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Task title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { newTitle ->
                            title = newTitle
                            viewModel.updateTask(currentTask.copy(title = newTitle))
                        },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Task description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { newDescription ->
                            description = newDescription
                            viewModel.updateTask(currentTask.copy(description = newDescription))
                        },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        minLines = 3
                    )

                    // Task options
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // My Day toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Add to My Day",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                Switch(
                                    checked = currentTask.isInMyDay,
                                    onCheckedChange = {
                                        viewModel.toggleMyDay(currentTask)
                                    }
                                )
                            }

                            // Important toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Mark as important",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                Switch(
                                    checked = currentTask.isImportant,
                                    onCheckedChange = {
                                        viewModel.toggleImportant(currentTask)
                                    }
                                )
                            }

                            // Due date
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Due date",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )

                                currentTask.dueDate?.let { dueDate ->
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    Text(
                                        text = dateFormat.format(Date(dueDate)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    IconButton(onClick = {
                                        viewModel.setDueDate(currentTask, null)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear due date"
                                        )
                                    }
                                } ?: Button(
                                    onClick = {
                                        // TODO: Show date picker
                                        val tomorrow = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        viewModel.setDueDate(currentTask, tomorrow.timeInMillis)
                                    }
                                ) {
                                    Text("Add")
                                }
                            }

                            // Reminder
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Reminder",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )

                                currentTask.reminderTime?.let { reminderTime ->
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    Text(
                                        text = dateFormat.format(Date(reminderTime)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    IconButton(onClick = {
                                        viewModel.setReminder(currentTask, null)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear reminder"
                                        )
                                    }
                                } ?: Button(
                                    onClick = {
                                        // TODO: Show date-time picker
                                        val tomorrow = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                            set(Calendar.HOUR_OF_DAY, 9)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        viewModel.setReminder(currentTask, tomorrow.timeInMillis)
                                    }
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                // Subtasks section
                item {
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Add subtask input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskTitle,
                            onValueChange = { newSubtaskTitle = it },
                            placeholder = { Text("Add a step") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (newSubtaskTitle.isNotBlank()) {
                                    viewModel.addSubTask(
                                        taskId = currentTask.id,
                                        title = newSubtaskTitle,
                                        position = subtasks.size
                                    )
                                    newSubtaskTitle = ""
                                }
                            },
                            enabled = newSubtaskTitle.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add step"
                            )
                        }
                    }
                }

                // Subtasks list
                items(subtasks) { subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = {
                                viewModel.toggleSubTaskCompletion(subtask)
                            }
                        )

                        Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = {
                            viewModel.deleteSubTask(subtask)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete step"
                            )
                        }
                    }
                }

                // Attachments section
                if (attachments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(attachments) { attachment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = attachment.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "${attachment.size / 1024} KB",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(onClick = {
                                viewModel.deleteAttachment(attachment)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete attachment"
                                )
                            }
                        }
                    }
                }

                // Add file button
                item {
                    Button(
                        onClick = {
                            // TODO: Implement file attachment
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add attachment")
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
