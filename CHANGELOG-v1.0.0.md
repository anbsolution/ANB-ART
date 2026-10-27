# ANB Android Recovery Tool — v1.0.0

## Consolidated share safety guard
- Combines consent, technician-notes equality, exact report equality,
  and explicit review acknowledgement into one testable guard.
- Adds unit tests for valid and rejected sharing conditions.
- Updates version to 1.0.0 / versionCode 11.

## Safety
- Reports remain local until the user explicitly chooses a share target.
- No automatic uploads or lock, encryption, account, or FRP bypass.
- Device interaction testing remains separate from GitHub CI.
