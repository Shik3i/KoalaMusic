# Android release signing

KoalaMusic uses one permanent Android release key. Keep every published Google
Play build on this key.

## Protected copies

- GitHub Actions stores four repository secrets for automated APK and AAB builds.
- `android-signing.env.enc` is the SOPS/age disaster-recovery copy for the
  authorized macOS and Windows machines.

GitHub Actions does not decrypt the SOPS file. Private age identities never
belong in GitHub or this repository.

## Required GitHub Actions secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The `Release · Android app` workflow runs manually or for `android-v*` tags. It
tests and lints the app, builds a signed APK and AAB, verifies both signatures,
writes SHA-256 checksums, attests the artifacts, and creates a GitHub release
for tag builds.

## macOS recovery

The private age identity is stored at:

```text
~/Library/Application Support/sops/age/keys.txt
```

Validate the encrypted backup without writing plaintext:

```bash
sops filestatus --input-type dotenv android-signing.env.enc
sops decrypt --input-type dotenv --output-type dotenv android-signing.env.enc >/dev/null
```

Decrypt only when recovery is necessary:

```bash
umask 077
sops decrypt --input-type dotenv --output-type dotenv \
  android-signing.env.enc > android-signing.env
```

## Windows recovery

Install `sops` and `age`, then place the authorized private age identity at:

```text
%APPDATA%\sops\age\keys.txt
```

Validate from PowerShell:

```powershell
sops filestatus --input-type dotenv android-signing.env.enc
sops decrypt --input-type dotenv --output-type dotenv android-signing.env.enc | Out-Null
```

Decrypt only when recovery is necessary:

```powershell
sops decrypt --input-type dotenv --output-type dotenv android-signing.env.enc |
  Set-Content -Encoding utf8NoBOM android-signing.env
```

`android-signing.env`, `*.jks`, and `*.keystore` are ignored. Delete any
decrypted recovery file after use.

## Adding another authorized device

Add only its public age recipient to `.sops.yaml`, then rewrap:

```bash
sops updatekeys --input-type dotenv -y android-signing.env.enc
```
