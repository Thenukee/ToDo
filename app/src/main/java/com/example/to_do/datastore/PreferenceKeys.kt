package com.example.to_do.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object PreferenceKeys {
    val DARK_THEME     = booleanPreferencesKey("dark_theme")     // true = dark
    val SORT_ASC       = booleanPreferencesKey("sort_asc")       // true = A→Z
    val AUTO_BACKUP    = booleanPreferencesKey("auto_backup")    // true = enabled
    val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time") // timestamp in milliseconds
}
