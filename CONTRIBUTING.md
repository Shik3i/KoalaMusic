# Contributing

Use an issue before large protocol, schema, or module changes. Keep commits
focused and never include credentials, private server URLs, signing material,
authenticated URLs, or production music metadata.

Before submitting a change:

```bash
./gradlew spotlessCheck test lint assembleDebug
```

New dependencies require an official-source stability check and an update to
`docs/DEPENDENCY_POLICY.md` when they affect the toolchain group. Protocol
behavior needs fixtures, redaction tests, and failure-path coverage. Schema
changes require Room migrations after the first public release.

All contributions are licensed under the MIT License.
