package net.koalastuff.music.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.koalastuff.music.core.playback.PlaybackController
import net.koalastuff.music.core.playback.PlaybackUiState
import net.koalastuff.music.core.playback.QueueItemUiState

@Composable
fun PlayerScreen(state: PlaybackUiState, controller: PlaybackController, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text(stringResource(R.string.player_back))
        }
        Text(
            state.title ?: stringResource(R.string.nothing_playing),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            state.artist.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.error?.let { error ->
            Text(
                text = stringResource(R.string.playback_error, error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Slider(
            value = state.positionMs.toFloat().coerceIn(
                0f,
                state.durationMs.coerceAtLeast(1).toFloat()
            ),
            onValueChangeFinished = {},
            onValueChange = { controller.seekTo(it.toLong()) },
            valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            enabled = state.durationMs > 0
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(state.positionMs))
            Text(formatDuration(state.durationMs))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = controller::previous) { Text(stringResource(R.string.previous)) }
            Button(onClick = controller::togglePlayPause) {
                Text(stringResource(if (state.isPlaying) R.string.pause else R.string.play))
            }
            TextButton(onClick = controller::next) { Text(stringResource(R.string.next)) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { controller.setShuffle(!state.shuffleEnabled) }) {
                Text(
                    stringResource(
                        if (state.shuffleEnabled) R.string.shuffle_on else R.string.shuffle_off
                    )
                )
            }
            TextButton(onClick = controller::cycleRepeatMode) {
                Text(
                    stringResource(
                        when (state.repeatMode) {
                            PlaybackController.REPEAT_ALL -> R.string.repeat_all
                            PlaybackController.REPEAT_ONE -> R.string.repeat_one
                            else -> R.string.repeat_off
                        }
                    )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.queue_title, state.queue.size),
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(
                onClick = controller::clearQueue,
                enabled = state.queue.isNotEmpty()
            ) {
                Text(stringResource(R.string.clear_queue))
            }
        }
        if (state.queue.isEmpty()) {
            Text(
                stringResource(R.string.queue_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = state.queue,
                    key = { index, item -> "${item.mediaId}:$index" }
                ) { index, item ->
                    QueueRow(
                        item = item,
                        index = index,
                        itemCount = state.queue.size,
                        onPlay = controller::playAt,
                        onMove = controller::moveQueueItem,
                        onRemove = controller::removeFromQueue
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: QueueItemUiState,
    index: Int,
    itemCount: Int,
    onPlay: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { onPlay(index) },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (item.isCurrent) {
                        stringResource(R.string.now_playing, item.title)
                    } else {
                        item.title
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.artist?.takeIf(String::isNotBlank)?.let { artist ->
                    Text(
                        artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Box {
            TextButton(onClick = { menuExpanded = true }) {
                Text(stringResource(R.string.queue_actions))
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_up)) },
                    enabled = index > 0,
                    onClick = {
                        menuExpanded = false
                        onMove(index, index - 1)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_down)) },
                    enabled = index < itemCount - 1,
                    onClick = {
                        menuExpanded = false
                        onMove(index, index + 1)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.remove_from_queue)) },
                    onClick = {
                        menuExpanded = false
                        onRemove(index)
                    }
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
