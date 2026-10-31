# Privacy Policy for Teleprompter Plus

**Effective Date:** October 31, 2026  
**Last Updated:** October 31, 2026  
**Developer:** Gigabyte Labs (Aadesh Chauhan)  
**Application:** Teleprompter Plus (`com.gigabytelabs.teleprompter`)  
**Contact:** [aadeshchauhan27@gmail.com](mailto:aadeshchauhan27@gmail.com)  
**GitHub:** [https://github.com/aadeshchauhan27](https://github.com/aadeshchauhan27)  

---

## 1. Introduction & Core Privacy Pledge

Welcome to **Teleprompter Plus** ("Teleprompter Plus", "we", "us", or "our"), developed by **Gigabyte Labs**.

Teleprompter Plus is engineered with an **Offline-First, Privacy-Centric Architecture**. We believe that your video creations, speeches, presentation scripts, and recordings are your exclusive intellectual property.

* **No Data Monetization:** We never sell, rent, monetize, broker, or trade your personal information, scripts, or video recordings to third parties or data brokers.
* **No Ads or Behavioral Tracking:** The application contains zero third-party advertising SDKs, zero user tracking networks, and zero invasive telemetry.
* **Local Storage Primary:** All scripts, folder structures, display preferences, and video recordings are stored directly and securely on your physical device.

---

## 2. Information We Process & How It Is Used

### A. Scripts, Content & Teleprompter Settings (Local Only)
* **What We Store:** Script titles, text content, folder categories, favorite tags, word counts, estimated reading times, and presentation preferences (scrolling speeds from 10 to 100 WPM, font sizes from 16 to 56sp, typography styles, text alignment, mirror mode, safe area guide positioning, and color swatches).
* **Storage Location:** Exclusively within your device's private application sandbox via an encrypted/sandboxed SQLite Room database.
* **Transmission:** Script data is processed 100% locally on your device and is never transmitted over the network unless you explicitly enable optional Cloud Backup.

### B. Camera & Studio Video Recording (On-Device Processing)
* **Permission Requested:** `android.permission.CAMERA`
* **Purpose:** Allows you to record video using your device's front or rear camera while simultaneously viewing and reading your scrolling teleprompter script.
* **Processing & Storage:**
  * Live camera frames are rendered in real-time onto an on-device viewfinder via Google Jetpack CameraX.
  * Recorded video files (.mp4) are encoded locally using device hardware encoders and saved directly to your device's private app storage or public media gallery.
  * **We do not stream, upload, or transmit your camera feeds or video recordings to any server.**

### C. Microphone & Audio Recording (On-Device Processing)
* **Permission Requested:** `android.permission.RECORD_AUDIO`
* **Purpose:** Captures audio from your device's microphone simultaneously during video recording sessions to produce synchronized sound.
* **Processing & Storage:**
  * Audio streams are packaged directly into your local video recording file (.mp4).
  * **We do not perform speech-to-text analysis, voice profiling, biometric identification, or cloud transcription.**
  * Audio is never recorded outside of active video filming sessions initiated by you.

### D. Tactile Haptic Feedback
* **Permission Requested:** `android.permission.VIBRATE`
* **Purpose:** Drives subtle, physical vibration impulses during the studio countdown (3, 2, 1, RECORD), play/pause toggles, and stepper adjustments.
* **Transmission:** 100% hardware-driven on-device; no data is logged or transmitted.

---

## 3. Optional Cloud Synchronization & Account Backup

Teleprompter Plus provides an **optional** cloud synchronization service:

* **Default State: DISABLED by default.** Teleprompter Plus functions completely offline without an internet connection or account.
* **Authentication:** If you choose to enable cloud backup, authentication is securely handled using Google Sign-In via Firebase Authentication.
* **App-Isolated Cloud Storage:**
  * When enabled, scripts and display settings are backed up to Google Cloud Firestore in an isolated top-level collection:  
    `teleprompter_users/{userEmail}/scripts/{scriptId}`
  * Data is completely isolated and never commingled or shared with any other applications.
* **User Control:** You can disable cloud sync or sign out at any time directly from the **Profile & Cloud Sync** screen.

---

## 4. Android System Permissions Summary

| Permission | Category | Purpose |
| :--- | :--- | :--- |
| `android.permission.CAMERA` | Dangerous (Runtime) | Live video preview and video recording while presenting scripts. |
| `android.permission.RECORD_AUDIO` | Dangerous (Runtime) | Recording audio synchronized with video footage. |
| `android.permission.VIBRATE` | Normal | Providing tactile feedback for countdown ticks, recording triggers, and UI controls. |
| `android.permission.INTERNET` | Normal | Used strictly for optional Firebase Cloud Sync when signed in. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Detecting network connectivity to intelligently pause/resume cloud sync. |
| `android.permission.WAKE_LOCK` | Normal | Keeps the screen illuminated while reading scripts during teleprompter sessions. |

---

## 5. Third-Party Services

When you voluntarily opt into Cloud Sync, the following Google services are utilized:
* **Google Play Services:** [Google Play Privacy Policy](https://policies.google.com/privacy)
* **Firebase Authentication:** Secure identity verification. [Firebase Privacy Overview](https://firebase.google.com/support/privacy)
* **Cloud Firestore:** Encrypted cloud backup storage.

We do not embed any third-party marketing, analytics, or behavioral tracking SDKs.

---

## 6. Data Retention, Security & Deletion Rights

You have complete control over your personal data:

1. **Local Data Deletion:** You can delete individual scripts, past recordings, or clear all app data at any time through Android Settings &rarr; Apps &rarr; Teleprompter Plus &rarr; Clear Storage.
2. **Cloud Data Deletion:** You can delete your scripts from the cloud directly within the application, or request complete account and data removal by contacting us at the email below.
3. **Data in Transit:** All network communications between the application and Google Cloud Firestore are protected using industry-standard Transport Layer Security (TLS / HTTPS) encryption.

---

## 7. Children's Privacy

Teleprompter Plus does not knowingly collect, solicit, or store personal information from children under the age of 13 (or under 16 in the European Union). The application is intended for general audiences as a content creation and speech utility.

---

## 8. Changes to This Privacy Policy

We may update our Privacy Policy periodically. We will notify users of any material changes by updating the "Last Updated" date at the top of this document. We recommend checking this page occasionally to review any updates.

---

## 9. Contact Us

If you have any questions, feedback, or requests regarding this Privacy Policy or your personal data, please contact:

**Gigabyte Labs**  
Developer: Aadesh Chauhan  
Email: [aadeshchauhan27@gmail.com](mailto:aadeshchauhan27@gmail.com)  
GitHub: [https://github.com/aadeshchauhan27/TeleprompterPlus](https://github.com/aadeshchauhan27/TeleprompterPlus)
