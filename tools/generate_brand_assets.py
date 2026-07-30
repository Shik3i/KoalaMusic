#!/usr/bin/env python3
"""Generate KoalaMusic launcher, store, and README assets from one master PNG."""

from pathlib import Path

from PIL import Image, ImageChops, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "design/brand/source/koalamusic-icon-source.png"
BRAND = ROOT / "design/brand"
RES = ROOT / "app/src/main/res"
BACKGROUND = (221, 214, 247, 255)
LEGACY_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def load_artwork() -> Image.Image:
    image = Image.open(SOURCE).convert("RGBA")
    alpha = image.getchannel("A")
    bounds = alpha.getbbox()
    if bounds is None:
        raise ValueError(f"{SOURCE} has no visible pixels")
    return image.crop(bounds)


def fit(image: Image.Image, canvas_size: int, artwork_size: int) -> Image.Image:
    scale = min(artwork_size / image.width, artwork_size / image.height)
    resized = image.resize(
        (round(image.width * scale), round(image.height * scale)),
        Image.Resampling.LANCZOS,
    )
    canvas = Image.new("RGBA", (canvas_size, canvas_size))
    position = (
        (canvas_size - resized.width) // 2,
        (canvas_size - resized.height) // 2,
    )
    canvas.alpha_composite(resized, position)
    return canvas


def composite_icon(
    artwork: Image.Image,
    size: int,
    mask_shape: str | None = None,
) -> Image.Image:
    canvas = Image.new("RGBA", (size, size), BACKGROUND)
    foreground = fit(artwork, size, round(size * 0.82))
    canvas.alpha_composite(foreground)
    if mask_shape:
        mask = Image.new("L", (size, size))
        draw = ImageDraw.Draw(mask)
        if mask_shape == "circle":
            draw.ellipse((0, 0, size - 1, size - 1), fill=255)
        elif mask_shape == "rounded":
            draw.rounded_rectangle(
                (0, 0, size - 1, size - 1),
                radius=round(size * 0.22),
                fill=255,
            )
        else:
            raise ValueError(f"Unsupported icon mask: {mask_shape}")
        canvas.putalpha(mask)
    return canvas


def monochrome(foreground: Image.Image) -> Image.Image:
    rgba = foreground.convert("RGBA")
    luminance = rgba.convert("L")
    source_alpha = rgba.getchannel("A")
    contrast_alpha = luminance.point(lambda value: round(255 * (0.35 + 0.65 * (1 - value / 255))))
    alpha = ImageChops.multiply(source_alpha, contrast_alpha)
    result = Image.new("RGBA", rgba.size, (255, 255, 255, 0))
    result.putalpha(alpha)
    return result


def save_png(image: Image.Image, path: Path, *, optimize: bool = True) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, "PNG", optimize=optimize)


def masked_preview(icon: Image.Image, monochrome_icon: Image.Image) -> Image.Image:
    tile_size = 240
    gap = 16
    sheet = Image.new(
        "RGBA",
        (tile_size * 4 + gap * 5, tile_size + gap * 2),
        (248, 248, 252, 255),
    )
    masks = []
    circle = Image.new("L", icon.size)
    ImageDraw.Draw(circle).ellipse((0, 0, icon.width - 1, icon.height - 1), fill=255)
    masks.append(circle)
    rounded = Image.new("L", icon.size)
    ImageDraw.Draw(rounded).rounded_rectangle(
        (0, 0, icon.width - 1, icon.height - 1),
        radius=round(icon.width * 0.22),
        fill=255,
    )
    masks.append(rounded)
    squircle = Image.new("L", icon.size)
    ImageDraw.Draw(squircle).rounded_rectangle(
        (0, 0, icon.width - 1, icon.height - 1),
        radius=round(icon.width * 0.35),
        fill=255,
    )
    masks.append(squircle)

    for index, mask in enumerate(masks):
        sample = icon.copy()
        sample.putalpha(mask)
        sample = sample.resize((tile_size, tile_size), Image.Resampling.LANCZOS)
        sheet.alpha_composite(sample, (gap + index * (tile_size + gap), gap))

    themed = Image.new("RGBA", icon.size, (80, 65, 125, 255))
    tint = Image.new("RGBA", monochrome_icon.size, (255, 255, 255, 0))
    tint.putalpha(monochrome_icon.getchannel("A"))
    themed.alpha_composite(tint)
    themed = themed.resize((tile_size, tile_size), Image.Resampling.LANCZOS)
    sheet.alpha_composite(themed, (gap + 3 * (tile_size + gap), gap))
    return sheet


def main() -> None:
    artwork = load_artwork()
    BRAND.mkdir(parents=True, exist_ok=True)

    save_png(Image.open(SOURCE).convert("RGBA"), BRAND / "koalamusic-icon-master.png")
    save_png(fit(artwork, 256, 236), BRAND / "readme-icon.png")
    play_store = composite_icon(artwork, 512)
    save_png(play_store, BRAND / "play-store-icon.png")

    adaptive = fit(artwork, 432, 304)
    monochrome_adaptive = monochrome(adaptive)
    save_png(adaptive, RES / "drawable-nodpi/ic_launcher_foreground.png")
    save_png(monochrome_adaptive, RES / "drawable-nodpi/ic_launcher_monochrome.png")
    adaptive_composite = Image.new("RGBA", (432, 432), BACKGROUND)
    adaptive_composite.alpha_composite(adaptive)
    save_png(
        masked_preview(adaptive_composite, monochrome_adaptive),
        BRAND / "icon-mask-preview.png",
    )

    for density, size in LEGACY_SIZES.items():
        directory = BRAND / "android-legacy" / density
        save_png(
            composite_icon(artwork, size, mask_shape="rounded"),
            directory / "ic_launcher.png",
        )
        save_png(
            composite_icon(artwork, size, mask_shape="circle"),
            directory / "ic_launcher_round.png",
        )

    print(f"Generated brand assets from {SOURCE.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
