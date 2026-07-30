# Data model

`server_profiles` stores non-secret identity, normalized base URL, active state,
HTTP consent, server/API versions, and timestamps. `server_capabilities` stores
extension name/version pairs. Credentials live only in the encrypted vault.

Artists, albums, tracks, playlists, genres, star/rating state, history,
scrobbles, queue, and sync state include `profile_id`. Composite unique keys use
`(profile_id, remote_id)`; foreign keys include both columns. Indexes cover
profile, normalized sort/search columns, album/artist relations, playlist
position, history time, and scrobble state.

Artwork is referenced by server ID and cached by the bounded image cache; blobs
are not stored in Room. Queue items persist remote IDs and non-secret display
metadata, never authenticated URLs.

Queries are paged or bounded. No UI state contains all 100,000 tracks. Released
schemas use explicit migrations; destructive migration is never a production
fallback.
