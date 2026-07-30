package net.koalastuff.music.core.model

import java.time.Instant

@JvmInline
value class ServerProfileId(val value: String)

data class ServerProfile(
    val id: ServerProfileId,
    val displayName: String,
    val baseUrl: String,
    val username: String?,
    val allowInsecureHttp: Boolean,
    val serverType: String?,
    val serverVersion: String?,
    val apiVersion: String?,
    val openSubsonic: Boolean,
    val isActive: Boolean,
    val lastConnectedAt: Instant?
)

data class ServerCapability(val name: String, val versions: Set<Int>)

data class ServerCapabilities(val profileId: ServerProfileId, val values: List<ServerCapability>) {
    fun supports(name: String, minimumVersion: Int = 1): Boolean =
        values.any { it.name == name && it.versions.any { version -> version >= minimumVersion } }
}

data class Artist(
    val profileId: ServerProfileId,
    val remoteId: String,
    val name: String,
    val albumCount: Int?,
    val coverArtId: String?,
    val starredAt: Instant?
)

data class Album(
    val profileId: ServerProfileId,
    val remoteId: String,
    val name: String,
    val artistId: String?,
    val artistName: String?,
    val coverArtId: String?,
    val songCount: Int,
    val durationSeconds: Long?,
    val year: Int?,
    val genre: String?,
    val createdAt: Instant?,
    val starredAt: Instant?
)

data class Track(
    val profileId: ServerProfileId,
    val remoteId: String,
    val title: String,
    val albumId: String?,
    val albumName: String?,
    val artistId: String?,
    val artistName: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationSeconds: Long?,
    val contentType: String?,
    val suffix: String?,
    val coverArtId: String?,
    val starredAt: Instant?
)

data class Playlist(
    val profileId: ServerProfileId,
    val remoteId: String,
    val name: String,
    val songCount: Int,
    val durationSeconds: Long?,
    val coverArtId: String?,
    val changedAt: Instant?
)

data class Genre(
    val profileId: ServerProfileId,
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

enum class SyncPhase {
    IDLE,
    CONNECTING,
    CAPABILITIES,
    LIBRARY,
    COMPLETE,
    FAILED
}

data class SyncStatus(
    val profileId: ServerProfileId?,
    val phase: SyncPhase,
    val completed: Int = 0,
    val total: Int? = null,
    val errorCode: String? = null,
    val updatedAt: Instant = Instant.now()
)

data class QueueItem(val position: Int, val track: Track)
