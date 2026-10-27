# ANB Android Recovery Tool — v0.5.0

## Safety and reliability
- Editing technician notes invalidates the previously generated summary.
- Changing the customer-consent checkbox invalidates the previously generated summary.
- Sharing stays disabled until a fresh summary is generated with consent checked.
- Add a regression test for multiline technician notes.
- Version 0.5.0; versionCode 6.

## Verification
- Confirm unit tests and debug APK build in GitHub Actions.
- This patch is not verified until the workflow passes.
- No lock, encryption, account, or FRP bypass is added.
