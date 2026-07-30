package net.koalastuff.music.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.ui.AlbumRow

@Composable
fun HomeScreen(profileName: String, albums: List<Album>, onAlbumClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    stringResource(R.string.home_greeting, profileName),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    stringResource(R.string.recently_added),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }
        }
        if (albums.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_empty),
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(albums.take(20), key = { "${it.profileId.value}:${it.remoteId}" }) { album ->
                AlbumRow(album, onClick = { onAlbumClick(album.remoteId) })
            }
        }
    }
}
