package com.example.to_do.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.*
import com.example.to_do.data.worker.BackupWorker
import com.example.to_do.data.firebase.FirebaseAuthManager
import com.example.to_do.ui.viewmodel.ExportState
import com.example.to_do.ui.viewmodel.RestoreState
import com.example.to_do.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    vm: SettingsViewModel = hiltViewModel()
) {
    /* ---------- state & helpers ---------- */
    val darkTheme by vm.darkTheme.collectAsState()
    val sortAsc   by vm.sortAsc.collectAsState()
    val autoBackup by vm.autoBackup.collectAsState()
    val exportState by vm.exportState.collectAsState()
    val restoreState by vm.restoreState.collectAsState()

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val authManager = remember { FirebaseAuthManager() }
    var signedIn    by remember { mutableStateOf(authManager.isSignedIn()) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    // File picker launcher for JSON export
    val jsonExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            vm.exportTasksToJson(context, it)
            Toast.makeText(context, "Exporting tasks to JSON...", Toast.LENGTH_SHORT).show()
        }
    }

    // Show export result
    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show()
                vm.resetExportState()
            }
            is ExportState.Error -> {
                Toast.makeText(
                    context, 
                    "Export failed: ${(exportState as ExportState.Error).message}", 
                    Toast.LENGTH_LONG
                ).show()
                vm.resetExportState()
            }
            else -> { /* No action needed */ }
        }
    }

    // Monitor restore state for feedback
    LaunchedEffect(restoreState) {
        when (restoreState) {
            is RestoreState.Success -> {
                Toast.makeText(context, (restoreState as RestoreState.Success).message, Toast.LENGTH_LONG).show()
                vm.resetRestoreState()
            }
            is RestoreState.Error -> {
                Toast.makeText(context, "Restore failed: ${(restoreState as RestoreState.Error).message}", Toast.LENGTH_LONG).show()
                vm.resetRestoreState()
            }
            else -> { /* No action needed */ }
        }
    }

    fun triggerBackup() {
        // Use our new method to send test data directly
        vm.sendTestDataToFirestore()
        
        // Export tasks as JSON to Firestore
        vm.exportTasksAsJsonToFirestore()
        
        // Also start the regular backup with WorkManager
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
        Toast.makeText(context, "Backup started with JSON export", Toast.LENGTH_SHORT).show()
    }
    
    fun triggerRestore() {
        // Close the dialog first
        showRestoreDialog = false
        
        // Ensure we're signed in
        if (!signedIn) {
            scope.launch {
                val ok = authManager.ensureSignedIn()
                signedIn = ok
                if (ok) {
                    // Restore from JSON in Firestore
                    vm.restoreFromJsonFirestore()
                } else {
                    Toast.makeText(context, "Sign-in required for restore", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Already signed in, proceed with restore
            vm.restoreFromJsonFirestore()
        }
        
        // Show initial feedback
        Toast.makeText(context, "Restore from JSON started...", Toast.LENGTH_SHORT).show()
    }

    /* ---------- UI ---------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* Appearance ----------------------------------------------------- */
        SectionHeader("Appearance")

        SettingsCard {
            SettingsSwitchItem(
                title = "Dark Theme",
                icon = Icons.Default.DarkMode,
                checked = darkTheme,
                onCheckedChange = vm::setDarkTheme
            )
            Divider(Modifier.padding(vertical = 8.dp))
            SettingsSwitchItem(
                title = "Sort A → Z",
                icon = Icons.Default.Sort,
                checked = sortAsc,
                onCheckedChange = vm::setSortAsc
            )
        }

        /* Backup & Sync -------------------------------------------------- */
        SectionHeader("Backup & Sync")

        SettingsCard {
            Column(Modifier.padding(16.dp)) {
                /* sign-in status row */
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${if (signedIn) "Signed in" else "Not signed in"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (signedIn)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.weight(1f))
                    if (!signedIn) {
                        TextButton(onClick = {
                            scope.launch {
                                val ok = authManager.ensureSignedIn()
                                signedIn = ok
                            }
                        }) { Text("Sign in") }
                    }
                }

                /* auto backup toggle */
                SettingsSwitchItem(
                    title = "Auto Backup",
                    icon = Icons.Default.Backup,
                    checked = autoBackup,
                    onCheckedChange = vm::setAutoBackup
                )

                Spacer(Modifier.height(16.dp))

                /* backup row */
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null)
                    Text(
                        "Cloud Backup",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    
                    // JSON Export Button
                    OutlinedButton(
                        onClick = {
                            jsonExportLauncher.launch(vm.getDefaultJsonExportFilename())
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = exportState !is ExportState.InProgress
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Export as JSON",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("JSON")
                    }
                    
                    // Backup Button
                    Button(
                        onClick = {
                            if (!signedIn) {
                                scope.launch {
                                    val ok = authManager.ensureSignedIn()
                                    signedIn = ok
                                    if (ok) triggerBackup()
                                }
                            } else {
                                triggerBackup()
                            }
                        },
                        enabled = exportState !is ExportState.InProgress
                    ) {
                        Text("Backup Now")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                /* restore row */
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Text(
                        "Restore ",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Button(
                        onClick = { showRestoreDialog = true },
                        enabled = restoreState !is RestoreState.InProgress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Restore") }
                }
                
                // Show progress during restore
                if (restoreState is RestoreState.InProgress) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Restoring data from JSON...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        /* About ---------------------------------------------------------- */
        SectionHeader("About")

        SettingsCard {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Todo App", style = MaterialTheme.typography.bodyLarge)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text("© 2025 Thenuke", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
    
    // Confirmation dialog for restore
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Confirm Restore") },
            text = { 
                Text(
                    "This will restore tasks from the latest JSON backup in Firestore. " +
                    "This may create duplicate tasks if you already have data. Continue?"
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { triggerRestore() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) { Text("Restore") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------- re-usable sub-components ---------- */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) { content() }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
