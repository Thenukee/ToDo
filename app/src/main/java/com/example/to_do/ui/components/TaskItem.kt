package com.example.to_do.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.to_do.data.entity.TaskEntity
import com.example.to_do.data.entity.TaskListEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: TaskEntity,
    onTaskClick: (TaskEntity) -> Unit,
    onCompleteToggle: (TaskEntity) -> Unit,
    onImportantToggle: (TaskEntity) -> Unit,
    onDelete: ((TaskEntity) -> Unit)? = null,
    onMoveTask: ((TaskEntity, String) -> Unit)? = null,
    availableLists: List<TaskListEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    // State to control the task action menu
    var showTaskMenu by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Animate color changes for smoother transitions
    val cardColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else
            MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else
            MaterialTheme.colorScheme.onSurface,
        label = "textColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = { onTaskClick(task) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTaskMenu = true
                }
            )
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isImportant) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = if (task.isImportant) 
                MaterialTheme.colorScheme.tertiary 
            else 
                MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCompleteToggle(task) }) {
                Icon(
                    imageVector = if (task.isCompleted)
                        Icons.Filled.CheckCircle
                    else
                        Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (task.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = textColor
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                task.dueDate?.let { dueDate ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dueDateText = dateFormat.format(Date(dueDate))

                    val today = Calendar.getInstance()
                    val dueCalendar = Calendar.getInstance().apply { timeInMillis = dueDate }
                    val isPastDue = dueCalendar.before(today) && !task.isCompleted

                    val dueDateColor = if (isPastDue)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant

                    Text(
                        text = "Due: $dueDateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = dueDateColor
                    )
                }
            }

            IconButton(onClick = { onImportantToggle(task) }) {
                Icon(
                    imageVector = if (task.isImportant) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (task.isImportant) "Remove from important" else "Mark as important",
                    tint = if (task.isImportant) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Task menu dialog with improved styling
    if (showTaskMenu) {
        AlertDialog(
            onDismissRequest = { showTaskMenu = false },
            title = { Text("Task Options", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Choose an action for ${task.title}", style = MaterialTheme.typography.bodyMedium) },
            shape = MaterialTheme.shapes.medium,
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = {
                                onDelete(task)
                                showTaskMenu = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                    
                    if (onMoveTask != null && availableLists.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showTaskMenu = false
                                showMoveDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Move to...")
                        }
                    }
                    
                    TextButton(
                        onClick = { showTaskMenu = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
    }
    
    // Move task dialog with improved styling
    if (showMoveDialog && onMoveTask != null) {
        Dialog(onDismissRequest = { showMoveDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Move to list",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    availableLists.forEach { list ->
                        val isCurrentList = list.id == task.listId
                        ListItem(
                            headlineContent = { Text(list.name) },
                            leadingContent = {
                                if (isCurrentList) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(Icons.Default.List, contentDescription = null)
                                }
                            },
                            modifier = Modifier.clickable {
                                if (!isCurrentList) {
                                    onMoveTask(task, list.id)
                                }
                                showMoveDialog = false
                            }
                        )
                    }
                    
                    TextButton(
                        onClick = { showMoveDialog = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// Helper function for haptic feedback
private fun performHapticFeedback() {
    // Standard haptic feedback implementation
}