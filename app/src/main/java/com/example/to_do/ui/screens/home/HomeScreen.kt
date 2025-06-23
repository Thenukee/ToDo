// ui/screens/home/HomeScreen.kt
package com.example.to_do.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.getValue
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
    val allLists by viewModel.allLists.collectAsState(initial = emptyList())

    /* ---------------- whole page wrapped in drawer ---------------------- */
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Todo App",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()

                // Home
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("All Tasks") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )

                // My Day
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                    label = { Text("My Day") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("my_day")
                        }
                    }
                )

                // Important
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Important") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("important")
                        }
                    }
                )

                // Lists section
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lists",
                        style = MaterialTheme.typography.titleSmall
                    )

                    IconButton(onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("lists")
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add List")
                    }
                }

                // List items
                allLists.forEach { list ->
                    NavigationDrawerItem(
                        icon = {
                            list.emoji?.let { emoji -> Text(emoji) }
                                ?: Icon(Icons.Default.List, contentDescription = null)
                        },
                        label = { Text(list.name) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate("list_tasks/${list.id}")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Settings
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    }
                )
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
