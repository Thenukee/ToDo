package com.example.to_do.ui.screens.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    navController: NavController,
    vm: TaskViewModel = hiltViewModel()
) {
    val lists by vm.allLists.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "My Lists",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add List")
            }
        }
    ) { inner ->
        if (lists.isEmpty()) {
            // Show empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No lists yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Create a list to organize your tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create List")
                }
            }
        } else {
            // Show list of lists
            LazyColumn(
                Modifier
                    .padding(inner)
                    .padding(horizontal = 16.dp)
            ) {
                items(lists) { list ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Fixed navigation route to match TodoNavHost
                                navController.navigate("list_tasks/${list.id}")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display emoji or color indicator
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(list.color),
                                        shape = MaterialTheme.shapes.small
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                list.emoji?.let { emoji ->
                                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    list.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
    
    // Add list dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            vm.createList(name = newListName.trim(), color = 0xFF90CAF9.toInt())
                            showAddDialog = false
                            newListName = ""
                        }
                    },
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
