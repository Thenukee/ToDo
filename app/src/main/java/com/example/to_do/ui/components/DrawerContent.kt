// app/src/main/java/com/example/to_do/ui/components/DrawerContent.kt
package com.example.to_do.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val scope    = rememberCoroutineScope()
    val context  = LocalContext.current
    val workMgr  = WorkManager.getInstance(context)
    val lists    by vm.allLists.collectAsState(initial = emptyList())

    // Use filtered lists if provided, otherwise use all lists
    val displayLists = if (filteredLists.isNotEmpty() || searchQuery.isNotBlank()) filteredLists else lists

    // reorder state for the LazyColumn
    // 1) Hoist your reorder state for lists…
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            // → swap list positions by index
            vm.swapListPositions(from.index, to.index)
        }
    )

    // long-press dialog state
    var editDialogFor by remember { mutableStateOf<TaskListEntity?>(null) }
    var editName by remember { mutableStateOf("") }

    // "New list" dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        // App title at the top with proper styling
        Text(
            "Todo App",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // ─ Settings ───────────────────────
        NavigationDrawerItem(
            label = { 
                Text(
                    "Settings", 
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            icon = { 
                Icon(
                    Icons.Default.Settings, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            selected = false,
            onClick = {
                navController.navigate("settings")
                scope.launch { closeDrawer() }
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // ─ Lists header ────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
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

        // ─ Reorderable list of your lists ─
        if (displayLists.isEmpty() && searchQuery.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .reorderable(reorderState)
                    .detectReorderAfterLongPress(reorderState)
            ) {
                items(displayLists, key = { it.id }) { list ->
                    // 3) Each row becomes a ReorderableItem
                    ReorderableItem(reorderState, key = list.id) { isDragging ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDragging) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                                    else 
                                        Color.Transparent
                                )
                                .combinedClickable(
                                    onClick = {
                                        scope.launch {
                                            closeDrawer()
                                            navController.navigate("list_tasks/${list.id}")
                                        }
                                    },
                                    onLongClick = {
                                        editDialogFor = list
                                        editName = list.name
                                    }
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                list.name, 
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.DragHandle, 
                                contentDescription = "Drag handle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))

        // ─ Backup utility ───────────────────
        NavigationDrawerItem(
            label = { 
                Text(
                    "Backup now",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            icon = { 
                Icon(
                    Icons.Default.CloudUpload, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            selected = false,
            onClick = {
                vm.backupToFirestore()
                // Show feedback toast
                Toast.makeText(context, "Backup started", Toast.LENGTH_SHORT).show()
                scope.launch { closeDrawer() }
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // ─ New list entry ──────────────────
        NavigationDrawerItem(
            label = { 
                Text(
                    "New list",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            icon = { 
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            selected = false,
            onClick = {
                newListName = ""
                showAddDialog = true
            },
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(vertical = 4.dp)
        )
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
