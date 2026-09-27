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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
fun RecordsScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewModel,
    toHome: () -> Unit = {},
    onHandoffClick: (Database.Handoff) -> Unit = {},
    toTeams: () -> Unit = {}
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

    var teamList = remember { mutableStateListOf<Database.TeamUserDisplay>() }

    var userExpanded by remember { mutableStateOf(false) }
    var userSelected by remember { mutableStateOf(Database.TeamUserDisplay("", "Any", "", emptyList())) }
    val userInteractionSource = remember { MutableInteractionSource() }

    var age: Int? by remember { mutableStateOf<Int?>(0) }

    val ageOptions = mapOf(
        0.toShort() to "Days",
        1.toShort() to "Weeks",
        2.toShort() to "Months",
        3.toShort() to "Years"
    )
    var ageExpanded by remember { mutableStateOf(false) }
    var ageSelected by remember { mutableStateOf(ageOptions.entries.find { it.key == (0).toShort() }) }
    val ageInteractionSource = remember { MutableInteractionSource() }

    val shiftOptions = mapOf(
        (-1).toShort() to "Any",
        0.toShort() to "Day (7:00 AM to 3:00 PM)",
        1.toShort() to "Evening (3:00 PM to 11:00 PM)",
        2.toShort() to "Night (11:00 PM to 7:00 AM)",
    )
    var shiftExpanded by remember { mutableStateOf(false) }
    var shiftSelection by remember { mutableStateOf(shiftOptions.entries.find { it.key == (-1).toShort() }) }
    val shiftInteractionSource = remember { MutableInteractionSource() }

    var showComplete by remember { mutableStateOf(false) }
    val showCompleteInteractionSource = remember { MutableInteractionSource() }

    fun calculateAge(age: Int, timespan: Short): Int {
        var age = age
        when (timespan) {
            1.toShort() -> age *= 7
            2.toShort() -> age *= 30
            3.toShort() -> age *= 365
        }
        return age
    }

    fun filterByShift(handoffs: List<Database.Handoff>, shift: Short): List<Database.Handoff> {
        var startTime: Short = 0
        var endTime: Short = 24

        if (shift == (-1).toShort()) {
            return handoffs
        } else {
            when (shift) {
                0.toShort() -> {
                    startTime = 7
                    endTime = 15
                }
                1.toShort() -> {
                    startTime = 15
                    endTime = 23
                }
                2.toShort() -> {
                    startTime = 23
                    endTime = 7
                }
            }
            return handoffs.filter { handoff ->
                val handoffTimestamp = handoff.editTimestamp ?: handoff.timestamp
                val handoffHour = handoffTimestamp?.toLocalDateTime(TimeZone.currentSystemDefault())?.hour
                handoffHour in startTime..endTime

                if (handoffHour == null) {
                    false
                } else {
                    if (startTime < endTime) { // standard times
                        handoffHour in startTime until endTime
                    } else { // over-midnight
                        handoffHour >= startTime || handoffHour < endTime
                    }
                }
            }
        }
    }

    @Composable
    fun FilterDialog(
        db: Database,
        onDismiss: () -> Unit,
    ) {
        LaunchedEffect(Unit) {
            teamList.clear()
            teamList.add(userSelected)
            teamList += db.getTeamMembers()
        }

        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 450.dp)
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
                        LaunchedEffect(userInteractionSource) {
                            userInteractionSource.interactions.collect { interaction ->
                                if (interaction is PressInteraction.Release) {
                                    userExpanded = true
                                }
                            }
                        }
                        LaunchedEffect(shiftInteractionSource) {
                            shiftInteractionSource.interactions.collect { interaction ->
                                if (interaction is PressInteraction.Release) {
                                    shiftExpanded = true
                                }
                            }
                        }
                        LaunchedEffect(ageInteractionSource) {
                            ageInteractionSource.interactions.collect { interaction ->
                                if (interaction is PressInteraction.Release) {
                                    ageExpanded = true
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
                                trailingIcon = {
                                    if (!statExpanded) {
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
                                    focusedTextColor = when (statSelected!!.key.toInt()) {
                                        0 -> Color(0xffFF5F15)
                                        1 -> Color.Yellow
                                        2 -> BluePrimary
                                        3 -> Color.Cyan
                                        4 -> Color.Green
                                        else -> PrimaryText
                                    },
                                    unfocusedTextColor = when (statSelected!!.key.toInt()) {
                                        0 -> Color(0xffFF5F15)
                                        1 -> Color.Yellow
                                        2 -> BluePrimary
                                        3 -> Color.Cyan
                                        4 -> Color.Green
                                        else -> PrimaryText
                                    },
                                    focusedLabelColor = MutedText,
                                    unfocusedLabelColor = MutedText,
                                    cursorColor = BluePrimary,
                                    focusedPlaceholderColor = MutedText,
                                    unfocusedPlaceholderColor = MutedText
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Box (Modifier.align(Alignment.BottomStart)) {
                                DropdownMenu(
                                    expanded = statExpanded,
                                    onDismissRequest = { statExpanded = false }
                                ) {
                                    statOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.value,
                                                color = when (option.key.toInt()) {
                                                    0 -> Color(0xffFF5F15)
                                                    1 -> Color.Yellow
                                                    2 -> BluePrimary
                                                    3 -> Color.Cyan
                                                    4 -> Color.Green
                                                    else -> PrimaryText
                                                })
                                            },
                                            onClick = {
                                                statSelected = option

                                                if (statSelected!!.key == 4.toShort()) {
                                                    showComplete = true
                                                }
                                                else if (statSelected!!.key != (-1).toShort()) {
                                                    showComplete = false
                                                }

                                                coroutineScope.launch(Dispatchers.IO) {
                                                    handoffs = viewModel.db.getHandoffsFilter(
                                                        keyword = searchText,
                                                        status = statSelected!!.key,
                                                        priority = prioSelected!!.key,
                                                        user = userSelected.userID,
                                                        age = calculateAge(age?: 0, ageSelected!!.key),
                                                        includeCompleted = showComplete
                                                    )
                                                    handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                                }

                                                statExpanded = false
                                            }
                                        )
                                    }
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
                                trailingIcon = {
                                    if (!prioExpanded) {
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
                                    focusedTextColor = when (prioSelected!!.key.toInt()) {
                                        0 -> Color.Red
                                        1 -> Color(0xffFF5F15)
                                        2 -> Color.Yellow
                                        3 -> BluePrimary
                                        4 -> Color.Green
                                        else -> PrimaryText
                                    },
                                    unfocusedTextColor = when (prioSelected!!.key.toInt()) {
                                        0 -> Color.Red
                                        1 -> Color(0xffFF5F15)
                                        2 -> Color.Yellow
                                        3 -> BluePrimary
                                        4 -> Color.Green
                                        else -> PrimaryText
                                    },
                                    focusedLabelColor = MutedText,
                                    unfocusedLabelColor = MutedText,
                                    cursorColor = BluePrimary,
                                    focusedPlaceholderColor = MutedText,
                                    unfocusedPlaceholderColor = MutedText
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Box(Modifier.align(Alignment.BottomEnd)) {
                                DropdownMenu(
                                    expanded = prioExpanded,
                                    onDismissRequest = { prioExpanded = false }
                                ) {
                                    prioOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(
                                                option.value,
                                                color = when (option.key.toInt()) {
                                                    0 -> Color.Red
                                                    1 -> Color(0xffFF5F15)
                                                    2 -> Color.Yellow
                                                    3 -> BluePrimary
                                                    4 -> Color.Green
                                                    else -> PrimaryText
                                                })
                                            },
                                            onClick = {
                                                prioSelected = option

                                                coroutineScope.launch(Dispatchers.IO) {
                                                    handoffs = viewModel.db.getHandoffsFilter(
                                                        keyword = searchText,
                                                        status = statSelected!!.key,
                                                        priority = prioSelected!!.key,
                                                        user = userSelected.userID,
                                                        age = calculateAge(age?: 0, ageSelected!!.key),
                                                        includeCompleted = showComplete
                                                    )
                                                    handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                                }

                                                prioExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Box()
                    {
                        OutlinedTextField(
                            value = userSelected.firstName + " " + userSelected.lastName,
                            onValueChange = { },
                            label = { Text("User") },
                            readOnly = true,
                            singleLine = true,
                            interactionSource = userInteractionSource,
                            trailingIcon = {
                                if (!userExpanded) {
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
                        Box(Modifier.align(Alignment.BottomEnd)) {
                            DropdownMenu(
                                expanded = userExpanded,
                                onDismissRequest = { userExpanded = false }
                            ) {
                                teamList.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(
                                            option.firstName + " " + option.lastName,
                                            color = PrimaryText
                                        )},
                                        onClick = {
                                            userSelected = option

                                            coroutineScope.launch(Dispatchers.IO) {
                                                handoffs = viewModel.db.getHandoffsFilter(
                                                    keyword = searchText,
                                                    status = statSelected!!.key,
                                                    priority = prioSelected!!.key,
                                                    user = userSelected.userID,
                                                    age = calculateAge(age?: 0, ageSelected!!.key),
                                                    includeCompleted = showComplete
                                                )
                                                handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                            }

                                            userExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Box()
                    {
                        OutlinedTextField(
                            value = shiftSelection!!.value,
                            onValueChange = { },
                            label = { Text("Shift") },
                            readOnly = true,
                            singleLine = true,
                            interactionSource = shiftInteractionSource,
                            trailingIcon = {
                                if (!shiftExpanded) {
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
                        Box(Modifier.align(Alignment.BottomEnd)) {
                            DropdownMenu(
                                expanded = shiftExpanded,
                                onDismissRequest = { shiftExpanded = false }
                            ) {
                                shiftOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(
                                            option.value,
                                            color = PrimaryText
                                        )},
                                        onClick = {
                                            shiftSelection = option

                                            coroutineScope.launch(Dispatchers.IO) {
                                                handoffs = viewModel.db.getHandoffsFilter(
                                                    keyword = searchText,
                                                    status = statSelected!!.key,
                                                    priority = prioSelected!!.key,
                                                    user = userSelected.userID,
                                                    age = calculateAge(age?: 0, ageSelected!!.key),
                                                    includeCompleted = showComplete
                                                )
                                                handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                            }

                                            userExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row (horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(0.5f))
                        {
                            OutlinedTextField(
                                value = age?.toString() ?: "",
                                onValueChange = { digit ->
                                    if (digit.all { it.isDigit() }) {
                                        age = digit.toIntOrNull()
                                    }

                                    coroutineScope.launch(Dispatchers.IO) {
                                        handoffs = viewModel.db.getHandoffsFilter(
                                            keyword = searchText,
                                            status = statSelected!!.key,
                                            priority = prioSelected!!.key,
                                            user = userSelected.userID,
                                            age = calculateAge(age?: 0, ageSelected!!.key),
                                            includeCompleted = showComplete
                                        )
                                        handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                    }
                                },
                                label = { Text("Age") },
                                readOnly = false,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        }

                        Box(modifier = Modifier.weight(0.5f))
                        {
                            OutlinedTextField(
                                value = ageSelected!!.value ,
                                onValueChange = { },
                                label = { Text("Timespan") },
                                readOnly = true,
                                singleLine = true,
                                interactionSource = ageInteractionSource,
                                trailingIcon = {
                                    if (!ageExpanded) {
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
                            Box(Modifier.align(Alignment.BottomEnd)) {
                                DropdownMenu(
                                    expanded = ageExpanded,
                                    onDismissRequest = { ageExpanded = false }
                                ) {
                                    ageOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.value) },
                                            onClick = {
                                                ageSelected = option

                                                coroutineScope.launch(Dispatchers.IO) {
                                                    handoffs = viewModel.db.getHandoffsFilter(
                                                        keyword = searchText,
                                                        status = statSelected!!.key,
                                                        priority = prioSelected!!.key,
                                                        user = userSelected.userID,
                                                        age = calculateAge(age?: 0, ageSelected!!.key),
                                                        includeCompleted = showComplete
                                                    )
                                                    handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                                }

                                                ageExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .clickable(
                                enabled = statSelected!!.key == (-1).toShort(),
                                indication = null,
                                onClick = {
                                    showComplete = !showComplete

                                    coroutineScope.launch(Dispatchers.IO) {
                                        handoffs = viewModel.db.getHandoffsFilter(
                                            keyword = searchText,
                                            status = statSelected!!.key,
                                            priority = prioSelected!!.key,
                                            user = userSelected.userID,
                                            age = calculateAge(age?: 0, ageSelected!!.key),
                                            includeCompleted = showComplete
                                        )
                                        handoffs = filterByShift(handoffs, shiftSelection!!.key)
                                    }
                                },
                                role = Role.Checkbox,
                                onClickLabel = "Toggle Completed Handoffs",
                                interactionSource = showCompleteInteractionSource

                            ),
                        )
                    {
                        Checkbox(
                            checked = showComplete,
                            enabled = ( false ),
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = BluePrimary,
                                checkmarkColor = PrimaryText,
                                uncheckedColor = MutedText,
                                disabledCheckedColor = BluePrimary,
                                disabledUncheckedColor = MutedText,
                            )
                        )

                        Text(
                            "Show Completed Handoffs",
                            fontSize = 14.sp,
                            modifier = Modifier,
                            color = PrimaryText
                        )
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
       handoffs = viewModel.db.getHandoffs()
    }

    Scaffold (
        modifier = modifier.fillMaxSize(),
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
                        onClick = toHome
                    )

                    BottomNavItem(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        label = "Handoffs",
                        selected = true
                    )

                    BottomNavItem(
                        icon = Icons.Outlined.Groups,
                        label = "Team",
                        onClick = toTeams
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
                            handoffs = viewModel.db.getHandoffsFilter(
                                keyword = searchText,
                                status = statSelected!!.key,
                                priority = prioSelected!!.key,
                                user = userSelected.userID,
                                age = calculateAge(age?: 0, ageSelected!!.key),
                                includeCompleted = showComplete
                            )
                            handoffs = filterByShift(handoffs, shiftSelection!!.key)
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
                modifier = Modifier.imePadding().verticalScroll(rememberScrollState()),
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
                    handoffs.forEach { handoff ->
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
                                    text = run {
                                        val parsedContent =
                                            viewModel.db.separateContent(handoff.content ?: "")
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
                                                append(viewModel.db.statOptions[handoff.status].toString())
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
                                                append(viewModel.db.prioOptions[handoff.priority].toString())
                                            }
                                        },
                                        color = BluePrimary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = handoff.timestamp?.let { timestamp ->
                                            timestamp
                                                .toJavaInstant()
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
                                            text = """(${handoff.editTimestamp?.let { timestamp ->
                                                timestamp
                                                    .toJavaInstant()
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