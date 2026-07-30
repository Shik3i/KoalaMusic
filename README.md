<p align="center">
  <img src="design/brand/readme-icon.png" width="180" alt="KoalaMusic koala hugging a vinyl record">
</p>

# KoalaMusic

KoalaMusic is a privacy-first, native Android OpenSubsonic music player. It is
connected-first, keeps a resilient local metadata cache, and is optimized
against Navidrome without coupling its domain model to one server.

## Status

Early MVP foundation. The implemented path connects to an OpenSubsonic server,
stores credentials with Android Keystore, synchronizes album metadata, browses
an album, and hands authenticated streams to a Media3 playback service.

## Requirements

- JDK 17
- Android SDK 37.0 and Build Tools 36.0.0
- No analytics, telemetry, advertising, or KoalaMusic cloud account

## Build

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug test lint
```

Architecture, scope, security, and current limitations are under [`docs/`](docs/).
Release-key recovery and the signed GitHub APK/AAB workflow are documented in
[`docs/ANDROID_SIGNING.md`](docs/ANDROID_SIGNING.md).

## License

GPL-3.0-or-later. See [`LICENSE`](LICENSE).
