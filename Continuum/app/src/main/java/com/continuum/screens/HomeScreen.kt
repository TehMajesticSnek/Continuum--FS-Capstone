package com.continuum.screens

import android.R
import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.continuum.Database
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun showError(context: Context, error: String) {
    withContext(Dispatchers.Main) {
        AlertDialog.Builder(context, com.continuum.R.style.AlertTheme)
            .setTitle("Error:")
            .setMessage(error)
            .setPositiveButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}

@Composable
fun JoinTeamDialog(
    db: Database,
    onDismissJoin: () -> Unit,
    onShowCreate: () -> Unit,
    onSuccessJoin: () -> Unit
) {
    var teamCode by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissJoin) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 300.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(top = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    )
            ) {
                Text(
                    text = "Join a Team",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = teamCode,
                    onValueChange = {
                        if (it.length <= 8) {
                            teamCode = it.uppercase()
                        }

                    },
                    label = { Text(text = "Team code") },
                    placeholder = { Text("Enter team code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedLabelColor = BluePrimary,
                        unfocusedLabelColor = MutedText,
                        cursorColor = BluePrimary,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {

                        coroutineScope.launch(Dispatchers.IO) {
                            val response = db.joinTeam(teamCode)
                            if (response == "") {
                                onSuccessJoin()
                            }
                            else {
                                showError(context, response)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(36.dp),
                    enabled = (teamCode.length == 8),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = PrimaryText
                    )
                ) {
                    Text(
                        text = "Submit",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = { onShowCreate() }

                    ) {
                        Text("Create Team")
                    }

                    TextButton(
                        onClick = { onDismissJoin() }

                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeamDialog(db: Database, onDismissCreate: () -> Unit, onSuccessCreate: () -> Unit) {
    var teamName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    Dialog(onDismissRequest = onDismissCreate) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 300.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(top = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    )
            ) {
                Text(
                    text = "Create a Team",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text(text = "Team name") },
                    placeholder = { Text("Enter team name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedLabelColor = BluePrimary,
                        unfocusedLabelColor = MutedText,
                        cursorColor = BluePrimary,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val response = db.createTeam(teamName)
                            if (response == "") {
                                onSuccessCreate()
                            }
                            else {
                                showError(context, response)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(36.dp),
                    enabled = teamName.isNotBlank(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = PrimaryText
                    )
                ) {
                    Text(
                        text = "Create Team",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    modifier = Modifier
                        .align(Alignment.End),
                    onClick = { onDismissCreate() }

                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun QuickNoteDialog(
    db: Database,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var noteContent by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(width = 320.dp, height = 320.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Quick Note",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text("Enter note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = BluePrimary,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val response = db.saveNote(noteContent)

                                if (response.isEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        onSuccess()
                                    }
                                } else {
                                    showError(context, response)
                                }
                            }
                        },
                        enabled = noteContent.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = PrimaryText
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SavedNotesDialog(
    db: Database,
    onDismiss: () -> Unit,
    onSelectNote: (Database.Note) -> Unit
) {
    var notes by remember {
        mutableStateOf<List<Database.Note>>(emptyList())
    }

    LaunchedEffect(Unit) {
        notes = db.getNotes()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(width = 340.dp, height = 420.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Saved Notes",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (notes.isEmpty()) {
                    Text(
                        text = "No saved notes.",
                        color = MutedText
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        notes.reversed().forEach { note ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        onSelectNote(note)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Surface
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Border
                                )
                            ) {
                                Text(
                                    text = note.content,
                                    color = PrimaryText,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    db: Database,
    note: Database.Note,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onCreateHandoff: () -> Unit
) {
    var noteContent by remember(note.noteID) {
        mutableStateOf(note.content)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(width = 320.dp, height = 320.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Edit Note",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onCreateHandoff,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = BluePrimary
                    )
                ) {
                    Text(
                        text = "Create Handoff",
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val response = db.updateNote(
                                    noteID = note.noteID,
                                    content = noteContent
                                )

                                if (response.isEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        onSuccess()
                                    }
                                } else {
                                    showError(context, response)
                                }
                            }
                        },
                        enabled = noteContent.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = PrimaryText
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    db: Database,
    toNewHandoff: () -> Unit = {},
    toHandoffFromNote: (String) -> Unit = {},
    toHistory: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showQuickNoteDialog by remember { mutableStateOf(false) }

    var showSavedNotesDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Database.Note?>(null) }

    val firstName = db.getFirstName()
    val currentHour = java.util.Calendar
        .getInstance()
        .get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    var teams by remember {
        mutableStateOf<List<Database.Team>>(emptyList())
    }

    var selectedTeam by remember {
        mutableStateOf<Database.Team?>(null)
    }

    var handoffs by remember {
        mutableStateOf<List<Database.Handoff>>(emptyList())
    }

    LaunchedEffect(Unit) {
        teams = db.getUserTeams()

        if (selectedTeam == null && teams.isNotEmpty()) {
            selectedTeam = teams.first()
            //TODO replace this with a more permanent solution
            db.activeTeam = selectedTeam!!.teamID // temp solution
            handoffs = db.getHandoffs()
        }
    }

    fun refreshTeams() {
        scope.launch {
            teams = db.getUserTeams()

            if (teams.isNotEmpty()) {
                selectedTeam = teams.first()
                db.activeTeam = selectedTeam!!.teamID // temp solution
            } else {
                selectedTeam = null
            }
        }
    }

    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                drawerContainerColor = Surface
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                ) {

                    Text(
                        text = "CONTINUUM",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "TEAMS",
                        color = MutedText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    teams.forEach { team ->

                        TeamMenuItem(
                            teamName = team.teamName.toString(),
                            selected = team.teamID == selectedTeam?.teamID,
                            onClick = {
                                selectedTeam = team
                                db.activeTeam = selectedTeam!!.teamID // temp solution

                                scope.launch {
                                    handoffs = db.getHandoffs()
                                    drawerState.close()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showJoinDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = BluePrimary
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Join Team",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Join Team",
                            color = BluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Current Team",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedTeam?.teamName ?: "No Team Selected", color = PrimaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
                .statusBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 24.dp
                )
                .verticalScroll(rememberScrollState()),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Teams Menu",
                        tint = PrimaryText,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "CONTINUUM",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryText,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(7.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "$greeting, $firstName!",
                color = PrimaryText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Here's what's happening across your shifts.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Team: ${selectedTeam?.teamName ?: "No Team Selected"}",
                color = BluePrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = { showQuickNoteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = BluePrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Note",
                    tint = BluePrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Quick Note",
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showSavedNotesDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = BluePrimary
                )
            ) {
                Text(
                    text = "Saved Notes",
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = toNewHandoff,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = PrimaryText
                )
            ) {
                Text(
                    text = "New Handoff",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Summary",
                color = PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SummaryCard(
                    label = "Open\nIssues",
                    value = handoffs.count { it.status.toInt() == 0 }.toString(),
                    indicatorColor = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    label = "Critical",
                    value = handoffs.count { it.priority.toInt() >= 4 }.toString(),
                    indicatorColor = Color.Red,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    label = "Resolved\nToday",
                    value = handoffs.count { handoff ->
                        handoff.status.toInt() == 3 &&
                                handoff.timestamp?.toString()?.substringBefore("T") ==
                                kotlin.time.Clock.System.now().toString().substringBefore("T")
                    }.toString(),
                    indicatorColor = Color.Green,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Recent Handoffs",
                color = PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            handoffs.reversed()
                .filter { handoff ->
                    val isRecent =
                        handoff.timestamp != null &&
                                handoff.timestamp >= kotlin.time.Clock.System.now() - kotlin.time.Duration.parse("24h")

                    val isHighPriority = handoff.priority >= 4

                    isRecent || isHighPriority
                }
                .take(3)
                .forEach { handoff ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Border
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = handoff.title,
                                color = PrimaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = handoff.content ?: "",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Priority: ${handoff.priority}",
                                color = BluePrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {

                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = true
                )

                BottomNavItem(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    label = "Handoffs"
                )

                BottomNavItem(
                    icon = Icons.Outlined.Warning,
                    label = "Issues"
                )

                BottomNavItem(
                    icon = Icons.Default.Search,
                    label = "History",
                    onClick = toHistory
                )

                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings"
                )
            }
        }
    }
    if (showQuickNoteDialog) {
        QuickNoteDialog(
            db = db,
            onDismiss = { showQuickNoteDialog = false },
            onSuccess = { showQuickNoteDialog = false }
        )
    }
    if (showSavedNotesDialog) {
        SavedNotesDialog(
            db = db,
            onDismiss = {
                showSavedNotesDialog = false
            },
            onSelectNote = { note ->
                selectedNote = note
                showSavedNotesDialog = false
            }
        )
    }
    selectedNote?.let { note ->
        EditNoteDialog(
            db = db,
            note = note,
            onDismiss = {
                selectedNote = null
            },
            onSuccess = {
                selectedNote = null
            },
            onCreateHandoff = {
                toHandoffFromNote(note.content)
                selectedNote = null
            }
        )
    }

    if (showJoinDialog) {
        JoinTeamDialog(
            db = db,
            onDismissJoin = { showJoinDialog = false },
            onShowCreate = { showCreateDialog = true },
            onSuccessJoin = {
                showJoinDialog = false
                refreshTeams()
            }
        )
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            db = db,
            onDismissCreate = { showCreateDialog = false },
            onSuccessCreate = {
                showCreateDialog = false
                showJoinDialog = false
                refreshTeams()
            }
        )
    }
}



@Composable
private fun TeamMenuItem(
    teamName: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) {
                    BluePrimary.copy(alpha = 0.15f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = teamName,
            color = if (selected) {
                BluePrimary
            } else {
                PrimaryText
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected Team",
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Border
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Text(
                    text = "•",
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.size(5.dp))

                Text(
                    text = label,
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = PrimaryText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val itemColor =
        if (selected) BluePrimary else MutedText

    Column(
        modifier = Modifier.clickable {
            onClick() //
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier.size(27.dp)
        )

        Text(
            text = label,
            color = itemColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}