package net.koalastuff.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.koalastuff.music.core.data.MusicRepository
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.Track
import net.koalastuff.music.core.playback.PlaybackController

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playback: PlaybackController
) : ViewModel() {
    private val albumId = MutableStateFlow<String?>(null)

    val album: StateFlow<Album?> = albumId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.album(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tracks: StateFlow<List<Track>> = albumId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.albumTracks(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(id: String) {
        if (albumId.value == id) return
        albumId.value = id
        viewModelScope.launch { runCatching { repository.refreshAlbum(id) } }
    }

    fun play(track: Track) = playback.play(track)
    fun playAll() = playback.playAll(tracks.value)
    fun playNext(track: Track) = playback.playNext(track)
    fun addToQueue(track: Track) = playback.addToQueue(track)
}
