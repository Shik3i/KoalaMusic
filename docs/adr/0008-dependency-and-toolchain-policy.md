# ADR 0008: Dependency and toolchain policy

Status: Accepted — 2026-07-30

Use AGP 9.3.1, built-in Kotlin, JDK 17, Gradle 9.6.1, and SDK 37 as one verified
group. Stable pinned dependencies only; Google Maven/Maven Central/Plugin Portal
only. Toolchain updates are grouped and gated by clean builds/tests/lint.
Details and primary evidence live in `docs/DEPENDENCY_POLICY.md`.
