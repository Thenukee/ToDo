package com.example.to_do.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Sort
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

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val authManager = remember { FirebaseAuthManager() }
    var signedIn    by remember { mutableStateOf(authManager.isSignedIn()) }

    fun triggerBackup() {
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
        Toast.makeText(context, "Backup started", Toast.LENGTH_SHORT).show()
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
                    Button(onClick = {
                        if (!signedIn) {
                            scope.launch {
                                val ok = authManager.ensureSignedIn()
                                signedIn = ok
                                if (ok) triggerBackup()
                            }
                        } else {
                            triggerBackup()
                        }
                    }) { Text("Backup Now") }
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
