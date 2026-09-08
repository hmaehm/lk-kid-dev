#!/usr/bin/env python3
import os
import shutil
import json
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
BUILD_CS3 = os.path.join(BASE_DIR, "build", "layarkaca-kid-dev.cs3")
ROOT_CS3 = os.path.join(BASE_DIR, "layarkaca-kid-dev.cs3")
PLUGINS_JSON = os.path.join(BASE_DIR, "plugins.json")

def main():
    # 1. Locate the built cs3 file
    target_cs3 = None
    if os.path.exists(BUILD_CS3):
        target_cs3 = BUILD_CS3
    else:
        # Search for any .cs3 in build/
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

    # 2. Copy to repository root
    shutil.copyfile(target_cs3, ROOT_CS3)
    file_size_kb = os.path.getsize(ROOT_CS3) / 1024
    print(f"✅ Copied {os.path.basename(target_cs3)} -> layarkaca-kid-dev.cs3 ({file_size_kb:.1f} KB)")

    # 3. Bump version in plugins.json if requested
    bump = "--bump" in sys.argv
    if os.path.exists(PLUGINS_JSON):
        with open(PLUGINS_JSON, "r") as f:
            data = json.load(f)
        if isinstance(data, list) and len(data) > 0:
            current_ver = data[0].get("version", 1)
            new_ver = current_ver + 1 if bump else current_ver
            data[0]["version"] = new_ver
            with open(PLUGINS_JSON, "w") as f:
                json.dump(data, f, indent=2)
            print(f"📦 Extension version in plugins.json: v{new_ver} (use --bump to increment)")

    print()
    print("=" * 60)
    print("🎉 Local build is staged for GitHub!")
    print("Cloudstream In-App Repository Link:")
    print("👉 https://raw.githubusercontent.com/hmaehm/lk-kid-dev/master/repo.json")
    print("=" * 60)
    print("When ready, commit and push your changes yourself:")
    print("   git add layarkaca-kid-dev.cs3 plugins.json")
    print("   git commit -m 'release: update extension'")
    print("   git push origin master")
    print("=" * 60)

if __name__ == "__main__":
    main()
