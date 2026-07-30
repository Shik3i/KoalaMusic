package net.koalastuff.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ServerProfileEntity::class,
        ServerCapabilityEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        GenreEntity::class,
        FavoriteEntity::class,
        RatingEntity::class,
        PlaybackHistoryEntity::class,
        PendingScrobbleEntity::class,
        QueueEntity::class,
        QueueItemEntity::class,
        SyncStateEntity::class,
        ArtworkCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class KoalaMusicDatabase : RoomDatabase() {
    abstract fun serverProfileDao(): ServerProfileDao
    abstract fun capabilityDao(): CapabilityDao
    abstract fun libraryDao(): LibraryDao
    abstract fun queueDao(): QueueDao
    abstract fun syncStateDao(): SyncStateDao
}
