package com.continuum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.continuum.data.Database
import com.continuum.data.UserPreferences
import com.continuum.data.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewModel(private val repository: UserPreferences, val db: Database) : ViewModel() {

    val selectedTeam: StateFlow<Int?> = repository.selectedTeamFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun selectTeam(teamID: Int) { // TODO When logout is added, make sure to clear local team info
        viewModelScope.launch {
            repository.saveSelectedTeam(teamID)
            db.activeTeam = teamID
        }
    }

    class Factory(private val repository: UserPreferences, private val database: Database) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewModel(repository, database) as T
        }
    }
}