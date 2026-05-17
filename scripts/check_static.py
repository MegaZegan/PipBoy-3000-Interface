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

    print("Static checks passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
