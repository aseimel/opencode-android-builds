# OpenCode Android Automatic Builds

This repository is intended to contain only automation. It periodically builds signed APK releases from:

```text
https://github.com/mulkymalikuldhrs/opencode-android
```

Obtainium should track this repository's GitHub releases, not the upstream repository.

## What It Does

- Checks upstream `main` every day.
- Skips the build if the current upstream commit already has a release.
- Builds the Android APK from upstream source.
- Sets `versionCode` from the upstream commit timestamp so Android updates work.
- Signs the APK with your private keystore from GitHub Secrets.
- Publishes a GitHub Release named `upstream-<commit>` with the signed APK attached.
- Hardcodes the OpenCode Basic Auth username to `armin` for this personal build.

## Required GitHub Secrets

Create these secrets in this repository under `Settings -> Secrets and variables -> Actions`:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Generate a release keystore once:

```bash
keytool -genkeypair \
  -v \
  -keystore opencode-release.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -alias opencode
```

Create the base64 secret value:

```bash
base64 -w 0 opencode-release.jks
```

Use these values:

```text
ANDROID_KEYSTORE_BASE64 = output from base64 command
ANDROID_KEYSTORE_PASSWORD = keystore password
ANDROID_KEY_ALIAS = opencode
ANDROID_KEY_PASSWORD = key password
```

Do not lose the keystore or passwords. Future APK updates must be signed with the same key.

## Obtainium Setup

- Source: GitHub
- App source URL: this repository URL
- Release asset filter: `.apk`
- Use latest release: enabled

## Manual Run

Open the `Actions` tab, select `Build and release upstream APK`, then run the workflow manually.

Use `force_rebuild` only if you want to replace the release for the current upstream commit.
