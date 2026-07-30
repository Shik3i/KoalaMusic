# ADR 0006: HTTP and TLS policy

Status: Accepted — 2026-07-30

Require valid HTTPS by default. Arbitrary runtime local hosts cannot be
expressed as a manifest network-security allowlist, so the manifest must permit
cleartext transport while an OkHttp policy enforces exact per-profile consent.
Never trust all certificates, disable hostname checks, pin arbitrary servers,
or follow HTTPS-to-HTTP/cross-host authenticated redirects.
