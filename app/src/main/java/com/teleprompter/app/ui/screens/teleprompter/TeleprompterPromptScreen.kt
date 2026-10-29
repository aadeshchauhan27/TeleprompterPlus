package com.teleprompter.app.ui.screens.teleprompter

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ReadingGuideBorder
import com.teleprompter.app.core.theme.ReadingGuideHighlight
import com.teleprompter.app.ui.components.OverlayCustomizationSheet
import com.teleprompter.app.ui.screens.settings.ScriptSettingsViewModel

@Composable
fun TeleprompterPromptScreen(
    scriptTitle: String,
    scriptContent: String,
    viewModel: ScriptSettingsViewModel,
    onNavigateBack: () -> Unit,
    onOpenFullSettings: () -> Unit,
    onOpenThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    val context = LocalContext.current

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

    var isPlaying by remember { mutableStateOf(false) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var showCustomizationSheet by remember { mutableStateOf(false) }

    // Smooth time-based 60 FPS scrolling engine
    LaunchedEffect(isPlaying, settings.scrollSpeed) {
        if (!isPlaying) return@LaunchedEffect
        var lastTimeNanos = 0L

        while (isPlaying) {
            withFrameNanos { nowNanos ->
                if (lastTimeNanos != 0L) {
                    val deltaSeconds = (nowNanos - lastTimeNanos) / 1_000_000_000f
                    val pixelsPerSecond = settings.scrollSpeed * 2.8f
                    scrollOffset += pixelsPerSecond * deltaSeconds
                }
                lastTimeNanos = nowNanos
            }
        }
    }

    val fontFam = when (settings.fontStyle) {
        FontStyleType.SANS -> FontFamily.SansSerif
        FontStyleType.SERIF -> FontFamily.Serif
        FontStyleType.MONO -> FontFamily.Monospace
    }

    val textColor = try {
        Color(android.graphics.Color.parseColor(settings.textColorHex))
    } catch (_: Exception) {
        Color.White
    }

    val bgColor = Color(0xFF070B14).copy(alpha = settings.bgOpacity)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    scrollOffset = (scrollOffset - dragAmount.y).coerceAtLeast(0f)
                }
            }
    ) {
        // Safe Area Guide Box (Screen 5/6/8 in mockup)
        if (settings.showSafeAreaGuide) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 70.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0x3338BDF8),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        // Active Reading Line Highlight Guide (Screen 8 in mockup)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ReadingGuideHighlight)
                .border(1.5.dp, ReadingGuideBorder, RoundedCornerShape(12.dp))
        )

        // Scrolling Script Text Container with mirror transformation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    if (settings.mirrorText) {
                        scaleX = -1f // Mirror text for front camera/beam splitter glass
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = 320f - scrollOffset
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val parsedAlign = when (settings.textAlignment) {
                    com.teleprompter.app.core.model.TextAlignmentType.LEFT -> TextAlign.Start
                    com.teleprompter.app.core.model.TextAlignmentType.RIGHT -> TextAlign.End
                    com.teleprompter.app.core.model.TextAlignmentType.CENTER -> TextAlign.Center
                }

                Text(
                    text = scriptContent.ifBlank { "No content to read." },
                    color = textColor,
                    fontSize = settings.fontSize.sp,
                    fontFamily = fontFam,
                    lineHeight = (settings.fontSize * settings.lineSpacingMultiplier * 1.45f).sp,
                    letterSpacing = settings.letterSpacingSp.sp,
                    textAlign = parsedAlign,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(400.dp))
            }
        }

        // Top Floating Control Bar (matching Screen 6 & Screen 7 in mockup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close / Back button
            FloatingBarIconButton(
                icon = Icons.Default.Close,
                onClick = onNavigateBack
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Themes Shortcut (Mockup Screen 12)
                FloatingBarIconButton(
                    icon = Icons.Default.Palette,
                    onClick = onOpenThemes
                )

                // Overlay Customization Toggle (Mockup Screen 7)
                FloatingBarIconButton(
                    icon = Icons.Default.Tune,
                    onClick = { showCustomizationSheet = !showCustomizationSheet }
                )

                // Full Script Settings (Mockup Screen 5)
                FloatingBarIconButton(
                    icon = Icons.Default.Settings,
                    onClick = onOpenFullSettings
                )
            }
        }

        // Bottom HUD / Play-Pause Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restart button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0x991E293B))
                    .clickable { scrollOffset = 0f },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Restart",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Big Play/Pause Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Overlay Customization Sheet (Screen 7 in mockup)
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

@Composable
private fun FloatingBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0x800F172A))
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
