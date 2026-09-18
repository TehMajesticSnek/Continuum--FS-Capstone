package com.continuum.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

    var issueDetails by remember(initialContent) {
        mutableStateOf(initialContent)
    }

    var aiSourceContent by remember(initialContent) {
        mutableStateOf(initialContent)
    }

    var actionsTaken by remember {
        mutableStateOf("")
    }

    var nextSteps by remember {
        mutableStateOf("")
    }

    var isGenerating by remember {
        mutableStateOf(false)
    }

    var statExpanded by remember { mutableStateOf(false) }
    var statSelected by remember { mutableStateOf(viewModel.db.statOptions.entries.find { it.key == 0.toShort() }) }
    val statInteractionSource = remember { MutableInteractionSource() }

    var prioExpanded by remember { mutableStateOf(false) }
    var prioSelected by remember { mutableStateOf(viewModel.db.prioOptions.entries.find { it.key == 3.toShort() }) }
    val prioInteractionSource = remember { MutableInteractionSource() }


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedFileUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedFileName by remember {
        mutableStateOf<String?>(null)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri

            selectedFileName = context.contentResolver
                .query(uri, null, null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(
                        android.provider.OpenableColumns.DISPLAY_NAME
                    )

                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        cursor.getString(nameIndex)
                    } else {
                        null
                    }
                }
                ?: "Selected file"
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
                bottom = 32.dp
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
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MutedText,
                        )
                    },
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
                    viewModel.db.statOptions.forEach { option ->
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
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MutedText,
                        )
                    },
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
                    viewModel.db.prioOptions.forEach { option ->
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

        Text(
            text = "Issue Details",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = issueDetails,
            onValueChange = { issueDetails = it },
            placeholder = {
                Text("Describe the issue or important shift information...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isGenerating = true

                    val sourceContent = if (aiSourceContent.isNotBlank()) {
                        aiSourceContent
                    } else {
                        issueDetails.also {
                            aiSourceContent = it
                        }
                    }

                    val draft = withContext(Dispatchers.IO) {
                        viewModel.db.generateHandoffDraft(sourceContent)
                    }

                    if (draft != null) {
                        title = draft.title
                        issueDetails = draft.issueDetails
                        actionsTaken = draft.actionsTaken
                        nextSteps = draft.nextSteps
                    } else {
                        showError(
                            context,
                            "Unable to generate AI draft. Please try again."
                        )
                    }

                    isGenerating = false
                }
            },
            enabled = issueDetails.isNotBlank() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isGenerating) "Generating..." else "Generate AI Draft"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Actions Taken",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = actionsTaken,
            onValueChange = { actionsTaken = it },
            placeholder = {
                Text("Enter any actions already taken...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Next Steps",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = nextSteps,
            onValueChange = { nextSteps = it },
            placeholder = {
                Text("Enter recommended next steps...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
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
            text = "Attachment",
            color = PrimaryText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (selectedFileUri == null) {
                    "Attach File"
                } else {
                    "Change File"
                }
            )
        }

        if (selectedFileName != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedFileName!!,
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Cancel",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val structuredContent = """
                            Issue Details:
                            ${issueDetails.trim()}
                        
                            Actions Taken:
                            ${actionsTaken.trim()}
                        
                            Next Steps:
                            ${nextSteps.trim()}
                        """.trimIndent()

                        val result = viewModel.db.newHandoff(
                            title,
                            structuredContent,
                            statSelected!!.key,
                            prioSelected!!.key
                        )

                        if (result.error.isEmpty()) {
                            val handoffID = result.handoff?.handoffID

                            if (selectedFileUri != null && handoffID != null) {
                                val fileBytes = context.contentResolver
                                    .openInputStream(selectedFileUri!!)
                                    ?.use { it.readBytes() }

                                if (fileBytes == null) {
                                    withContext(Dispatchers.Main) {
                                        showError(context, "Unable to read the selected file.")
                                    }
                                    return@withContext
                                }

                                val uploadResult = viewModel.db.uploadFileAttachment(
                                    handoffID = handoffID,
                                    fileName = selectedFileName ?: "attachment",
                                    fileBytes = fileBytes
                                )

                                if (uploadResult.isNotEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        showError(context, uploadResult)
                                    }
                                    return@withContext
                                }
                            }

                            withContext(Dispatchers.Main) {
                                onSubmitClick()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                showError(context, result.error)
                            }
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