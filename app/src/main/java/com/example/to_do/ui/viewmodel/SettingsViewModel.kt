package com.example.to_do.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    // expose each preference as a StateFlow the UI can collect
    val darkTheme: StateFlow<Boolean> = prefs.darkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val sortAsc: StateFlow<Boolean> = prefs.sortAsc
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    /** Toggle the Material-3 dark theme */
    fun setDarkTheme(on: Boolean) = viewModelScope.launch {
        prefs.setDarkTheme(on)
    }

    /** Change default sort order */
    fun setSortAsc(on: Boolean) = viewModelScope.launch {
        prefs.setSortAsc(on)
    }
}
