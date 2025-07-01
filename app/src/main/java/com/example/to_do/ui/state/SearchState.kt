package com.example.to_do.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Manages the state for the search UI in the top app bar
 */
class SearchState {
    var query by mutableStateOf("")
        private set
    
    var isSearchActive by mutableStateOf(false)
        private set
    
    fun activateSearch() {
        isSearchActive = true
    }
    
    fun deactivateSearch() {
        isSearchActive = false
        query = "" // Clear search on exit
    }
    
    fun updateQuery(newQuery: String) {
        query = newQuery
    }
}

@Composable
fun rememberSearchState(): SearchState {
    return remember { SearchState() }
}
