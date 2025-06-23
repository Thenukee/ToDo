package com.example.to_do.ui.components

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.to_do.data.worker.BackupWorker
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    closeDrawer: suspend () -> Unit          // supplied by the caller
) {
    val context     = LocalContext.current            // Android Context
    val scope       = rememberCoroutineScope()        // Compose-tied CoroutineScope :contentReference[oaicite:0]{index=0}
    val workManager = WorkManager.getInstance(context)

    ModalDrawerSheet {                                // Material-3 drawer sheet :contentReference[oaicite:1]{index=1}
        /* ---------- “Lists” ---------- */
        NavigationDrawerItem(
            label     = { Text("Lists") },
            selected  = false,
            onClick   = {
                navController.navigate("lists")
                scope.launch { closeDrawer() }        // launch from scope, not context :contentReference[oaicite:2]{index=2}
            }
        )

        /* ---------- “Search” ---------- */
        NavigationDrawerItem(
            label     = { Text("Search") },
            selected  = false,
            onClick   = {
                navController.navigate("search")
                scope.launch { closeDrawer() }
            }
        )

        /* ---------- “Backup now” ---------- */
        NavigationDrawerItem(
            label     = { Text("Backup now") },
            selected  = false,
            onClick   = {
                val req = OneTimeWorkRequest.from(BackupWorker::class.java)
                workManager.enqueue(req)              // enqueue a one-shot WorkManager job
                scope.launch { closeDrawer() }
            }
        )
    }
}
