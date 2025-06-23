// ui/navigation/Screen.kt

package com.example.to_do.ui.navigation

import androidx.navigation.NavBackStackEntry
import com.example.to_do.data.entity.TaskListEntity

sealed class Screen(
    val routePattern: String,
    val needsBack: Boolean,
    val title: String
) {
    object Splash     : Screen("splash",     needsBack = false, title = "Welcome")
    object Lists      : Screen("lists",      needsBack = false, title = "My Lists") 
    object Home       : Screen("home",       needsBack = false, title = "All Tasks")
    object MyDay      : Screen("my_day",     needsBack = false, title = "My Day")
    object Important  : Screen("important",  needsBack = false, title = "Important")
    object Planned    : Screen("planned",    needsBack = false, title = "Planned")
    object Settings   : Screen("settings",   needsBack = true,  title = "Settings")
    object TaskDetail : Screen("task_detail/{taskId}", needsBack = true, title = "Task Details")

    // ① Declare a real subclass for list-specific screens
    data class ListTasks(
        val listId: String,
        val listName: String
    ) : Screen(
        routePattern = "list_tasks/$listId",
        needsBack    = true,
        title        = listName
    )

    companion object {
        fun fromRoute(
            route: String?,
            backStackEntry: NavBackStackEntry?,
            allLists: List<TaskListEntity>
        ): Screen {
            return when {
                route == Splash.routePattern    -> Splash
                route == Lists.routePattern     -> Lists
                route == Home.routePattern      -> Home
                route == MyDay.routePattern     -> MyDay
                route == Important.routePattern -> Important
                route == Planned.routePattern   -> Planned
                route == Settings.routePattern  -> Settings
                route?.startsWith("task_detail/")==true -> TaskDetail

                // ② Match any actual "list_tasks/{listId}" route
                route?.startsWith("list_tasks/")==true -> {
                    val listId = backStackEntry
                        ?.arguments
                        ?.getString("listId")
                        ?: return Settings   // fallback
                    val name = allLists.firstOrNull { it.id == listId }?.name
                        ?: "Lists"
                    ListTasks(listId, name)
                }

                else -> Home
            }
        }
    }
}
