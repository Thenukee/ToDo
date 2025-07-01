package com.example.to_do.ui.components
// ui/components/TodoDrawer.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.to_do.data.entity.TaskListEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDrawer(
    selectedRoute: String,
    taskLists: List<TaskListEntity>,
    onMyDayClick: () -> Unit,
    onImportantClick: () -> Unit,
    onPlannedClick: () -> Unit,
    onListClick: (TaskListEntity) -> Unit,
    onSettingsClick: () -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Todo App",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Divider()

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.WbSunny, contentDescription = "My Day") },
            label = { 
                Text(
                    "My Day",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            selected = selectedRoute == "my_day",
            onClick = onMyDayClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Important") },
            label = { 
                Text(
                    "Important",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            selected = selectedRoute == "important",
            onClick = onImportantClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Planned") },
            label = { 
                Text(
                    "Planned",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onCreateListClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new list",
                    tint = MaterialTheme.colorScheme.primary
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
                label = { 
                    Text(
                        list.name,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
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
            label = { 
                Text(
                    "Settings",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            selected = selectedRoute == "settings",
            onClick = onSettingsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}