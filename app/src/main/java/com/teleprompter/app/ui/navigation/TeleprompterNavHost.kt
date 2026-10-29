package com.teleprompter.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teleprompter.app.TelePrompterApp
import com.teleprompter.app.ui.AppViewModelProvider
import com.teleprompter.app.ui.screens.auth.SignInScreen
import com.teleprompter.app.ui.screens.camera.CameraRecordingScreen
import com.teleprompter.app.ui.screens.camera.CameraRecordingViewModel
import com.teleprompter.app.ui.screens.camera.PreviewRecordingScreen
import com.teleprompter.app.ui.screens.detail.ScriptDetailScreen
import com.teleprompter.app.ui.screens.detail.ScriptDetailViewModel
import com.teleprompter.app.ui.screens.editor.EditorViewModel
import com.teleprompter.app.ui.screens.editor.ScriptEditorScreen
import com.teleprompter.app.ui.screens.folders.FolderScreen
import com.teleprompter.app.ui.screens.folders.FolderViewModel
import com.teleprompter.app.ui.screens.home.HomeScreen
import com.teleprompter.app.ui.screens.home.HomeViewModel
import com.teleprompter.app.ui.screens.pro.ProFeaturesScreen
import com.teleprompter.app.ui.screens.profile.CloudSyncProfileScreen
import com.teleprompter.app.ui.screens.recordings.RecordingsScreen
import com.teleprompter.app.ui.screens.recordings.RecordingsViewModel
import com.teleprompter.app.ui.screens.search.SearchScreen
import com.teleprompter.app.ui.screens.search.SearchViewModel
import com.teleprompter.app.ui.screens.settings.AdvancedSettingsScreen
import com.teleprompter.app.ui.screens.settings.ScriptSettingsScreen
import com.teleprompter.app.ui.screens.settings.ScriptSettingsViewModel
import com.teleprompter.app.ui.screens.splash.SplashScreen
import com.teleprompter.app.ui.screens.teleprompter.TeleprompterPromptScreen
import com.teleprompter.app.ui.screens.themes.ThemesScreen

