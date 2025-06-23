package com.example.to_do

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.to_do.ui.components.DrawerContent
import com.example.to_do.ui.components.TodoAppBar
import com.example.to_do.ui.navigation.Screen
import com.example.to_do.ui.navigation.TodoNavHost
import com.example.to_do.ui.theme.TodoAppTheme
import com.example.to_do.ui.viewmodel.SettingsViewModel
import com.example.to_do.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RootApp() }
    }
}

@Composable
fun RootApp() {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val useDarkTheme by settingsVm.darkTheme.collectAsStateWithLifecycle()

    TodoAppTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val drawerState   = rememberDrawerState(DrawerValue.Closed)
        val scope         = rememberCoroutineScope()

        // ③ Hoist a TaskViewModel so we can read allLists here
        val taskVm: TaskViewModel = hiltViewModel()
        val allLists by taskVm.allLists.collectAsState(initial = emptyList())

        ModalNavigationDrawer(
            drawerState   = drawerState,
            drawerContent = {
                DrawerContent(
                    navController = navController,
                    closeDrawer   = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val backStack by navController.currentBackStackEntryAsState()
                    val route     = backStack?.destination?.route
                    // ④ Lookup the right Screen (and its title)
                    val screen    = Screen.fromRoute(route, backStack, allLists)

                    TodoAppBar(
                        title           = screen.title,
                        canNavigateBack = screen.needsBack,
                        onNavigateBack  = { navController.navigateUp() },
                        onMenuClick     = { scope.launch { drawerState.open() } }
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    TodoNavHost(navController = navController)
                }
            }
        }
    }
}
