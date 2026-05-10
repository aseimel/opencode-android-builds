# OpenCode Android Personal WebView Build

This repository builds a personal Android WebView wrapper for OpenCode Web.

It replaces the previously attempted upstream Android client because that project currently builds only with heavy patches and does not appear to be a functional client.

Obtainium should track this repository's GitHub releases.

## What It Does

- Builds a small WebView app with package `ai.opencode.mobile`.
- Signs the APK with your private keystore from GitHub Secrets.
- Publishes a GitHub Release with the signed APK attached.
- Handles HTTP Basic Auth with username `armin` and the password entered in the app.

## Server Command

Use OpenCode Web, not the headless server:

```bash
OPENCODE_SERVER_USERNAME=armin OPENCODE_SERVER_PASSWORD='<password>' opencode web --hostname 0.0.0.0 --port 4096
```

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
