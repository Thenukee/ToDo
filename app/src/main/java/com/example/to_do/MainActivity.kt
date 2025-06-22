package com.example.to_do

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.to_do.ui.viewmodel.SettingsViewModel
//import com.example.to_do.ui.theme.ToDoTheme
import dagger.hilt.android.HiltAndroidApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle   // NEW
import androidx.hilt.navigation.compose.hiltViewModel          // NEW



import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.to_do.ui.navigation.TodoNavHost
import com.example.to_do.ui.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoApp()
                }
            }
        }
    }
}
/* Root composable ­– now grabs SettingsViewModel and re-applies theme       */

@Composable
fun TodoApp() {
    // 1. Pull the preferences via SettingsViewModel
    val settingsVm: SettingsViewModel = hiltViewModel()
    val useDarkTheme by settingsVm.darkTheme.collectAsStateWithLifecycle()

    // 2. Apply your custom Material-3 theme
    TodoAppTheme(darkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            TodoNavHost(navController)
        }
    }
}