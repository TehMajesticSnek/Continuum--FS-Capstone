package com.continuum.screens

import com.continuum.Database
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface

@Composable
fun HandoffDetailsScreen(
    db: Database,
    handoff: Database.Handoff,
    onBackClick: () -> Unit = {}
) {
    var acknowledged by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(handoff.handoffID) {
        val id = handoff.handoffID
        if (id != null) {
            acknowledged = db.hasAcknowledgedHandoff(id)
        }
    }
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

        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Handoff Details",
            color = PrimaryText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = handoff.title,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = handoff.timestamp
                        ?.toString()
                        ?.substringBefore("T")
                        ?: "Unknown date",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailLabel(
                        label = "Status",
                        value = handoff.status.toString()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    DetailLabel(
                        label = "Priority",
                        value = handoff.priority.toString()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Handoff Notes",
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = handoff.content ?: "",
                    color = PrimaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val id = handoff.handoffID

                        if (id != null && !acknowledged) {
                            coroutineScope.launch {
                                val result = db.acknowledgeHandoff(id)

                                if (result.isEmpty()) {
                                    acknowledged = true
                                }
                            }
                        }
                    },
                    enabled = !acknowledged,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (acknowledged) {
                            "Acknowledged"
                        } else {
                            "Acknowledge Handoff"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLabel(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = PrimaryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}