package com.continuum.screens

import android.app.AlertDialog
import android.content.Context
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.continuum.R
import com.continuum.data.Database
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import androidx.compose.material.icons.filled.Description
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.abs
import kotlin.time.toJavaInstant
import androidx.compose.runtime.collectAsState

suspend fun showError(context: Context, error: String) {
    withContext(Dispatchers.Main) {
        AlertDialog.Builder(context, R.style.AlertTheme)
            .setTitle("Error:")
            .setMessage(error)
            .setPositiveButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: ViewModel,
    toNewHandoff: () -> Unit = {},
    toHandoffFromNote: (String) -> Unit = {},
    onHandoffClick: (Database.Handoff) -> Unit = {},
    toHandoffList: () -> Unit = {},
    toTeams: () -> Unit = {},
    logout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val firstName = viewModel.db.getFirstName()
    val currentHour = Calendar
        .getInstance()
        .get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }


    val handoffs = viewModel.handoffs
    val teams = viewModel.teams

    val handoffLoad = viewModel.handoffLoadState
    val teamLoad = viewModel.teamLoadState

    val selectedTeam = viewModel.selectedTeam.collectAsState().value

    val pulseMenuButton = viewModel.teamsIsEmpty

    LaunchedEffect(Unit) {
        if (handoffs.isEmpty()) viewModel.getHandoffs()
        if (teams.isEmpty()) viewModel.getTeams()
        viewModel.getTeamRole()
    }

    var teamSearchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val filteredTeams = teams
        .filter { team ->
            team.teamID != selectedTeam?.teamID &&
                team.teamName?.contains(
                    teamSearchQuery,
                    ignoreCase = true
                ) == true
        }
        .sortedBy { it.teamName?.lowercase() }

    val recentHandoffs = handoffs
        //.sortedByDescending { it.timestamp }
        .take(3)

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

                    TeamDrawerBodyContent(
                        modifier = Modifier.weight(1f),
                        teamLoad = teamLoad,
                        selectedTeam = selectedTeam,
                        teamSearchQuery = teamSearchQuery,
                        onSearchChange = { teamSearchQuery = it },
                        filteredTeams = filteredTeams,
                        onTeamClick = { team ->
                            viewModel.session.activeTeam = team
                            viewModel.getHandoffs()
                            viewModel.getTeams()
                            viewModel.selectTeam(team)

                            scope.launch {
                                drawerState.close()
                                teamSearchQuery = ""
                            }
                        }
                    )

                    // JOIN BUTTON
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

                    Spacer(modifier = Modifier.height(12.dp))

                }
            }
        }
    ) {
        Scaffold (
            modifier = Modifier
                .onSwipeNavigation(
                    onSwipeLeft = {
                        if (viewModel.session.activeTeam != null) {
                            toHandoffList()
                        }
                    },
                    onSwipeRight = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
                .background(NavyBackground),
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
                            selected = true
                        )

                        BottomNavItem(
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            label = "Handoffs",
                            enabled = (viewModel.session.activeTeam != null),
                            onClick = {
                                toHandoffList()
                            }
                        )

                        BottomNavItem(
                            icon = Icons.Outlined.Groups,
                            label = "Team",
                            enabled = (viewModel.session.activeTeam != null),
                            onClick = {
                                toTeams()
                            }
                        )
                    }
                }
            }
        ) {
            it
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 12.dp,
                        bottom = 24.dp
                    )
            ) {

                TopBar(
                    db = viewModel.db,
                    pulseMenuButton = pulseMenuButton,
                    scope = scope,
                    drawerState = drawerState,
                    highPriorityHandoffs = viewModel.highPriorityHandoffs,
                    logout = logout,
                    onClick = { handoff: Database.Handoff ->
                        onHandoffClick(handoff)
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                UpperBlock(
                    db = viewModel.db,
                    greeting = greeting,
                    firstName = firstName,
                    selectedTeam = selectedTeam,
                    awaitNoteNavigate = viewModel::waitSelectingTeams,
                    toHandoff = {
                        toNewHandoff()
                    },
                    toHandoffFromNote = { note: String ->
                        toHandoffFromNote(note)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                BodyColumn(
                    db = viewModel.db,
                    handoffLoad = handoffLoad,
                    unresolved = viewModel.incompleteHandoffs,
                    highPriority = viewModel.highPriorityHandoffs,
                    inProgressCount = viewModel.inProgressHandoffs.size,
                    recent = recentHandoffs,
                    onHandoffClick = onHandoffClick
                )
            }
        }
    }

    if (showJoinDialog) {
        JoinTeamDialog(
            db = viewModel.db,
            onDismissJoin = { showJoinDialog = false },
            onShowCreate = { showCreateDialog = true },
            onSuccessJoin = {
                viewModel.getHandoffs()
                viewModel.getTeams()
                viewModel.selectTeam(viewModel.session.activeTeam)
                showJoinDialog = false
            }
        )
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            db = viewModel.db,
            onDismissCreate = { showCreateDialog = false },
            onSuccessCreate = {
                viewModel.getHandoffs()
                viewModel.getTeams()
                viewModel.selectTeam(viewModel.session.activeTeam)
                showCreateDialog = false
                showJoinDialog = false
            }
        )
    }
}

