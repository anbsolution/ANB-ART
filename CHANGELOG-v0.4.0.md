# ANB Android Recovery Tool — v0.4.0

## Upgrade
- Add JVM unit tests for customer-consent gating.
- Add repair-summary tests covering device fields, local-only notice, and blank-note fallback.
- Add recovery-guide safeguard tests.
- Bump Android version to `0.4.0` (`versionCode = 5`).
- Keep the app local-first; no credentials, PINs, passwords, or recovery codes are collected.

## Verification
- Tests and APK build must be confirmed by the GitHub Actions run for this branch/PR.
- Do not treat the patch as verified until `Run unit tests`, `Build debug APK`, and `Upload APK artifact` all succeed.
