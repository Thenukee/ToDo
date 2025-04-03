package com.example.to_do.ui.screens.planned

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlannedScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val plannedTasks by viewModel.plannedTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Planned",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            AddTaskBar(
                onAddTask = { title ->
                    // Create task with tomorrow's date as due date
                    val tomorrow = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    viewModel.createTask(title)
                    // Note: In a real implementation, we would need to get the task ID
                    // and then set the due date, but this is simplified for now
                },
                placeholder = "Add a planned task"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Group tasks by due date
            val tasksByDate = plannedTasks.groupBy { task ->
                task.dueDate?.let { dueDate ->
                    val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                } ?: "No Date"
            }.toSortedMap()

            tasksByDate.forEach { (dateStr, tasksForDate) ->
                item {
                    // Format the date for display
                    val displayDate = if (dateStr == "No Date") {
                        "No Date"
                    } else {
                        try {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(date)
                        } catch (e: Exception) {
                            dateStr
                        }
                    }

                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(tasksForDate) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { selectedTask ->
                            navController.navigate("task_detail/${selectedTask.id}")
                        },
                        onCompleteToggle = { selectedTask ->
                            viewModel.toggleTaskCompletion(selectedTask)
                        },
                        onImportantToggle = { selectedTask ->
                            viewModel.toggleImportant(selectedTask)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}