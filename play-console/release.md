# Release guide

## One-time setup

1. Create an upload key outside the repository and back it up.
2. Set these environment variables:

```text
NIGHT_SCREEN_UPLOAD_STORE_FILE=/absolute/path/to/upload.jks
NIGHT_SCREEN_UPLOAD_STORE_PASSWORD=...
NIGHT_SCREEN_UPLOAD_KEY_ALIAS=...
NIGHT_SCREEN_UPLOAD_KEY_PASSWORD=...
```

3. Keep all key files and passwords out of Git.
4. Enable Play App Signing when creating the first Play release.

## Build

Every uploaded release needs a new integer version code:

```text
./gradlew bundleRelease \
  -PreleaseVersionCode=1 \
  -PreleaseVersionName=1.0
```

The bundle is written to:

```text
app/build/outputs/bundle/release/app-release.aab
```

The build is unsigned when the four upload-key environment variables are not
set. Do not upload an unsigned bundle.

## Before production

1. Publish `PRIVACY.md` at a public HTTPS URL.
2. Add the support email and privacy URL in Play Console.
3. Upload the store icon, feature graphic, and at least two screenshots.
4. Complete `app-content.md`.
5. Submit the declaration and video from `foreground-service.md`.
6. Test through the internal track.
7. If required for the developer account, complete the 12-person, 14-day
   closed test.
