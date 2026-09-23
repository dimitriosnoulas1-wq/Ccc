# CryptoCycles Agent Rules

- Every time a new version is built, automatically compile both the Debug APK, Release APK, and Release AAB.
- Store all generated build files in the `app/release_builds` folder.
- Clean up old release files from previous versions, keeping only the requested target versions (e.g., v108 and v109).
- Strictly respect user intent and ensure zero-regression on all localized strings, UI components, and theme constraints (Galaxy Dark).
