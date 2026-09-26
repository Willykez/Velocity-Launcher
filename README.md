# Aura Launcher

A high-performance, fluid, and deeply customizable native Android launcher: a
smart categorized app drawer with fuzzy search, gesture mapping (swipe/pinch/
double-tap, including a real device-lock gesture), adaptive icon shapes, icon
pack support, folders, widgets, and full JSON backup/restore.

Aura is fully offline: no accounts, no network calls, no analytics.

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio.
2. Select **Open** and choose the directory containing this project.
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Remove this line from `app/build.gradle.kts` if you don't have a release
   keystore configured: `signingConfig = signingConfigs.getByName("release")`
   (the `debugConfig` signing config works out of the box for local runs).
5. Run the app on an emulator or physical device. To try it as your home
   screen, set it as the default launcher from Settings once installed.

## Notable settings

- **Settings > Appearance > Icon Pack** — picks up any installed Nova/ADW/GO
  -compatible icon pack automatically.
- **Settings > App Vault & Privacy** — set a PIN to hide sensitive apps from
  the drawer, and optionally grant device-admin rights so the "Lock Screen"
  gesture can actually lock the device (opt-in; requests no other permission).
- **Backup & Restore** — exports layout, folders, favorites/hidden apps and
  custom labels as a single shareable JSON file. The vault PIN is never
  included in the export.
