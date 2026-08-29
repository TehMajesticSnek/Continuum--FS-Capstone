package com.continuum.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declared at the top level of the file so only one instance exists
private val Context.userDataStore by preferencesDataStore(name = "user_data")
private val Context.settingDataStore by preferencesDataStore(name = "user_Settings")

class UserPreferences(private val context: Context) {
    companion object {
        private val selTeamKey = intPreferencesKey("selected_team")
    }

    val selectedTeamFlow: Flow<Int> = context.userDataStore.data.map { preferences -> preferences[selTeamKey] ?: 0 }

    suspend fun saveSelectedTeam(teamID: Int) {
        context.userDataStore.edit { preferences ->
            preferences[selTeamKey] = teamID
        }
    }
}
class Settings(private val context: Context) {

}