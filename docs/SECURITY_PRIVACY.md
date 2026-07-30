# Security and privacy

Normal traffic goes only to the selected OpenSubsonic server. No analytics,
crash upload, ads, tracking, remote recommendations, external artwork, or
KoalaMusic backend exists.

Secrets use an Android Keystore non-exportable AES-256 key and AES-GCM. Only
ciphertext, IV, and a format version are stored under `noBackupFilesDir`.
Deleting a profile deletes its encrypted credential. Room, DataStore,
`BuildConfig`, logs, backups, and media metadata never receive credentials.

HTTPS is mandatory by default. Invalid certificates and hostnames fail. HTTP is
an exact-profile opt-in with a warning. The manifest permits dynamic cleartext
hosts because Android network security configuration cannot express runtime
host allowlists; an application interceptor enforces the actual profile rule.
HTTPS-to-HTTP and cross-host authenticated redirects are rejected.

No certificate pinning or trust-all switch is appropriate for arbitrary
self-hosted servers. Backups are disabled; exported components are minimized.
