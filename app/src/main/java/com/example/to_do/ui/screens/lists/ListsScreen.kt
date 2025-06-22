package com.example.to_do.ui.screens.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel

@Composable
fun ListsScreen(
    navController: NavController,
    vm: TaskViewModel = hiltViewModel()
) {
    val lists by vm.allLists.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Lists",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.createList(name = "New list", color = 0xFF90CAF9.toInt())
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { inner ->
        LazyColumn(Modifier.padding(inner).padding(8.dp)) {
            items(lists) { list ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            navController.navigate("list/${list.id}")
                        }
                ) {
                    ListItem(
                        headlineContent = { Text(list.name) },
                        supportingContent = { Text(list.emoji ?: "") }
                    )
                }
            }
        }
    }
}
