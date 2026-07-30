# ADR 0002: OpenSubsonic protocol boundary

Status: Accepted — 2026-07-30

Name the boundary `OpenSubsonicClient`/`OpenSubsonicRepository`. Navidrome is the
reference server, not a domain dependency. DTOs remain private to the protocol
module and map into stable models. Explicit advertised capabilities drive UI.
No generic unused provider framework is introduced.
