# Teleprompter Plus — Production Release Document (v1.0.0)

> **Application**: Teleprompter Plus  
> **Package ID**: `com.teleprompter.app`  
> **Release Target**: Production / Google Play Store  
> **Version Name**: `1.0.0` | **Version Code**: `1`  
> **Target SDK**: `36` (Android 15+) | **Min SDK**: `26` (Android 8.0 Oreo+)  
> **Build Date**: October 30, 2026  
> **Git Repository**: https://github.com/aadeshchauhan27/TeleprompterPlus.git  

---

## 1. Executive Summary

**Teleprompter Plus** is a professional, high-performance Android teleprompter application engineered for solo video creators, journalists, podcasters, keynote speakers, and educators. Built entirely using modern Android architecture components and 100% Jetpack Compose, the application combines smooth 60 FPS hardware-accelerated text scrolling, simultaneous 1080p/4K video recording with CameraX, a custom studio countdown pacing engine, tactile haptic feedback across all key interactions, and offline-first local Room storage with cloud synchronization isolated to dedicated Firestore collections.

This document serves as the official release manifest for the **v1.0.0 Production Release**, documenting binary artifacts, cryptographic signatures, feature specifications, architecture topology, permissions, and deployment procedures.

---

## 2. Release Artifacts & Cryptographic Checksums

Both standalone signed APK and Google Play Store App Bundle (AAB) formats have been compiled, verified with `apksigner`, and signed using the production RSA-2048 release key.

| Artifact Type | File Name | Size | SHA-256 Checksum | MD5 Checksum |
| :--- | :--- | :--- | :--- | :--- |
| **Signed Release APK** | `app/build/outputs/apk/release/app-release.apk` | 20.24 MB (20,238,515 B) | `EFA5AF6898A79C6F04889A60EF63234D52DE2ED2C7995EA3CAA34D57EBC5097F` | `592669EA604A1B1BD0DF7970C9CFE788` |
| **Signed App Bundle** | `app/build/outputs/bundle/release/app-release.aab` | 19.39 MB (19,392,146 B) | `1E51B07097149CD819052C8EC66310D25687976F69BCA108C486E641334C2D67` | `DF41DAB48680D0AB1BEB6CC2E8DEF23D` |

---

## 3. Cryptographic Signature & Certificate Information

The release builds are signed with APK Signature Scheme v2 using a dedicated 2048-bit RSA key valid through the year 2054.

```
Keystore File:         release.keystore
Key Alias:             teleprompter_release
Key Algorithm:         2048-bit RSA
Signature Algorithm:   SHA384withRSA
Certificate DN:        CN=Teleprompter Plus, OU=Mobile, O=Teleprompter Plus, L=Mumbai, ST=Maharashtra, C=IN
Validity:              Fri Oct 30 23:23:17 IST 2026 — Tue Mar 17 23:23:17 IST 2054 (10,000 Days)
Verification Scheme:   APK Signature Scheme v2 (apksigner verified: true)
```

### Certificate Fingerprints

Add these certificate fingerprints to the **Google Cloud Console** under your OAuth 2.0 Client IDs and **Firebase Console** (under Project Settings -> Android App) to enable production Google Sign-In and Cloud Firestore operations:

- **SHA-1 Digest**:
  ```
  08:21:2E:94:56:F8:33:B5:05:44:2E:C9:5F:00:91:AE:03:ED:C9:4F
  ```
- **SHA-256 Digest**:
  ```
  6F:A0:61:15:49:B1:21:06:1F:C8:5D:87:62:FB:D3:EF:68:B9:CE:23:48:F2:2A:F9:59:E4:00:1C:75:56:AE:A0
  ```

---

## 4. Architecture & Technology Stack

The application strictly adheres to the official Android App Architecture guidelines (Clean Architecture with MVVM + Repository pattern) and Kotlin 2.0:

- **UI & Toolkit**: 100% Jetpack Compose using Material 3 and custom luxury monochrome typography and surfaces.
- **Dependency Injection**: Modular App Container architecture (`AppContainer.kt`).
- **Local Persistence**: Room SQLite database (`ScriptDao.kt`, `ScriptEntity.kt`) supporting reactive `Flow` streams.
- **Media & Hardware**:
  - **CameraX 1.4**: Lifecycle-aware viewfinder, video recording (`QualitySelector.HIGHEST`), torch/flash toggle, and lens switching.
  - **Media3 ExoPlayer**: Hardware-accelerated local video playback with auto-looping in preview cards.
- **Haptic Engine**: Custom dual-engine `AppHaptics` utilizing `View.performHapticFeedback` (system-clock aligned) and `Vibrator`/`VibratorManager` fallback.

---

## 5. Comprehensive Feature Breakdown (v1.0.0)

