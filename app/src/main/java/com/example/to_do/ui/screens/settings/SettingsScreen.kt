package com.example.to_do.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    vm: SettingsViewModel = hiltViewModel()          // ← inject VM
) {
    /* ---------- collect prefs ------------------------------------------- */
    val darkTheme by vm.darkTheme.collectAsState()   // Flow → State<Boolean>
    val sortAsc   by vm.sortAsc.collectAsState()

    Scaffold(
        topBar = {
            TodoAppBar(
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            /* Appearance section ---------------------------------------- */
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = vm::setDarkTheme        // ← save to DataStore
                    )
                }
            }

            /* Sort-order preference ------------------------------------- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort A → Z",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = sortAsc,
                        onCheckedChange = vm::setSortAsc          // ← save to DataStore
                    )
                }
            }

            /* About section (unchanged) --------------------------------- */
            Text("About", style = MaterialTheme.typography.titleLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Todo App", style = MaterialTheme.typography.bodyLarge)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("© 2025 MyCompany", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
