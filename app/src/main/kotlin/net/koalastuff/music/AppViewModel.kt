package net.koalastuff.music

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
import net.koalastuff.music.core.model.ServerCapabilities
import net.koalastuff.music.core.model.ServerProfile
import net.koalastuff.music.core.playback.PlaybackController

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: MusicRepository,
    val playback: PlaybackController
) : ViewModel() {
    val profile: StateFlow<ServerProfile?> = repository.activeProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )
    val albums: StateFlow<List<Album>> = repository.albums.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val query = MutableStateFlow("")
    val searchResults: StateFlow<List<Album>> = query.flatMapLatest { value ->
        if (value.isBlank()) flowOf(emptyList()) else repository.searchAlbums(value)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val capabilities = MutableStateFlow<ServerCapabilities?>(null)

    fun loadCapabilities() {
        viewModelScope.launch { capabilities.value = repository.capabilities() }
    }

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refreshLibrary() } }
    }

    fun removeProfile() {
        viewModelScope.launch { repository.removeActiveProfile() }
    }
}
