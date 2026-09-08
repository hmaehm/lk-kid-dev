# Layarkaca Kid Dev - Cloudstream Extension

A Cloudstream 3 extension for LayarKaca21 (LK21) supporting both **Movies** and **TV Series**, with real-time in-app domain switching.

## Features

- **Movies & TV Series Support**: Full catalog for movies and TV series (with automated redirect resolution to `dramamu.lk21.de`).
- **Dynamic Domain Setting**: Change domain mirror anytime from Cloudstream extension settings (or reset back to default).
- **Default Mirrors**:
  - Primary: `https://tv12.lk21official.cc`
  - Alternate: `https://lk21.de`
  - Drama portal: `https://dramamu.lk21.de`
- **Home Sections**:
  - Film Terbaru (Latest Movies)
  - Populer (Popular)
  - Rating Tertinggi (Top Rated)
  - Series Terbaru (Latest Series)
- **Search**: Search movies and series with posters, year, and metadata.
- **Embedded Player Extractors**: Support for Videonode (`videonode.de`) and Dadadidi (`dadadidi.de`).

---

## Project Structure

```
layarkaca-kid/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/
    └── main/
        ├── AndroidManifest.xml
        ├── res/values/strings.xml
        └── kotlin/com/layarkacakid/dev/
            ├── LayarkacaKidPlugin.kt       # Plugin lifecycle and settings dialog
            ├── LayarkacaKidProvider.kt     # Scraper for LK21 movies and series
            └── extractors/
                ├── VideonodeExtractor.kt   # Videonode player extractor
                └── DadadidiExtractor.kt    # Dadadidi stream/download extractor
```

---

## How to Build Locally and Host on GitHub

This repository is configured so you can build the `.cs3` extension file locally on your Mac, and host the extension on GitHub so Cloudstream can install and update it directly via URL.

### 1. Build the Extension Locally
From the root of your project:
```bash
./gradlew make
```
*(Produces `build/layarkaca-kid-dev.cs3`)*

### 2. Prepare the Release
Run the helper script:
```bash
python3 publish_local_build.py
# Or to bump the version number:
python3 publish_local_build.py --bump
```
This copies the `.cs3` file to the root and synchronizes `plugins.json`.

### 3. Commit and Push to GitHub (You do this yourself)
```bash
git add layarkaca-kid-dev.cs3 plugins.json repo.json .gitignore
git commit -m "release: update extension build"
git push origin master
```

### 4. Cloudstream In-App Repository URL
Once pushed to GitHub, add this link in **Cloudstream (Settings > Extensions > Add Repository)**:
```
https://raw.githubusercontent.com/hmaehm/lk-kid-dev/master/repo.json
```
Cloudstream will automatically fetch the extension list and install/update `layarkaca-kid-dev.cs3` directly from your GitHub repository!

---

## Changing the LK21 Domain in Cloudstream

1. Open **Cloudstream** on your device.
2. Go to **Settings** > **Extensions**.
3. Locate **Layarkaca Kid Dev** in your installed extensions list.
4. Tap the **Settings** (gear) icon next to the extension.
5. In the settings dialog:
   - Type any new mirror domain (e.g. `https://tv12.lk21official.cc` or `https://lk21.de`).
   - Or tap one of the preset mirror buttons.
   - Tap **Save** (or **Reset Default** to restore the default domain).
