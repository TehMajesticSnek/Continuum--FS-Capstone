package com.continuum.screens

import android.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun JoinTeamDialog(db: Database, onDismissJoin: () -> Unit, onShowCreate: () -> Unit) {
    var teamCode by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()


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
                            val response = db.JoinTeam(teamCode)
                            if (response == "") {
                                onDismissJoin()
                            }
                            else {
                                //TODO error popup
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
                                //TODO error popup
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
fun HomeScreen(
    db: Database,
    toNewHandoff: () -> Unit = {},
    toHistory: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val teams = listOf(
        "Night Shift",
        "IT Support",
        "Operations"
    )

    var selectedTeam by remember {
        mutableStateOf(teams.first())
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
                            teamName = team,
                            selected = team == selectedTeam,
                            onClick = {
                                selectedTeam = team

                                scope.launch {
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
                        text = selectedTeam,
                        color = PrimaryText,
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
                text = "Good Morning, Dominic!",
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
                text = "Team: $selectedTeam",
                color = BluePrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

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
                    value = "8",
                    indicatorColor = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    label = "Critical",
                    value = "2",
                    indicatorColor = Color.Red,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    label = "Resolved\nToday",
                    value = "12",
                    indicatorColor = Color.Green,
                    modifier = Modifier.weight(1f)
                )
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
    if (showJoinDialog) {
        JoinTeamDialog(
            db,
            onDismissJoin = { showJoinDialog = false },
            onShowCreate = { showCreateDialog = true }
        )
    }
    if (showCreateDialog) {
        CreateTeamDialog(
            db,
            onDismissCreate = { showCreateDialog = false },
            onSuccessCreate = {
                showCreateDialog = false
                showJoinDialog = false
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