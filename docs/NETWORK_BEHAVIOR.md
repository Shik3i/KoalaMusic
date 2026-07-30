# Network behavior

Only the active configured server receives normal requests. Calls use bounded
connect/read timeouts, cancellation, response-size limits for metadata, and
retry only for safe idempotent transient failures. Search is local by default.
No polling, analytics, remote artwork, captive-portal probe, or third-party
connectivity check occurs.

URLs are normalized to an origin plus optional path prefix. HTTPS is required.
HTTP requires explicit consent stored on that profile. Redirects carrying
authentication are accepted only when scheme and host remain identical; HTTPS
never downgrades. Auth query values, authorization/cookie headers, request
bodies, and full authenticated URLs are redacted.

Endpoints: unauthenticated `getOpenSubsonicExtensions` where supported;
authenticated `ping`, library/browse/search/star/scrobble/playlist endpoints;
binary `stream` and `getCoverArt`. API-key auth omits username. Token auth uses
a fresh cryptographic salt per request.
