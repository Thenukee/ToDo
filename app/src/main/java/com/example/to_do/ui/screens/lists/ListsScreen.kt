package com.example.to_do.ui.screens.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.data.entity.TaskListEntity
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListsScreen(
    navController: NavController,
    vm: TaskViewModel = hiltViewModel()
) {
    val lists by vm.allLists.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var selectedList by remember { mutableStateOf<TaskListEntity?>(null) }
    var selectedColor by remember { mutableStateOf(0xFF90CAF9.toInt()) } // Default blue
    var selectedEmoji by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        // No topBar needed - it's handled by MainActivity
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
            // Show list of lists with long press menu instead of swipe
            LazyColumn(
                Modifier
                    .padding(inner)
                    .padding(horizontal = 16.dp)
            ) {
                items(lists, key = { it.id }) { list ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .combinedClickable(
                                onClick = {
                                    navController.navigate("list_tasks/${list.id}")
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedList = list
                                }
                            ),
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
    
    // List action menu dialog
    selectedList?.let { list ->
        var showRenameDialog by remember { mutableStateOf(false) }
        var newName by remember { mutableStateOf(list.name) }
            
        AlertDialog(
            onDismissRequest = { selectedList = null },
            title = { Text("List Options") },
            text = { Text("Choose an action for ${list.name}") },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            vm.deleteList(list.id)
                            selectedList = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                    
                    TextButton(
                        onClick = {
                            showRenameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rename")
                    }
                    
                    TextButton(
                        onClick = { selectedList = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
        
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename List") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("List Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                vm.renameList(list.id, newName.trim())
                                showRenameDialog = false
                                selectedList = null
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    
    // Add list dialog with color and emoji selection
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New List") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text("List Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color preview
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(selectedColor), MaterialTheme.shapes.small)
                                .clickable { showColorPicker = true }
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Emoji selection
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                                .clickable { showEmojiPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedEmoji ?: "🔤",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Color select button
                        TextButton(onClick = { showColorPicker = true }) {
                            Text("Change Color")
                        }
                        
                        // Emoji select button
                        TextButton(onClick = { showEmojiPicker = true }) {
                            Text("Change Emoji")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            vm.createList(
                                name = newListName.trim(),
                                color = selectedColor,
                                emoji = selectedEmoji
                            )
                            showAddDialog = false
                            newListName = ""
                            selectedColor = 0xFF90CAF9.toInt()
                            selectedEmoji = null
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
    
    // Color picker dialog
    if (showColorPicker) {
        val colors = listOf(
            0xFF3F51B5.toInt(), // Indigo
            0xFF2196F3.toInt(), // Blue
            0xFF009688.toInt(), // Teal
            0xFF4CAF50.toInt(), // Green
            0xFFFF9800.toInt(), // Orange
            0xFFFF5252.toInt(), // Red
            0xFF9C27B0.toInt(), // Purple
            0xFF673AB7.toInt()  // Deep Purple
        )
        
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Choose a Color") },
            text = {
                LazyColumn {
                    items(colors.chunked(4)) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(color), shape = MaterialTheme.shapes.small)
                                        .clickable {
                                            selectedColor = color
                                            showColorPicker = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Emoji picker dialog
    if (showEmojiPicker) {
        val emojis = listOf(
            "📝", "📌", "🏠", "🛒", "💼", "🎓", "🎯", "🔖",
            "📚", "💡", "🎬", "🎮", "🏋️", "🍔", "🛫", "💰"
        )
        
        AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            title = { Text("Choose an Emoji") },
            text = {
                LazyColumn {
                    items(emojis.chunked(4)) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .clickable {
                                            selectedEmoji = emoji
                                            showEmojiPicker = false
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
