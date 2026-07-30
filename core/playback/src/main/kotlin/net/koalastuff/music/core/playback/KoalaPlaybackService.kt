package net.koalastuff.music.core.playback

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.koalastuff.music.core.database.QueueDao
import net.koalastuff.music.core.database.QueueEntity
import net.koalastuff.music.core.database.QueueItemEntity
import net.koalastuff.music.core.database.ServerProfileDao
import okhttp3.OkHttpClient

@AndroidEntryPoint
class KoalaPlaybackService : MediaLibraryService() {
    @Inject lateinit var httpClient: OkHttpClient

    @Inject lateinit var resolver: AuthenticatedDataSpecResolver

    @Inject lateinit var queueDao: QueueDao

    @Inject lateinit var profileDao: ServerProfileDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibraryService.MediaLibrarySession
    private var persistJob: Job? = null
    private var positionPersistJob: Job? = null

    @OptIn(markerClass = [UnstableApi::class])
    override fun onCreate() {
        super.onCreate()
        val upstream = OkHttpDataSource.Factory(httpClient)
        val resolving = ResolvingDataSource.Factory(upstream, resolver)
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(resolving)
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaLibraryService.MediaLibrarySession.Builder(
            this,
            player,
            object : MediaLibraryService.MediaLibrarySession.Callback {}
        ).build()

        player.addListener(
            object : Player.Listener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) = schedulePersist()
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) =
                    schedulePersist()
                override fun onRepeatModeChanged(repeatMode: Int) = schedulePersist()
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) =
                    schedulePersist()

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    schedulePersist()
                    updatePositionPersistence(isPlaying)
                }
            }
        )
        restoreQueue()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaLibraryService.MediaLibrarySession = mediaSession

    override fun onDestroy() {
        positionPersistJob?.cancel()
        serviceScope.cancel()
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    private fun restoreQueue() {
        serviceScope.launch {
            val profile = withContext(Dispatchers.IO) { profileDao.active() } ?: return@launch
            val queue = withContext(Dispatchers.IO) { queueDao.queue(profile.id) } ?: return@launch
            val items = withContext(Dispatchers.IO) { queueDao.items(profile.id) }
            if (items.isEmpty()) return@launch
            player.repeatMode = queue.repeatMode
            player.shuffleModeEnabled = queue.shuffleEnabled
            player.setMediaItems(
                items.map(QueueItemEntity::asMediaItem),
                queue.currentIndex.coerceIn(0, items.lastIndex),
                queue.currentPositionMs.coerceAtLeast(0)
            )
            player.prepare()
        }
    }

    private fun schedulePersist() {
        if (!::player.isInitialized) return
        persistJob?.cancel()
        persistJob = serviceScope.launch {
            delay(300)
            if (player.mediaItemCount == 0) {
                val profile = withContext(Dispatchers.IO) { profileDao.active() } ?: return@launch
                withContext(Dispatchers.IO) { queueDao.clear(profile.id) }
                return@launch
            }
            persistQueue()
        }
    }

    private fun updatePositionPersistence(isPlaying: Boolean) {
        positionPersistJob?.cancel()
        if (!isPlaying) return
        positionPersistJob = serviceScope.launch {
            while (true) {
                delay(POSITION_PERSIST_INTERVAL_MS)
                persistQueue()
            }
        }
    }

    private suspend fun persistQueue() {
        if (!::player.isInitialized || player.mediaItemCount == 0) return
        val parsed = (0 until player.mediaItemCount).mapNotNull { index ->
            val item = player.getMediaItemAt(index)
            val parts = item.mediaId.split('|', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            QueueItemEntity(
                profileId = parts[0],
                position = index,
                trackId = parts[1],
                title = item.mediaMetadata.title?.toString().orEmpty(),
                artistName = item.mediaMetadata.artist?.toString(),
                albumName = item.mediaMetadata.albumTitle?.toString(),
                coverArtId = null
            )
        }
        val profileId = parsed.firstOrNull()?.profileId ?: return
        val queue = QueueEntity(
            profileId = profileId,
            currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
            currentPositionMs = player.currentPosition.coerceAtLeast(0),
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) { queueDao.replace(queue, parsed) }
    }

    private companion object {
        const val POSITION_PERSIST_INTERVAL_MS = 15_000L
    }
}
