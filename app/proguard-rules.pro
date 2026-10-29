# TelePrompter ProGuard Rules

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.video.** { *; }
-keep class androidx.camera.view.** { *; }
-dontwarn androidx.camera.**

# Media3 ExoPlayer
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.common.** { *; }
-dontwarn androidx.media3.**

# Firebase & Play Services
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# App Data Models
-keep class com.teleprompter.app.data.local.entity.** { *; }
-keep class com.teleprompter.app.core.model.** { *; }
-keep class com.teleprompter.app.core.auth.** { *; }
