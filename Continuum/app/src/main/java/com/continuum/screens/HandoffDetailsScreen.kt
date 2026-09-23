package com.continuum.screens

import com.continuum.data.Database
import coil3.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import kotlinx.coroutines.launch
import kotlin.time.toJavaInstant
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HandoffDetailsScreen(
    viewModel: ViewModel,
    handoff: Database.Handoff,
    onBackClick: () -> Unit = {}
) {
    var optionsMenuExpanded by remember { mutableStateOf(false) }
    var showOwnerDialog by remember { mutableStateOf(false) }
    var owner by remember { mutableStateOf<Database.User?>(null) }

    var acknowledged by remember {
        mutableStateOf(false)
    }

    var currentStatus by remember(handoff.handoffID) {
        mutableStateOf(handoff.status)
    }

    var statusMenuExpanded by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var comments by remember {
        mutableStateOf<List<Database.Comment>>(emptyList())
    }
    var attachments by remember {
        mutableStateOf<List<Database.FileAttachment>>(emptyList())
    }
    var attachmentImageUrls by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var commentAuthors by remember {
        mutableStateOf(emptyMap<String, String>())
    }
    var newEntry by remember {
        mutableStateOf("")
    }
    var isAction by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(handoff.handoffID) {
        val id = handoff.handoffID

        if (id != null) {
            owner = viewModel.db.getUserInfo(handoff.userID ?: "")

            acknowledged = viewModel.db.hasAcknowledgedHandoff(id)

            comments = viewModel.db.getComments(id)

            attachments = viewModel.db.getFileAttachments(id)

            val imageUrls = mutableMapOf<String, String>()

            attachments.forEach { attachment ->
                val fileName = attachment.fileURL.lowercase()

                val isImage = fileName.endsWith(".jpg") ||
                        fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png") ||
                        fileName.endsWith(".webp")

                if (isImage) {
                    val signedUrl = viewModel.db.getAttachmentSignedUrl(
                        attachment.fileURL
                    )

                    if (signedUrl != null) {
                        imageUrls[attachment.fileURL] = signedUrl
                    }
                }
            }

            attachmentImageUrls = imageUrls

            commentAuthors = comments
                .map { it.userID }
                .distinct()
                .associateWith { userID ->
                    viewModel.db.getUserFirstName(userID)
                }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .imePadding()
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

        Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Handoff Details",
                color = PrimaryText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (viewModel.db.userRole == 1L || owner?.userID == viewModel.db.uid) {
                Box(Modifier.align(Alignment.CenterVertically)) {
                    IconButton(onClick = { optionsMenuExpanded = !optionsMenuExpanded }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Team options dropdown menu")
                    }
                    DropdownMenu(
                        expanded = optionsMenuExpanded,
                        onDismissRequest = { optionsMenuExpanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Reassign owner") },
                            onClick = {
                                showOwnerDialog = true
                                optionsMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete handoff",
                                    color = Color(0xffff0000)
                                )
                            },
                            onClick = { //TODO add functionality
                                //showConfirmKickDialog = true
                                optionsMenuExpanded = false
                            } // ensure there will be an admin if you are an admin and leave
                        )
                    }
                }
            }
        }


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
                    text = owner?.firstName + " " + owner?.lastName,
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = handoff.timestamp?.let { timestamp ->
                        val localDateTime = timestamp
                            .toJavaInstant()
                            .atZone(ZoneId.systemDefault())

                        DateTimeFormatter
                            .ofPattern("M/d/yyyy • h:mm a")
                            .format(localDateTime)
                    } ?: "Unknown date",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                statusMenuExpanded = true
                            }
                        ) {
                            Text(
                                text = viewModel.db.statOptions[currentStatus].toString(),
                                color = when (currentStatus.toInt()) {
                                    0 -> Color(0xffFF5F15)
                                    1 -> Color.Yellow
                                    2 -> BluePrimary
                                    3 -> Color.Cyan
                                    4 -> Color.Green
                                    else -> BluePrimary
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (!statusMenuExpanded) {
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

                            DropdownMenu(
                                expanded = statusMenuExpanded,
                                onDismissRequest = {
                                    statusMenuExpanded = false
                                }
                            ) {
                                viewModel.db.statOptions
                                    .forEach { (statusValue, statusName) -> DropdownMenuItem(
                                        text = {
                                            Text(statusName, color = when (statusValue.toInt()) {
                                                0 -> Color(0xffFF5F15)
                                                1 -> Color.Yellow
                                                2 -> BluePrimary
                                                3 -> Color.Cyan
                                                4 -> Color.Green
                                                else -> BluePrimary
                                            })
                                        },
                                        onClick = {
                                            val id = handoff.handoffID

                                            if (id != null) {
                                                coroutineScope.launch {
                                                    val result = viewModel.db.updateHandoffStatus(
                                                        handoffID = id,
                                                        status = statusValue
                                                    )

                                                    if (result.isEmpty()) {
                                                        currentStatus = statusValue
                                                    }
                                                }
                                            }
                                            statusMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column {
                        Text(
                            text = "Priority",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = viewModel.db.prioOptions[handoff.priority].toString(),
                            color = when (handoff.priority.toInt()) {
                                0 -> Color.Red
                                1 -> Color(0xffFF5F15)
                                2 -> Color.Yellow
                                3 -> BluePrimary
                                4 -> Color.Green
                                else -> BluePrimary
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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

                if (attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Attachments",
                        color = BluePrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    attachments.forEach { attachment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        val signedUrl = viewModel.db.getAttachmentSignedUrl(
                                            attachment.fileURL
                                        )

                                        if (signedUrl != null) {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(signedUrl)
                                            )
                                            context.startActivity(intent)
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = NavyBackground
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Border
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {

                                val fileName = attachment.fileURL
                                    .substringAfterLast("/")
                                    .substringAfter("_")

                                val isAudio = fileName.lowercase().let {
                                    it.endsWith(".m4a") ||
                                            it.endsWith(".mp3") ||
                                            it.endsWith(".wav") ||
                                            it.endsWith(".aac")
                                }

                                val imageUrl = attachmentImageUrls[attachment.fileURL]

                                if (imageUrl != null) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Handoff photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = attachment.fileURL
                                        .substringAfterLast("/")
                                        .substringAfter("_"),
                                    color = PrimaryText,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isAudio) "Voice note • Tap to play" else "File attachment",
                                    color = MutedText,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (isAudio && !attachment.transcription.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Transcription",
                                        color = PrimaryText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = attachment.transcription!!,
                                        color = MutedText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val id = handoff.handoffID

                        if (id != null && !acknowledged) {
                            coroutineScope.launch {
                                val result = viewModel.db.acknowledgeHandoff(id)

                                if (result.isEmpty()) {
                                    acknowledged = true

                                    if (currentStatus.toInt() == 0) {
                                        val statusResult = viewModel.db.updateHandoffStatus(
                                            handoffID = id,
                                            status = 1.toShort()
                                        )

                                        if (statusResult.isEmpty()) {
                                            currentStatus = 1.toShort()
                                        }
                                    }
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
                                        .toJavaInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .format(
                                            DateTimeFormatter.ofPattern(
                                                "M/d/yyyy • h:mm a"
                                            )
                                        )
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
                                val result = viewModel.db.addComment(
                                    handoffID = id,
                                    content = newEntry,
                                    isAction = isAction
                                )

                                if (result.isEmpty()) {
                                    newEntry = ""
                                    comments = viewModel.db.getComments(id)
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
    if (showOwnerDialog) {
        OwnerDialog(
            currentOwner = owner,
            handoff = handoff,
            db = viewModel.db,
            onDismissOwner = {
                showOwnerDialog = false
            },
            onSuccessOwner = { ownerID ->
                coroutineScope.launch {
                    owner = viewModel.db.getUserInfo(ownerID)
                    showOwnerDialog = false
                }
            }
        )
    }
}

@Composable
fun OwnerDialog(currentOwner: Database.User?, handoff: Database.Handoff, db: Database, onDismissOwner: () -> Unit, onSuccessOwner: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var ownerSelection by remember {
        mutableStateOf(
            Database.TeamUserDisplay(
                currentOwner?.userID ?: "",
                currentOwner?.firstName ?: "",
                currentOwner?.lastName ?: "",
                emptyList()
            )
        )
    }
    var ownerExpanded by remember { mutableStateOf(false) }
    val ownerInteractionSource = remember { MutableInteractionSource() }
    var teamList by remember { mutableStateOf<List<Database.TeamUserDisplay>>(emptyList()) }

    LaunchedEffect(Unit) {
        teamList = db.getTeamMembers()
    }

    LaunchedEffect(ownerInteractionSource) {
        ownerInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                ownerExpanded = true
            }
        }
    }

    Dialog(onDismissRequest = onDismissOwner) {
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
                    text = "Change Role",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select a new owner"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ownerSelection.firstName + " " + ownerSelection.lastName,
                        onValueChange = { },
                        label = { Text("Select User") },
                        readOnly = true,
                        singleLine = true,
                        interactionSource = ownerInteractionSource,
                        trailingIcon = {
                            if (!ownerExpanded) {
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
                            expanded = ownerExpanded,
                            onDismissRequest = { ownerExpanded = false }
                        ) {

                        teamList.forEach { teamMember ->
                            val teamName = teamMember.firstName + " " + teamMember.lastName
                            DropdownMenuItem(
                                text = {
                                    Text(teamName)
                                },
                                onClick = {
                                    ownerSelection = teamMember
                                    ownerExpanded = false
                                }
                            )
                        }
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
                            onDismissOwner()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            var result: String
                            coroutineScope.launch(Dispatchers.IO) {
                                result = db.updateHandoffOwner(handoffID = handoff.handoffID ?: 0L, userID = ownerSelection.userID)
                                if (result == "") {
                                    onSuccessOwner(ownerSelection.userID)
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