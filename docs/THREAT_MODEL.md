# Threat model

| Threat | Control | Residual risk |
|---|---|---|
| Stolen unlocked device | Keystore encryption, no backups, private files | an unlocked app can request decryption |
| Malicious local app | non-exported UI, private storage, scoped service API | accessibility/root can observe UI |
| MITM/home network | HTTPS default, strict TLS, explicit HTTP warning | opted-in HTTP exposes traffic |
| Malicious server | size/time limits, tolerant parsing, no WebView | media decoders remain platform attack surface |
| Redirect credential theft | same-host/scheme policy, no downgrade | compromised original host controls content |
| Log/diagnostic leak | parameter/header redaction and allowlisted export | new fields require sanitizer tests |
| Oversized metadata/artwork | bounded bodies/pages and image sizing | extreme valid libraries require batching |
| Database corruption | transactions, retained cache, visible recovery | local history may be lost after reset |
| Credential key invalidation | explicit reconnect flow | password must be entered again |

Trust boundaries: Android process/Keystore, configured server, local database,
and Android media clients. The server is authoritative for library data but not
trusted to provide safe URLs, HTML, or executable content.
