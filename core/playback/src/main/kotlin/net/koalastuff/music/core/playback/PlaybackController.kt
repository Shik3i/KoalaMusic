package net.koalastuff.music.core.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.koalastuff.music.core.model.Track

data class QueueItemUiState(
    val mediaId: String,
    val title: String,
    val artist: String?,
    val isCurrent: Boolean
)

data class PlaybackUiState(
    val connected: Boolean = false,
    val hasQueue: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleEnabled: Boolean = false,
    val currentIndex: Int = 0,
    val queue: List<QueueItemUiState> = emptyList(),
    val error: String? = null
)

@Singleton
class PlaybackController @Inject constructor(@ApplicationContext context: Context) {
    private val appContext = context.applicationContext
    private val future = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, KoalaPlaybackService::class.java))
    ).buildAsync()
    private val mutableState = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = mutableState.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { controller ->
                        controller.addListener(
                            object : Player.Listener {
                                override fun onEvents(player: Player, events: Player.Events) {
                                    update(player)
                                }
                            }
                        )
                        update(controller)
                    }
                    .onFailure { failure ->
                        mutableState.value = PlaybackUiState(error = failure.javaClass.simpleName)
                    }
            },
            ContextCompat.getMainExecutor(appContext)
        )
        scope.launch {
            while (true) {
                delay(POSITION_UPDATE_INTERVAL_MS)
                if (future.isDone) {
                    runCatching { future.get() }.onSuccess(::update)
                }
            }
        }
    }

    fun play(track: Track) = withController { controller ->
        controller.setMediaItem(track.asMediaItem())
        controller.prepare()
        controller.play()
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0) = withController { controller ->
        if (tracks.isEmpty()) return@withController
        controller.setMediaItems(
            tracks.map(Track::asMediaItem),
            startIndex.coerceIn(0, tracks.lastIndex),
            0
        )
        controller.prepare()
        controller.play()
    }

    fun playNext(track: Track) = withController { controller ->
        controller.addMediaItem(
            QueueIndexPolicy.insertionIndexAfterCurrent(
                controller.mediaItemCount,
                controller.currentMediaItemIndex
            ),
            track.asMediaItem()
        )
    }

    fun addToQueue(track: Track) = withController { it.addMediaItem(track.asMediaItem()) }
    fun togglePlayPause() = withController { if (it.isPlaying) it.pause() else it.play() }
    fun next() = withController(MediaController::seekToNextMediaItem)
    fun previous() = withController(MediaController::seekToPreviousMediaItem)
    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs.coerceAtLeast(0)) }
    fun playAt(index: Int) = withController { controller ->
        if (!QueueIndexPolicy.validIndex(controller.mediaItemCount, index)) return@withController
        controller.seekToDefaultPosition(index)
        controller.play()
    }

    fun removeFromQueue(index: Int) = withController { controller ->
        if (QueueIndexPolicy.validIndex(controller.mediaItemCount, index)) {
            controller.removeMediaItem(index)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) = withController { controller ->
        QueueIndexPolicy.move(controller.mediaItemCount, fromIndex, toIndex)?.let { (from, to) ->
            controller.moveMediaItem(from, to)
        }
    }

    fun clearQueue() = withController { controller ->
        controller.stop()
        controller.clearMediaItems()
    }

    fun setShuffle(enabled: Boolean) = withController { it.shuffleModeEnabled = enabled }
    fun setRepeatMode(mode: Int) = withController { it.repeatMode = mode }
    fun cycleRepeatMode() = withController {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    companion object {
        const val REPEAT_OFF = Player.REPEAT_MODE_OFF
        const val REPEAT_ALL = Player.REPEAT_MODE_ALL
        const val REPEAT_ONE = Player.REPEAT_MODE_ONE
        private const val POSITION_UPDATE_INTERVAL_MS = 1_000L
    }

    private fun withController(block: (MediaController) -> Unit) {
        future.addListener(
            { runCatching { future.get() }.onSuccess(block) },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    private fun update(player: Player) {
        val currentIndex = player.currentMediaItemIndex.coerceAtLeast(0)
        mutableState.value = PlaybackUiState(
            connected = true,
            hasQueue = player.mediaItemCount > 0,
            isPlaying = player.isPlaying,
            title = player.currentMediaItem?.mediaMetadata?.title?.toString(),
            artist = player.currentMediaItem?.mediaMetadata?.artist?.toString(),
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it > 0 } ?: 0,
            repeatMode = player.repeatMode,
            shuffleEnabled = player.shuffleModeEnabled,
            currentIndex = currentIndex,
            queue = (0 until player.mediaItemCount).map { index ->
                val item = player.getMediaItemAt(index)
                QueueItemUiState(
                    mediaId = item.mediaId,
                    title = item.mediaMetadata.title?.toString().orEmpty(),
                    artist = item.mediaMetadata.artist?.toString(),
                    isCurrent = index == currentIndex
                )
            },
            error = player.playerError?.errorCodeName
        )
    }
}
