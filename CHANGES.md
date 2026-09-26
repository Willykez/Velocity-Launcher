# Changes made to the AI-Studio-generated build

Reviewed against `android-launcher-design.md` end to end. Below is everything
that was fixed, added, or cleaned up.

## Bugs fixed

1. **Duplicate home-screen icons over time.** `checkDefaultLayout()` started a
   brand-new collector on the home-items flow every time it ran (once on
   launch, and again on every `onResume`). Each leaked collector could
   independently re-seed the default layout. Now guarded to run at most once.
2. **Icon decoding on the main thread**, contradicting the design doc's own
   "off the main thread" priority. Moved to `IconManager.getAppIconAsync`
   (`Dispatchers.IO`) with an in-memory cache and a fade-in placeholder, so
   scrolling the drawer/home grid never blocks on `PackageManager` calls.
3. **Backup/restore was lossy.** Folders were exported but never restored on
   import; favorites, hidden apps, and custom labels weren't backed up at
   all. Both are now fully round-tripped (folder IDs are remapped on
   restore).
4. **Importing a backup silently wiped the Hidden Apps vault PIN** (it reset
   to blank every time). The PIN is intentionally excluded from the backup
   file for privacy, but the *device's* existing PIN is now preserved across
   an import instead of being cleared.
5. **Vault PIN was stored and compared as plain text.** Added `PinUtils`
   (SHA-256) and fixed the "Change PIN" dialog, which used to prefill the
   stored value (now would've been a hash) into the input field.
6. **"Lock Screen" gesture was selectable but did nothing.** It now uses a
   proper (opt-in) `DeviceAdminReceiver` with the `FORCE_LOCK` policy only,
   falling back to opening Recents if the user hasn't enabled it.
7. **Pinch gesture was configurable in Settings but never actually detected**
   anywhere in the UI. Wired up in `HomeScreen`.
8. **Unused `QUERY_ALL_PACKAGES` permission** removed — `LauncherApps`
   already gets full visibility as the default launcher, so this was an
   unnecessary sensitive permission (and a Play Store review risk).

## New features

- **Icon pack support** (`IconPackManager`): detects installed Nova/ADW/GO
  -compatible icon packs, parses their `appfilter.xml`, and lets you pick one
  from Settings > Appearance.
- **Real device lock** for the Lock Screen gesture (see bug #6), with an
  Enable/Disable toggle in Settings > App Vault & Privacy.
- **Fuzzy search** in the app drawer (`FuzzyMatch`): typo-tolerant,
  abbreviation-aware, relevance-ranked — replacing a plain `contains()`
  check.
- **Haptic feedback** (`HapticUtils`) on recognized gestures and a wrong-PIN
  entry — the `VIBRATE` permission was declared but never actually used
  before.

## UI polish

- Icons now cross-fade in once decoded instead of popping in abruptly.
- Icon-pack picker loads lazily (only when opened) and off the main thread.

## Cleanup

- Removed Firebase (incl. an unused Gemini/AI SDK), Retrofit, OkHttp, Moshi,
  Firebase App Check, and the Secrets Gradle Plugin — all unused leftovers
  from the AI Studio template that contradicted the design doc's own
  "lightweight, low RAM/battery footprint" goal. Deleted `.env.example`
  along with them.
- `README.md` and `metadata.json` updated to match.
- Room DB bumped to version 3 for the schema change (new `iconPackPackage`
  column); the app already uses `fallbackToDestructiveMigration()`, so this
  is a clean upgrade path, not a breaking one.

## Known limitations / good next steps

- Icon pack detection currently relies on the `org.adw.launcher.THEMES`
  intent (the most widely supported convention). A few icon packs use only
  Nova- or GO-specific discovery intents — worth adding as fallbacks if you
  hit one that doesn't show up.
- `lockNow()` requires the user to opt in via Settings; Android has no way
  for a regular app to lock the screen without device-admin rights.
- I could not compile this build myself (no Android SDK/Gradle network access
  in this environment) — I checked it very carefully by hand (brace
  balance, imports, call-site signatures, XML validity) but please do a
  build in Android Studio before you rely on it, and let me know if
  anything doesn't compile so I can fix it fast.
