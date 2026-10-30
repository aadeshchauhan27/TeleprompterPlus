package com.teleprompter.app.ui.screens.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.teleprompter.app.core.camera.RecordingState
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ReadingGuideBorder
import com.teleprompter.app.core.theme.ReadingGuideHighlight
import com.teleprompter.app.core.theme.RecordRed
import com.teleprompter.app.core.util.rememberAppHaptics
import com.teleprompter.app.ui.components.OverlayCustomizationSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CameraRecordingScreen(
    viewModel: CameraRecordingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (videoUri: Uri, scriptId: String?, scriptTitle: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = rememberAppHaptics()
    val uiState by viewModel.uiState.collectAsState()
    val cameraStatus = uiState.cameraStatus
    val settings = uiState.settings
    val script = uiState.script

    // Keep screen awake while in teleprompter camera mode if enabled
    DisposableEffect(settings.keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Permission handling (Camera & Audio)
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Teleprompter smooth scrolling engine
    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var textTotalHeightPx by remember { mutableFloatStateOf(1000f) }
    var isPromptPlaying by remember { mutableStateOf(false) }

    // Auto-hide controls state
    var areControlsVisible by remember { mutableStateOf(true) }

    // Countdown state (3, 2, 1, RECORD)
    var countdownLabel by remember { mutableStateOf<String?>(null) }
    var isFinalizing by remember { mutableStateOf(false) }

    // Pause on background lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isPromptPlaying = false
                if (cameraStatus.state == RecordingState.RECORDING) {
                    viewModel.pauseRecording()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.cameraEngine.unbindCamera()
        }
    }

    // Monotonic 60 FPS smooth scrolling engine
    LaunchedEffect(isPromptPlaying, settings.scrollSpeed) {
        if (!isPromptPlaying) return@LaunchedEffect

        var lastFrameTimeNanos = 0L
        // Continuous speed model: 10..100 maps to 25..250 pixels per second
        val pxPerSecond = settings.scrollSpeed * 2.5f

        while (isPromptPlaying) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    val deltaPx = pxPerSecond * deltaSeconds
                    scrollOffsetPx = (scrollOffsetPx + deltaPx).coerceAtMost(textTotalHeightPx + 400f)
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    // Auto-hide controls timer: after 3 seconds of playing, fade out HUD
    LaunchedEffect(isPromptPlaying, areControlsVisible) {
        if (isPromptPlaying && areControlsVisible && settings.autoHideControls) {
            delay(3000)
            if (isPromptPlaying && !uiState.isCustomizationSheetOpen) {
                areControlsVisible = false
            }
        }
    }

    val promptFontFamily = when (settings.fontStyle) {
        FontStyleType.SERIF -> FontFamily.Serif
        FontStyleType.MONO -> FontFamily.Monospace
        FontStyleType.SANS -> FontFamily.SansSerif
    }

    val parsedTextColor = try {
        Color(android.graphics.Color.parseColor(settings.textColorHex))
    } catch (_: Exception) {
        Color.White
    }.copy(alpha = settings.textOpacity)

    // Calculate progress and remaining time
    val scrollProgress = if (textTotalHeightPx > 0) {
        (scrollOffsetPx / textTotalHeightPx).coerceIn(0f, 1f)
    } else 0f

    val totalScriptTimeSec = uiState.targetTotalTimeSeconds
    val remainingSec = (totalScriptTimeSec * (1f - scrollProgress)).toInt().coerceAtLeast(0)
    val remainingFormatted = String.format(Locale.US, "%02d:%02d", remainingSec / 60, remainingSec % 60)

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    fun startWithCountdown(onAction: () -> Unit) {
        if (settings.countdownSeconds > 0) {
            coroutineScope.launch {
                for (i in settings.countdownSeconds downTo 1) {
                    countdownLabel = i.toString()
                    haptics.countdownTick()
                    delay(1000)
                }
                countdownLabel = "RECORD"
                haptics.heavyClick()
                delay(400)
                countdownLabel = null
                onAction()
            }
        } else {
            haptics.heavyClick()
            onAction()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    // Tap anywhere to toggle controls back
                    areControlsVisible = !areControlsVisible
                }
            }
    ) {
        // 1. Fullscreen CameraX Live Preview
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                        viewModel.bindCamera(lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission fallback message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B14)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Camera & Audio Access Required",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To read your transparent script while recording yourself, please grant camera and microphone access.",
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permissions", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Safe Area Guide (Screen 5/6/8 in mockup)
        if (settings.showSafeAreaGuide) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 72.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        // 3. Transparent Floating Teleprompter Overlay (Near Camera Lens)
        val scriptText = if (!script?.content.isNullOrBlank()) {
            script?.content.orEmpty()
        } else {
            "No script content available. Add script text in the editor to read on teleprompter."
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val totalScreenWidth = maxWidth
            val totalScreenHeight = maxHeight

            // Width determined by readingWidthPercent (default 85%) to avoid excessive eye movement
            val overlayWidth = totalScreenWidth * (settings.readingWidthPercent / 100f)

            // Vertical positioning near camera lens (default ~25% from top)
            val lensPositionY = totalScreenHeight * (settings.verticalPositionPercent / 100f)

            Box(
                modifier = Modifier
                    .width(overlayWidth)
                    .height(totalScreenHeight * 0.58f)
                    .offset(y = lensPositionY)
                    .align(Alignment.TopCenter)
            ) {
                // Active Reading Guide Highlight Box (Screen 8 in mockup)
                if (isPromptPlaying || cameraStatus.state == RecordingState.RECORDING) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 24.dp)
                            .background(
                                color = ReadingGuideHighlight,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = ReadingGuideBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                    )
                }

                // Transparent Floating Script Container (NOT inside a card)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = settings.bgOpacity))
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                scrollOffsetPx = (scrollOffsetPx - dragAmount.y).coerceAtLeast(0f)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = scriptText,
                        color = parsedTextColor,
                        fontSize = settings.fontSize.sp,
                        fontFamily = promptFontFamily,
                        lineHeight = (settings.fontSize * settings.lineSpacingMultiplier * 1.35f).sp,
                        letterSpacing = settings.letterSpacingSp.sp,
                        textAlign = when (settings.textAlignment) {
                            com.teleprompter.app.core.model.TextAlignmentType.LEFT -> TextAlign.Start
                            com.teleprompter.app.core.model.TextAlignmentType.RIGHT -> TextAlign.End
                            com.teleprompter.app.core.model.TextAlignmentType.CENTER -> TextAlign.Center
                        },
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.9f),
                                offset = Offset(2f, 2f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textTotalHeightPx = coordinates.size.height.toFloat()
                            }
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (-scrollOffsetPx).roundToInt()
                                )
                            }
                            .scale(
                                scaleX = if (settings.mirrorText) -1f else 1f,
                                scaleY = 1f
                            )
                    )
                }
            }
        }

        // 4. Premium Countdown Overlay (3, 2, 1, RECORD)
        if (countdownLabel != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color(0xE6070B14))
                        .border(
                            width = 3.dp,
                            color = if (countdownLabel == "RECORD") RecordRed else ElectricBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countdownLabel ?: "",
                        fontSize = if (countdownLabel == "RECORD") 22.sp else 64.sp,
                        fontWeight = FontWeight.Black,
                        color = if (countdownLabel == "RECORD") RecordRed else Color.White
                    )
                }
            }
        }

        // Finalizing Video Progress Overlay
        if (isFinalizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = ElectricBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Finalizing video...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Camera Error Banner
        AnimatedVisibility(
            visible = cameraStatus.errorMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.94f))
                    .clickable { viewModel.clearCameraError() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cameraStatus.errorMessage ?: "Camera error",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 5. Floating Top Bar with Auto-Hide (Close, Torch, Switch Camera, Settings)
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                IconButton(
                    onClick = {
                        haptics.click()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash / Torch Toggle
                    IconButton(
                        onClick = {
                            haptics.click()
                            viewModel.toggleTorch()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (cameraStatus.isTorchOn) ElectricBlue else Color.Black.copy(alpha = 0.45f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (cameraStatus.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Switch Camera Lens (Front/Back)
                    IconButton(
                        onClick = {
                            haptics.click()
                            previewViewRef?.let { preview ->
                                viewModel.toggleCameraLens(lifecycleOwner, preview)
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // In-camera Customization Gear
                    IconButton(
                        onClick = {
                            haptics.click()
                            viewModel.toggleCustomizationSheet()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 6. Floating Bottom Controls HUD with Auto-Hide
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Progress Bar & Remaining Time Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Linear Progress Bar
                    LinearProgressIndicator(
                        progress = { scrollProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ElectricBlue,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Remaining Time
                    Text(
                        text = "$remainingFormatted remaining",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Main Controls Row: Zoom Pill | Play/Pause & Record | Timer/Restart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Restart Button & 1.0x Pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable {
                                    haptics.confirm()
                                    scrollOffsetPx = 0f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1.0x",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Center: Play / Pause / Record
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Pause Teleprompter Scroll
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isPromptPlaying) ElectricBlue else Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    haptics.heavyClick()
                                    isPromptPlaying = !isPromptPlaying
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPromptPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Large Circular Red Record Button (Standby vs Recording)
                        if (cameraStatus.state == RecordingState.IDLE || cameraStatus.state == RecordingState.STOPPED) {
                            // STANDBY RECORD BUTTON (Screen 6 in mockup)
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(RecordRed)
                                    .clickable {
                                        scrollOffsetPx = 0f
                                        startWithCountdown {
                                            isPromptPlaying = true
                                            viewModel.startRecording { outputUri ->
                                                isFinalizing = false
                                                onNavigateToPreview(outputUri, script?.id, script?.title)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {}
                        } else {
                            // ACTIVE RECORDING STOP BUTTON (Screen 8 in mockup)
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                                    .padding(6.dp)
                                    .clickable {
                                        if (!isFinalizing) {
                                            haptics.confirm()
                                            isPromptPlaying = false
                                            isFinalizing = true
                                            viewModel.stopRecording()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RecordRed)
                                )
                            }
                        }
                    }

                    // Right: Recording Timer Pill
                    val timerText = if (cameraStatus.state == RecordingState.RECORDING || cameraStatus.state == RecordingState.PAUSED) {
                        cameraStatus.formattedDuration
                    } else {
                        "00:00"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cameraStatus.state == RecordingState.RECORDING) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(RecordRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = timerText,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 7. Overlay Customization Sheet (Mockup Screen 7)
        AnimatedVisibility(
            visible = uiState.isCustomizationSheetOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OverlayCustomizationSheet(
                settings = settings,
                onFontSizeChange = { viewModel.updateFontSize(it) },
                onScrollSpeedChange = { viewModel.updateScrollSpeed(it) },
                onLineSpacingChange = { viewModel.updateLineSpacing(it) },
                onLetterSpacingChange = { viewModel.updateLetterSpacing(it) },
                onTextAlignmentChange = { viewModel.updateTextAlignment(it) },
                onBgOpacityChange = { viewModel.updateBackgroundOpacity(it) },
                onTextOpacityChange = { viewModel.updateTextOpacity(it) },
                onReadingWidthChange = { viewModel.updateReadingWidth(it) },
                onVerticalPositionChange = { viewModel.updateVerticalPosition(it) },
                onMirrorTextChange = { viewModel.updateMirrorText(it) },
                onSafeAreaChange = { viewModel.updateShowSafeArea(it) },
                onCountdownChange = { viewModel.updateCountdown(it) },
                onPresetChange = { viewModel.applyPreset(it) },
                onSaveForScript = {
                    viewModel.saveScriptSpecificSettings {
                        android.widget.Toast.makeText(context, "Saved for this script", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                hasScript = script != null,
                onReset = { viewModel.resetSettingsToDefaults() },
                onClose = { viewModel.closeCustomizationSheet() }
            )
        }
    }
}
