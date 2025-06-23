package com.example.to_do.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.to_do.data.worker.BackupWorker
import com.example.to_do.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    closeDrawer: suspend () -> Unit,
    vm: TaskViewModel = hiltViewModel()
) {
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    val workMgr = WorkManager.getInstance(context)
    val lists   by vm.allLists.collectAsState(initial = emptyList())

    //–– NEW: state to show/hide the “Add list” dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName   by remember { mutableStateOf("") }

    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        // ─── top: Settings ────────────────────────────────────────
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

        // ─── your dynamic lists ───────────────────────────────────
        Text(
            "Lists",
            style    = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        LazyColumn {
            items(lists, key = { it.id }) { list ->
                NavigationDrawerItem(
                    label    = { Text(list.name) },
                    selected = false,
                    onClick  = {
                        navController.navigate("list_tasks/${list.id}")
                        scope.launch { closeDrawer() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider()

        // ─── backup & utilities ───────────────────────────────────
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

        // ─── NEW: “Add new list” entry ─────────────────────────────
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

    // ─── the dialog, shown on demand ───────────────────────────
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
                        vm.createList(
                            name  = newListName.trim(),
                            color = 0xFF90CAF9.toInt()
                        )
                        showAddDialog = false
                        scope.launch { closeDrawer() }
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton   = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



