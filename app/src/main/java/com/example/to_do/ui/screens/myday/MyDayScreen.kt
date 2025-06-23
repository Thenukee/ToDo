package com.example.to_do.ui.screens.myday

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
fun MyDayScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val myDayTasks by viewModel.myDayTasks.collectAsState(initial = emptyList())
    val allLists by viewModel.allLists.collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val todayDate = dateFormat.format(Date())

    Scaffold(
        bottomBar = {
            AddTaskBar(
                onAddTask = { title ->
                    viewModel.createTask(title, isInMyDay = true)
                },
                placeholder = "Add a task to My Day"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = todayDate,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(myDayTasks, key = { it.id }) { task ->
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
                        },
                        onDelete = { viewModel.deleteTask(it) },
                        onMoveTask = viewModel::moveTaskToList,
                        availableLists = allLists
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}