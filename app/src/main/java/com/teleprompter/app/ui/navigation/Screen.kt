package com.teleprompter.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("sign_in")
    object Home : Screen("home")
    object Editor : Screen("editor?scriptId={scriptId}") {
        fun createRoute(scriptId: String? = null): String {
            return if (scriptId != null) "editor?scriptId=$scriptId" else "editor"
        }
    }
    object Detail : Screen("detail/{scriptId}") {
        fun createRoute(scriptId: String): String = "detail/$scriptId"
    }
    object Search : Screen("search")
    object Folders : Screen("folders")
    object FolderDetail : Screen("folders/{folderId}") {
        fun createRoute(folderId: String): String = "folders/$folderId"
    }
    object ScriptSettings : Screen("script_settings?scriptId={scriptId}") {
        fun createRoute(scriptId: String? = null): String {
            return if (scriptId != null) "script_settings?scriptId=$scriptId" else "script_settings"
        }
    }
    object Themes : Screen("themes")
    object TeleprompterPrompt : Screen("teleprompter/{scriptId}") {
        fun createRoute(scriptId: String): String = "teleprompter/$scriptId"
    }
    object CameraRecording : Screen("camera_recording/{scriptId}") {
        fun createRoute(scriptId: String): String = "camera_recording/$scriptId"
    }
    object PreviewRecording : Screen("preview_recording?videoUri={videoUri}&scriptId={scriptId}&scriptTitle={scriptTitle}") {
        fun createRoute(videoUri: Uri, scriptId: String? = null, scriptTitle: String? = null): String {
            val encodedUri = Uri.encode(videoUri.toString())
            val sId = scriptId ?: ""
            val sTitle = Uri.encode(scriptTitle ?: "")
            return "preview_recording?videoUri=$encodedUri&scriptId=$sId&scriptTitle=$sTitle"
        }
    }
    object Recordings : Screen("recordings")
    object AdvancedSettings : Screen("advanced_settings")
    object CloudSyncProfile : Screen("cloud_sync_profile")
    object ProFeatures : Screen("pro_features")
}
