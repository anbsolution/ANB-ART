# ANB Android Recovery Tool — v0.3.1

## Fixes and upgrade
- Fix the Android TextView selectable property compile error by using `isTextSelectable`.
- Add an explicit Share Local Repair Summary button. It remains disabled until the technician confirms customer authorization and creates the local summary.
- Sharing uses Android's system chooser only after a user tap; there is no background or automatic upload. The user is responsible for selecting the destination.
- Bump Android `versionCode` to 4 and `versionName` to `0.3.1`.
- Preserve the dark Recovery Pro UI, device diagnostics, local summary creation, official recovery checklist, Google Account Recovery link, and existing launcher icons.

## Verification status
- v0.3.0 GitHub Actions run failed at Kotlin compilation on `textIsSelectable`.
- This patch has not been built yet. GitHub Actions must pass unit tests, assembleDebug, and upload the APK artifact before v0.3.1 is considered verified.
