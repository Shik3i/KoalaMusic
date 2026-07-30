# Implementation plan

## A — research

Lock official stable versions, OpenSubsonic contracts, Navidrome deviations,
Android cleartext constraints, and Media3 service guidance.

Status: complete for the foundation; revalidate before every toolchain upgrade.

## B — documents

Resolve product/security/data/playback/module decisions and ADRs before broad
feature implementation. Contradictions are reviewed against settled scope.

Status: complete.

## C — foundation

Wrapper, catalog, convention plugins, modules, design system, Hilt/KSP, Room,
OkHttp, Keystore, CI, formatting, tests, and dependency verification. Gate:
clean debug build, tests, lint, and release compile.

Status: complete.

## D — vertical slice

Setup state machine -> secure profile -> ping/extensions -> initial albums ->
album details -> service stream -> system controls -> queue restore.

Status: implemented and build-verified; real-device/Navidrome verification
remains.

## E — breadth

Home, paging library, local search, details, favorites/scrobbles, queue
operations, player, settings, diagnostics.

Status: partial. Home, local album search, album details, player surfaces,
server settings, and refresh exist. Remaining breadth is tracked in
`docs/MVP.md` and `docs/ROADMAP.md`.

## F — hardening

Fixtures, large-library tests, accessibility/adaptive review, redaction/security
review, performance measures, fresh-checkout CI, and docs reconciliation.

Status: partial. Unit/API tests, lint, formatting, R8, dependency verification,
security defaults, and documentation reconciliation pass. Instrumentation,
adaptive UI, larger fixture coverage, and measured performance remain.