@Composable
private fun TopBar(
    db: Database,
    pulseMenuButton: Boolean,
    scope: CoroutineScope,
    drawerState: DrawerState,
    highPriorityHandoffs: List<Database.Handoff>,
    onClick: (Database.Handoff) -> Unit,
    logout: () -> Unit
) {
    var showConfirmSignOut by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {


        val (pulseScale, pulseColor) = if (pulseMenuButton) {
            val duration = 1000
            val transition = rememberInfiniteTransition(label = "PulseTransition")
            val animScale by transition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.25f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(durationMillis = duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "PulseIcon"
            )

            val animColor by transition.animateColor(
                initialValue = PrimaryText,
                targetValue = BluePrimary,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(durationMillis = duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "PulseHue"
            )
            Pair(animScale, animColor)
        } else {
            Pair(1.0f, PrimaryText)
        }

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
                tint = pulseColor,
                modifier = Modifier
                    .size(30.dp)
                    .scale(pulseScale)
            )
        }

        Spacer(modifier = Modifier.width(40.dp))
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "CONTINUUM",
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.weight(1f))


        Notifications(db, highPriorityHandoffs, onClick)

        IconButton(
            onClick = {
                showConfirmSignOut = true
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Log out",
                tint = PrimaryText,
                modifier = Modifier.size(30.dp)
            )
        }
    }
    if (showConfirmSignOut) {
        ConfirmSignOut (
            db = db,
            onDismissSignOut = { showConfirmSignOut = false },
            onSuccessSignOut = {
                showConfirmSignOut = false
                logout()
            }
        )
    }
}

