# Test strategy

Fast JVM tests cover URL policy, redirect rejection, auth token generation,
redaction, capability/DTO mapping, queue operations, reconciliation, scrobble
thresholds, diagnostics, and error mapping. MockWebServer fixtures cover
Navidrome, legacy wrappers, unknown/missing fields, malformed/large responses,
auth errors, server errors, and safe/unsafe redirects.

Room instrumentation tests cover profile isolation, unique keys, relations,
transaction rollback, replacement/deletion, queue restore, paging, and query
plans with synthetic 100,000-track data. Compose tests cover setup success and
failure, library/album navigation, playback entry, mini/full player, settings,
and critical semantics. Media3 device tests cover service connection and
notification behavior; deterministic queue/repeat/shuffle logic stays on JVM.

CI gate: wrapper validation, dependency verification, formatting, unit tests,
lint, debug assembly, release compile, and a bounded emulator smoke suite.
Live Navidrome tests are optional and never the only protocol coverage.
