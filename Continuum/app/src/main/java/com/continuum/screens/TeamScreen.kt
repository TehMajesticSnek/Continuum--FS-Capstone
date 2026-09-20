package com.continuum.screens

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
    modifier: Modifier = Modifier,
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
    var selectedMember by remember { mutableStateOf<Database.TeamUserDisplay?>(null) }

    var teamOptionsExpanded by remember { mutableStateOf(false) }

    var showRoleDialog by remember { mutableStateOf(false) }

    var showNameDialog by remember { mutableStateOf(false) }
    var teamName: String? by remember { mutableStateOf("") }

    var showCodeDialog by remember { mutableStateOf(false) }
    var teamCode: String? by remember { mutableStateOf("") }

    var showConfirmKickDialog by remember { mutableStateOf(false) }

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
        viewModel.db.getTeamRole() // TODO consider looking into Realtime stuff for live role updates. Same with handoff ownership and such
    }

    Scaffold (
        modifier = modifier.fillMaxSize(),
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
                                onClick = {
                                    showNameDialog = true
                                    teamOptionsExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Request new code") },
                                onClick = {
                                    showCodeDialog = true
                                    teamOptionsExpanded = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Leave Team", color = Color(0xffff0000)) },
                            onClick = {
                                showConfirmLeaveDialog = true
                                teamOptionsExpanded = false
                            } // ensure there will be an admin if you are an admin and leave
                        )

                        if (viewModel.db.userRole == 1L) {
                            DropdownMenuItem(
                                text = { Text("Delete Team", color = Color(0xffff0000)) },
                                onClick = {
                                    showConfirmDeleteDialog = true
                                    teamOptionsExpanded = false
                                } // confirm popup for both this and leave
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
                        var userOptionsExpanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Border
                            )
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,) {
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

                                    Text(
                                        text = if (member.teamData[0].roleID == 1L) {
                                            "Admin"
                                        } else {
                                            ""
                                        },
                                        color = MutedText,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                }
                                if (viewModel.db.userRole == 1L) {
                                    Box(Modifier.align(Alignment.CenterVertically)) {
                                        IconButton(onClick = { userOptionsExpanded = !userOptionsExpanded }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "Team options dropdown menu")
                                        }
                                        DropdownMenu(
                                            expanded = userOptionsExpanded,
                                            onDismissRequest = { userOptionsExpanded = false }
                                        ) {

                                            DropdownMenuItem(
                                                text = { Text("Change user role") },
                                                onClick = {
                                                    selectedMember = member
                                                    showRoleDialog = true
                                                    userOptionsExpanded = false
                                                }
                                            )

                                            if (member.userID != viewModel.db.uid) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "Remove User",
                                                            color = Color(0xffff0000)
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedMember = member
                                                        showConfirmKickDialog = true
                                                        userOptionsExpanded = false
                                                    } // ensure there will be an admin if you are an admin and leave
                                                )
                                            }

                                        }
                                    }
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
                viewModel.selectTeam(0)
                toHome()
            }
        )
    }
    if (showConfirmKickDialog) {
        ConfirmKickDialog(
            user = selectedMember,
            db = viewModel.db,
            onDismissKick = {
                showConfirmKickDialog = false
                selectedMember = null
            },
            onSuccessKick = {
                showConfirmKickDialog = false
                selectedMember = null

                coroutineScope.launch {
                    members = viewModel.db.getTeamMembers()
                }
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
                viewModel.selectTeam(0)
                toHome()
            }
        )
    }
    if (showRoleDialog) {
        RoleDialog(
            user = selectedMember,
            db = viewModel.db,
            onDismissRole = {
                showRoleDialog = false
                selectedMember = null
            },
            onSuccessRole = {
                coroutineScope.launch {
                    val freshMembers = viewModel.db.getTeamMembers()
                    viewModel.db.userRole = viewModel.db.getTeamRole()
                    members = freshMembers.map { it.copy() }
                }
                showRoleDialog = false
                selectedMember = null
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
fun ConfirmKickDialog(user: Database.TeamUserDisplay?, db: Database, onDismissKick: () -> Unit, onSuccessKick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissKick) {
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
                    text = "Are you sure you want to remove ${user?.firstName + " " + user?.lastName}?"
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
                            onDismissKick()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = db.leaveTeam(user?.userID)

                                if (result == "") {
                                    withContext(Dispatchers.Main) {
                                        onSuccessKick()
                                    }
                                } else {
                                    showError(context, result)
                                }
                            }

                        },
                    ) {
                        Text("Confirm", color = Color(0xffff0000))
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
                    text = "Confirm Delete",
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
@Composable
fun RoleDialog(user: Database.TeamUserDisplay?, db: Database, onDismissRole: () -> Unit, onSuccessRole: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var roleSelection by remember { mutableStateOf(user?.teamData[0]?.roleID) }
    var roleExpanded by remember { mutableStateOf(false) }
    val roleInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(roleInteractionSource) {
        roleInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                roleExpanded = true
            }
        }
    }

    Dialog(onDismissRequest = onDismissRole) {
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
                    text = "Change Role",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select a role for ${user?.firstName + " " + user?.lastName}"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value =
                            if (roleSelection == 1L) {
                                "Admin"
                            } else {
                                "User"
                            },
                        onValueChange = { },
                        label = { Text("Role") },
                        readOnly = true,
                        singleLine = true,
                        interactionSource = roleInteractionSource,
                        trailingIcon = {
                            if (!roleExpanded) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Status",
                                    tint = MutedText
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropUp,
                                    contentDescription = "Change Status",
                                    tint = MutedText
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedBorderColor = Border,
                            unfocusedBorderColor = Border,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedLabelColor = MutedText,
                            unfocusedLabelColor = MutedText,
                            cursorColor = BluePrimary,
                            focusedPlaceholderColor = MutedText,
                            unfocusedPlaceholderColor = MutedText
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Admin") },
                                onClick = {
                                    roleSelection = 1L
                                    roleExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("User") },
                                onClick = {
                                    roleSelection = 0L
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onDismissRole()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            var result = ""
                            coroutineScope.launch(Dispatchers.IO) {
                                result = db.reassignRole(user?.userID, roleSelection ?: 0L)
                                if (result == "") {
                                    onSuccessRole()
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