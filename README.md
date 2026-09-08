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

## How to Build the Extension (.cs3)

Run the following command in your terminal from the repository root:

```bash
./gradlew make
```
Or:
```bash
./gradlew build
```

The generated `.cs3` plugin file will be located in:
`build/layarkaca-kid-dev.cs3`

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
