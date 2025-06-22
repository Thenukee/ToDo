package com.example.to_do.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferenceKeys {
    val DARK_THEME   = booleanPreferencesKey("dark_theme")   // true = dark
    val SORT_ASC     = booleanPreferencesKey("sort_asc")     // true = A→Z
}
