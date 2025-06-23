// ui/screens/home/HomeScreen.kt
package com.example.to_do.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    /* ---------------- drawer state & scope ------------------------------ */
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    /* ---------------- list of tasks ------------------------------------- */
    val allTasks = viewModel.allTasks.collectAsState(initial = emptyList())

    /* ---------------- whole page wrapped in drawer ---------------------- */
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    }
                )
                /* Add more items ( Lists, About, etc. ) here */
            }
        }
    ) {
        Scaffold(

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
                items(allTasks.value) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { navController.navigate("task_detail/${task.id}") },
                        onCompleteToggle = { viewModel.toggleTaskCompletion(it) },
                        onImportantToggle = { viewModel.toggleImportant(it) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
