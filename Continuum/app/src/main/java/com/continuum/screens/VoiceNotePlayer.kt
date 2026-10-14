
package com.continuum.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.PrimaryText
import kotlinx.coroutines.delay

@Composable
fun VoiceNotePlayer(
    audioUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val player = remember(context, audioUrl) {
        ExoPlayer.Builder(context).build()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var playbackError by remember { mutableStateOf(false) }

    DisposableEffect(player, audioUrl) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = player.duration.coerceAtLeast(0L)
                }
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    player.seekTo(0L)
                }
            }

            override fun onPlayerError(
                error: androidx.media3.common.PlaybackException
            ) {
                playbackError = true
            }
        }

        player.addListener(listener)
        player.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
        player.prepare()

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            currentPosition = player.currentPosition.coerceAtLeast(0L)
            delay(250L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Voice Note",
            color = PrimaryText,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    player.seekTo(
                        (player.currentPosition - 10_000L)
                            .coerceAtLeast(0L)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Rewind 10 seconds",
                    tint = BluePrimary
                )
            }

            IconButton(
                onClick = {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = BluePrimary
                )
            }

            IconButton(
                onClick = {
                    player.seekTo(
                        (player.currentPosition + 10_000L)
                            .coerceAtMost(duration)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Forward 10 seconds",
                    tint = BluePrimary
                )
            }
        }

        Slider(
            value = currentPosition.toFloat()
                .coerceIn(0f, duration.coerceAtLeast(1L).toFloat()),
            onValueChange = { value ->
                player.seekTo(value.toLong())
                currentPosition = value.toLong()
            },
            valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            enabled = duration > 0L
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatAudioTime(currentPosition), color = MutedText)
            Text(formatAudioTime(duration), color = MutedText)
        }

        if (playbackError) {
            Text(
                text = "Unable to play this recording.",
                color = androidx.compose.ui.graphics.Color.Red
            )
        }
    }
}

private fun formatAudioTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
