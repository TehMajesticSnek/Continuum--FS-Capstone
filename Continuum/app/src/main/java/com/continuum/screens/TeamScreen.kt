package com.continuum.screens

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.continuum.data.Database
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun TeamScreen(
    viewModel: ViewModel,
    toHome: () -> Unit = {},
    toHandoffList: () -> Unit = {},
) {

    var team: Database.Team? by rememberSaveable {
        mutableStateOf(null)
    }
    var members by rememberSaveable {
        mutableStateOf<List<Database.TeamUserDisplay>>(emptyList())
    }
    var teamOptionsExpanded by remember { mutableStateOf(false) }

    var showNameDialog by remember { mutableStateOf(false) }
    var teamName: String? by remember { mutableStateOf("") }

    var showCodeDialog by remember { mutableStateOf(false) }
    var teamCode: String? by remember { mutableStateOf("") }

    var showConfirmLeaveDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (members.isEmpty()) {
            members = viewModel.db.getTeamMembers()
        }
        if (team == null) {
            team = viewModel.db.getActiveTeam()
            teamName = team?.teamName
            teamCode = team?.teamCode
        }
    }
    Scaffold (
        bottomBar = {
            BottomAppBar(
                containerColor = NavyBackground,
                contentPadding = PaddingValues(start = 40.dp, top = 10.dp, end = 40.dp, bottom = 30.dp),
                windowInsets = WindowInsets(0, 0, 0, 80),
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,

                    ) {

                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        onClick = toHome
                    )

                    BottomNavItem(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        label = "Handoffs",
                        onClick = toHandoffList
                    )

                    BottomNavItem(
                        icon = Icons.Outlined.Groups,
                        label = "Team",
                        selected = true
                    )
                }
            }
        }
    ) {
        it
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
        ) {

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {

                Text(
                    text = "Team Details",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row() {
                Column() {
                    Text(
                        teamName ?: "",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Join Code -- $teamCode",
                        color = MutedText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box() {
                    IconButton(onClick = { teamOptionsExpanded = !teamOptionsExpanded }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Team options dropdown menu")
                    }
                    DropdownMenu(
                        expanded = teamOptionsExpanded,
                        onDismissRequest = { teamOptionsExpanded = false }
                    ) {
                        if (viewModel.db.userRole == 1L) {
                            DropdownMenuItem(
                                text = { Text("Change team name") },
                                onClick = { showNameDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Request new code") },
                                onClick = { showCodeDialog = true }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Leave Team", color = Color(0xffff0000)) }, // make red
                            onClick = { showConfirmLeaveDialog = true } // ensure there will be an admin if you are an admin and leave
                        )

                        if (viewModel.db.userRole == 1L) {
                            DropdownMenuItem(
                                text = { Text("Delete Team", color = Color(0xffff0000)) }, // make red
                                onClick = { showConfirmDeleteDialog = true } // confirm popup for both this and leave
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Members",
                color = PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                if (members.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Error fetching team members",
                                color = PrimaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    members.forEach { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                                // TODO see if this can be clickable only if you are admin, and if other user isn't
//                                .clickable {
//
//                                },
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
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "${member.firstName} ${member.lastName}",
                                    color = PrimaryText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                if (member.teamData[0].roleID == 1L) {
                                    Text(
                                        text = "Admin",
                                        color = MutedText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                else {
                                    Text(
                                        text = "",
                                        color = MutedText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    if (showNameDialog) {
        NameDialog(
            teamName = teamName,
            db = viewModel.db,
            onDismissName = { showNameDialog = false },
            onSuccessName = { result ->
                teamName = result
                showNameDialog = false
                teamOptionsExpanded = false
            }
        )
    }
    if (showCodeDialog) {
        CodeDialog(
            teamCode = teamCode,
            db = viewModel.db,
            onDismissCode = { showCodeDialog = false},
            onSuccessCode = { result ->
                teamCode = result
                teamOptionsExpanded = false
            }
        )
    }
    if (showConfirmLeaveDialog) {
        ConfirmLeaveDialog(
            teamName = teamName,
            db = viewModel.db,
            onDismissLeave = { showConfirmLeaveDialog = false},
            onSuccessLeave = {
                showConfirmLeaveDialog = false
                teamOptionsExpanded = false
                viewModel.selectTeam(0)
                toHome()
            }
        )
    }
    if (showConfirmDeleteDialog) {
        ConfirmDeleteDialog(
            teamName = teamName,
            db = viewModel.db,
            onDismissDelete = { showConfirmDeleteDialog = false},
            onSuccessDelete = {
                showConfirmDeleteDialog = false
                teamOptionsExpanded = false
                viewModel.selectTeam(0)
                toHome()
            }
        )
    }
}

@Composable
fun NameDialog(teamName: String?, db: Database, onDismissName: () -> Unit, onSuccessName: (String) -> Unit) {
    var teamName by remember { mutableStateOf(teamName) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissName) {
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
                    text = "Change Team Name",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = teamName ?: "...",
                    onValueChange = {
                        teamName = it
                    },
                    label = { Text("Name") },
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

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onDismissName()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = db.updateTeamName(teamName)

                                if (result == "") {
                                    onSuccessName(teamName ?: "")
                                } else {
                                    showError(context, result)
                                }
                            }

                        },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
@Composable
fun CodeDialog(teamCode: String?, db: Database, onDismissCode: () -> Unit, onSuccessCode: (String) -> Unit) {
    var teamCode by remember { mutableStateOf(teamCode) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissCode) {
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
                    text = "Request New Code",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = teamCode ?: "...",
                    onValueChange = { teamCode = it },
                    readOnly = true,
                    label = { Text("Code") },
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
                            val result = db.newCode()
                            if (result.length <= 16) {
                                teamCode = result
                                onSuccessCode(teamCode ?: "")
                            }
                            else {
                                showError(context, result)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(36.dp),
                    enabled = (teamCode?.length == 8),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = PrimaryText
                    )
                ) {
                    Text(
                        text = "Generate New Code",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.End
                ) {

                    TextButton(
                        onClick = onDismissCode
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
@Composable
fun ConfirmLeaveDialog(teamName: String?, db: Database, onDismissLeave: () -> Unit, onSuccessLeave: () -> Unit) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissLeave) {
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
                    text = "Confirm Leave",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to leave the team ${teamName}?"
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onDismissLeave()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = db.leaveTeam()

                                if (result == "") {
                                    withContext(Dispatchers.Main) {
                                        onSuccessLeave()
                                    }
                                } else {
                                    showError(context, result)
                                }
                            }

                        },
                    ) {
                        Text("Leave", color = Color(0xffff0000))
                    }
                }
            }
        }
    }
}
@Composable
fun ConfirmDeleteDialog(teamName: String?, db: Database, onDismissDelete: () -> Unit, onSuccessDelete: () -> Unit) {

    var teamNameInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissDelete) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 350.dp)
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
                    text = "Confirm Leave",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to delete this team? This action is permanent."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Enter name \"${teamName}\" to delete", color = MutedText)

                OutlinedTextField(
                    value = teamNameInput,
                    onValueChange = {
                        teamNameInput = it
                    },
                    label = { },
                    placeholder = { },
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

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onDismissDelete()
                        },
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.deleteTeam()
                            }
                            onSuccessDelete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffff0000),
                        ),
                        enabled = (teamNameInput == teamName),
                    ) {
                        Text("Delete", color = PrimaryText)
                    }
                }
            }
        }
    }
}