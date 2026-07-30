package net.koalastuff.music.core.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.koalastuff.music.core.database.QueueItemEntity
import net.koalastuff.music.core.model.Track

internal fun Track.asMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId("${profileId.value}|$remoteId")
    .setUri(
        Uri.Builder()
            .scheme(OPAQUE_SCHEME)
            .authority("stream")
            .appendPath(profileId.value)
            .appendPath(remoteId)
            .build()
    )
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .setIsPlayable(true)
            .build()
    )
    .build()

internal fun QueueItemEntity.asMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId("$profileId|$trackId")
    .setUri(
        Uri.Builder()
            .scheme(OPAQUE_SCHEME)
            .authority("stream")
            .appendPath(profileId)
            .appendPath(trackId)
            .build()
    )
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .setIsPlayable(true)
            .build()
    )
    .build()

internal const val OPAQUE_SCHEME = "koalamusic"
