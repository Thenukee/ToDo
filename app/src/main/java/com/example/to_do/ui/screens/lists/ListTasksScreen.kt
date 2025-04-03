package com.example.to_do.ui.screens.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.HiltAndroidApp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.data.model.TaskList
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel

@Composable
fun ListTasksScreen(
    listId: String,
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val allLists by viewModel.allLists.collectAsState(initial = emptyList())
    val currentList = remember(allLists, listId) {
        allLists.find { it.id == listId } ?: TaskList(id = listId, name = "List", color = 0)
    }

    val listTasks by viewModel.getTasksByList(listId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TodoAppBar(
                title = currentList.name,
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            AddTaskBar(
                onAddTask = { title ->
                    viewModel.createTask(title, listId = listId)
                },
                placeholder = "Add a task to ${currentList.name}"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(listTasks) { task ->
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