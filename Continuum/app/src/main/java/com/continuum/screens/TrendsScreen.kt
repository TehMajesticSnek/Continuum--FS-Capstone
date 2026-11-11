
package com.continuum.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.continuum.data.Database
import com.continuum.data.PrioOptions
import com.continuum.data.StatOptions
import com.continuum.ui.theme.BlueAlternate
import com.continuum.ui.theme.CardColor
import com.continuum.ui.theme.CardTextColor
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.Success
import com.continuum.ui.theme.Warning
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val CardMutedColor = Color(0xFFB0BAC9)

@Composable
fun TrendsScreen(
    db: Database,
    onBack: () -> Unit
) {
    var trends by remember {
        mutableStateOf<Database.HandoffTrends?>(null)
    }
    var isLoading by remember {
        mutableStateOf(true)
    }

    var repeatedIssues by remember {
        mutableStateOf<List<Database.RepeatedIssueTrend>>(emptyList())
    }


    val scope = rememberCoroutineScope()


    suspend fun loadTrends() {
        isLoading = true
        try {
            trends = db.getHandoffTrends()
            repeatedIssues = db.getRepeatedIssueTrends()
        } finally {
            isLoading = false
        }
    }


    LaunchedEffect(db.session.activeTeam) {
        loadTrends()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),

    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CardTextColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trends & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CardTextColor
                )
                Text(
                    text = "Team handoff insights",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardMutedColor
                )
            }

            IconButton(
                onClick = {
                    scope.launch { loadTrends() }
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh trends",
                    tint = CardTextColor
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center,

            ) {
                CircularProgressIndicator()
            }
        } else {
            trends?.let { data ->

                val completed =
                    data.statusCounts[4.toShort()] ?: 0

                val unresolved =
                    data.totalHandoffs - completed


                val completionRate =
                    if (data.totalHandoffs > 0) {
                        (completed * 100.0 / data.totalHandoffs).roundToInt()
                    } else {
                        0
                    }


                // Summary cards
                Text(
                    text = "Team Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CardTextColor
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrendsStatCard(
                        title = "Total Handoffs",
                        value = data.totalHandoffs.toString(),
                        valueColor = CardTextColor,
                        modifier = Modifier.weight(1f)
                    )

                    TrendsStatCard(
                        title = "Completed",
                        value = completed.toString(),
                        valueColor = Success,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrendsStatCard(
                        title = "Unresolved",
                        value = unresolved.toString(),
                        valueColor = Warning,
                        modifier = Modifier.weight(1f)
                    )

                    TrendsStatCard(
                        title = "Completion Rate",
                        value = "$completionRate%",
                        valueColor = BlueAlternate,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Priority breakdown
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Priority Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CardTextColor
                        )

                        PrioOptions.entries.forEach { priority ->
                            val count =
                                data.priorityCounts[priority.ordinal.toShort()] ?: 0

                            val fraction =
                                if (data.totalHandoffs > 0) {
                                    count.toFloat() / data.totalHandoffs
                                } else {
                                    0f
                                }

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = priority.text,
                                        color = CardTextColor
                                    )
                                    Text(
                                        text = count.toString(),
                                        color = CardMutedColor
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = priority.prioColor(),
                                    trackColor = Color(0xFF394456),
                                    gapSize = 0.dp,
                                    drawStopIndicator = {}
                                )
                            }
                        }
                    }
                }

                // Activity over the last 7 days
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Activity Over Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CardTextColor
                        )

                        Text(
                            text = "Handoffs created in the last 7 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = CardMutedColor
                        )

                        val today = LocalDate.now()
                        val days = (6 downTo 0).map {
                            today.minusDays(it.toLong())
                        }

                        val maxCount = days.maxOfOrNull {
                            data.dailyActivity[it] ?: 0
                        }?.coerceAtLeast(1) ?: 1

                        days.forEach { day ->
                            val count = data.dailyActivity[day] ?: 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = day.format(
                                        DateTimeFormatter.ofPattern("EEE")
                                    ),
                                    color = CardMutedColor,
                                    modifier = Modifier.width(42.dp)
                                )

                                LinearProgressIndicator(
                                    progress = {
                                        count.toFloat() / maxCount
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp),
                                    color = BlueAlternate,
                                    trackColor = Color(0xFF394456),
                                    gapSize = 0.dp,
                                    drawStopIndicator = {}
                                )

                                Text(
                                    text = count.toString(),
                                    color = CardTextColor,
                                    modifier = Modifier.width(24.dp)
                                )
                            }
                        }
                    }
                }

                // Repeated Issues
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Repeated Issues",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CardTextColor
                        )

                        Text(
                            text = "Potentially recurring handoff topics",
                            style = MaterialTheme.typography.bodySmall,
                            color = CardMutedColor
                        )

                        if (repeatedIssues.isEmpty()) {
                            Text(
                                text = "No repeated issues detected.",
                                color = CardMutedColor
                            )
                        } else {
                            repeatedIssues.take(5).forEach { issue ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = issue.title,
                                        color = CardTextColor,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "${issue.occurrences} occurrences",
                                        color = BlueAlternate,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Status breakdown
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Handoff Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CardTextColor
                        )

                        StatOptions.entries.forEach { status ->
                            val count =
                                data.statusCounts[status.ordinal.toShort()] ?: 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = status.text,
                                    color = status.statColor()
                                )
                                Text(
                                    text = count.toString(),
                                    color = CardMutedColor,
                                    fontWeight = FontWeight.Bold
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
private fun TrendsStatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = CardMutedColor
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
