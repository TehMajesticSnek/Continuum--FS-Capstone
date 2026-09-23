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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.media.AudioAttributes
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.continuum.data.Database
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.FileProvider
import java.io.File

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

    var voiceTranscription by remember { mutableStateOf("") }

    var statExpanded by remember { mutableStateOf(false) }
    var statSelected by remember { mutableStateOf(viewModel.db.statOptions.entries.find { it.key == 0.toShort() }) }
    val statInteractionSource = remember { MutableInteractionSource() }

    var prioExpanded by remember { mutableStateOf(false) }
    var prioSelected by remember { mutableStateOf(viewModel.db.prioOptions.entries.find { it.key == 3.toShort() }) }
    val prioInteractionSource = remember { MutableInteractionSource() }


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val offlineRecognizer = remember {
        OfflineRecognizer(
            assetManager = context.assets,
            config = OfflineRecognizerConfig(
                modelConfig = OfflineModelConfig(
                    nemo = OfflineNemoEncDecCtcModelConfig(
                        model = "sherpa/model.onnx"
                    ),
                    tokens = "sherpa/tokens.txt",
                    numThreads = 2,
                    debug = false
                )
            )
        )
    }

    var selectedFileUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedFileName by remember {
        mutableStateOf<String?>(null)
    }

    var cameraPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var mediaRecorder by remember {
        mutableStateOf<MediaRecorder?>(null)
    }

    var mediaPlayer by remember {
        mutableStateOf<MediaPlayer?>(null)
    }

    var isRecording by remember {
        mutableStateOf(false)
    }

    var audioFile by remember {
        mutableStateOf<File?>(null)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                try {
                    mediaRecorder?.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
            }
        }
    }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            voiceTranscription = ""

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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            voiceTranscription = ""

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
                ?: "Selected photo"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            selectedFileUri = cameraPhotoUri
            voiceTranscription = ""
            selectedFileName = "handoff_photo_${System.currentTimeMillis()}.jpg"
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                "Microphone permission is required to record a voice note.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun decodeM4aToPcm(file: File): Pair<FloatArray, Int> {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)

            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                audioFormat = format
                break
            }
        }

        if (audioTrackIndex == -1 || audioFormat == null) {
            extractor.release()
            throw IllegalArgumentException("No audio track found in recording")
        }

        extractor.selectTrack(audioTrackIndex)

        val mime = audioFormat.getString(MediaFormat.KEY_MIME)
            ?: throw IllegalArgumentException("Audio format has no MIME type")

        val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(audioFormat, null, null, 0)
        codec.start()

        val samples = mutableListOf<Float>()
        val bufferInfo = MediaCodec.BufferInfo()

        var inputFinished = false
        var outputFinished = false

        try {
            while (!outputFinished) {

                if (!inputFinished) {
                    val inputIndex = codec.dequeueInputBuffer(10_000)

                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)

                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)

                            if (sampleSize < 0) {
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputFinished = true
                            } else {
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    0
                                )

                                extractor.advance()
                            }
                        }
                    }
                }

                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)

                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)

                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                        while (outputBuffer.remaining() >= 2) {
                            val low = outputBuffer.get().toInt() and 0xFF
                            val high = outputBuffer.get().toInt()

                            val pcm16 = ((high shl 8) or low).toShort()

                            samples.add(
                                pcm16.toFloat() / 32768.0f
                            )
                        }
                    }

                    codec.releaseOutputBuffer(outputIndex, false)

                    if (
                        bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    ) {
                        outputFinished = true
                    }
                }
            }
        } finally {
            codec.stop()
            codec.release()
            extractor.release()
        }

        return Pair(samples.toFloatArray(), sampleRate)
    }

    fun transcribeVoiceRecording(file: File): String {
        return try {
            val (samples, sampleRate) = decodeM4aToPcm(file)

            println("SHERPA AUDIO: ${samples.size} samples @ $sampleRate Hz")

            val stream = offlineRecognizer.createStream()

            stream.acceptWaveform(
                samples = samples,
                sampleRate = sampleRate
            )

            offlineRecognizer.decode(stream)

            val result = offlineRecognizer.getResult(stream)
            val text = result.text

            println("SHERPA TRANSCRIPTION: $text")

            stream.release()

            text
        } catch (e: Exception) {
            println("SHERPA TRANSCRIPTION ERROR: ${e.message}")
            e.printStackTrace()
            ""
        }
    }

    fun startVoiceRecording() {
        val file = File.createTempFile(
            "voice_note_",
            ".m4a",
            context.cacheDir
        )

        audioFile = file

        @Suppress("DEPRECATION")
        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        mediaRecorder = recorder
        isRecording = true

    }

    fun stopVoiceRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }

        audioFile?.let { file ->
            selectedFileUri = Uri.fromFile(file)
            selectedFileName = file.name

            coroutineScope.launch {
                val transcription = withContext(Dispatchers.Default) {
                    transcribeVoiceRecording(file)
                }

                voiceTranscription = transcription
                println("FINAL VOICE TRANSCRIPTION: $voiceTranscription")
            }
        }
    }

    fun playVoiceRecording() {
        val file = audioFile ?: return

        mediaPlayer?.release()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setOnPreparedListener { player ->
                    player.start()
                }

                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }

                setOnErrorListener { _, what, extra ->
                    Toast.makeText(
                        context,
                        "Playback error: $what / $extra",
                        Toast.LENGTH_LONG
                    ).show()
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Playback failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
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

            Box (modifier = Modifier.weight(0.5f))
            {
                OutlinedTextField(
                    value = statSelected!!.value,
                    onValueChange = { },
                    label = { Text("Status") },
                    readOnly = true,
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
                            else -> BluePrimary
                        },
                        unfocusedTextColor = when (statSelected!!.key.toInt()) {
                            0 -> Color(0xffFF5F15)
                            1 -> Color.Yellow
                            2 -> BluePrimary
                            3 -> Color.Cyan
                            4 -> Color.Green
                            else -> BluePrimary
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
                        viewModel.db.statOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option.value,
                                        color = when (option.key.toInt()) {
                                            0 -> Color(0xffFF5F15)
                                            1 -> Color.Yellow
                                            2 -> BluePrimary
                                            3 -> Color.Cyan
                                            4 -> Color.Green
                                            else -> BluePrimary
                                        }
                                    )
                                },
                                onClick = {
                                    statSelected = option
                                    statExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box (modifier = Modifier.weight(0.5f))
            {
                OutlinedTextField(
                    value = prioSelected!!.value,
                    onValueChange = { },
                    label = { Text("Priority") },
                    readOnly = true,
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
                            else -> BluePrimary
                        },
                        unfocusedTextColor = when (prioSelected!!.key.toInt()) {
                            0 -> Color.Red
                            1 -> Color(0xffFF5F15)
                            2 -> Color.Yellow
                            3 -> BluePrimary
                            4 -> Color.Green
                            else -> BluePrimary
                        },
                        focusedLabelColor = MutedText,
                        unfocusedLabelColor = MutedText,
                        cursorColor = BluePrimary,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Box (Modifier.align(Alignment.BottomEnd)) {
                    DropdownMenu(
                        expanded = prioExpanded,
                        onDismissRequest = { prioExpanded = false }
                    ) {
                        viewModel.db.prioOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option.value,
                                        color = when (option.key.toInt()) {
                                            0 -> Color.Red
                                            1 -> Color(0xffFF5F15)
                                            2 -> Color.Yellow
                                            3 -> BluePrimary
                                            4 -> Color.Green
                                            else -> BluePrimary
                                        },
                                    )
                                },

                                onClick = {
                                    prioSelected = option
                                    prioExpanded = false
                                }
                            )
                        }
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Add Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val photoFile = File.createTempFile(
                    "handoff_photo_",
                    ".jpg",
                    context.cacheDir
                )

                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )

                cameraPhotoUri = photoUri
                cameraLauncher.launch(photoUri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Take Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (isRecording) {
                    stopVoiceRecording()
                } else {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startVoiceRecording()
                    } else {
                        microphonePermissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isRecording) {
                    "Stop Recording"
                } else {
                    "Record Voice Note"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (audioFile != null && !isRecording) {
            OutlinedButton(
                onClick = {
                    playVoiceRecording()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Play Voice Note")
            }
        }

        if (selectedFileName != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (audioFile != null) {
                    "Voice Note"
                } else {
                    selectedFileName!!
                },
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (voiceTranscription.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Transcription",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = voiceTranscription,
                style = MaterialTheme.typography.bodyMedium
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
                                    fileBytes = fileBytes,
                                    transcription = voiceTranscription.ifBlank { null }
                                )

                                if (uploadResult.error.isNotEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        showError(context, uploadResult.error)
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