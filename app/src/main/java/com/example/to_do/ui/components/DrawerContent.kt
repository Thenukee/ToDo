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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
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
    vm: TaskViewModel = hiltViewModel()
) {
    val scope    = rememberCoroutineScope()
    val context  = LocalContext.current
    val workMgr  = WorkManager.getInstance(context)
    val lists    by vm.allLists.collectAsState(initial = emptyList())

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

    // “New list” dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        // ─ Settings ───────────────────────
        NavigationDrawerItem(
            label    = { Text("Settings") },
            icon     = { Icon(Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick  = {
                navController.navigate("settings")
                scope.launch { closeDrawer() }
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(Modifier.padding(vertical = 8.dp))

        // ─ Lists header ────────────────────
        Text(
            "Lists",
            style    = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )

        // ─ Reorderable list of your lists ─
        LazyColumn(
            state    = reorderState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState)
        ) {
            items(lists, key = { it.id }) { list ->
                // 3) Each row becomes a ReorderableItem
                ReorderableItem(reorderState, key = list.id) { isDragging ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .combinedClickable(
                                onClick = {
                                    scope.launch {
                                        closeDrawer()
                                        navController.navigate("list_tasks/${list.id}")
                                    }
                                },
                                onLongClick = {
                                    /* open rename/delete dialog… */
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(list.name, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.DragHandle, contentDescription = "Drag handle")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider()

        // ─ Backup utility ───────────────────
        NavigationDrawerItem(
            label    = { Text("Backup now") },
            icon     = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
            selected = false,
            onClick  = {
                workMgr.enqueue(OneTimeWorkRequest.from(BackupWorker::class.java))
                scope.launch { closeDrawer() }
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // ─ New list entry ──────────────────
        NavigationDrawerItem(
            label    = { Text("New list") },
            icon     = { Icon(Icons.Default.Add, contentDescription = null) },
            selected = false,
            onClick  = {
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
            title            = { Text("Edit “${list.name}”") },
            text             = {
                OutlinedTextField(
                    value         = editName,
                    onValueChange = { editName = it },
                    label         = { Text("List name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            },
            confirmButton   = {
                TextButton(onClick = {
                    vm.renameList(list.id, editName)
                    editDialogFor = null
                }) { Text("Rename") }
            },
            dismissButton   = {
                Row {
                    TextButton(onClick = {
                        vm.deleteList(list.id)
                        editDialogFor = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { editDialogFor = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // ─── “Add new list” dialog ─────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title            = { Text("Create new list") },
            text             = {
                OutlinedTextField(
                    value         = newListName,
                    onValueChange = { newListName = it },
                    label         = { Text("List name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            },
            confirmButton   = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        val newId = UUID.randomUUID().toString()
                        vm.createList(
                            name  = newListName.trim(),
                            color = 0xFF90CAF9.toInt(),
                            id    = newId
                        )
                        showAddDialog = false
                        scope.launch {
                            closeDrawer()
                            navController.navigate("list_tasks/$newId")
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton   = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
