# Release artifacts

This folder holds the signed, release-ready APK of **Météo**.

| File | Description |
| :--- | :--- |
| `meteo-1.1.0-release.apk` | Signed release build (versionCode 7, versionName 1.1.0), minified with R8 and shrunk for resources. |

## Installation

```bash
adb install -r meteo-1.1.0-release.apk
```

## Signature

The APK is signed with the keystore documented in [`../SIGNING_INFO.txt`](../SIGNING_INFO.txt):

* APK Signature Scheme **v2** and **v3**
* Certificate SHA-1: `EC:4A:B0:B4:31:D1:F1:92:6D:63:0C:5C:D1:17:AF:21:A5:67:F0:93`

Verify it yourself with:

```bash
apksigner verify --verbose --print-certs meteo-1.1.0-release.apk
```

## Rebuilding

```bash
bash ./gradlew :app:assembleRelease
```

The Gradle build expects `keystore.properties` in the repository root. See
[`../SIGNING_INFO.txt`](../SIGNING_INFO.txt) for the exact values and the
step-by-step instructions.
