// ui/screens/home/HomeScreen.kt
package com.example.to_do.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.viewmodel.TaskViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    /* ---------------- list of tasks ------------------------------------- */
    val allTasks = viewModel.allTasks.collectAsState(initial = emptyList())
    val allLists by viewModel.allLists.collectAsState(initial = emptyList())

    Scaffold(
        // TopBar is already provided in MainActivity, no need for redundant one here
        bottomBar = {
            AddTaskBar(
                onAddTask = { title -> viewModel.createTask(title) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(allTasks.value, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { navController.navigate("task_detail/${task.id}") },
                    onCompleteToggle = { viewModel.toggleTaskCompletion(it) },
                    onImportantToggle = { viewModel.toggleImportant(it) },
                    onDelete = { viewModel.deleteTask(it) },
                    onMoveTask = viewModel::moveTaskToList,
                    availableLists = allLists
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
