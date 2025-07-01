package com.example.to_do.ui.screens.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.viewmodel.TaskViewModel
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListTasksScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    vm: TaskViewModel = hiltViewModel()
) {
    // ────────────────────────────────────────────────────────────────
    // 1.  Read the listId that was passed in the Nav route
    // ────────────────────────────────────────────────────────────────
    val listId = backStackEntry.arguments?.getString("listId")!!
    val list by vm.getList(listId).collectAsState(initial = null)
    val tasks by vm.getTasksByList(listId).collectAsState(initial = emptyList())
    val allLists by vm.allLists.collectAsState(initial = emptyList())
    val haptic = LocalHapticFeedback.current
    
    // List edit state management
    var showEditDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    // ────────────────────────────────────────────────────────────────
    // 2.  Reorder-state (new API)
    // ────────────────────────────────────────────────────────────────
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            vm.swapTaskPositions(listId, from.index, to.index)   // <— updated name
        },
        onDragEnd = { _, _ ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    Scaffold(
        // Add topBar with list actions
        topBar = {
            SmallTopAppBar(
                title = { Text(list?.name ?: "Tasks") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename list") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                list?.let {
                                    newListName = it.name
                                    showEditDialog = true
                                }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change color") },
                            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                            onClick = {
                                showColorPicker = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change emoji") },
                            leadingIcon = { Icon(Icons.Default.EmojiEmotions, contentDescription = null) },
                            onClick = {
                                showEmojiPicker = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate list") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                vm.duplicateList(listId)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear completed tasks") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                            onClick = {
                                vm.clearCompletedTasks(listId)
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            AddTaskBar(
                onAddTask = { title -> vm.createTask(title, listId = listId) },
                placeholder = "Add a task to ${(list?.name ?: "this list")}"
            )
        }
    ) { inner ->
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .reorderable(reorderState)                 // drag-logic
                .detectReorderAfterLongPress(reorderState) // long-press starts drag
        ) {
            // NEW – wrap every row in ReorderableItem -----------------------------
            items(tasks, key = { it.id }) { task ->
                ReorderableItem(reorderState, key = task.id) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 8.dp else 0.dp
                    )
                    
                    val background = if (isDragging) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else 
                        Color.Transparent
                    
                    Box(
                        modifier = Modifier
                            .shadow(elevation)
                            .background(background)
                    ) {
                        TaskItem(
                            task = task,
                            modifier = Modifier.animateItemPlacement(),
                            onTaskClick = { navController.navigate("task_detail/${task.id}") },
                            onCompleteToggle = vm::toggleTaskCompletion,
                            onImportantToggle = vm::toggleImportant,
                            onDelete = vm::deleteTask,
                            onMoveTask = vm::moveTaskToList,
                            availableLists = allLists.filter { it.id != listId }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    
    // Rename list dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Rename List") },
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
                            vm.renameList(listId, newListName.trim())
                            showEditDialog = false
                        }
                    },
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
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
                                            vm.updateListColor(listId, color)
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
                                            vm.updateListEmoji(listId, emoji)
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