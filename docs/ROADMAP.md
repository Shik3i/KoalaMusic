# Roadmap

1. **Vertical slice** — secure setup, capability discovery, bounded sync, album
   browse, Media3 stream, system controls, queue restore.
2. **MVP breadth** — home/search/library/details, favorites/scrobbling,
   playlists read-only, adaptive player, settings/diagnostics. Core queue
   editing is implemented.
3. **Offline** — playback cache, explicit downloads, policies, storage limits,
   pending scrobbles, safe cleanup.
4. **Advanced playback** — ReplayGain, gapless validation, crossfade, fades,
   quality/transcoding profiles, speed/pitch, sleep timer, EQ/AutoEQ.
5. **Smart library** — rule playlists, local explainable mixes, radio, improved
   shuffle, private yearly summaries.
6. **Metadata/migration** — lyrics, credits, MusicBrainz/ISRC, Spotify export
   matching with ambiguity review; never Spotify streaming or scraping.
7. **Android surfaces** — Auto, widgets, shortcuts, cast, DLNA, Wear, TV.
8. **Future clients** — evaluate SvelteKit, Compose Desktop, Wails, and selective
   KMP only after stable Android boundaries exist.

Koala Connect remains a concept: local discovery, remote control, playback
transfer, synchronized queue/shared sessions, and optional self-hosted relay.
No central KoalaMusic service relays audio.
