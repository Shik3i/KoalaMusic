# ADR 0003: Connected-first local cache

Status: Accepted — 2026-07-30

Setup and uncached playback require a server. Room is the durable UI read model
for metadata, queue, history, capabilities, and sync state. Cached content stays
browsable during failure, but uncached tracks are visibly unavailable. The app
does not claim fully offline-first behavior.
