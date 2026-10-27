# ANB Android Recovery Tool — v0.6.0

## Summary integrity
- Tracks the exact technician notes used to generate the visible repair summary.
- Adds a final share guard that requires the current notes to match the generated summary and customer consent to remain checked.
- Existing listeners continue to hide and invalidate a summary when notes or consent change.
- Adds unit tests for unchanged notes, changed notes, missing summary snapshot, and unchecked consent.
- Updates app version to 0.6.0 / versionCode 7.

## Scope and safety
- Existing diagnostics and official account/device recovery guidance remain in place.
- No lock, encryption, account, or FRP bypass is added.
- No automatic data upload is added.

## Verification status
- This script only prepares a local branch; it does not push or build an APK.
- Verify unit tests, debug APK build, and artifact upload in GitHub Actions after pushing.
- Physical-device installation and interaction testing remain separate.
