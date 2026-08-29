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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

    var comments by remember {
        mutableStateOf<List<Database.Comment>>(emptyList())
    }

    var commentAuthors by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var newEntry by remember {
        mutableStateOf("")
    }

    var isAction by remember {
        mutableStateOf(false)
    }

    val statOptions = mapOf(
        0.toShort() to "New",
        1.toShort() to "Acknowledged",
        2.toShort() to "In Progress",
        3.toShort() to "Under Review",
        4.toShort() to "Complete"
    )

    val prioOptions = mapOf(
        0.toShort() to "Urgent",
        1.toShort() to "High",
        2.toShort() to "Medium",
        3.toShort() to "Neutral",
        4.toShort() to "Low"
    )

    LaunchedEffect(handoff.handoffID) {
        val id = handoff.handoffID

        if (id != null) {
            acknowledged = db.hasAcknowledgedHandoff(id)

            comments = db.getComments(id)

            commentAuthors = comments
                .map { it.userID }
                .distinct()
                .associateWith { userID ->
                    db.getUserFirstName(userID)
                }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
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
                        value = statOptions[handoff.status].toString()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    DetailLabel(
                        label = "Priority",
                        value = prioOptions[handoff.priority].toString()
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

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Updates",
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                comments.forEach { comment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NavyBackground
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Border
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = if (comment.isAction) {
                                    "Attempted Action"
                                } else {
                                    "Comment"
                                },
                                color = BluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = comment.content,
                                color = PrimaryText
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${commentAuthors[comment.userID] ?: "User"} • ${
                                    comment.timeCreated
                                        .toString()
                                        .replace("T", " ")
                                        .substringBefore(".")
                                }",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            isAction = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Comment")
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                    OutlinedButton(
                        onClick = {
                            isAction = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Attempted Action")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newEntry,
                    onValueChange = {
                        newEntry = it
                    },
                    placeholder = {
                        Text(
                            if (isAction) {
                                "Describe the attempted action..."
                            } else {
                                "Enter a comment..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText,
                        cursorColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val id = handoff.handoffID

                        if (id != null && newEntry.isNotBlank()) {
                            coroutineScope.launch {
                                val result = db.addComment(
                                    handoffID = id,
                                    content = newEntry,
                                    isAction = isAction
                                )

                                if (result.isEmpty()) {
                                    newEntry = ""
                                    comments = db.getComments(id)
                                }
                            }
                        }
                    },
                    enabled = newEntry.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAction) {
                            "Add Attempted Action"
                        } else {
                            "Add Comment"
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