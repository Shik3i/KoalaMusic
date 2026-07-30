package net.koalastuff.music.core.database

import java.time.Instant
import java.util.Locale
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.ServerProfile
import net.koalastuff.music.core.model.ServerProfileId
import net.koalastuff.music.core.model.Track

fun ServerProfileEntity.asModel() = ServerProfile(
    id = ServerProfileId(id),
    displayName = displayName,
    baseUrl = baseUrl,
    username = username,
    allowInsecureHttp = allowInsecureHttp,
    serverType = serverType,
    serverVersion = serverVersion,
    apiVersion = apiVersion,
    openSubsonic = openSubsonic,
    isActive = isActive,
    lastConnectedAt = lastConnectedAtEpochMs?.let(Instant::ofEpochMilli)
)

fun AlbumEntity.asModel() = Album(
    profileId = ServerProfileId(profileId),
    remoteId = remoteId,
    name = name,
    artistId = artistId,
    artistName = artistName,
    coverArtId = coverArtId,
    songCount = songCount,
    durationSeconds = durationSeconds,
    year = year,
    genre = genre,
    createdAt = createdAtEpochMs?.let(Instant::ofEpochMilli),
    starredAt = starredAtEpochMs?.let(Instant::ofEpochMilli)
)

fun Album.asEntity(generation: Long) = AlbumEntity(
    profileId = profileId.value,
    remoteId = remoteId,
    name = name,
    sortName = name.lowercase(Locale.ROOT),
    artistId = artistId,
    artistName = artistName,
    coverArtId = coverArtId,
    songCount = songCount,
    durationSeconds = durationSeconds,
    year = year,
    genre = genre,
    createdAtEpochMs = createdAt?.toEpochMilli(),
    starredAtEpochMs = starredAt?.toEpochMilli(),
    syncGeneration = generation
)

fun TrackEntity.asModel() = Track(
    profileId = ServerProfileId(profileId),
    remoteId = remoteId,
    title = title,
    albumId = albumId,
    albumName = albumName,
    artistId = artistId,
    artistName = artistName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationSeconds = durationSeconds,
    contentType = contentType,
    suffix = suffix,
    coverArtId = coverArtId,
    starredAt = starredAtEpochMs?.let(Instant::ofEpochMilli)
)

fun Track.asEntity(generation: Long) = TrackEntity(
    profileId = profileId.value,
    remoteId = remoteId,
    title = title,
    sortTitle = title.lowercase(Locale.ROOT),
    albumId = albumId,
    albumName = albumName,
    artistId = artistId,
    artistName = artistName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationSeconds = durationSeconds,
    contentType = contentType,
    suffix = suffix,
    coverArtId = coverArtId,
    starredAtEpochMs = starredAt?.toEpochMilli(),
    syncGeneration = generation
)
