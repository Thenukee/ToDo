package com.example.to_do.ui.components
// ui/components/TodoDrawer.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App title with enhanced styling
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Todo App",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main navigation items
        NavigationDrawerItem(
            icon = { 
                Icon(
                    if (selectedRoute == "my_day") 
                        Icons.Filled.WbSunny 
                    else 
                        Icons.Outlined.WbSunny, 
                    contentDescription = "My Day"
                ) 
            },
            label = { 
                Text(
                    "My Day",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selectedRoute == "my_day") FontWeight.SemiBold else FontWeight.Normal
                    )
                ) 
            },
            selected = selectedRoute == "my_day",
            onClick = onMyDayClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationDrawerItem(
            icon = { 
                Icon(
                    if (selectedRoute == "important") 
                        Icons.Filled.Star 
                    else 
                        Icons.Outlined.Star, 
                    contentDescription = "Important"
                ) 
            },
            label = { 
                Text(
                    "Important",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selectedRoute == "important") FontWeight.SemiBold else FontWeight.Normal
                    )
                ) 
            },
            selected = selectedRoute == "important",
            onClick = onImportantClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedContainerColor = Color.Transparent
            )
        )

        NavigationDrawerItem(
            icon = { 
                Icon(
                    if (selectedRoute == "planned") 
                        Icons.Filled.CalendarToday 
                    else 
                        Icons.Outlined.CalendarToday, 
                    contentDescription = "Planned"
                ) 
            },
            label = { 
                Text(
                    "Planned",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selectedRoute == "planned") FontWeight.SemiBold else FontWeight.Normal
                    )
                ) 
            },
            selected = selectedRoute == "planned",
            onClick = onPlannedClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedContainerColor = Color.Transparent
            )
        )

        Divider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )

        // Lists section with better styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            // Enhanced "Add" button with better visual feedback
            IconButton(
                onClick = onCreateListClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create new list",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Improved task lists with visual indicators
        taskLists.forEach { list ->
            val listRoute = "list_tasks/${list.id}"
            val isSelected = selectedRoute == listRoute

            NavigationDrawerItem(
                icon = {
                    list.emoji?.let { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } ?: Icon(
                        if (isSelected) Icons.Filled.FormatListBulleted else Icons.Outlined.FormatListBulleted,
                        contentDescription = null
                    )
                },
                label = { 
                    Text(
                        list.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        ),
                        maxLines = 1
                    ) 
                },
                selected = isSelected,
                onClick = { onListClick(list) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )

        // Settings option with improved styling
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { 
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            selected = selectedRoute == "settings",
            onClick = onSettingsClick,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .padding(bottom = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedContainerColor = Color.Transparent
            )
        )
    }
}