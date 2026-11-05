package com.continuum.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.ComposeNavigator
import com.continuum.MainActivity
import com.continuum.data.Database
import com.continuum.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModel(private val repository: UserPreferences, val db: Database, val session: MainActivity.UserSession) : ViewModel() {

    val selectedTeam: StateFlow<Database.Team?> = repository.selectedTeamFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    private var selectTeamJob: Job? = null
    var teamRole by mutableIntStateOf(0)


    fun selectTeam(team: Database.Team?) {
        selectTeamJob = viewModelScope.launch {
            selectingTeam = true

            repository.saveSelectedTeam(team)
            session.activeTeam = team

            selectingTeam = false
        }
    }



    var handoffs by mutableStateOf<List<Database.Handoff>>(emptyList())
        private set
    var filteredHandoffs by mutableStateOf<List<Database.Handoff>>(emptyList())
        private set

    var teams by mutableStateOf<List<Database.Team>>(emptyList())
        private set

    var notes by mutableStateOf<List<Database.Note>>(emptyList())
        private set

    var teamMembers by mutableStateOf<List<Database.TeamUserDisplay>>(emptyList())
        private set

    var handoffLoadState by mutableStateOf(false)
        private set
    var teamLoadState by mutableStateOf(false)
        private set
    var teamMemberLoadState by mutableStateOf(false)
    var selectingTeam by mutableStateOf(false)
        private set

    fun getHandoffs() {
        if (handoffLoadState) return

        viewModelScope.launch {
            handoffLoadState = true
            handoffs = emptyList()
            withContext(Dispatchers.IO) {
                if (handoffs.isEmpty()) {
                    handoffs = db.getHandoffs()
                }
            }

            handoffLoadState = false
        }
    }

    fun getTeams() {
        if (teamLoadState) return

        viewModelScope.launch {
            teamLoadState = true
            teams = emptyList()
            withContext(Dispatchers.IO) {
                if (teams.isEmpty()) {
                    teams = db.getUserTeams()
                }
            }

            teamLoadState = false
        }
    }

    fun getNotes() {

    }

    fun waitSelectingTeams(initiateNavigation: () -> Unit) {
        viewModelScope.launch {
            selectTeamJob?.join()
            initiateNavigation()
        }
    }

    fun getTeamMembers() {
        if (teamMemberLoadState) return

        viewModelScope.launch {
            teamMemberLoadState = true

            withContext(Dispatchers.IO) {
                if (teamMembers.isEmpty()) {
                    teamMembers = db.getTeamMembers()
                }
            }

            teamMemberLoadState = false
        }
    }


    class Factory(private val repository: UserPreferences, private val database: Database, private val session: MainActivity.UserSession) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewModel(repository, database, session) as T
        }
    }
}