### 5.1 Luxury Monochrome Visual Experience
- **Light & Dark Theme Parity**: Designed with high-contrast monochrome elegance. Pure whites and light grays in light mode; deep pitch black (`#070B14`) in dark mode.
- **Animated Speed-Dial FAB**: Expanding floating action button with spring animations presenting two distinct choices:
  - **Paste from Clipboard**: Automatically reads system clipboard and creates a pre-filled script.
  - **Blank Script**: Opens fresh editor.
- **Dynamic Filter Tabs**: Filter scripts across All, Favorites, and custom folders with instantaneous reactive counts.

### 5.2 Studio Video Recording & Camera Engine
- **Simultaneous Capture & Read**: Overlay teleprompter text scrolling directly on top of the live camera preview.
- **Studio Countdown Engine**: Configurable (0s, 3s, 5s, 10s) countdown pacing featuring distinct tactile haptic feedback on each tick (3... 2... 1...) and a heavy studio pulse on "RECORD".
- **Hardware Controls**: Instant front/back camera lens switching and torch toggle for low-light recording.
- **Video Review & Export**: Direct review in embedded ExoPlayer with share and save to device gallery options.

### 5.3 Pro Teleprompter Engine
- **Fluid 60 FPS Scrolling**: Monotonic frame-clock scrolling engine supporting continuous speed ranges (10 to 100 WPM).
- **Text Customization**:
  - Font sizes from 16sp to 56sp.
  - Typefaces: Sans-Serif, Serif, Monospace.
  - Custom reading width percentages (50% to 100%) and reading box positioning.
  - Color swatches: High-contrast white, warm yellow, cyan, soft green, pink, and lavender.
- **Mirror Mode**: Inverts text horizontally for optical beam-splitter glass and professional teleprompter rigs.
- **Eye Contact Safe Area Guide**: Adjustable reading highlight bar to keep speaker eyes aligned directly with camera aperture.

### 5.4 Offline-First & Multi-App Cloud Isolation
- **100% Functional Offline**: All scripts, settings, and folders are stored locally in Room SQLite; zero network required for core functionality.
- **Multi-App Isolated Cloud Sync**:
  - Stores data strictly inside dedicated top-level collection `teleprompter_users/{userEmail}/scripts/{scriptId}`.
  - Zero collision with other apps in the same Firebase project (e.g. WakeUp's `sleepSessions` or Phone Doctor's `reports`).
  - Seamless automatic backwards-compatible migration from legacy tables.

---

## 6. Permissions & Privacy Manifest

| Permission | Protection Level | Purpose |
| :--- | :--- | :--- |
| `android.permission.CAMERA` | Dangerous (Runtime) | Capturing front and rear video while presenting teleprompter scripts. |
| `android.permission.RECORD_AUDIO` | Dangerous (Runtime) | Studio sound recording synchronized with video. |
| `android.permission.VIBRATE` | Normal | Providing tactile feedback for countdown ticks, record start, and button interactions. |
| `android.permission.INTERNET` | Normal | Secure synchronization of scripts and preferences with Firebase Cloud Firestore. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Detecting network reachability to safely pause and resume background synchronization. |

---

## 7. Deployment, Installation & Verification Guide

### 7.1 Direct Testing on Physical Device via ADB

To install and verify the signed release build on any connected Android device:

```powershell
# 1. Ensure target device is attached and authorized
adb devices

# 2. If a debug version is already on the device, uninstall it first to avoid certificate mismatch:
adb uninstall com.teleprompter.app

# 3. Stream-install the signed release APK
adb install "app\build\outputs\apk\release\app-release.apk"

# 4. Launch the application
adb shell am start -n com.teleprompter.app/.MainActivity
```

### 7.2 Google Play Console Submission (App Bundle)

1. Open the [Google Play Console](https://play.google.com/console).
2. Select your application or create a new application named **Teleprompter Plus**.
3. Navigate to **Release -> Production** (or **Testing -> Internal Testing**).
4. Click **Create new release**.
5. Upload the App Bundle file:  
   `app\build\outputs\bundle\release\app-release.aab`
6. Provide release notes for version `1.0.0`:
   ```
   Welcome to Teleprompter Plus v1.0.0!
   - High-contrast luxury monochrome interface with dark mode
   - Fluid 60 FPS scrolling teleprompter with mirror mode & safe area guides
   - Simultaneous 1080p/4K video recording with tactile studio countdown
   - Offline-first local storage with optional isolated cloud backup
   - Animated speed-dial FAB for instant clipboard paste & creation
   ```
7. Review and rollout release!

---

## 8. Summary of Release Verification

- **Lint Vital Analysis**: Completed with **0 errors**.
- **Kotlin 2.0 Bytecode Compilation**: Completed with **0 errors**.
- **APK Signature Verification**: Passed (`apksigner verify -v` reports `Verifies: true` under Scheme v2).
- **Git Branch Status**: Up to date on `main` with all signing configs and haptics tracked.

*Signed off by*: **Lead Mobile Engineer / Antigravity Agent**  
*Release Timestamp*: **October 30, 2026 — 23:33 IST**
