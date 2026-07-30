package net.koalastuff.music.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "server_profiles")
data class ServerProfileEntity(
    @androidx.room.PrimaryKey val id: String,
    val displayName: String,
    val baseUrl: String,
    val username: String?,
    val allowInsecureHttp: Boolean,
    val serverType: String?,
    val serverVersion: String?,
    val apiVersion: String?,
    val openSubsonic: Boolean,
    val isActive: Boolean,
    val lastConnectedAtEpochMs: Long?
)

@Entity(
    tableName = "server_capabilities",
    primaryKeys = ["profileId", "name", "version"],
    foreignKeys = [
        ForeignKey(
            entity = ServerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class ServerCapabilityEntity(val profileId: String, val name: String, val version: Int)

@Entity(
    tableName = "artists",
    primaryKeys = ["profileId", "remoteId"],
    indices = [Index("profileId"), Index(value = ["profileId", "sortName"])]
)
data class ArtistEntity(
    val profileId: String,
    val remoteId: String,
    val name: String,
    val sortName: String,
    val albumCount: Int?,
    val coverArtId: String?,
    val starredAtEpochMs: Long?,
    val syncGeneration: Long
)

@Entity(
    tableName = "albums",
    primaryKeys = ["profileId", "remoteId"],
    indices = [
        Index("profileId"),
        Index(value = ["profileId", "sortName"]),
        Index(value = ["profileId", "artistId"]),
        Index(value = ["profileId", "createdAtEpochMs"])
    ]
)
data class AlbumEntity(
    val profileId: String,
    val remoteId: String,
    val name: String,
    val sortName: String,
    val artistId: String?,
    val artistName: String?,
    val coverArtId: String?,
    val songCount: Int,
    val durationSeconds: Long?,
    val year: Int?,
    val genre: String?,
    val createdAtEpochMs: Long?,
    val starredAtEpochMs: Long?,
    val syncGeneration: Long
)

@Entity(
    tableName = "tracks",
    primaryKeys = ["profileId", "remoteId"],
    indices = [
        Index("profileId"),
        Index(value = ["profileId", "sortTitle"]),
        Index(value = ["profileId", "albumId"]),
        Index(value = ["profileId", "artistId"])
    ]
)
data class TrackEntity(
    val profileId: String,
    val remoteId: String,
    val title: String,
    val sortTitle: String,
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
    val starredAtEpochMs: Long?,
    val syncGeneration: Long
)

@Entity(
    tableName = "playlists",
    primaryKeys = ["profileId", "remoteId"],
    indices = [Index("profileId"), Index(value = ["profileId", "sortName"])]
)
data class PlaylistEntity(
    val profileId: String,
    val remoteId: String,
    val name: String,
    val sortName: String,
    val songCount: Int,
    val durationSeconds: Long?,
    val coverArtId: String?,
    val changedAtEpochMs: Long?,
    val syncGeneration: Long
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["profileId", "playlistId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["profileId", "remoteId"],
            childColumns = ["profileId", "playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["profileId", "remoteId"],
            childColumns = ["profileId", "trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId", "playlistId"]),
        Index(value = ["profileId", "trackId"])
    ]
)
data class PlaylistTrackEntity(
    val profileId: String,
    val playlistId: String,
    val position: Int,
    val trackId: String
)

@Entity(
    tableName = "genres",
    primaryKeys = ["profileId", "name"],
    indices = [Index("profileId")]
)
data class GenreEntity(
    val profileId: String,
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val syncGeneration: Long
)

@Entity(
    tableName = "favorites",
    primaryKeys = ["profileId", "entityType", "remoteId"],
    indices = [Index(value = ["profileId", "starredAtEpochMs"])]
)
data class FavoriteEntity(
    val profileId: String,
    val entityType: String,
    val remoteId: String,
    val starredAtEpochMs: Long,
    val pending: Boolean
)

@Entity(
    tableName = "ratings",
    primaryKeys = ["profileId", "entityType", "remoteId"],
    indices = [Index("profileId")]
)
data class RatingEntity(
    val profileId: String,
    val entityType: String,
    val remoteId: String,
    val rating: Int,
    val pending: Boolean
)

@Entity(
    tableName = "playback_history",
    indices = [
        Index(value = ["profileId", "playedAtEpochMs"]),
        Index(value = ["profileId", "trackId"])
    ]
)
data class PlaybackHistoryEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val trackId: String,
    val playedAtEpochMs: Long,
    val completed: Boolean,
    val durationPlayedMs: Long
)

@Entity(
    tableName = "pending_scrobbles",
    indices = [Index(value = ["profileId", "nextAttemptAtEpochMs"])]
)
data class PendingScrobbleEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val trackId: String,
    val submission: Boolean,
    val playedAtEpochMs: Long,
    val attemptCount: Int,
    val nextAttemptAtEpochMs: Long
)

@Entity(tableName = "queues")
data class QueueEntity(
    @androidx.room.PrimaryKey val profileId: String,
    val currentIndex: Int,
    val currentPositionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "queue_items",
    primaryKeys = ["profileId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = QueueEntity::class,
            parentColumns = ["profileId"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "trackId"])]
)
data class QueueItemEntity(
    val profileId: String,
    val position: Int,
    val trackId: String,
    val title: String,
    val artistName: String?,
    val albumName: String?,
    val coverArtId: String?
)

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @androidx.room.PrimaryKey val profileId: String,
    val phase: String,
    val completed: Int,
    val total: Int?,
    val errorCode: String?,
    val generation: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "artwork_cache",
    primaryKeys = ["profileId", "artworkId"],
    indices = [Index("lastAccessedAtEpochMs")]
)
data class ArtworkCacheEntity(
    val profileId: String,
    val artworkId: String,
    val cacheKey: String,
    val byteSize: Long,
    val etag: String?,
    val lastModified: String?,
    val lastAccessedAtEpochMs: Long
)
