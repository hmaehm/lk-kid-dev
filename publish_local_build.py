#!/usr/bin/env python3
import os
import shutil
import json
import hashlib
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
BUILD_CS3 = os.path.join(BASE_DIR, "build", "layarkaca-kid-dev.cs3")
ROOT_CS3 = os.path.join(BASE_DIR, "layarkaca-kid-dev.cs3")
PLUGINS_JSON = os.path.join(BASE_DIR, "plugins.json")

def get_sha256(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(8192):
            h.update(chunk)
    return h.hexdigest()

def main():
    target_cs3 = None
    if os.path.exists(BUILD_CS3):
        target_cs3 = BUILD_CS3
    else:
        build_dir = os.path.join(BASE_DIR, "build")
        if os.path.exists(build_dir):
            for root, _, files in os.walk(build_dir):
                for f in files:
                    if f.endswith(".cs3"):
                        target_cs3 = os.path.join(root, f)
                        break

    if not target_cs3:
        print("❌ Error: No .cs3 file found in build/")
        print("   Please build the project first using: ./gradlew make")
        sys.exit(1)

    # Copy to repository root
    shutil.copyfile(target_cs3, ROOT_CS3)
    file_size = os.path.getsize(ROOT_CS3)
    sha256_hash = get_sha256(ROOT_CS3)

    print(f"✅ Copied {os.path.basename(target_cs3)} -> layarkaca-kid-dev.cs3 ({file_size / 1024:.1f} KB)")
    print(f"🔑 SHA256: {sha256_hash}")

    # Read version directly from manifest.json inside .cs3
    import zipfile
    version = 1
    try:
        with zipfile.ZipFile(ROOT_CS3) as z:
            manifest_data = json.loads(z.read("manifest.json").decode("utf-8"))
            version = manifest_data.get("version", 1)
    except Exception:
        pass

    plugin_entry = {
        "name": "layarkaca-kid-dev",
        "internalName": "layarkaca-kid-dev",
        "version": version,
        "apiVersion": 1,
        "fileSize": file_size,
        "fileHash": f"sha256-{sha256_hash}",
        "repositoryUrl": "https://github.com/hmaehm/lk-kid-dev",
        "url": "https://raw.githubusercontent.com/hmaehm/lk-kid-dev/master/layarkaca-kid-dev.cs3",
        "description": "LK21 movie and series extension with configurable domain settings",
        "authors": ["hmaehm"],
        "tvTypes": ["Movie", "TvSeries"],
        "language": "id",
        "status": 1
    }

    with open(PLUGINS_JSON, "w") as f:
        json.dump([plugin_entry], f, indent=2)

    print(f"📦 Updated plugins.json (v{version}, status: 1)")

    if "--purge" in sys.argv:
        purge_jsdelivr()

    print()
    print("=" * 60)
    print("🎉 Release ready for GitHub!")
    print("Cloudstream In-App Repository URL:")
    print("👉 https://raw.githubusercontent.com/hmaehm/lk-kid-dev/master/repo.json")
    print("=" * 60)

def purge_jsdelivr():
    import urllib.request
    print("🌐 Purging jsDelivr CDN cache...")
    files = ["layarkaca-kid-dev.cs3", "plugins.json", "repo.json"]
    for f in files:
        url = f"https://purge.jsdelivr.net/gh/hmaehm/lk-kid-dev@master/{f}"
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=10) as resp:
                print(f"  ✓ Purged {f} (HTTP {resp.status})")
        except Exception as e:
            print(f"  ⚠ Failed to purge {f}: {e}")

if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--purge-only":
        purge_jsdelivr()
    else:
        main()
