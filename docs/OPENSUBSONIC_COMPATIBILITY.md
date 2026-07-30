# OpenSubsonic compatibility

Reference date: 2026-07-30.

KoalaMusic speaks JSON OpenSubsonic/Subsonic REST API `1.16.1`. Navidrome is the
primary test server. `ping` determines protocol version, server type/version,
and the `openSubsonic` flag. OpenSubsonic extension discovery is attempted
without authentication as specified, then repeated/authenticated only for
non-conforming compatible servers.

Authentication preference:

1. user-provided OpenSubsonic `apiKey` without username;
2. per-request random salt plus `MD5(password + salt)` compatibility token;
3. plaintext/`enc:` password only behind a future explicit legacy mode.

Capabilities are stored as extension name/version sets. UI never infers support
from `type == Navidrome`. Missing/extra fields, unknown extensions, empty lists,
legacy wrappers, and malformed non-critical metadata are tolerated. Error codes
40/44 map to authentication, 41/42 to unsupported authentication, 43 to client
configuration, and 10/20/30/50/70 to explicit domain errors.

Official references:

- https://opensubsonic.netlify.app/docs/api-reference/
- https://opensubsonic.netlify.app/docs/endpoints/ping/
- https://opensubsonic.netlify.app/docs/extensions/apikeyauth/
- https://opensubsonic.netlify.app/docs/opensubsonic-changes/
- https://www.navidrome.org/docs/developers/subsonic-api/
