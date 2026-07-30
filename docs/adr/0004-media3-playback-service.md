# ADR 0004: Media3 playback service

Status: Accepted — 2026-07-30

A `MediaLibraryService` owns one `MediaLibrarySession` and ExoPlayer. UI uses a
`MediaController`. This supports background playback, system controls, future
Android Auto browsing, and process-independent lifetime. Authenticated stream
resolution and queue persistence remain service-side.
