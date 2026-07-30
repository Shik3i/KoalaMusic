# Architecture

KoalaMusic uses a single activity, Compose UI, unidirectional state, repositories,
and Room as the primary UI read model.

```text
Compose feature -> ViewModel -> repository -> Room Flow
                                  |       -> OpenSubsonic client
                                  |       -> credential vault
MediaController -> MediaLibraryService -> MediaLibrarySession -> ExoPlayer
```

Network DTO parsing is confined to `core:opensubsonic`; DTOs map to domain
models, then database entities. Every server-owned identity is `(profileId,
remoteId)`. `core:playback` owns the player. UI code sends media commands and
observes controller state; it never creates ExoPlayer.

Coroutines use structured scopes. Database replacement is transactional. A
failed remote refresh retains the last usable library and records a sync error.
Domain errors cross module boundaries; raw exceptions do not reach UI.

Accepted early debt: initial reconciliation uses bounded full pages because the
protocol has no universal change feed. Playlist mutation, full-library walking,
and advanced queue editing are deferred behind existing repository boundaries.
