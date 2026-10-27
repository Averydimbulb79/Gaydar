# gaydar · Android

An offline, adults-only camera comedy toy with cyan radar graphics, pink controls, and random fictional alter egos. No photo analysis or real assessments.

## Features

- Live camera, front/back switch, and system photo import.
- Theatrical scan and 120 unique personas (30 descriptors × 4 roles), with no immediate repeat.
- Fictional Big Energy score and Fantasy Wildcard, independent of the image.
- Reroll, retake, save JPEG to Pictures/Gaydar, and Android share sheet. Sharing also saves the card to Pictures/Gaydar.
- Adult/participant consent gate on every app launch. Fiction labels are embedded in exported cards.
- No network permission, analytics, accounts, or uploads. Unsaved photos remain in memory. Portrait orientation; sessions reset if Android kills the process.

## Install an APK

Open **Actions → Android APK → a successful run → Artifacts → gaydar-debug-apk**. Download and unzip the artifact, then open `app-debug.apk` on an Android 10+ device. Allow installation from your file manager when Android prompts. This debug build is for testing.

## Build locally

Requires JDK 17, Android SDK 35, and Gradle 8.9. Open the project in Android Studio or install Gradle 8.9 and run:

```shell
gradle testDebugUnitTest lintDebug assembleDebug bundleRelease
```

CI pins Gradle 8.9; no Gradle wrapper is checked in yet. APK: `app/build/outputs/apk/debug/app-debug.apk`. Unsigned store bundle: `app/build/outputs/bundle/release/app-release.aab`. Production signing and store preparation are required before publication. Never commit signing keys.

## Validation

Tests check catalog uniqueness, role distribution, energy ranges, and reroll behavior. CI runs tests, Android lint, debug APK assembly, and unsigned release bundle assembly.

Physical-device checklist: consent accept/decline; deny/grant camera; rear/front preview and capture; import rotated/large images; cancel picker; background/resume during scan; reroll/retake; save/share with fiction label; small display and large system font. Camera capture uses the viewfinder resolution. No image recognition is present.
