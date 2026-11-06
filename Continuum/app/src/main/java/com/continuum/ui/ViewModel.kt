package com.continuum.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun selectTeam(team: Database.Team?) { // TODO redo so that switching pages too fast doesn't result in blank fields
        selectTeamJob = viewModelScope.launch {
            selectingTeam = true

            repository.saveSelectedTeam(team)
            session.activeTeam = team
            getTeamRole()

            selectingTeam = false
        }
    }

    var handoffs by mutableStateOf<List<Database.Handoff>>(emptyList())
        private set
    val incompleteHandoffs by derivedStateOf {
        handoffs.filter { it.status != 4.toShort() }
        }
    val highPriorityHandoffs by derivedStateOf {
        incompleteHandoffs.filter { it.priority <= 1 }.sortedWith(
            compareBy<Database.Handoff> { it.priority }
                .thenByDescending { it.timestamp }
        )
    }
    val inProgressHandoffs by derivedStateOf {
        incompleteHandoffs.filter { it.status == 2.toShort() }
    }


    var teams by mutableStateOf<List<Database.Team>>(emptyList())
        private set
    val teamsIsEmpty by derivedStateOf { !teamLoadState && teams.isEmpty() }

    var notes by mutableStateOf<List<Database.Note>>(emptyList())
        private set

    var teamMembers by mutableStateOf<List<Database.TeamUserDisplay>>(emptyList())
        private set

    var handoffLoadState by mutableStateOf(true)
        private set
    var teamLoadState by mutableStateOf(true)
        private set
    var teamMemberLoadState by mutableStateOf(true)
    var selectingTeam by mutableStateOf(true)
        private set

    fun getHandoffs() {
        //if (handoffLoadState) return

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
        //if (teamLoadState) return

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

    fun getTeamRole() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                session.teamRole = db.getTeamRole()
            }
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
        //if (teamMemberLoadState) return

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