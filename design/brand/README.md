# KoalaMusic brand assets

The selected source is `source/koalamusic-icon-source.png`. Generated files
must not be edited manually. Rejected local concept renders are intentionally
excluded from version control.

Regenerate with the bundled Codex Python runtime or any Python 3 environment
containing Pillow:

```bash
python3 tools/generate_brand_assets.py
```

Outputs:

- `koalamusic-icon-master.png`: transparent archival source copy;
- `readme-icon.png`: 256 px transparent repository artwork;
- `play-store-icon.png`: 512 px, 32-bit RGBA Play listing icon;
- `android-legacy/`: launcher and round icon exports for mdpi through xxxhdpi;
- Android adaptive foreground/background and monochrome layers;
- `icon-mask-preview.png`: circle, rounded-square, squircle, and themed previews.

The adaptive artwork is rendered into a 432 px layer with its subject fitted to
304 px. This keeps critical detail near Android's 66/108 safe-zone ratio while
leaving the outer area available for launcher masking and motion.

The app consumes only the adaptive resources because its minimum Android
version is API 26. Legacy density exports remain available for external tooling
without creating duplicate Android resource names.
