from __future__ import annotations

from html.parser import HTMLParser
from pathlib import Path


class AssetParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.assets: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        attr_map = dict(attrs)
        if tag in {"script", "img"} and attr_map.get("src"):
            self.assets.append(attr_map["src"] or "")
        if tag == "link" and attr_map.get("href"):
            self.assets.append(attr_map["href"] or "")


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    html_path = root / "index.html"
    html = html_path.read_text(encoding="utf-8")
    watch_html = (root / "watch.html").read_text(encoding="utf-8")

    required_text = [
        "PIP-Boy 3000",
        "STATUS",
        "INV",
        "DATA",
        "MAP",
        "RADIO",
        "boot-screen",
        "theme-btn",
    ]
    missing_text = [item for item in required_text if item not in html]
    if missing_text:
        raise SystemExit(f"Missing required UI markers: {missing_text}")

    parser = AssetParser()
    parser.feed(html)
    missing_assets: list[str] = []
    for asset in parser.assets:
        if asset.startswith(("http://", "https://", "#", "data:")):
            continue
        path = asset.split("?", 1)[0]
        if path and not (root / path).exists():
            missing_assets.append(asset)

    if missing_assets:
        raise SystemExit(f"Missing local assets: {missing_assets}")

    watch_required_text = [
        "VaultWatch Terminal",
        "face-clock",
        "STAT",
        "INV",
        "DATA",
        "MAP",
        "RAD",
        "watch.css",
        "watch.js",
    ]
    missing_watch_text = [item for item in watch_required_text if item not in watch_html]
    if missing_watch_text:
        raise SystemExit(f"Missing required watch UI markers: {missing_watch_text}")

    watch_parser = AssetParser()
    watch_parser.feed(watch_html)
    for asset in watch_parser.assets:
        if asset.startswith(("http://", "https://", "#", "data:")):
            continue
        path = asset.split("?", 1)[0]
        if path and not (root / path).exists():
            missing_assets.append(asset)

    if missing_assets:
        raise SystemExit(f"Missing local assets: {missing_assets}")

    watchface_required = [
        root / "img" / "imported-pipboy" / "VaultBoy.png",
        root / "img" / "imported-pipboy" / "ezgif-4f3eb3aa896b3f93.gif",
        root / "img" / "imported-pipboy" / "radiowaves.gif",
        root / "img" / "imported-pipboy" / "radiowaves1.gif",
        root / "img" / "imported-pipboy" / "startup.mp4",
        root / "watchface" / "settings.gradle.kts",
        root / "watchface" / "README.md",
        root / "watchface" / "watchface" / "build.gradle.kts",
        root / "watchface" / "watchface" / "src" / "main" / "AndroidManifest.xml",
        root / "watchface" / "watchface" / "src" / "main" / "res" / "raw" / "watchface.xml",
        root / "watchface" / "watchface" / "src" / "main" / "res" / "xml" / "watch_face_info.xml",
        root / "watchface" / "watchface" / "src" / "main" / "res" / "drawable-nodpi" / "pipboy_face_bg.png",
        root / "watchface" / "watchface" / "src" / "main" / "res" / "drawable-nodpi" / "preview.png",
        root / "watchface" / "watchface" / "src" / "main" / "assets" / "img" / "imported-pipboy" / "ezgif-4f3eb3aa896b3f93.gif",
        root / "watchface" / "watchface" / "src" / "main" / "assets" / "img" / "imported-pipboy" / "radiowaves1.gif",
    ]
    missing_watchface = [str(path.relative_to(root)) for path in watchface_required if not path.exists()]
    if missing_watchface:
        raise SystemExit(f"Missing watch face project files: {missing_watchface}")

    print("Static checks passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
