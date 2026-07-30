# Data synchronization

Initial sync writes profile-scoped pages into staging batches, then commits each
coherent entity set transactionally. Album lists are synchronized first so the
user can browse quickly; album tracks are fetched on demand and refreshed in the
background.

OpenSubsonic provides no universal incremental library change feed. MVP refresh
therefore reconciles bounded pages by remote ID and a generation marker.
Deletion is applied only after a complete successful enumeration. Partial,
cancelled, timed-out, or malformed refreshes preserve prior rows and record
failure. Changed favorites and playlists use their dedicated endpoints.

WorkManager performs user-requested/periodic refresh with network constraints
and exponential backoff. Authentication and deterministic client errors do not
retry indefinitely. Switching active profiles cancels profile work and changes
the Room query scope; libraries are never merged.