@Composable
private fun TeamDrawerBodyContent(
    modifier: Modifier = Modifier,
    teamLoad: Boolean,
    selectedTeam: Database.Team?,
    teamSearchQuery: String,
    onSearchChange: (String) -> Unit,
    filteredTeams: List<Database.Team>,
    onTeamClick: (Database.Team) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (teamLoad) {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 4.dp
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(32.dp))

                // Active Selection
                Text(
                    text = "CURRENT TEAM",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTeam != null) {
                    TeamMenuItem(
                        teamName = selectedTeam.teamName ?: "No Team Selected",
                        selected = true,
                        onClick = {}
                    )
                } else {
                    Text(
                        text = "No Team Selected",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 14.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SEARCH TEAMS
                TeamSearchField(teamSearchQuery, onSearchChange)


                Spacer(modifier = Modifier.height(20.dp))

                TeamList(
                    modifier = modifier,
                    teamSearchQuery = teamSearchQuery,
                    filteredTeams = filteredTeams,
                    onTeamClick = onTeamClick
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TeamSearchField(teamSearchQuery: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = teamSearchQuery,
        onValueChange = onChange,
        placeholder = {
            Text("Search teams...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Teams",
                tint = MutedText
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        singleLine = true,
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
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun TeamList(
    modifier: Modifier = Modifier,
    teamSearchQuery: String,
    filteredTeams: List<Database.Team>,
    onTeamClick: (Database.Team) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = if (teamSearchQuery.isBlank()) {
                "YOUR TEAMS"
            } else {
                "SEARCH RESULTS"
            },
            color = MutedText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))


        if (filteredTeams.isEmpty()) {
            Text(
                text = if (teamSearchQuery.isBlank()) {
                    "No other teams"
                } else {
                    "No teams found"
                },
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 16.dp
                )
            )

        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredTeams, key = { it.teamID!! }) { team ->
                    TeamMenuItem(
                        teamName = team.teamName.toString(),
                        selected = false,
                        onClick = { onTeamClick(team) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
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
                            if (response.errorCode == "") {
                                db.session.activeTeam = response.team
                                onSuccessJoin()
                            }
                            else {
                                showError(context, response.errorCode)
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

                            if (response.errorCode == "") {
                                db.session.activeTeam = response.team
                                onSuccessCreate()
                            }
                            else {
                                showError(context, response.errorCode)
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
private fun Notifications(
    db: Database,
    highPriorityHandoffs: List<Database.Handoff>,
    onHandoffClick: (Database.Handoff) -> Unit
) {
    var showNotifications by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                showNotifications = true
            },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = PrimaryText,
                modifier = Modifier.size(26.dp)
            )
        }

        if (highPriorityHandoffs.isNotEmpty()) {
            val highPriorityCount = highPriorityHandoffs.size
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(
                        Color.Red,
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (highPriorityCount > 9)
                        "9+"
                    else
                        "$highPriorityCount",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if (showNotifications) {
            NotificationsDialog(
                db = db,
                highPriorityHandoffs = highPriorityHandoffs,
                onHandoffClick = onHandoffClick,
                onDismiss = { showNotifications = false }
            )
        }
    }
}
@Composable
private fun NotificationsDialog(
    db: Database,
    highPriorityHandoffs: List<Database.Handoff>,
    onHandoffClick: (Database.Handoff) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Priority Alerts")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                if (highPriorityHandoffs.isEmpty()) {
                    Text(
                        text = "No active priority alerts.",
                        color = MutedText
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(highPriorityHandoffs.sortedBy { it.priority }, key = { it.handoffID!! }) { handoff ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        onDismiss()
                                        onHandoffClick(handoff)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    Border
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = handoff.title,
                                        color = PrimaryText,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = if (handoff.priority.toInt() == 0)
                                            "URGENT"
                                        else
                                            "HIGH PRIORITY",
                                        color = if (handoff.priority.toInt() == 0)
                                            Color.Red
                                        else
                                            Color(0xffFF5F15)
                                    )

                                    Text(
                                        text = db.statOptions[handoff.status]
                                            ?: "Unknown",
                                        color = MutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        }
    )
}
@Composable
fun ConfirmSignOut(
    db: Database,
    onDismissSignOut: () -> Unit,
    onSuccessSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissSignOut) {
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
                    text = "Confirm Sign Out",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Are you sure you want to sign out of your account?")

                Spacer(modifier = Modifier.weight(1f))

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = { onDismissSignOut() }

                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.logout()

                                withContext(Dispatchers.Main) { onSuccessSignOut() }
                            }
                        }

                    ) {
                        Text("Confirm", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpperBlock(
    db: Database,
    greeting: String,
    firstName: String,
    selectedTeam: Database.Team?,
    awaitNoteNavigate: (() -> Unit) -> Unit,
    toHandoff: () -> Unit,
    toHandoffFromNote: (String) -> Unit
) {
    var showQuickNoteDialog by remember { mutableStateOf(false) }
    var showSavedNotesDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Database.Note?>(null) }

    val activeTeam = db.session.activeTeam


    var savedNoteCount by remember { mutableStateOf(0) }
    var notesRefreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(activeTeam?.teamID, notesRefreshKey) {
        savedNoteCount = if (activeTeam != null) {
            db.getNotes().size
        } else {
            0
        }
    }

    // Greeting
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


    // Quick Notes section
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF172238)
        ),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quick Notes",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Capture something to follow up on",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        awaitNoteNavigate {
                            showQuickNoteDialog = true
                        }
                    },
                    enabled = activeTeam != null,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = PrimaryText
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFF34435D))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = activeTeam != null) {
                        awaitNoteNavigate {
                            showSavedNotesDialog = true
                        }
                    }
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "View saved notes",
                    color = PrimaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$savedNoteCount ${if (savedNoteCount == 1) "note" else "notes"}",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "›",
                    color = MutedText,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))




    // New Handoff button
    Button(
        enabled = activeTeam != null,
        onClick = toHandoff,
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

    // Create note dialog
    if (showQuickNoteDialog) {
        QuickNoteDialog(
            db = db,
            onDismiss = {
                showQuickNoteDialog = false
            },
            onSuccess = {
                showQuickNoteDialog = false
                notesRefreshKey++
            }
        )
    }

    // Saved notes dialog
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


    // Edit existing note
    selectedNote?.let { note ->
        EditNoteDialog(
            db = db,
            note = note,
            onDismiss = {
                selectedNote = null
            },
            onSuccess = {
                selectedNote = null
                notesRefreshKey++
            },
            onCreateHandoff = { content ->
                selectedNote = null
                toHandoffFromNote(content)
            }
        )
    }

}



