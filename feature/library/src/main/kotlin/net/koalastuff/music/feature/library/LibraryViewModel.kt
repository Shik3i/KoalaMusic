package net.koalastuff.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.koalastuff.music.core.data.MusicRepository
import net.koalastuff.music.core.model.Album

@HiltViewModel
class LibraryViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {
    val albums: StateFlow<List<Album>> = repository.albums.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refreshLibrary() } }
    }
}
