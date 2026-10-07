#!/usr/bin/env python3
"""Generate update manifest json files for SmartTubeEx releases."""

import argparse
import json
import re
from pathlib import Path
from typing import Any

REPO_NAME = "Praveensenpai/smarttubeex"
BASE_URL = f"https://github.com/{REPO_NAME}/releases/download"


def clean_changelog_line(line: str) -> str:
    """Strip markdown list bullets, bold markers, and excess punctuation."""
    cleaned = re.sub(r"^[-*•]\s*", "", line.strip())
    cleaned = re.sub(r"\*\*([^*]+)\*\*", r"\1", cleaned)
    cleaned = re.sub(r"`([^`]+)`", r"\1", cleaned)
    cleaned = re.sub(r"^[^\w\s]+", "", cleaned).strip()
    return cleaned


def extract_changelog(notes_path: Path) -> list[str]:
    """Extract release changelog bullet points from markdown file."""
    if not notes_path.exists():
        return ["Performance optimizations and bug fixes"]

    content = notes_path.read_text(encoding="utf-8")
    lines = content.splitlines()
    changelog: list[str] = []
    capture = False

    for line in lines:
        if line.startswith("### "):
            header = line.lower()
            capture = "what's new" in header or "changelog" in header
            continue
        if capture:
            stripped = line.strip()
            if stripped.startswith(("- ", "* ")):
                cleaned = clean_changelog_line(stripped)
                if cleaned:
                    changelog.append(cleaned)

    if not changelog:
        changelog.append("Performance optimizations and bug fixes")
    return changelog


def parse_gradle_version(gradle_path: Path) -> tuple[str, int]:
    """Parse versionName and versionCode from smarttubetv/build.gradle."""
    content = gradle_path.read_text(encoding="utf-8")
    version_name_match = re.search(r'versionName\s+["\']([^"\']+)["\']', content)
    version_code_match = re.search(r"versionCode\s+(\d+)", content)

    if not version_name_match or not version_code_match:
        raise ValueError(f"Failed to find version in {gradle_path}")

    return version_name_match.group(1), int(version_code_match.group(1))


def find_apk_for_abi(apk_dir: Path, abi: str) -> str:
    """Find matching apk filename for an ABI or fallback to universal."""
    for apk_file in apk_dir.glob("*.apk"):
        name = apk_file.name
        if abi == "universal" and "universal" in name:
            return name
        if abi != "universal" and abi in name:
            return name

    universal_apk = next(apk_dir.glob("*universal*.apk"), None)
    if universal_apk:
        return universal_apk.name

    first_apk = next(apk_dir.glob("*.apk"), None)
    if first_apk:
        return first_apk.name
    return f"SmartTube_stable_{abi}.apk"


def build_package_section(tag: str, apk_dir: Path) -> dict[str, list[str]]:
    """Build package download URLs per ABI."""
    arm64_apk = find_apk_for_abi(apk_dir, "arm64-v8a")
    arm_apk = find_apk_for_abi(apk_dir, "armeabi-v7a")
    x86_apk = find_apk_for_abi(apk_dir, "x86")
    universal_apk = find_apk_for_abi(apk_dir, "universal")

    return {
        "downloadUrlList": [f"{BASE_URL}/{tag}/{universal_apk}"],
        "downloadUrlList_arm64-v8a": [f"{BASE_URL}/{tag}/{arm64_apk}"],
        "downloadUrlList_armeabi-v7a": [f"{BASE_URL}/{tag}/{arm_apk}"],
        "downloadUrlList_x86": [f"{BASE_URL}/{tag}/{x86_apk}"],
    }


def update_manifest_data(
    base_data: dict[str, Any],
    version_name: str,
    version_code: int,
    changelog: list[str],
    package_section: dict[str, list[str]],
) -> dict[str, Any]:
    """Assemble output manifest with package section and ordered versions."""
    output: dict[str, Any] = {"package": package_section}
    output[version_name] = {
        "versionCode": version_code,
        "changelog": changelog,
    }

    for key, value in base_data.items():
        if key not in ("package", version_name):
            output[key] = value

    return output


def main() -> None:
    """CLI entrypoint."""
    parser = argparse.ArgumentParser(description="Generate SmartTubeEx update manifest")
    parser.add_argument("--tag", required=True, help="Release tag name e.g. v32.13")
    parser.add_argument(
        "--apk-dir", type=Path, required=True, help="Path to directory containing APKs"
    )
    parser.add_argument(
        "--base-manifest", type=Path, default=Path(".github/smarttube_stable2.json")
    )
    parser.add_argument(
        "--release-notes", type=Path, default=Path(".github/RELEASE_NOTES.md")
    )
    parser.add_argument(
        "--build-gradle", type=Path, default=Path("smarttubetv/build.gradle")
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        required=True,
        help="Directory to write output JSON files",
    )
    args = parser.parse_args()

    version_name, version_code = parse_gradle_version(args.build_gradle)
    changelog = extract_changelog(args.release_notes)
    package_section = build_package_section(args.tag, args.apk_dir)

    base_data: dict[str, Any] = {}
    if args.base_manifest.exists():
        base_data = json.loads(args.base_manifest.read_text(encoding="utf-8"))

    manifest_data = update_manifest_data(
        base_data=base_data,
        version_name=version_name,
        version_code=version_code,
        changelog=changelog,
        package_section=package_section,
    )

    args.output_dir.mkdir(parents=True, exist_ok=True)
    json_text = json.dumps(manifest_data, indent=2, ensure_ascii=False) + "\n"

    (args.output_dir / "smarttube_stable2.json").write_text(json_text, encoding="utf-8")
    (args.output_dir / "smarttube_stable.json").write_text(json_text, encoding="utf-8")

    # Keep base manifest in repository in sync
    if args.base_manifest.parent.exists():
        args.base_manifest.write_text(json_text, encoding="utf-8")

    print(
        f"Generated update manifests for {version_name} ({version_code}) in {args.output_dir}"
    )


if __name__ == "__main__":
    main()