@Composable
fun TeleprompterNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // 1. Splash Screen (Screen 1 in mockup)
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Sign In Screen (Screen 2 in mockup)
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen (Screen 4 in mockup)
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCreateScript = {
                    navController.navigate(Screen.Editor.createRoute(null))
                },
                onNavigateToScriptDetail = { scriptId ->
                    navController.navigate(Screen.Detail.createRoute(scriptId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToFolders = {
                    navController.navigate(Screen.Folders.route)
                },
                onNavigateToRecordings = {
                    navController.navigate(Screen.Recordings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.CloudSyncProfile.route)
                },
                onNavigateToPro = {
                    navController.navigate(Screen.ProFeatures.route)
                }
            )
        }

        // 4. Script Editor (Screen 4 / Distraction-free)
        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory)
            ScriptEditorScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Script Detail & Management (Screen 10 in mockup)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                }
            )
        ) {
            val viewModel: ScriptDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
            ScriptDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { scriptId ->
                    navController.navigate(Screen.Editor.createRoute(scriptId))
                },
                onStartTeleprompter = { scriptId ->
                    navController.navigate(Screen.TeleprompterPrompt.createRoute(scriptId))
                },
                onStartRecording = { scriptId ->
                    navController.navigate(Screen.CameraRecording.createRoute(scriptId))
                },
                onNavigateToRecordings = {
                    navController.navigate(Screen.Recordings.route)
                }
            )
        }

        // 6. Search Screen
        composable(Screen.Search.route) {
            val viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToScriptDetail = { scriptId ->
                    navController.navigate(Screen.Detail.createRoute(scriptId))
                }
            )
        }

        // 7. Folders Screen (Screen 11 in mockup)
        composable(Screen.Folders.route) {
            val viewModel: FolderViewModel = viewModel(factory = AppViewModelProvider.Factory)
            FolderScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToScriptDetail = { scriptId ->
                    navController.navigate(Screen.Detail.createRoute(scriptId))
                }
            )
        }

        // 8. Script Settings Screen (Screen 5 in mockup)
        composable(
            route = Screen.ScriptSettings.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: ScriptSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            ScriptSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 9. Themes Screen (Screen 12 in mockup)
        composable(Screen.Themes.route) {
            val viewModel: ScriptSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            ThemesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 10. Teleprompter Reader & Live Overlay
        composable(
            route = Screen.TeleprompterPrompt.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                }
            )
        ) {
            val scriptDetailViewModel: ScriptDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val settingsViewModel: ScriptSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val detailState by scriptDetailViewModel.uiState.collectAsState()
            val script = detailState.script

            TeleprompterPromptScreen(
                scriptTitle = script?.title ?: "TelePrompter",
                scriptContent = script?.content ?: "",
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenFullSettings = {
                    navController.navigate(Screen.ScriptSettings.createRoute(script?.id))
                },
                onOpenThemes = {
                    navController.navigate(Screen.Themes.route)
                }
            )
        }

        // 11. Camera Recording & Live Teleprompter HUD (Mockup Screen 6 & Screen 8)
        composable(
            route = Screen.CameraRecording.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                }
            )
        ) {
            val cameraViewModel: CameraRecordingViewModel = viewModel(factory = AppViewModelProvider.Factory)
            CameraRecordingScreen(
                viewModel = cameraViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = { videoUri, scriptId, scriptTitle ->
                    navController.navigate(Screen.PreviewRecording.createRoute(videoUri, scriptId, scriptTitle))
                }
            )
        }

        // 12. Preview Recording Screen (Mockup Screen 9)
        composable(
            route = Screen.PreviewRecording.route,
            arguments = listOf(
                navArgument("videoUri") {
                    type = NavType.StringType
                },
                navArgument("scriptId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("scriptTitle") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val videoUriString = backStackEntry.arguments?.getString("videoUri") ?: ""
            val videoUri = Uri.parse(Uri.decode(videoUriString))
            val scriptId = backStackEntry.arguments?.getString("scriptId")?.ifBlank { null }
            val scriptTitleRaw = backStackEntry.arguments?.getString("scriptTitle")
            val scriptTitle = if (!scriptTitleRaw.isNullOrBlank()) Uri.decode(scriptTitleRaw) else null
            val app = LocalContext.current.applicationContext as TelePrompterApp

            PreviewRecordingScreen(
                videoUri = videoUri,
                scriptId = scriptId,
                scriptTitle = scriptTitle,
                cameraEngine = app.container.cameraEngine,
                recordingRepository = app.container.recordingRepository,
                onNavigateBackToCamera = {
                    navController.popBackStack()
                },
                onNavigateToRecordings = {
                    navController.navigate(Screen.Recordings.route) {
                        popUpTo(Screen.CameraRecording.route) { inclusive = true }
                    }
                }
            )
        }

        // 12.1 Recordings Library Screen
        composable(Screen.Recordings.route) {
            val recordingsViewModel: RecordingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            RecordingsScreen(
                viewModel = recordingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlayRecording = { videoUri ->
                    navController.navigate(Screen.PreviewRecording.createRoute(videoUri))
                }
            )
        }

        // 13. Advanced Settings Screen (Mockup Screen 13)
        composable(Screen.AdvancedSettings.route) {
            val settingsViewModel: ScriptSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            AdvancedSettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 14. Cloud Sync & Profile Screen (Mockup Screen 14)
        composable(Screen.CloudSyncProfile.route) {
            CloudSyncProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPro = {
                    navController.navigate(Screen.ProFeatures.route)
                },
                onNavigateToAdvancedSettings = {
                    navController.navigate(Screen.AdvancedSettings.route)
                },
                onNavigateToMyScripts = {
                    navController.navigate(Screen.Home.route)
                },
                onSignOut = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route)
                }
            )
        }

        // 15. Pro Features Paywall Screen (Mockup Screen 15)
        composable(Screen.ProFeatures.route) {
            ProFeaturesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
