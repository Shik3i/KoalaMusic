# ADR 0005: Credential storage

Status: Accepted — 2026-07-30

Use Android Keystore AES-256/GCM with a non-exportable key. Store ciphertext,
12-byte IV, and format metadata in private no-backup files. Room stores only
profile metadata. Deprecated security wrappers and plaintext preferences are
rejected. Key invalidation requires user reauthentication.
