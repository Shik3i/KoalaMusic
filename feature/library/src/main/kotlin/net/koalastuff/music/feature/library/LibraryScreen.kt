package net.koalastuff.music.feature.library

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.Track
import net.koalastuff.music.core.ui.AlbumRow

@Composable
fun LibraryRoute(onAlbumClick: (String) -> Unit, viewModel: LibraryViewModel = hiltViewModel()) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    LibraryScreen(albums, onAlbumClick, viewModel::refresh)
}

@Composable
fun LibraryScreen(albums: List<Album>, onAlbumClick: (String) -> Unit, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.library_title),
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onRefresh) { Text(stringResource(R.string.refresh)) }
        }
        if (albums.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.library_empty))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onRefresh) { Text(stringResource(R.string.try_again)) }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(albums, key = { "${it.profileId.value}:${it.remoteId}" }) { album ->
                    AlbumRow(album = album, onClick = { onAlbumClick(album.remoteId) })
                }
            }
        }
    }
}

@Composable
fun AlbumRoute(albumId: String, onBack: () -> Unit, viewModel: AlbumViewModel = hiltViewModel()) {
    val album by viewModel.album.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    LaunchedEffect(albumId) { viewModel.load(albumId) }
    AlbumScreen(
        album = album,
        tracks = tracks,
        onBack = onBack,
        onPlayAll = viewModel::playAll,
        onTrackClick = viewModel::play,
        onPlayNext = viewModel::playNext,
        onAddToQueue = viewModel::addToQueue
    )
}

@Composable
private fun AlbumScreen(
    album: Album?,
    tracks: List<Track>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(stringResource(R.string.back))
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    album?.name ?: stringResource(R.string.loading_album),
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    album?.artistName.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onPlayAll, enabled = tracks.isNotEmpty()) {
                    Text(stringResource(R.string.play_album))
                }
            }
        }
        items(tracks, key = { it.remoteId }) { track ->
            TrackRow(track, onTrackClick, onPlayNext, onAddToQueue)
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
        }
        if (tracks.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.loading_tracks),
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    onTrackClick: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onTrackClick(track) }
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = track.trackNumber?.toString().orEmpty(),
            modifier = Modifier.padding(end = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                track.artistName.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Box {
            TextButton(onClick = { menuExpanded = true }) {
                Text(stringResource(R.string.track_actions))
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.play_next)) },
                    onClick = {
                        menuExpanded = false
                        onPlayNext(track)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_to_queue)) },
                    onClick = {
                        menuExpanded = false
                        onAddToQueue(track)
                    }
                )
            }
        }
    }
}
