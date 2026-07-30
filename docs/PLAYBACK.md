# Playback

```text
MediaLibraryService
└─ MediaLibrarySession
   └─ one ExoPlayer
```

The service owns queue, position, repeat, shuffle, errors, audio focus,
notification, and external controls. A `MediaController` is the UI boundary.
Queue entries use opaque profile/track IDs. The service resolves those IDs to a
fresh authenticated stream request internally; secrets and authenticated URLs
are not placed in browsable media metadata.

Media3 handles background lifetime, notification, lock screen, Bluetooth,
headset controls, audio focus, and becoming-noisy behavior. Timeline changes
persist ordered queue rows in Room. The UI can add a track next or at the end,
jump to an entry, reorder or remove entries, and clear the queue. The current
position is saved while playback advances and when play/pause changes.
Restoration skips missing profile/track rows, keeps the last position, and
reports failures instead of silently advancing through an entirely unavailable
queue.

Future ReplayGain, crossfade, quality policy, downloads, and Android Auto extend
the service/data-source boundary, not the UI.
