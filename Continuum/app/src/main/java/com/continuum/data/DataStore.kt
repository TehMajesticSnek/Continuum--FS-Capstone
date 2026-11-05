package com.continuum.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

// Declared at the top level of the file so only one instance exists
private val Context.userDataStore by preferencesDataStore(name = "user_data")
private val Context.settingDataStore by preferencesDataStore(name = "user_Settings")

class UserPreferences(private val context: Context) {
    companion object {
        private val selTeamKey = stringPreferencesKey("selected_team")
    }

    val selectedTeamFlow: Flow<Database.Team?> = context.userDataStore.data.map { preferences ->
        val jsonString = preferences[selTeamKey]
        if (jsonString.isNullOrEmpty()) {
            null
        } else {
            try {
                Json.decodeFromString<Database.Team>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun saveSelectedTeam(team: Database.Team?) {
        context.userDataStore.edit { preferences ->
            if (team != null) {
                val jsonString = Json.encodeToString(team)
                preferences[selTeamKey] = jsonString
            }
            else {
                preferences.remove(selTeamKey)
            }

        }
    }
}
class Settings(private val context: Context) {

}