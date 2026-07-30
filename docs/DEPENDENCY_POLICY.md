# Dependency policy

Verification date: 2026-07-30. Only stable releases from primary sources were
selected. No alpha, beta, RC, milestone, snapshot, dynamic version, range, or
JitPack dependency is allowed.

## Locked compatibility group

| Component | Version | Primary evidence |
|---|---:|---|
| Android Gradle Plugin | 9.3.1 | Android AGP 9.3 release notes and Google Maven metadata |
| Gradle wrapper | 9.6.1 | latest stable Gradle accepted by AGP 9.3 |
| JDK | 17 | AGP 9.3 compatibility table |
| compile/target SDK | 37.0 | AGP 9.3 maximum supported API |
| AGP built-in Kotlin | AGP-managed | AGP 9.3.1 Google Maven metadata; no Kotlin Android plugin |
| Compose compiler plugin | 2.4.10 | current stable Kotlin/Compose compiler plugin |
| KSP | 2.3.10 | Google Maven stable release metadata |
| Dagger/Hilt | 2.60.1 | Maven Central release metadata, updated 2026-07-06 |
| Compose BOM | 2026.06.01 | Google Maven stable metadata |
| Navigation 3 | 1.1.5 | AndroidX stable channel |
| AndroidX Hilt | 1.4.0 | AndroidX stable channel |
| Lifecycle | 2.11.0 | AndroidX stable channel |
| Room | 2.8.4 | AndroidX Room stable channel |
| Paging | 3.5.0 | AndroidX stable channel |
| DataStore | 1.2.1 | AndroidX stable channel |
| WorkManager | 2.11.2 | AndroidX stable channel |
| Media3 | 1.10.1 | AndroidX stable channel |
| OkHttp | 5.4.0 | Maven Central stable metadata and Square official repository |
| kotlinx.coroutines | 1.11.0 | Kotlin official repository |
| kotlinx.serialization | 1.11.0 | Kotlin official repository |
| Coil | 3.5.0 | Coil official changelog |
| ktlint Gradle plugin | 14.2.0 | Gradle Plugin Portal |

Room 2 is retained instead of the separate Room 3 KMP-focused artifact because
WorkManager and the Android ecosystem still consume Room 2; this avoids two
Room runtimes in an Android-only app. This is an artifact-family choice, not use
of an outdated Room 2 release.

Repositories are `google()`, `mavenCentral()`, and `gradlePluginPortal()` only
where plugin resolution requires it. Local flat directories, JCenter, JitPack,
unverified mirrors, and vendor snapshot repositories are forbidden.

Upgrade the entire AGP/Gradle/JDK/Kotlin/Compose/KSP/Hilt/SDK group together.
Verify primary release notes and metadata, regenerate dependency verification,
run clean sync, unit tests, lint, debug/release builds, and record the date.
Dependabot may group proposals but never auto-merges.

Sources:

- https://developer.android.com/build/releases/agp-9-3-0-release-notes
- https://developer.android.com/build/migrate-to-built-in-kotlin
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/jetpack/androidx/versions/stable-channel
- https://github.com/google/ksp/releases
- https://github.com/google/dagger/releases
- https://repo1.maven.org/maven2/com/google/dagger/hilt-android/maven-metadata.xml
- https://github.com/square/okhttp
- https://github.com/Kotlin/kotlinx.coroutines/releases
- https://github.com/Kotlin/kotlinx.serialization/releases
- https://coil-kt.github.io/coil/changelog/
- https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint
