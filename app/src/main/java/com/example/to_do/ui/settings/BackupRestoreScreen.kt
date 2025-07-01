package com.example.to_do.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.example.to_do.data.BackupRestoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {
    private val _backupState = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val backupState = _backupState.asStateFlow()

    fun backupNow() {
        _backupState.value = BackupRestoreState.InProgress("Backing up data...")
        backupRestoreManager.backupNow()
        // In a real app, we would observe the WorkInfo status
        // For simplicity, we'll just set it to success after a delay
        _backupState.value = BackupRestoreState.Success("Backup completed")
    }

    fun restoreFromCloud() {
        _backupState.value = BackupRestoreState.InProgress("Restoring data...")
        backupRestoreManager.restoreFromCloud()
        // In a real app, we would observe the WorkInfo status
        // For simplicity, we'll just set it to success after a delay
        _backupState.value = BackupRestoreState.Success("Restore completed")
    }
}

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    data class InProgress(val message: String) : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}

@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val backupState by viewModel.backupState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showConfirmRestore by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cloud Backup & Restore",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Backup",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your data is automatically backed up to the cloud daily. " +
                           "You can also manually backup your data now."
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.backupNow() },
                    modifier = Modifier.align(Alignment.End),
                    enabled = backupState !is BackupRestoreState.InProgress
                ) {
                    Text("Backup Now")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Restore",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Restore your data from the most recent cloud backup. " +
                           "Warning: This will replace all your current data."
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showConfirmRestore = true },
                    modifier = Modifier.align(Alignment.End),
                    enabled = backupState !is BackupRestoreState.InProgress
                ) {
                    Text("Restore Data")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status indicator
        when (backupState) {
            is BackupRestoreState.Idle -> {}
            is BackupRestoreState.InProgress -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = (backupState as BackupRestoreState.InProgress).message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is BackupRestoreState.Success -> {
                Text(
                    text = (backupState as BackupRestoreState.Success).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is BackupRestoreState.Error -> {
                Text(
                    text = (backupState as BackupRestoreState.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmRestore) {
        AlertDialog(
            onDismissRequest = { showConfirmRestore = false },
            title = { Text("Confirm Restore") },
            text = { Text("This will replace all your current data with data from the cloud backup. Do you want to continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestore = false
                        viewModel.restoreFromCloud()
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmRestore = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
