package com.example.to_do.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    vm: TaskViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val results by vm.searchTasks(query).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Search",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                active = true,
                onActiveChange = {}
            ) {}
            LazyColumn {
                items(results) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = {
                            navController.navigate("task_detail/${task.id}")
                        }
                    )
                }
            }
        }
    }
}
