# Diagnostics

Diagnostics are generated manually and saved only after user confirmation.
Export includes app/build version, Android/API version, sanitized server
scheme/host hash and declared type/version, capability names, last sync status,
database counts, recent domain error codes, and playback state names.

It excludes credentials, API keys, tokens, salts, encryption material, private
headers, bodies, full request/authenticated URLs, usernames, track/artist/album
names, raw server paths, IP/hostname unless separately confirmed, and stack
traces containing user data. Sanitization uses an allowlist plus tests; it does
not attempt to blacklist every secret shape. Nothing uploads automatically.
