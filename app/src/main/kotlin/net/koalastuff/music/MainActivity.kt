package net.koalastuff.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import net.koalastuff.music.core.designsystem.KoalaMusicTheme
import net.koalastuff.music.core.playback.PlaybackController
import net.koalastuff.music.core.playback.PlaybackUiState
import net.koalastuff.music.feature.home.HomeScreen
import net.koalastuff.music.feature.library.AlbumRoute
import net.koalastuff.music.feature.library.LibraryRoute
import net.koalastuff.music.feature.player.PlayerScreen
import net.koalastuff.music.feature.search.SearchScreen
import net.koalastuff.music.feature.settings.SettingsScreen
import net.koalastuff.music.feature.setup.SetupRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoalaMusicTheme {
                KoalaMusicApp()
            }
        }
    }
}

private data object HomeKey : NavKey
private data object SearchKey : NavKey
private data object LibraryKey : NavKey
private data object PlayerKey : NavKey
private data object SettingsKey : NavKey
private data class AlbumKey(val id: String) : NavKey

@Composable
private fun KoalaMusicApp(viewModel: AppViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    if (profile == null) {
        SetupRoute(onCompleted = {})
        return
    }
    val currentProfile = requireNotNull(profile)
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val playbackState by viewModel.playback.state.collectAsStateWithLifecycle()
    val backStack = remember(currentProfile.id) { NavBackStack<NavKey>(HomeKey) }

    LaunchedEffect(currentProfile.id) { viewModel.loadCapabilities() }

    Scaffold(
        topBar = {
            if (backStack.lastOrNull() in setOf(HomeKey, SearchKey, LibraryKey)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { backStack.add(SettingsKey) }) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
        },
        bottomBar = {
            if (backStack.lastOrNull() in setOf(HomeKey, SearchKey, LibraryKey)) {
                Column {
                    if (playbackState.hasQueue) {
                        MiniPlayer(
                            state = playbackState,
                            controller = viewModel.playback,
                            onOpen = { backStack.add(PlayerKey) }
                        )
                    }
                    PrimaryNavigation(
                        selected = backStack.lastOrNull() ?: HomeKey,
                        onSelect = { destination ->
                            backStack.clear()
                            backStack.add(destination)
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryProvider = { key ->
                NavEntry(key) {
                    when (key) {
                        HomeKey -> HomeScreen(
                            profileName = currentProfile.displayName,
                            albums = albums,
                            onAlbumClick = { backStack.add(AlbumKey(it)) }
                        )
                        SearchKey -> SearchScreen(
                            query = query,
                            results = results,
                            onQueryChange = { viewModel.query.value = it },
                            onAlbumClick = { backStack.add(AlbumKey(it)) }
                        )
                        LibraryKey -> LibraryRoute(
                            onAlbumClick = { backStack.add(AlbumKey(it)) }
                        )
                        is AlbumKey -> AlbumRoute(
                            albumId = key.id,
                            onBack = { backStack.removeAt(backStack.lastIndex) }
                        )
                        PlayerKey -> PlayerScreen(
                            state = playbackState,
                            controller = viewModel.playback,
                            onBack = { backStack.removeAt(backStack.lastIndex) }
                        )
                        SettingsKey -> SettingsScreen(
                            profile = currentProfile,
                            capabilities = capabilities,
                            onBack = { backStack.removeAt(backStack.lastIndex) },
                            onRefresh = viewModel::refresh,
                            onRemoveProfile = viewModel::removeProfile
                        )
                        else -> Unit
                    }
                }
            }
        )
    }
}

@Composable
private fun PrimaryNavigation(selected: NavKey, onSelect: (NavKey) -> Unit) {
    NavigationBar {
        listOf(
            Triple(HomeKey, R.string.home, R.string.home_symbol),
            Triple(SearchKey, R.string.search, R.string.search_symbol),
            Triple(LibraryKey, R.string.library, R.string.library_symbol)
        ).forEach { (key, label, symbol) ->
            NavigationBarItem(
                selected = selected == key,
                onClick = { onSelect(key) },
                icon = { Text(stringResource(symbol)) },
                label = { Text(stringResource(label)) }
            )
        }
    }
}

@Composable
private fun MiniPlayer(state: PlaybackUiState, controller: PlaybackController, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(state.title ?: stringResource(R.string.nothing_playing), maxLines = 1)
            Text(
                state.artist.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Button(onClick = controller::togglePlayPause) {
            Text(stringResource(if (state.isPlaying) R.string.pause else R.string.play))
        }
    }
}
