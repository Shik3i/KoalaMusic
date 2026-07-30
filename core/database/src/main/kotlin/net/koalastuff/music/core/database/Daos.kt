package net.koalastuff.music.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerProfileDao {
    @Query("SELECT * FROM server_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ServerProfileEntity?>

    @Query("SELECT * FROM server_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun active(): ServerProfileEntity?

    @Query("SELECT * FROM server_profiles WHERE id = :profileId")
    suspend fun byId(profileId: String): ServerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ServerProfileEntity)

    @Query("UPDATE server_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM server_profiles WHERE id = :profileId")
    suspend fun delete(profileId: String)

    @Transaction
    suspend fun activate(profile: ServerProfileEntity) {
        deactivateAll()
        upsert(profile.copy(isActive = true))
    }
}

@Dao
interface CapabilityDao {
    @Query("SELECT * FROM server_capabilities WHERE profileId = :profileId")
    suspend fun forProfile(profileId: String): List<ServerCapabilityEntity>

    @Query("DELETE FROM server_capabilities WHERE profileId = :profileId")
    suspend fun deleteForProfile(profileId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<ServerCapabilityEntity>)

    @Transaction
    suspend fun replace(profileId: String, values: List<ServerCapabilityEntity>) {
        deleteForProfile(profileId)
        insert(values)
    }
}

@Dao
interface LibraryDao {
    @Query(
        "SELECT * FROM albums WHERE profileId = :profileId ORDER BY createdAtEpochMs DESC, sortName LIMIT :limit"
    )
    fun observeNewestAlbums(profileId: String, limit: Int = 100): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE profileId = :profileId AND remoteId = :albumId")
    fun observeAlbum(profileId: String, albumId: String): Flow<AlbumEntity?>

    @Query(
        """
        SELECT * FROM tracks
        WHERE profileId = :profileId AND albumId = :albumId
        ORDER BY COALESCE(discNumber, 1), COALESCE(trackNumber, 2147483647), sortTitle
        """
    )
    fun observeAlbumTracks(profileId: String, albumId: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE profileId = :profileId AND remoteId = :trackId")
    suspend fun track(profileId: String, trackId: String): TrackEntity?

    @Query(
        """
        SELECT * FROM albums
        WHERE profileId = :profileId
          AND (sortName LIKE '%' || :normalizedQuery || '%'
               OR lower(COALESCE(artistName, '')) LIKE '%' || :normalizedQuery || '%')
        ORDER BY sortName
        LIMIT :limit
        """
    )
    fun searchAlbums(
        profileId: String,
        normalizedQuery: String,
        limit: Int = 50
    ): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM albums WHERE profileId = :profileId AND syncGeneration < :generation")
    suspend fun deleteStaleAlbums(profileId: String, generation: Long)

    @Query("DELETE FROM tracks WHERE profileId = :profileId AND albumId = :albumId")
    suspend fun deleteAlbumTracks(profileId: String, albumId: String)

    @Transaction
    suspend fun replaceAlbums(
        profileId: String,
        generation: Long,
        albums: List<AlbumEntity>,
        completeEnumeration: Boolean
    ) {
        upsertAlbums(albums)
        if (completeEnumeration) deleteStaleAlbums(profileId, generation)
    }

    @Transaction
    suspend fun replaceAlbumTracks(profileId: String, albumId: String, tracks: List<TrackEntity>) {
        deleteAlbumTracks(profileId, albumId)
        upsertTracks(tracks)
    }
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queues WHERE profileId = :profileId")
    suspend fun queue(profileId: String): QueueEntity?

    @Query("SELECT * FROM queue_items WHERE profileId = :profileId ORDER BY position")
    suspend fun items(profileId: String): List<QueueItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQueue(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items WHERE profileId = :profileId")
    suspend fun clearItems(profileId: String)

    @Query("DELETE FROM queues WHERE profileId = :profileId")
    suspend fun clear(profileId: String)

    @Transaction
    suspend fun replace(queue: QueueEntity, items: List<QueueItemEntity>) {
        upsertQueue(queue)
        clearItems(queue.profileId)
        insertItems(items)
    }
}

@Dao
interface SyncStateDao {
    @Query("SELECT * FROM sync_state WHERE profileId = :profileId")
    fun observe(profileId: String): Flow<SyncStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)
}