@Composable
fun QuickNoteDialog(
    db: Database,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var noteContent by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = {
        if (!isSaving) onDismiss()
    }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101827)
            ),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Quick Note",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Capture something to follow up on later",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    enabled = !isSaving,
                    placeholder = {
                        Text("Enter note...")
                    },
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Cancel", color = MutedText)
                    }

                    Button(
                        onClick = {
                            isSaving = true

                            coroutineScope.launch {
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        db.saveNote(noteContent)
                                    }

                                    if (response.isEmpty()) {
                                        onSuccess()
                                    } else {
                                        isSaving = false
                                        showError(context, response)
                                    }
                                } catch (e: Exception) {
                                    isSaving = false
                                    showError(
                                        context,
                                        e.message ?: "Unable to save note."
                                    )
                                }
                            }
                        },
                        enabled = noteContent.isNotBlank() && !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = PrimaryText
                        )
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save",
                            fontWeight = FontWeight.SemiBold
                        )
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101827)
            ),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saved Notes",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "${notes.size} ${if (notes.size == 1) "note" else "notes"} saved",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Close", color = BluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(12.dp))

                if (notes.isEmpty()) {
                    Text(
                        text = "No saved notes yet.",
                        color = MutedText,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectNote(note)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF172238)
                                ),
                                border = BorderStroke(1.dp, Border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.content,
                                            color = PrimaryText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(5.dp))

                                        Text(
                                            text = "Tap to edit or create a handoff",
                                            color = MutedText,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "›",
                                        color = MutedText,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
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
    onCreateHandoff: (String) -> Unit
) {
    var noteContent by remember(note.noteID) {
        mutableStateOf(note.content)
    }

    var isSaving by remember { mutableStateOf(false) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101827)
            ),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit Note",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Update your note or turn it into a handoff",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving && !isDeleting
                    ) {
                        Text("✕", color = MutedText)
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp, max = 200.dp),
                    enabled = !isSaving,
                    placeholder = {
                        Text("Enter your note...")
                    },
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

                Button(
                    onClick = {
                        onCreateHandoff(noteContent)
                    },
                    enabled = noteContent.isNotBlank() && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = PrimaryText
                    )
                ) {
                    Text(
                        text = "Create Handoff",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = true
                        },
                        enabled = !isSaving && !isDeleting
                    ) {
                        Text(
                            text = "Delete",
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isSaving = true

                            coroutineScope.launch {
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        db.updateNote(
                                            noteID = note.noteID,
                                            content = noteContent
                                        )
                                    }

                                    if (response.isEmpty()) {
                                        onSuccess()
                                    } else {
                                        isSaving = false
                                        showError(context, response)
                                    }
                                } catch (e: Exception) {
                                    isSaving = false
                                    showError(
                                        context,
                                        e.message ?: "Unable to save note."
                                    )
                                }
                            }
                        },
                        enabled = noteContent.isNotBlank() && !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BluePrimary)
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save Changes",
                            color = BluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

