# MVP

## Release target

1. Setup validates and normalizes a URL.
2. HTTPS is required unless the exact profile has explicit HTTP consent.
3. Connectivity and authentication errors are actionable.
4. Credentials are Keystore-encrypted in no-backup storage.
5. Server identity and capabilities are persisted separately from secrets.
6. Album and track metadata is synchronized transactionally into Room.
7. UI browses the database, not live network DTOs.
8. Selecting a track starts the single Media3 service player.
9. notification, lock-screen, headset, Bluetooth, audio focus, and noisy-audio
   behavior are delegated to Media3.
10. Queue survives process death and unavailable tracks fail visibly.

## Current implementation

The first vertical slice is represented end to end: setup and URL policy,
Keystore-backed credentials, ping and capability discovery, transactional album
and track synchronization, Room-backed album browsing, authenticated Media3
stream resolution, background playback, system controls, and persisted queue
state.

The current breadth pass adds home, local album search, album details, compact
and full players, queue add/next/reorder/remove/clear controls, periodic
position persistence, server information, profile removal, and explicit
refresh. Device-level execution against a real Navidrome server remains
required before calling the slice release-ready.

Artists, playlists, genres, track-wide search, favorites, scrobbling, queue
diagnostic export, adaptive layouts, and instrumentation tests remain
implementation work. Playlist editing, downloads, ReplayGain, lyrics, and
advanced queue policies may follow the initial release.
