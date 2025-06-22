package com.example.to_do.ui.screens.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.to_do.ui.components.AddTaskBar
import com.example.to_do.ui.components.TaskItem
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.viewmodel.TaskViewModel

import org.burnoutcrew.reorderable.offsetByKey
import org.burnoutcrew.reorderable.draggedItem

import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTasksScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,                 // <-- get listId from NavArgs
    vm: TaskViewModel = hiltViewModel()
) {
    /* ------------------------------------------------------------- */
    /* read nav argument                                             */
    /* ------------------------------------------------------------- */
    val listId = backStackEntry.arguments?.getString("listId")!!
    val list by vm.getList(listId).collectAsState(initial = null)   // optional helper
    val tasks by vm.getTasksByList(listId).collectAsState(initial = emptyList())

    /* ---------------- reorder state ------------------------------ */
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> vm.swapPositions(listId, from.index, to.index) }
    )
    val scope = rememberCoroutineScope()

    /* ---------------- UI ----------------------------------------- */
    Scaffold(
        topBar = {
            TodoAppBar(
                title = list?.name ?: "List",
                canNavigateBack = true,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            AddTaskBar(
                onAddTask = { title -> vm.createTask(title, listId = listId) },
                placeholder = "Add a task to ${(list?.name ?: "this list")}"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .reorderable(reorderState)                // handle drag gesture
                .detectReorderAfterLongPress(reorderState) // long-press start
        ) {
            items(
                items = tasks,
                key = { it.id }                           // stable key for animation
            ) { task ->
                TaskItem(
                    task = task,
                    modifier = Modifier
                        .draggedItem(reorderState.offsetByKey(task.id)), // follow touch
                    onTaskClick = { navController.navigate("task_detail/${task.id}") },
                    onCompleteToggle = { vm.toggleTaskCompletion(it) },
                    onImportantToggle = { vm.toggleImportant(it) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
