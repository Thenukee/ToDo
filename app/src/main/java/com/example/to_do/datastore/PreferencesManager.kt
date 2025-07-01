package com.example.to_do.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // --- Public flows the UI can collect ------------------------------------
    val darkTheme   = context.appDataStore.data.map { it[PreferenceKeys.DARK_THEME] ?: false }
    val sortAsc     = context.appDataStore.data.map { it[PreferenceKeys.SORT_ASC] ?: true  }
    val autoBackup  = context.appDataStore.data.map { it[PreferenceKeys.AUTO_BACKUP] ?: true }
    val lastBackupTime = context.appDataStore.data.map { it[PreferenceKeys.LAST_BACKUP_TIME] ?: 0L }

    // --- Mutators ------------------------------------------------------------
    suspend fun setDarkTheme(on: Boolean) =
        context.appDataStore.edit { it[PreferenceKeys.DARK_THEME] = on }

    suspend fun setSortAsc(on: Boolean)  =
        context.appDataStore.edit { it[PreferenceKeys.SORT_ASC] = on }
        
    suspend fun setAutoBackup(on: Boolean) =
        context.appDataStore.edit { it[PreferenceKeys.AUTO_BACKUP] = on }
        
    suspend fun setLastBackupTime(time: Long) =
        context.appDataStore.edit { it[PreferenceKeys.LAST_BACKUP_TIME] = time }
}
