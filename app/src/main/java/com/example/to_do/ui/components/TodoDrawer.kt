package com.example.to_do.ui.components
// ui/components/TodoDrawer.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.to_do.data.entity.TaskList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDrawer(
    selectedRoute: String,
    taskLists: List<TaskList>,
    onMyDayClick: () -> Unit,
    onImportantClick: () -> Unit,
    onPlannedClick: () -> Unit,
    onListClick: (TaskList) -> Unit,
    onSettingsClick: () -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Todo App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Divider()

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.WbSunny, contentDescription = "My Day") },
            label = { Text("My Day") },
            selected = selectedRoute == "my_day",
            onClick = onMyDayClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Important") },
            label = { Text("Important") },
            selected = selectedRoute == "important",
            onClick = onImportantClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Planned") },
            label = { Text("Planned") },
            selected = selectedRoute == "planned",
            onClick = onPlannedClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lists",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onCreateListClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new list"
                )
            }
        }

        taskLists.forEach { list ->
            val listRoute = "list_tasks/${list.id}"

            NavigationDrawerItem(
                icon = {
                    list.emoji?.let { emoji ->
                        Text(text = emoji)
                    } ?: Icon(Icons.Default.List, contentDescription = null)
                },
                label = { Text(list.name) },
                selected = selectedRoute == listRoute,
                onClick = { onListClick(list) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedRoute == "settings",
            onClick = onSettingsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}