if (showDeleteConfirmation) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                showDeleteConfirmation = false
            }
        },
        title = {
            Text(
                "Delete Note?",
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Are you sure you want to delete this note? This action cannot be undone.",
                color = MutedText
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isDeleting = true

                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                db.deleteNote(note.noteID)
                            }

                            if (response.isEmpty()) {
                                showDeleteConfirmation = false
                                onSuccess()
                            } else {
                                isDeleting = false
                                showError(context, response)
                            }
                        } catch (e: Exception) {
                            isDeleting = false
                            showError(
                                context,
                                e.message ?: "Unable to delete note."
                            )
                        }
                    }
                },
                enabled = !isDeleting
            ) {
                Text(
                    if (isDeleting) "Deleting..." else "Delete",
                    color = Color(0xFFF87171)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDeleteConfirmation = false
                },
                enabled = !isDeleting
            ) {
                Text("Cancel", color = MutedText)
            }
        },
        containerColor = Color(0xFF101827),
        shape = RoundedCornerShape(16.dp)
    )
}
}

@Composable
private fun BodyColumn(
    db: Database,
    handoffLoad: Boolean,
    unresolved: List<Database.Handoff>,
    highPriority: List<Database.Handoff>,
    inProgressCount: Int,
    recent: List<Database.Handoff>,
    onHandoffClick: (Database.Handoff) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            SummaryCardSection(
                handoffLoad = handoffLoad,
                unresolvedCount = unresolved.size,
                highPriorityCount = highPriority.size,
                inProgressCount = inProgressCount
            )
        }

        item {
            CardLists(
                db = db,
                headerText = "Needs Attention",
                blankText = "No high-priority items need attention",
                handoffLoad = handoffLoad,
                cards = highPriority,
                onClick = onHandoffClick
            )
        }

        item {
            CardLists(
                db = db,
                headerText = "Recent Handoffs",
                blankText = "No recent items",
                handoffLoad = handoffLoad,
                cards = recent,
                onClick = onHandoffClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(62.dp))
        }
    }
}
@Composable
private fun SummaryCardSection(
    handoffLoad: Boolean,
    unresolvedCount: Int,
    highPriorityCount: Int,
    inProgressCount: Int,
) {

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
            value = if (handoffLoad) "-" else "$unresolvedCount",
            indicatorColor = Color.Green,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            label = "High\nPriority",
            value = if (handoffLoad) "-" else "$highPriorityCount",
            indicatorColor = Color.Red,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            label = "In\nProgress",
            value = if (handoffLoad) "-" else "$inProgressCount",
            indicatorColor = BluePrimary,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

}

