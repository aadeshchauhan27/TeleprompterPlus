# Teleprompter Plus — Account & Data Deletion Request

**Application:** Teleprompter Plus  
**Package Name:** `com.gigabytelabs.teleprompter`  
**Developer:** Aadesh Chauhan (Gigabyte Labs)  
**Effective Date:** October 31, 2026  
**Last Updated:** October 31, 2026  
**Contact Email:** [aadeshchauhan27@gmail.com](mailto:aadeshchauhan27@gmail.com)  

---

## 1. Overview & Privacy Commitment

Teleprompter Plus is engineered with an **offline-first, privacy-centric architecture**. By default, all scripts, video recordings, speech timings, and preferences are stored exclusively on your physical device.

If you voluntarily enable the optional **Cloud Sync & Backup** feature by signing in with your Google Account or Email, your scripts and teleprompter display preferences are backed up to our secure Google Cloud Firestore database (`teleprompter_users`). Video recordings **never** leave your device and are never uploaded to our servers.

You have the complete, unqualified right to request the permanent deletion of your account and all associated cloud-stored data at any time.

---

## 2. How to Request Account & Data Deletion

You can request account and data deletion using either of the following two methods:

### Method 1: Instant In-App Deletion (Recommended)

You can purge your data and disconnect your account directly within the app:
1. Open **Teleprompter Plus** on your device.
2. Tap the **Profile / Settings** tab in the bottom navigation.
3. To disable cloud sync:
   - Toggle **Sync & Backup** to **Off** (pauses all cloud synchronization).
4. To delete specific data:
   - Go to **My Scripts**, swipe or select any script, and choose **Delete** (this immediately deletes the script locally and removes it from the cloud).
5. To sign out completely:
   - Tap **Sign Out** at the bottom of the Profile screen to disconnect your Google / Email session.
6. To delete all local records:
   - Go to your Android device **Settings &rarr; Apps &rarr; Teleprompter Plus &rarr; Storage & Cache &rarr; Clear Storage**.

---

### Method 2: Web & Email Deletion Request (Without Needing the App Installed)

If you have uninstalled the app, lost access to your device, or prefer a manual administrator deletion of your account and cloud data:

1. Send an email to **[aadeshchauhan27@gmail.com](mailto:aadeshchauhan27@gmail.com)** with the subject line:
   ```text
   Teleprompter Plus — Account & Data Deletion Request
   ```
2. In the body of the email, please provide:
   * The **email address** or Google Account associated with your Teleprompter Plus account.
   * A brief statement requesting deletion (for example: *"Please permanently delete my Teleprompter Plus account and all associated cloud scripts and profile data."*).
3. **Verification & Timeline:**
   * We will verify the request against our Firebase Authentication and Firestore database records.
   * Your account and all cloud documents (`teleprompter_users/{user}`) will be **permanently deleted within 48 to 72 hours** (and no later than 7 calendar days).
   * You will receive a confirmation email once the purge is complete.

---

## 3. Data Retention & What Is Deleted vs. Kept

When an account deletion request is processed:

| Data Type | Action Taken | Retention Period |
| :--- | :--- | :--- |
| **Account Credentials (Firebase Auth)** | Permanently erased | 0 days (Immediate) |
| **Cloud Scripts & Folders (Firestore)** | Permanently purged | 0 days (Immediate) |
| **User Settings & Display Preferences** | Permanently purged | 0 days (Immediate) |
| **Video Recordings & Audio Files** | Stored **only locally on your device**; never uploaded to servers | User controlled |
| **Aggregated / Diagnostic Logs** | None kept (No third-party trackers or ad SDKs) | N/A |

No user data is retained for marketing, profiling, or monetization purposes.

---

## 4. Contact Information

For any questions regarding account or data deletion, reach out directly to:

* **Developer:** Aadesh Chauhan  
* **Organization:** Gigabyte Labs  
* **Email:** [aadeshchauhan27@gmail.com](mailto:aadeshchauhan27@gmail.com)  
* **Repository:** [https://github.com/aadeshchauhan27/TeleprompterPlus](https://github.com/aadeshchauhan27/TeleprompterPlus)  
