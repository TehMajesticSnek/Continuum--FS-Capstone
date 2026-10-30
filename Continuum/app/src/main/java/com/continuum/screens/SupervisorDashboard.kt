
package com.continuum.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    LaunchedEffect(refreshCount, viewModel.db.activeTeam) {
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
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { refreshCount++ },
                        enabled = !isLoading
                    ) {
                        Text("Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
                                    color = Color(0xFF60A5FA),
                                    modifier = Modifier.weight(1f)
                                )

                                DashboardStatCard(
                                    title = "Urgent",
                                    count = urgentCount,
                                    color = Color(0xFFF87171),
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
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.weight(1f)
                                )

                                DashboardStatCard(
                                    title = "In Progress",
                                    count = inProgressCount,
                                    color = Color(0xFF4ADE80),
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
                                containerColor = Color(0xFF242D3D)
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

                                val priorityColor = when (handoff.priority.toInt()) {
                                    0 -> Color(0xFFF87171) // Urgent
                                    1 -> Color(0xFFFBBF24) // High
                                    2 -> Color(0xFF60A5FA) // Medium
                                    3 -> Color(0xFFCBD5E1) // Neutral
                                    else -> Color(0xFF94A3B8) // Low
                                }

                                Surface(
                                    color = priorityColor.copy(alpha = 0.18f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = (
                                                viewModel.db.prioOptions[handoff.priority]
                                                    ?: "Unknown"
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
                                    "Status: ${
                                        viewModel.db.statOptions[handoff.status]
                                            ?: "Unknown"
                                    }"
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
            containerColor = Color(0xFF242D3D)
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
                color = Color(0xFFCBD5E1)
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
