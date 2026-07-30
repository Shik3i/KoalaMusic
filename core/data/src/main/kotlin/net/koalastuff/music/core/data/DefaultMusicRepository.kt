package net.koalastuff.music.core.data

import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException
import net.koalastuff.music.core.database.AlbumEntity
import net.koalastuff.music.core.database.CapabilityDao
import net.koalastuff.music.core.database.LibraryDao
import net.koalastuff.music.core.database.ServerCapabilityEntity
import net.koalastuff.music.core.database.ServerProfileDao
import net.koalastuff.music.core.database.ServerProfileEntity
import net.koalastuff.music.core.database.SyncStateDao
import net.koalastuff.music.core.database.SyncStateEntity
import net.koalastuff.music.core.database.asEntity
import net.koalastuff.music.core.database.asModel
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.ServerCapabilities
import net.koalastuff.music.core.model.ServerCapability
import net.koalastuff.music.core.model.ServerProfile
import net.koalastuff.music.core.model.ServerProfileId
import net.koalastuff.music.core.model.SyncPhase
import net.koalastuff.music.core.model.SyncStatus
import net.koalastuff.music.core.model.Track
import net.koalastuff.music.core.opensubsonic.AuthMaterial
import net.koalastuff.music.core.opensubsonic.OpenSubsonicClient
import net.koalastuff.music.core.opensubsonic.OpenSubsonicClientFactory
import net.koalastuff.music.core.opensubsonic.ServerUrlPolicy
import net.koalastuff.music.core.security.CredentialVault
import net.koalastuff.music.core.security.StoredCredential

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMusicRepository @Inject constructor(
    private val profileDao: ServerProfileDao,
    private val capabilityDao: CapabilityDao,
    private val libraryDao: LibraryDao,
    private val syncStateDao: SyncStateDao,
    private val credentialVault: CredentialVault,
    private val clientFactory: OpenSubsonicClientFactory
) : MusicRepository {
    private val transientSync = MutableStateFlow(SyncStatus(null, SyncPhase.IDLE))

    override val activeProfile: Flow<ServerProfile?> =
        profileDao.observeActive().map { it?.asModel() }

    override val albums: Flow<List<Album>> = profileDao.observeActive()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(emptyList())
            } else {
                libraryDao.observeNewestAlbums(profile.id).map { rows ->
                    rows.map(AlbumEntity::asModel)
                }
            }
        }

    override val syncStatus: Flow<SyncStatus> = transientSync

    override suspend fun connectAndSync(request: ServerSetupRequest): ServerProfile {
        val profileId = ServerProfileId(UUID.randomUUID().toString())
        updateStatus(profileId, SyncPhase.CONNECTING)
        val baseUrl = ServerUrlPolicy.normalize(request.serverUrl, request.allowInsecureHttp)
        if (request.secret.isBlank()) throw KoalaMusicException(AppError.AuthenticationFailed)
        val auth = when (request.credentialKind) {
            SetupCredentialKind.API_KEY -> AuthMaterial.ApiKey(request.secret)
            SetupCredentialKind.PASSWORD -> {
                if (request.username.isBlank()) {
                    throw KoalaMusicException(
                        AppError.AuthenticationFailed
                    )
                }
                AuthMaterial.Password(request.username.trim(), request.secret)
            }
        }
        val client = clientFactory.create(profileId, baseUrl, auth)

        try {
            val identity = client.ping()
            updateStatus(profileId, SyncPhase.CAPABILITIES)
            val capabilities = if (identity.openSubsonic) client.extensions() else emptyList()
            updateStatus(profileId, SyncPhase.LIBRARY)
            val generation = System.currentTimeMillis()
            val albums = client.newestAlbums(INITIAL_ALBUM_LIMIT)
            val now = Instant.now()
            val profile = ServerProfile(
                id = profileId,
                displayName = request.displayName.trim().ifBlank {
                    identity.type ?: baseUrl.host
                },
                baseUrl = baseUrl.toString(),
                username = (auth as? AuthMaterial.Password)?.username,
                allowInsecureHttp = request.allowInsecureHttp,
                serverType = identity.type,
                serverVersion = identity.serverVersion,
                apiVersion = identity.apiVersion,
                openSubsonic = identity.openSubsonic,
                isActive = true,
                lastConnectedAt = now
            )

            credentialVault.put(profileId.value, auth.asStoredCredential())
            try {
                profileDao.activate(profile.asEntity())
                capabilityDao.replace(
                    profileId.value,
                    capabilities.flatMap { capability ->
                        capability.versions.map { version ->
                            ServerCapabilityEntity(profileId.value, capability.name, version)
                        }
                    }
                )
                // This bounded first page is not a complete library enumeration,
                // therefore it must never trigger deletion of older local rows.
                libraryDao.replaceAlbums(
                    profileId = profileId.value,
                    generation = generation,
                    albums = albums.map { it.asEntity(generation) },
                    completeEnumeration = false
                )
            } catch (failure: Exception) {
                credentialVault.delete(profileId.value)
                throw failure
            }
            updateStatus(profileId, SyncPhase.COMPLETE, albums.size, albums.size)
            return profile
        } catch (failure: CancellationException) {
            updateStatus(profileId, SyncPhase.FAILED, errorCode = AppError.SyncInterrupted.code)
            throw failure
        } catch (failure: KoalaMusicException) {
            updateStatus(profileId, SyncPhase.FAILED, errorCode = failure.error.code)
            throw failure
        } catch (failure: Exception) {
            updateStatus(profileId, SyncPhase.FAILED, errorCode = AppError.ServerFailure.code)
            throw KoalaMusicException(AppError.ServerFailure, failure)
        }
    }

    override suspend fun refreshLibrary() {
        val profile = profileDao.active() ?: return
        val id = ServerProfileId(profile.id)
        val client = clientFor(profile)
        updateStatus(id, SyncPhase.LIBRARY)
        try {
            val generation = System.currentTimeMillis()
            val albums = client.newestAlbums(INITIAL_ALBUM_LIMIT)
            libraryDao.replaceAlbums(
                profileId = profile.id,
                generation = generation,
                albums = albums.map { it.asEntity(generation) },
                completeEnumeration = false
            )
            updateStatus(id, SyncPhase.COMPLETE, albums.size, albums.size)
        } catch (failure: KoalaMusicException) {
            updateStatus(id, SyncPhase.FAILED, errorCode = failure.error.code)
            throw failure
        }
    }

    override suspend fun refreshAlbum(albumId: String) {
        val profile = profileDao.active() ?: return
        val details = clientFor(profile).album(albumId)
        val generation = System.currentTimeMillis()
        libraryDao.upsertAlbums(listOf(details.album.asEntity(generation)))
        libraryDao.replaceAlbumTracks(
            profileId = profile.id,
            albumId = albumId,
            tracks = details.tracks.map { it.asEntity(generation) }
        )
    }

    override fun album(albumId: String): Flow<Album?> = profileDao.observeActive()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(null)
            } else {
                libraryDao.observeAlbum(profile.id, albumId).map { it?.asModel() }
            }
        }

    override fun albumTracks(albumId: String): Flow<List<Track>> = profileDao.observeActive()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(emptyList())
            } else {
                libraryDao.observeAlbumTracks(profile.id, albumId).map { rows ->
                    rows.map { it.asModel() }
                }
            }
        }

    override fun searchAlbums(query: String): Flow<List<Album>> = profileDao.observeActive()
        .flatMapLatest { profile ->
            if (profile == null || query.isBlank()) {
                flowOf(emptyList())
            } else {
                libraryDao.searchAlbums(profile.id, query.trim().lowercase(Locale.ROOT))
                    .map { rows -> rows.map { it.asModel() } }
            }
        }

    override suspend fun capabilities(): ServerCapabilities? {
        val profile = profileDao.active() ?: return null
        val grouped = capabilityDao.forProfile(profile.id)
            .groupBy(ServerCapabilityEntity::name)
            .map { (name, rows) -> ServerCapability(name, rows.map { it.version }.toSet()) }
        return ServerCapabilities(ServerProfileId(profile.id), grouped)
    }

    override suspend fun removeActiveProfile() {
        val profile = profileDao.active() ?: return
        profileDao.delete(profile.id)
        credentialVault.delete(profile.id)
        transientSync.value = SyncStatus(null, SyncPhase.IDLE)
    }

    private suspend fun clientFor(profile: ServerProfileEntity): OpenSubsonicClient {
        val stored = credentialVault.get(profile.id)
            ?: throw KoalaMusicException(AppError.CredentialUnavailable)
        val url = ServerUrlPolicy.normalize(profile.baseUrl, profile.allowInsecureHttp)
        return clientFactory.create(ServerProfileId(profile.id), url, stored.asAuthMaterial())
    }

    private suspend fun updateStatus(
        profileId: ServerProfileId,
        phase: SyncPhase,
        completed: Int = 0,
        total: Int? = null,
        errorCode: String? = null
    ) {
        val status = SyncStatus(profileId, phase, completed, total, errorCode)
        transientSync.value = status
        syncStateDao.upsert(
            SyncStateEntity(
                profileId = profileId.value,
                phase = phase.name,
                completed = completed,
                total = total,
                errorCode = errorCode,
                generation = System.currentTimeMillis(),
                updatedAtEpochMs = status.updatedAt.toEpochMilli()
            )
        )
    }

    private fun AuthMaterial.asStoredCredential(): StoredCredential = when (this) {
        is AuthMaterial.ApiKey -> StoredCredential.ApiKey(value)
        is AuthMaterial.Password -> StoredCredential.Password(username, value)
    }

    private fun StoredCredential.asAuthMaterial(): AuthMaterial = when (this) {
        is StoredCredential.ApiKey -> AuthMaterial.ApiKey(value)
        is StoredCredential.Password -> AuthMaterial.Password(username, value)
    }

    private fun ServerProfile.asEntity() = ServerProfileEntity(
        id = id.value,
        displayName = displayName,
        baseUrl = baseUrl,
        username = username,
        allowInsecureHttp = allowInsecureHttp,
        serverType = serverType,
        serverVersion = serverVersion,
        apiVersion = apiVersion,
        openSubsonic = openSubsonic,
        isActive = isActive,
        lastConnectedAtEpochMs = lastConnectedAt?.toEpochMilli()
    )

    companion object {
        private const val INITIAL_ALBUM_LIMIT = 100
    }
}
