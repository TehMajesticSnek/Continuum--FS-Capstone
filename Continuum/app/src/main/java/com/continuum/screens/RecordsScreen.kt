package com.continuum.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.continuum.data.Database
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch





@Composable
fun RecordsScreen(
    viewModel: ViewModel,
    toHome: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onHandoffClick: (Database.Handoff) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var searchText by rememberSaveable {
        mutableStateOf("")
    }
    var handoffs by rememberSaveable {
        mutableStateOf<List<Database.Handoff>>(emptyList())
    }

    var showFilterDialog by remember { mutableStateOf(false) }

    val statOptions = mapOf(
        (-1).toShort() to "Any",
        0.toShort() to "New",
        1.toShort() to "Acknowledged",
        2.toShort() to "In Progress",
        3.toShort() to "Under Review",
        4.toShort() to "Complete"
    )
    var statExpanded by remember { mutableStateOf(false) }
    var statSelected by remember { mutableStateOf(statOptions.entries.find { it.key == (-1).toShort() }) }
    val statInteractionSource = remember { MutableInteractionSource() }

    val prioOptions = mapOf(
        (-1).toShort() to "Any",
        0.toShort() to "Urgent",
        1.toShort() to "High",
        2.toShort() to "Medium",
        3.toShort() to "Neutral",
        4.toShort() to "Low"
    )
    var prioExpanded by remember { mutableStateOf(false) }
    var prioSelected by remember { mutableStateOf(statOptions.entries.find { it.key == (-1).toShort() }) }
    val prioInteractionSource = remember { MutableInteractionSource() }

    var showComplete by remember { mutableStateOf(false) }

    //TODO Make a modal or whatever for filters. Button next to search bar opens. For now, just have status, prio, and showComplete.
    // filter icon is just called filter list
    @Composable
    fun FilterDialog(
        db: Database,
        onDismiss: () -> Unit,
    ) {

        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 300.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                    .padding(top = 12.dp)
            ) {
                Column (modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    )
                ) {
                    Text(
                        text = "Search Filters",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                        Box(modifier = Modifier.weight(0.5f))
                        {
                            OutlinedTextField(
                                value = statSelected!!.value,
                                onValueChange = { },
                                label = { Text("Status") },
                                readOnly = true,
                                singleLine = true,
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
                                            coroutineScope.launch(Dispatchers.IO) {
                                                handoffs = viewModel.db.getHandoffsFilter(
                                                    keyword = searchText,
                                                    status = statSelected!!.key,
                                                    priority = prioSelected!!.key,
                                                    includeCompleted = showComplete
                                                )
                                            }

                                            statSelected = option
                                            statExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(0.5f))
                        {
                            OutlinedTextField(
                                value = prioSelected!!.value,
                                onValueChange = { },
                                label = { Text("Priority") },
                                readOnly = true,
                                singleLine = true,
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
                                            coroutineScope.launch(Dispatchers.IO) {
                                                handoffs = viewModel.db.getHandoffsFilter(
                                                    keyword = searchText,
                                                    status = statSelected!!.key,
                                                    priority = prioSelected!!.key,
                                                    includeCompleted = showComplete
                                                )
                                            }

                                            prioSelected = option
                                            prioExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(IntrinsicSize.Max),
                    ) {
                        Checkbox(
                            checked = showComplete,
                            onCheckedChange = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    handoffs = viewModel.db.getHandoffsFilter(
                                        keyword = searchText,
                                        status = statSelected!!.key,
                                        priority = prioSelected!!.key,
                                        includeCompleted = showComplete
                                    )
                                }
                                showComplete = it
                            }
                        )

                        TextButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    handoffs = viewModel.db.getHandoffsFilter(
                                        keyword = searchText,
                                        status = statSelected!!.key,
                                        priority = prioSelected!!.key,
                                        includeCompleted = showComplete
                                    )
                                }
                                showComplete = !showComplete
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PrimaryText
                            ),
                        ) {
                            Text(
                                "Show Completed Handoffs",
                                fontSize = 14.sp,
                                modifier = Modifier

                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                            modifier = Modifier.align(Alignment.End),


                        ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        if (handoffs.isEmpty()) {
            handoffs = viewModel.db.getHandoffs()
        }
    }
    Scaffold (
        bottomBar = {
            BottomAppBar(
                containerColor = NavyBackground,
                contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 30.dp),
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
                        onClick = toHome,

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
                        selected = true,
                    )

                    BottomNavItem(
                        icon = Icons.Default.Settings,
                        label = "Settings"
                    )
                }
            }
        }
    ) {
        it
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

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "History",
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                // Search
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        coroutineScope.launch(Dispatchers.IO) {
                            handoffs = viewModel.db.getHandoffsFilter(keyword = searchText, status = statSelected!!.key, priority = prioSelected!!.key, includeCompleted = showComplete)
                        }
                    },
                    placeholder = {
                        Text("Search handoffs")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MutedText
                        )
                    },
                    modifier = Modifier.weight(1f),
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

                OutlinedIconButton(
                    onClick = { showFilterDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(width = 1.dp, color = Border),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(.2f)

                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter list",
                        tint = MutedText,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, bottom = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Handoff Records",
                color = PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                if (handoffs.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No handoff records found",
                                color = PrimaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Handoffs for the selected team will appear here.",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    handoffs.reversed().forEach { handoff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable {
                                    onHandoffClick(handoff)
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
                                    text = handoff.content ?: "",
                                    color = MutedText,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Status: ${viewModel.db.statOptions[handoff.status]}  •  Priority: ${viewModel.db.prioOptions[handoff.priority]}",
                                    color = BluePrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    if (showFilterDialog) {
        FilterDialog(
            db = viewModel.db,
            onDismiss = { showFilterDialog = false },
        )
    }
}

@Composable
private fun FilterBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = PrimaryText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "$text filter",
                tint = MutedText
            )
        }
    }
}