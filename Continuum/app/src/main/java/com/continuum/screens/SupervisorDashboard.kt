
package com.continuum.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.continuum.data.Database
import com.continuum.ui.ViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.continuum.data.PrioOptions
import com.continuum.data.StatOptions
import com.continuum.ui.theme.BlueAlternate
import com.continuum.ui.theme.CardColor
import com.continuum.ui.theme.CardMutedColor
import com.continuum.ui.theme.CardTextColor
import com.continuum.ui.theme.Critical
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Success
import com.continuum.ui.theme.Warning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboardScreen(
    viewModel: ViewModel,
    onBackClick: () -> Unit,
    onHandoffClick: (Database.Handoff) -> Unit
) {
    var handoffs by remember {
        mutableStateOf<List<Database.Handoff>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("All Open") }

    LaunchedEffect(refreshCount, viewModel.session.activeTeam?.teamID) {
        isLoading = true
        errorMessage = null
        handoffs = emptyList()
        isAdmin = false

        try {
            val role = viewModel.db.getTeamRole()

            if (role == 1L) {
                isAdmin = true
                handoffs = viewModel.db.getHandoffs()
            } else {
                errorMessage = "Only team admins can view this dashboard."
            }
        } catch (e: Exception) {
            errorMessage = "Unable to load the dashboard."
        }

        isLoading = false
    }

    val urgentCount = handoffs.count { it.priority == 0.toShort() }
    val highCount = handoffs.count { it.priority == 1.toShort() }
    val inProgressCount = handoffs.count { it.status == 2.toShort() }

    val filteredHandoffs = handoffs.filter { handoff ->
        when (selectedFilter) {
            "Urgent / High" -> handoff.priority.toInt() <= 1
            "In Progress" -> handoff.status.toInt() == 2
            else -> true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supervisor Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CardTextColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            refreshCount++
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh trends",
                            tint = CardTextColor
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = NavyBackground,
                    scrolledContainerColor = NavyBackground,
                    navigationIconContentColor = CardTextColor,
                    titleContentColor = CardTextColor,
                    actionIconContentColor = CardTextColor,
                    subtitleContentColor = CardTextColor,
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !isAdmin -> {
                Text(
                    text = errorMessage ?: "Access denied.",
                    modifier = Modifier.padding(paddingValues).padding(16.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Team Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))


                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DashboardStatCard(
                                    title = "Open Handoffs",
                                    count = handoffs.size,
                                    color = BlueAlternate,
                                    modifier = Modifier.weight(1f)
                                )

                                DashboardStatCard(
                                    title = "Urgent",
                                    count = urgentCount,
                                    color = Critical,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DashboardStatCard(
                                    title = "High Priority",
                                    count = highCount,
                                    color = Warning,
                                    modifier = Modifier.weight(1f)
                                )

                                DashboardStatCard(
                                    title = "In Progress",
                                    count = inProgressCount,
                                    color = Success,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    item {
                        Text(
                            text = "Unresolved Handoffs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All Open", "Urgent / High", "In Progress").forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            text = filter,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (filteredHandoffs.isEmpty()) {
                        item {
                            Text(
                                text = if (handoffs.isEmpty()) {
                                    "No unresolved handoffs for this team."
                                } else {
                                    "No handoffs match this filter."
                                }
                            )
                        }
                    }


                    items(
                        items = filteredHandoffs.sortedWith(
                            compareBy<Database.Handoff> { it.priority.toInt() }
                                .thenBy { it.status.toInt() }
                        ),
                        key = { it.handoffID ?: it.hashCode().toLong() }
                    )
                    { handoff ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHandoffClick(handoff) },
                            colors = CardDefaults.cardColors(
                                containerColor = CardColor
                            )
                        ) {

                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = handoff.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )

                                val priorityColor = PrioOptions.fromValue(handoff.priority).prioColor()

                                Surface(
                                    color = priorityColor.copy(alpha = 0.18f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = (
                                                PrioOptions.fromValue(handoff.priority).text
                                                ).uppercase(),
                                        color = priorityColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Status: ${StatOptions.fromValue(handoff.status)}",
                                    color = StatOptions.fromValue(handoff.status).statColor()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = CardMutedColor
            )

            Text(
                text = count.toString(),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
