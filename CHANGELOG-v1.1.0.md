# ANB Android Recovery Tool — v1.1.0

## Privacy-aware sharing
- Adds an enabled-by-default option to redact the device code in the outgoing copy.
- Keeps the original local report unchanged.
- Adds a unit test for redaction and preservation of the original report.
- Updates version to 1.1.0 / versionCode 12.

## Safety
- Sharing remains an explicit user action after review and authorization.
- No automatic upload, screen-lock bypass, encryption bypass, account bypass, or FRP bypass.
- Device testing remains separate from GitHub Actions.
