// ui/components/DrawerContent.kt
package com.example.to_do.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.to_do.data.entity.TaskListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    smart: List<Pair<String, () -> Unit>>,
    lists: List<TaskListEntity>,
    onSelect : (TaskListEntity) -> Unit,
    onSettings: () -> Unit,
    scope: CoroutineScope,
    close: suspend () -> Unit
) {
    ModalDrawerSheet {
        Text("Navigation", modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium)

        smart.forEach { (label, act) ->
            NavigationDrawerItem(label = { Text(label) },
                selected = false,
                onClick = { act(); scope.launch { close() } })
        }

        Divider(Modifier.padding(vertical = 8.dp))

        lists.sortedBy { it.position }.forEach { list ->
            NavigationDrawerItem(label = { Text(list.name) },
                selected = false,
                onClick = { onSelect(list); scope.launch { close() } })
        }

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            leadingIcon = { Icon(Icons.Default.Settings, null) },
            label       = { Text("Settings") },
            selected    = false,
            onClick     = { onSettings(); scope.launch { close() } }
        )
    }
}
