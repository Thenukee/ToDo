
package com.example.to_do.ui.screens.important

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

@Composable
fun ImportantScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val importantTasks by viewModel.importantTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Important",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            AddTaskBar(
                onAddTask = { title ->
                    viewModel.createTask(title, isImportant = true)
                },
                placeholder = "Add an important task"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(importantTasks) { task ->
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

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}