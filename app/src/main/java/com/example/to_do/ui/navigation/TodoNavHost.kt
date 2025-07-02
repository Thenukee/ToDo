package com.example.to_do.ui.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.to_do.ui.screens.home.HomeScreen
import com.example.to_do.ui.screens.taskdetail.TaskDetailScreen
import com.example.to_do.ui.screens.myday.MyDayScreen
import com.example.to_do.ui.screens.important.ImportantScreen
import com.example.to_do.ui.screens.planned.PlannedScreen
import com.example.to_do.ui.screens.settings.SettingsScreen
import com.example.to_do.ui.screens.list.ListTasksScreen // Make sure this import is using the singular 'list' package
import com.example.to_do.ui.screens.lists.ListsScreen
import com.example.to_do.ui.screens.splash.SplashScreen
import com.example.to_do.ui.theme.AppTransitions
import com.example.to_do.ui.screens.search.SearchScreen

@Composable
fun TodoNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash" // Changed to splash screen
    ) {
        composable(
            route = "splash",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(navController = navController)
        }
        
        composable(
            route = "lists",
            enterTransition = AppTransitions.enterTransition,
            exitTransition = AppTransitions.exitTransition,
            popEnterTransition = AppTransitions.popEnterTransition,
            popExitTransition = AppTransitions.popExitTransition
        ) {
            ListsScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("my_day") {
            MyDayScreen(navController = navController)
        }

        composable("important") {
            ImportantScreen(navController = navController)
        }

        composable("planned") {
            PlannedScreen(navController = navController)
        }

        composable(
            "task_detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                navController = navController
            )
        }

        composable(
            route = "list_tasks/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->                       // ← this is what ListTasksScreen needs
            ListTasksScreen(
                navController  = navController,
                backStackEntry = backStackEntry     // ← pass the whole entry
            )
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }

        composable("search") {
            SearchScreen(navController = navController)
        }
    }
}

