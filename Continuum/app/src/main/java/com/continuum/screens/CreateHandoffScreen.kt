package com.continuum.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun CreateHandoffScreen(
    viewModel: ViewModel,
    initialContent: String = "",
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }

    var content by remember(initialContent) {
        mutableStateOf(initialContent)
    }
    val blankMapEntry = java.util.AbstractMap.SimpleEntry((-1).toShort(), "")

    var statExpanded by remember { mutableStateOf(false) }
    val statOptions = mapOf(
        0.toShort() to "New",
        1.toShort() to "Acknowledged",
        2.toShort() to "In Progress",
        3.toShort() to "Under Review",
        4.toShort() to "Complete"
    )
    var statSelected by remember { mutableStateOf(statOptions.entries.find { it.key == 0.toShort() }) }
    val statInteractionSource = remember { MutableInteractionSource() }

    var prioExpanded by remember { mutableStateOf(false) }
    val prioOptions = mapOf(
        0.toShort() to "Urgent",
        1.toShort() to "High",
        2.toShort() to "Medium",
        3.toShort() to "Neutral",
        4.toShort() to "Low"
    )
    var prioSelected by remember { mutableStateOf(prioOptions.entries.find { it.key == 3.toShort() }) }
    val prioInteractionSource = remember { MutableInteractionSource() }


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
            text = "New Handoff",
            color = PrimaryText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Capture the important details from your shift.",
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            LaunchedEffect(statInteractionSource) {
                statInteractionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        statExpanded = true
                    }
                }
            }
            LaunchedEffect(prioInteractionSource) {
                prioInteractionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        prioExpanded = true
                    }
                }
            }

            Box (modifier = Modifier.weight(0.75f))
            {
                OutlinedTextField(
                    value = statSelected!!.value,
                    onValueChange = { },
                    label = { Text("Status") },
                    readOnly = true,
                    interactionSource = statInteractionSource,
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

                DropdownMenu(
                    expanded = statExpanded,
                    onDismissRequest = { statExpanded = false }
                ) {
                    statOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.value) },
                            onClick = {
                                statSelected = option
                                statExpanded = false
                            }
                        )
                    }
                }
            }

            Box (modifier = Modifier.weight(0.75f))
            {
                OutlinedTextField(
                    value = prioSelected!!.value,
                    onValueChange = { },
                    label = { Text("Priority") },
                    readOnly = true,
                    interactionSource = prioInteractionSource,
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

                DropdownMenu(
                    expanded = prioExpanded,
                    onDismissRequest = { prioExpanded = false }
                ) {
                    prioOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.value) },
                            onClick = {
                                prioSelected = option
                                prioExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Title",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = {
                Text("Enter handoff title")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Handoff Notes",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = {
                Text(
                    "Enter shift updates, unresolved issues, and important notes..."
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
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
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val response = viewModel.db.newHandoff(title, content, statSelected!!.key, prioSelected!!.key)

                        if (response == "") {
                            withContext(Dispatchers.Main) {
                                onSubmitClick()
                            }
                        }
                        else {
                            showError(context, response)
                        }
                    }
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary,
                contentColor = PrimaryText,
                disabledContainerColor = Border,
                disabledContentColor = MutedText
            )
        ) {
            Text(
                text = "Submit Handoff",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}