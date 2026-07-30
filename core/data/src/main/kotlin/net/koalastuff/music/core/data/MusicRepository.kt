package net.koalastuff.music.core.data

import kotlinx.coroutines.flow.Flow
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.ServerCapabilities
import net.koalastuff.music.core.model.ServerProfile
import net.koalastuff.music.core.model.SyncStatus
import net.koalastuff.music.core.model.Track

enum class SetupCredentialKind {
    PASSWORD,
    API_KEY
}

data class ServerSetupRequest(
    val serverUrl: String,
    val displayName: String,
    val username: String,
    val secret: String,
    val credentialKind: SetupCredentialKind,
    val allowInsecureHttp: Boolean
)

interface MusicRepository {
    val activeProfile: Flow<ServerProfile?>
    val albums: Flow<List<Album>>
    val syncStatus: Flow<SyncStatus>

    suspend fun connectAndSync(request: ServerSetupRequest): ServerProfile
    suspend fun refreshLibrary()
    suspend fun refreshAlbum(albumId: String)
    fun album(albumId: String): Flow<Album?>
    fun albumTracks(albumId: String): Flow<List<Track>>
    fun searchAlbums(query: String): Flow<List<Album>>
    suspend fun capabilities(): ServerCapabilities?
    suspend fun removeActiveProfile()
}