@Composable
private fun CardLists(
    db: Database,
    headerText: String,
    blankText: String,
    handoffLoad: Boolean,
    cards: List<Database.Handoff>,
    onClick: (Database.Handoff) -> Unit
) {
    Text(
        text = headerText,
        color = PrimaryText,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (cards.isEmpty()) {
        Text(
            text = blankText,
            color = MutedText,
            style = MaterialTheme.typography.bodySmall
        )

    } else {
        cards.take(3).forEach { handoff ->
            HandoffCardSimple(db, handoff, onClick)
        }
    }

    if (handoffLoad) {
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 4.dp
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
fun HandoffCardSimple(
    db: Database,
    handoff: Database.Handoff,
    onClick: (Database.Handoff) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                onClick(handoff)
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
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // title
            Text(
                text = handoff.title,
                color = PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            //Content
            Text(
                text = db.separateContent(handoff.content ?: "").issue ?: "",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                //Status + Priority
                Row {
                    Text(
                        text = "${db.statOptions[handoff.status]}",
                        color = when (handoff.status.toInt()) {
                            0 -> Color(0xffFF5F15)
                            1 -> Color.Yellow
                            2 -> BluePrimary
                            3 -> Color.Cyan
                            4 -> Color.Green
                            else -> BluePrimary
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(" • ", color = BluePrimary, style = MaterialTheme.typography.bodySmall)

                    Text(
                        text = db.prioOptions[handoff.priority] ?: "",
                        color = when (handoff.priority.toInt()) {
                            0 -> Color.Red
                            1 -> Color(0xffFF5F15)
                            2 -> Color.Yellow
                            3 -> BluePrimary
                            4 -> Color.Green
                            else -> BluePrimary
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                }
                //Timestamps
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = handoff.timestamp?.let { timestamp ->
                            Instant
                                .parse(timestamp.toString())
                                .atZone(ZoneId.systemDefault())
                                .format(
                                    DateTimeFormatter.ofPattern(
                                        "M/d/yyyy • h:mm a"
                                    )
                                )
                        } ?: "",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (handoff.editTimestamp != null) {
                        Text(
                            text = """(${handoff.editTimestamp.let { timestamp ->
                                Instant
                                    .parse(timestamp.toString())
                                    .atZone(ZoneId.systemDefault())
                                    .format(
                                        DateTimeFormatter.ofPattern(
                                            "M/d/yyyy • h:mm a"
                                        )
                                    )
                            }})""",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun HandoffCardFull(
    db: Database,
    handoff: Database.Handoff,
    onClick: (Database.Handoff) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable {
                onClick(handoff)
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = handoff.title,
                color = PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = run {
                    val parsedContent =
                        db.separateContent(handoff.content ?: "")
                    """
                        |Issue Details: 
                        |${parsedContent.issue}
                        |
                        |Attempted Actions: 
                        |${parsedContent.action}
                        |
                        |Next Steps: 
                        |${parsedContent.next}
                    """.trimMargin()
                },
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Status: ")
                        withStyle(
                            style = SpanStyle(
                                color = when (handoff.status.toInt()) {
                                    0 -> Color(0xffFF5F15)
                                    1 -> Color.Yellow
                                    2 -> BluePrimary
                                    3 -> Color.Cyan
                                    4 -> Color.Green
                                    else -> BluePrimary
                                }
                            )
                        ) {
                            append("${db.statOptions[handoff.status]}")
                        }

                        append("  •  Priority: ")

                        withStyle(
                            style = SpanStyle(
                                color = when (handoff.priority.toInt()) {
                                    0 -> Color.Red
                                    1 -> Color(0xffFF5F15)
                                    2 -> Color.Yellow
                                    3 -> BluePrimary
                                    4 -> Color.Green
                                    else -> BluePrimary
                                }
                            )
                        ) {
                            append("${db.prioOptions[handoff.priority]}")
                        }
                    },
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = handoff.timestamp?.toJavaInstant()?.atZone(ZoneId.systemDefault())
                        ?.format(
                            DateTimeFormatter.ofPattern(
                                "M/d/yyyy • h:mm a"
                            )
                        ) ?: "",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
                if (handoff.editTimestamp != null) {
                    Text(
                        text = """(${
                            handoff.editTimestamp.toJavaInstant().atZone(ZoneId.systemDefault())
                                ?.format(
                                    DateTimeFormatter.ofPattern(
                                        "M/d/yyyy • h:mm a"
                                    )
                                )
                        })""",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val itemColor =
        if (selected) {
            BluePrimary
        } else if (enabled) {
            MutedText
        } else {
            Color(0x4494A3B8)
        }

    Column(
        modifier = Modifier.clickable {
            if (enabled) {
                onClick()
            }
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

fun Modifier.onSwipeNavigation(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeThreshold: Float = 100f
): Modifier = this.pointerInput(Unit) {
    var totalDragX = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDragX = 0f },
        onDragEnd = {
            if (abs(totalDragX) > swipeThreshold) {
                if (totalDragX < 0) onSwipeLeft() else onSwipeRight()
            }
        },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            totalDragX += dragAmount
        }
    )
}