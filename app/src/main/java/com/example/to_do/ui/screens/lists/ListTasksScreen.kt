package com.example.to_do.ui.screens.list

import androidx.compose.foundation.ExperimentalFoundationApi
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
import org.burnoutcrew.reorderable.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.shadow


import com.example.to_do.data.entity.TaskListEntity


//import org.burnoutcrew.reorderable.offsetByKey
//import org.burnoutcrew.reorderable.draggedItem

import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListTasksScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    vm: TaskViewModel = hiltViewModel()
) {
    // ────────────────────────────────────────────────────────────────
    // 1.  Read the listId that was passed in the Nav route
    // ────────────────────────────────────────────────────────────────
    val listId = backStackEntry.arguments?.getString("listId")!!
    val list by vm.getList(listId).collectAsState(initial = null)
    val tasks by vm.getTasksByList(listId).collectAsState(initial = emptyList())

    // ────────────────────────────────────────────────────────────────
    // 2.  Reorder-state (new API)
    // ────────────────────────────────────────────────────────────────
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> vm.swapPositions(listId, from.index, to.index) }
    )

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
    ) { inner ->
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .reorderable(reorderState)                 // drag-logic
                .detectReorderAfterLongPress(reorderState) // long-press starts drag
        ) {

            // NEW – wrap every row in ReorderableItem -----------------------------
            items(tasks, key = { it.id }) { task ->
                ReorderableItem(reorderState, key = task.id) { isDragging ->

                    TaskItem(
                        task = task,
                        modifier = Modifier
                            .animateItemPlacement()
                            .shadow(if (isDragging) 4.dp else 0.dp), // visual feedback
                        onTaskClick = { navController.navigate("task_detail/${task.id}") },
                        onCompleteToggle = vm::toggleTaskCompletion,
                        onImportantToggle = vm::toggleImportant
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


