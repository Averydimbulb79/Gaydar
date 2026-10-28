# gaydar

**The universe has a type.** An Android camera prank for consenting adults, with neon radar visuals and randomly generated fictional alter egos. Psychology, face feng shui, and divine messages are the parody premise—not real analysis.

[Download the Android APK](https://raw.githubusercontent.com/Averydimbulb79/Gaydar/5d8001f5517b523d21452e9b0752a033d1854672/docs/assets/gaydar-v0.1.0.apk) · [Landing-page source](https://github.com/Averydimbulb79/Gaydar/tree/codex/android-app/docs) · [App and website pull request](https://github.com/Averydimbulb79/Gaydar/pull/1)

## Project status

The Android app, landing page, artwork, and bundled APK are on `codex/android-app`, in PR #1. The app compiles and has passed unit tests and Android lint. Physical-phone testing remains necessary. The APK is a v0.1.0 debug test build, not a Google Play release.

**GitHub Pages hosting has not been configured by this project setup.** Uploading HTML to GitHub does not publish a website automatically. The repository owner must select a Pages publishing source. The private Sites preview is separate from GitHub Pages.

## Publish the landing page on GitHub Pages

1. Open [Settings → Pages](https://github.com/Averydimbulb79/Gaydar/settings/pages) while signed into the repository owner's GitHub account.
2. Under **Build and deployment**, choose **Deploy from a branch**.
3. Select **codex/android-app**, choose **/docs**, and click **Save**. This publishes the existing page without requiring the app pull request to be merged first.
4. Wait for GitHub's Pages deployment to finish. GitHub will display the live address in those settings.
5. After merging PR #1, change the publishing branch to **main**, keeping **/docs**.

The page is buildless HTML/CSS/JavaScript. Its relative asset and APK links work under the repository's Pages path. Once hosted publicly, visitors can download the bundled APK without signing into GitHub.

## Install the Android app

Requires **Android 10 or newer** and adult participants aged **18+**.

1. Download the APK using the link above on your Android phone.
2. Open the downloaded file. If Android prompts, allow installation from the browser or file manager you used.
3. Install and open gaydar, then review the adult/participant consent prompt. Camera access is optional; photo import is available.

Only install this test build if you trust its source. You can turn off the installation permission afterward. The bundled file's SHA-256 is `670023df609f000f890efa37d65d71b5d29406e545354f8e3d4e73cca7c4a90b`.

Fresh build artifacts are also available from successful runs of [Android APK](https://github.com/Averydimbulb79/Gaydar/actions): unzip `gaydar-debug-apk` and open `app-debug.apk`.

## Features

- Live camera, front/back switch, and system photo import.
- Futuristic scan animation and 120 fictional personas: 30 descriptors across top, bottom, flex, and side, without immediate repeats.
- Random Big Energy scores and Fantasy Wildcards, generated independently of photos.
- Reroll, retake, save JPEG cards, and share through Android.
- Permanent fiction labels on exported cards.
- Responsive landing page with a random-persona demo, direct APK download, installation instructions, and privacy information. The artwork is labeled as an interface concept.

## Privacy and consent

Nothing about a person's sexuality, anatomy, or preferences is inferred from their picture. Every result is made up for entertainment.

Everyone pictured must be 18+ and agree to participate. Keep fiction labels visible, and get permission before sharing a card. The app has no network permission, accounts, analytics, or uploads. Unsaved photos stay in memory for the session. Saving or sharing writes a card to `Pictures/Gaydar`; sharing then opens Android's share sheet.

## Source and local development

Check out `codex/android-app` until PR #1 is merged. The Android project is at the repository root. The static website is in `docs/`, and the downloadable APK is in `docs/assets/`.

Android builds require **JDK 17**, **Android SDK 35**, and **Gradle 8.9**:

```shell
gradle testDebugUnitTest lintDebug assembleDebug bundleRelease
```

CI pins Gradle 8.9; no Gradle wrapper is checked in yet. Outputs:

- Test APK: `app/build/outputs/apk/debug/app-debug.apk`
- Unsigned store bundle: `app/build/outputs/bundle/release/app-release.aab`

Production signing and store preparation are required before publication. Never commit signing keys.

To preview the landing page, serve `docs/` with any static web server; no dependency installation or build is needed. When updating the downloadable APK, replace the verified file and update version labels, links, and its checksum.

## Validation

Android tests cover persona uniqueness, role distribution, energy ranges, and reroll behavior. CI runs those tests, Android lint, APK assembly, and unsigned bundle assembly. Landing-page desktop/mobile layouts, its random demo, installation disclosure, local asset links, JavaScript syntax, and APK download/checksum were checked.

Before an app release, test consent accept/decline, camera permissions, front/rear capture, rotated and large imports, picker cancellation, background/resume during scanning, reroll/retake, saving/sharing, small displays, and large system fonts on physical devices. Camera captures currently use viewfinder resolution. Sessions reset if Android kills the process.
