package com.teleprompter.app.ui.screens.teleprompter

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.TextAlignmentType
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ReadingGuideBorder
import com.teleprompter.app.core.theme.ReadingGuideHighlight
import com.teleprompter.app.core.util.rememberAppHaptics
import com.teleprompter.app.ui.components.OverlayCustomizationSheet
import com.teleprompter.app.ui.screens.settings.ScriptSettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrompterBackground = Color(0xFF070B14)

/** Scroll speed unit: one speed step equals this many dp per second (density independent). */
private const val DP_PER_SECOND_PER_SPEED_STEP = 1.0f

@Composable
fun TeleprompterPromptScreen(
    scriptTitle: String,
    scriptContent: String,
    viewModel: ScriptSettingsViewModel,
    onNavigateBack: () -> Unit,
    onOpenFullSettings: () -> Unit,
    onOpenThemes: () -> Unit,
    modifier: Modifier = Modifier,
    initialPosition: Int = 0,
    estimatedSeconds: Int = 0,
    onSavePosition: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    val context = LocalContext.current
    val density = LocalDensity.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    // Keep screen awake while teleprompter is active if enabled
    DisposableEffect(settings.keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val paragraphs = remember(scriptContent) {
        scriptContent.ifBlank { "No content to read." }.split("\n")
    }
    val listState = rememberLazyListState()

    var isPlaying by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    var showCustomizationSheet by remember { mutableStateOf(false) }

    val currentPositionSaver by rememberUpdatedState(onSavePosition)
    val currentSpeed by rememberUpdatedState(settings.scrollSpeed)

    // Resume where the user left off once the script has loaded
    var restored by remember { mutableStateOf(false) }
    LaunchedEffect(paragraphs.size, initialPosition) {
        if (!restored && paragraphs.size > 1) {
            if (initialPosition > 0) {
                listState.scrollToItem(initialPosition.coerceIn(0, paragraphs.size - 1))
            }
            restored = true
        }
    }

    // Persist reading position when pausing/leaving
    LaunchedEffect(isPlaying) {
        if (!isPlaying && restored) {
            currentPositionSaver(if (isFinished) 0 else listState.firstVisibleItemIndex)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            currentPositionSaver(if (isFinished) 0 else listState.firstVisibleItemIndex)
        }
    }

    // Smooth, time-based, density-independent scrolling engine. Speed changes don't restart the loop.
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastTimeNanos = 0L
        while (isPlaying) {
            val deltaSeconds = withFrameNanos { nowNanos ->
                val d = if (lastTimeNanos == 0L) 0f else (nowNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = nowNanos
                d
            }
            val pixels = currentSpeed * DP_PER_SECOND_PER_SPEED_STEP * density.density * deltaSeconds
            // Raw delta: not cancelled/mutexed by finger touches (unlike scrollBy), so taps and drags can
            // interleave with auto-scroll without breaking the loop.
            if (pixels > 0f) listState.dispatchRawDelta(pixels)
            if (!listState.canScrollForward) {
                // Reached the end of the script
                isPlaying = false
                isFinished = true
                haptics.confirm()
            }
        }
    }

    fun restart() {
        countdownJob?.cancel()
        countdown = 0
        isPlaying = false
        isFinished = false
        scope.launch { listState.scrollToItem(0) }
    }

    fun togglePlay() {
        if (countdown > 0) {
            countdownJob?.cancel()
            countdown = 0
            return
        }
        if (isPlaying) {
            isPlaying = false
            return
        }
        if (isFinished) restart()
        val atStart = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        if (atStart && settings.countdownSeconds > 0) {
            countdownJob = scope.launch {
                for (i in settings.countdownSeconds downTo 1) {
                    countdown = i
                    delay(1000)
                }
                countdown = 0
                isPlaying = true
            }
        } else {
            isPlaying = true
        }
    }

    val fontFam = when (settings.fontStyle) {
        FontStyleType.SANS -> FontFamily.SansSerif
        FontStyleType.SERIF -> FontFamily.Serif
        FontStyleType.MONO -> FontFamily.Monospace
    }

    val textColor = remember(settings.textColorHex, settings.textOpacity) {
        val base = try {
            Color(android.graphics.Color.parseColor(settings.textColorHex))
        } catch (_: Exception) {
            Color.White
        }
        base.copy(alpha = settings.textOpacity.coerceIn(0.1f, 1f))
    }

    val parsedAlign = when (settings.textAlignment) {
        TextAlignmentType.LEFT -> TextAlign.Start
        TextAlignmentType.RIGHT -> TextAlign.End
        TextAlignmentType.CENTER -> TextAlign.Center
    }

    val lineHeightSp = settings.fontSize * settings.lineSpacingMultiplier * 1.45f
    val guideHeight: Dp = with(density) { (lineHeightSp * 1.15f).sp.toDp() }.coerceAtLeast(48.dp)

    val controlsVisible = !isPlaying || !settings.autoHideControls
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PrompterBackground)
            // Hardware keyboard / Bluetooth remote / volume keys
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Spacebar, Key.Enter, Key.MediaPlayPause, Key.MediaPlay, Key.MediaPause -> {
                        togglePlay(); true
                    }
                    Key.VolumeUp, Key.DirectionUp, Key.MediaPrevious -> {
                        viewModel.setScrollSpeed((settings.scrollSpeed + 5).coerceAtMost(100)); true
                    }
                    Key.VolumeDown, Key.DirectionDown, Key.MediaNext -> {
                        viewModel.setScrollSpeed((settings.scrollSpeed - 5).coerceAtLeast(10)); true
                    }
                    else -> false
                }
            }
    ) {
        val totalHeight = maxHeight
        val guideTop = (totalHeight * (settings.verticalPositionPercent.coerceIn(5, 80) / 100f))
        val sidePadding = (maxWidth * ((100 - settings.readingWidthPercent.coerceIn(40, 100)) / 200f))
            .coerceAtLeast(12.dp)

        // Safe Area Guide
        if (settings.showSafeAreaGuide) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 70.dp)
                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(16.dp))
            )
        }

        // Active reading line highlight, positioned at the configured vertical position
        Box(
            modifier = Modifier
                .offset(y = guideTop - 6.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(guideHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(ReadingGuideHighlight)
                .border(1.5.dp, ReadingGuideBorder, RoundedCornerShape(12.dp))
        )

        // Scrolling script (lazy, paragraph-by-paragraph) with mirror + edge fade
        LazyColumn(
            state = listState,
            userScrollEnabled = true,
            contentPadding = PaddingValues(
                start = sidePadding,
                end = sidePadding,
                top = guideTop - 6.dp,
                bottom = totalHeight
            ),
            modifier = Modifier
                .fillMaxSize()
                // Tap the text area to play / pause. Detected in the Initial pass without consuming, so it
                // works while the list auto-scrolls and never blocks drags. Scoped here so buttons don't toggle.
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (up != null &&
                            (up.position - down.position).getDistance() < viewConfiguration.touchSlop &&
                            up.uptimeMillis - down.uptimeMillis < 400L
                        ) {
                            haptics.click()
                            togglePlay()
                        }
                    }
                }
                .graphicsLayer {
                    if (settings.mirrorText) scaleX = -1f
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    val fade = size.height * 0.12f
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            (fade / size.height) to Color.Black,
                            (1f - fade / size.height) to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            items(paragraphs.size) { index ->
                val line = paragraphs[index]
                Text(
                    text = if (line.isEmpty()) " " else line,
                    color = textColor,
                    fontSize = settings.fontSize.sp,
                    fontFamily = fontFam,
                    lineHeight = lineHeightSp.sp,
                    letterSpacing = settings.letterSpacingSp.sp,
                    textAlign = parsedAlign,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Countdown overlay
        if (countdown > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdown.toString(),
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { contentDescription = "Starting in $countdown" }
                )
            }
        }

        // Top Floating Control Bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FloatingBarIconButton(
                        icon = Icons.Default.Close,
                        description = "Close teleprompter",
                        onClick = {
                            haptics.click()
                            onNavigateBack()
                        }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FloatingBarIconButton(
                            icon = Icons.Default.Palette,
                            description = "Themes",
                            onClick = {
                                haptics.click()
                                onOpenThemes()
                            }
                        )
                        FloatingBarIconButton(
                            icon = Icons.Default.Tune,
                            description = "Customize text and speed",
                            onClick = {
                                haptics.click()
                                showCustomizationSheet = !showCustomizationSheet
                            }
                        )
                        FloatingBarIconButton(
                            icon = Icons.Default.Settings,
                            description = "Script settings",
                            onClick = {
                                haptics.click()
                                onOpenFullSettings()
                            }
                        )
                    }
                }
                ScrollProgress(
                    listState = listState,
                    paragraphCount = paragraphs.size,
                    estimatedSeconds = estimatedSeconds,
                    speed = settings.scrollSpeed
                )
            }
        }

        // Bottom controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundControl(
                    size = 52.dp,
                    color = Color(0x991E293B),
                    icon = Icons.Default.Replay,
                    description = "Restart from the beginning",
                    onClick = {
                        haptics.confirm()
                        restart()
                    }
                )
                RoundControl(
                    size = 72.dp,
                    color = ElectricBlue,
                    icon = if (isPlaying || countdown > 0) Icons.Default.Pause else Icons.Default.PlayArrow,
                    description = if (isPlaying || countdown > 0) "Pause" else if (isFinished) "Play again" else "Play",
                    onClick = {
                        haptics.heavyClick()
                        togglePlay()
                    }
                )
            }
        }

        // Overlay Customization Sheet
        AnimatedVisibility(
            visible = showCustomizationSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OverlayCustomizationSheet(
                settings = settings,
                onFontSizeChange = { viewModel.setFontSize(it) },
                onScrollSpeedChange = { viewModel.setScrollSpeed(it) },
                onLineSpacingChange = { viewModel.setLineSpacing(it) },
                onLetterSpacingChange = { viewModel.setLetterSpacing(it) },
                onTextAlignmentChange = { viewModel.setTextAlignment(it) },
                onFontStyleChange = { viewModel.setFontStyle(it) },
                onBgOpacityChange = { viewModel.setBgOpacity(it) },
                onTextOpacityChange = { viewModel.setTextOpacity(it) },
                onReadingWidthChange = { viewModel.setReadingWidth(it) },
                onVerticalPositionChange = { viewModel.setVerticalPosition(it) },
                onMirrorTextChange = { viewModel.setMirrorText(it) },
                onSafeAreaChange = { viewModel.setShowSafeAreaGuide(it) },
                onCountdownChange = { viewModel.setCountdown(it) },
                onPresetChange = { viewModel.applyPreset(it) },
                onSaveForScript = {
                    viewModel.saveScriptSpecificSettings {
                        android.widget.Toast.makeText(context, "Saved for this script", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                hasScript = uiState.scriptId != null,
                onReset = { viewModel.resetToDefaults() },
                onClose = { showCustomizationSheet = false }
            )
        }
    }
}

/** Progress bar plus remaining time. Reads scroll state in its own scope so only it recomposes per frame. */
@Composable
private fun ScrollProgress(
    listState: androidx.compose.foundation.lazy.LazyListState,
    paragraphCount: Int,
    estimatedSeconds: Int,
    speed: Int
) {
    val progress by remember(paragraphCount) {
        derivedStateOf {
            if (paragraphCount <= 0) 0f else {
                val first = listState.layoutInfo.visibleItemsInfo.firstOrNull()
                val frac = if (first != null && first.size > 0) {
                    (-first.offset).coerceAtLeast(0).toFloat() / first.size
                } else 0f
                ((listState.firstVisibleItemIndex + frac) / paragraphCount).coerceIn(0f, 1f)
            }
        }
    }
    val remaining = (estimatedSeconds * (1f - progress)).toInt()
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ElectricBlue,
            trackColor = Color(0x33FFFFFF)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${(progress * 100).toInt()}%", color = Color(0xCCFFFFFF), fontSize = 12.sp)
            Text(
                text = "Speed $speed" + if (estimatedSeconds > 0) "  •  %d:%02d left".format(remaining / 60, remaining % 60) else "",
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RoundControl(
    size: Dp,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

@Composable
private fun FloatingBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0x800F172A))
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
