// app/src/main/java/com/example/to_do/ui/components/DrawerContent.kt
package com.example.to_do.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import android.widget.Toast
import com.example.to_do.data.worker.BackupWorker
import com.example.to_do.data.entity.TaskListEntity
import com.example.to_do.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

import java.util.UUID

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun DrawerContent(
    navController: NavController,
    closeDrawer: suspend () -> Unit,
    vm: TaskViewModel = hiltViewModel(),
    filteredLists: List<TaskListEntity> = emptyList(),
    searchQuery: String = ""
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val workMgr = WorkManager.getInstance(context)
    val lists by vm.allLists.collectAsState(initial = emptyList())
    val haptic = LocalHapticFeedback.current

    // Use filtered lists if provided, otherwise use all lists
    val displayLists = if (filteredLists.isNotEmpty() || searchQuery.isNotBlank()) filteredLists else lists

    // reorder state for the LazyColumn
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            vm.swapListPositions(from.index, to.index)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    // long-press dialog state
    var editDialogFor by remember { mutableStateOf<TaskListEntity?>(null) }
    var editName by remember { mutableStateOf("") }

    // "New list" dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // App logo/header with enhanced styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                "Todo App",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Settings with better styling
        NavigationDrawerItem(
            label = { 
                Text(
                    "Settings", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                ) 
            },
            icon = { 
                Icon(
                    Icons.Outlined.Settings, 
                    contentDescription = null
                ) 
            },
            selected = false,
            onClick = {
                navController.navigate("settings")
                scope.launch { closeDrawer() }
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )
        
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )

        // Lists header with better styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            if (searchQuery.isNotBlank()) {
                Text(
                    "Showing ${displayLists.size} results",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty state with better styling
        if (displayLists.isEmpty() && searchQuery.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No lists found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Lists with improved styling and visual feedback
            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .reorderable(reorderState)
                    .detectReorderAfterLongPress(reorderState),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(displayLists, key = { it.id }) { list ->
                    ReorderableItem(reorderState, key = list.id) { isDragging ->
                        val elevation = if (isDragging) 8.dp else 0.dp
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = elevation,
                            tonalElevation = if (isDragging) 2.dp else 0.dp,
                            color = if (isDragging)
                                MaterialTheme.colorScheme.secondaryContainer
                            else 
                                Color.Transparent
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            scope.launch {
                                                closeDrawer()
                                                navController.navigate("list_tasks/${list.id}")
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            editDialogFor = list
                                            editName = list.name
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // List emoji or icon
                                list.emoji?.let { emoji ->
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .size(24.dp)
                                    )
                                } ?: Icon(
                                    Icons.Filled.List,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                // List name
                                Text(
                                    list.name, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Drag handle with better styling
                                Icon(
                                    Icons.Filled.DragHandle, 
                                    contentDescription = "Drag to reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )

        // Action buttons with improved styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Backup button
            FilledTonalButton(
                onClick = {
                    vm.backupToFirestore()
                    Toast.makeText(context, "Backup started", Toast.LENGTH_SHORT).show()
                    scope.launch { closeDrawer() }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    Icons.Filled.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Backup")
            }
            
            // New list button
            Button(
                onClick = {
                    newListName = ""
                    showAddDialog = true
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("New List")
            }
        }
        
        Spacer(Modifier.height(8.dp))
    }

    // ─── Rename / Delete dialog ─────────────────────
    editDialogFor?.let { list ->
        AlertDialog(
            onDismissRequest = { editDialogFor = null },
            title = { 
                Text(
                    "Edit \"${list.name}\"",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.renameList(list.id, editName)
                    editDialogFor = null
                }) { 
                    Text(
                        "Rename",
                        style = MaterialTheme.typography.labelLarge
                    ) 
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        vm.deleteList(list.id)
                        editDialogFor = null
                    }) {
                        Text(
                            "Delete", 
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TextButton(onClick = { editDialogFor = null }) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        )
    }

    // ─── "Add new list" dialog ─────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { 
                Text(
                    "Create new list",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        val newId = UUID.randomUUID().toString()
                        vm.createList(
                            name = newListName.trim(),
                            color = 0xFF90CAF9.toInt(),
                            id = newId
                        )
                        showAddDialog = false
                        scope.launch {
                            closeDrawer()
                            navController.navigate("list_tasks/$newId")
                        }
                    }
                }) { 
                    Text(
                        "OK",
                        style = MaterialTheme.typography.labelLarge
                    ) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}
