package com.teleprompter.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.PresetType
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.model.TextAlignmentType
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.RecordRed
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import java.util.Locale

@Composable
fun OverlayCustomizationSheet(
    settings: TeleprompterSettings,
    onFontSizeChange: (Int) -> Unit,
    onScrollSpeedChange: (Int) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit = {},
    onTextAlignmentChange: (TextAlignmentType) -> Unit = {},
    onFontStyleChange: (FontStyleType) -> Unit = {},
    onBgOpacityChange: (Float) -> Unit,
    onTextOpacityChange: (Float) -> Unit = {},
    onReadingWidthChange: (Int) -> Unit = {},
    onVerticalPositionChange: (Int) -> Unit = {},
    onMirrorTextChange: (Boolean) -> Unit = {},
    onSafeAreaChange: (Boolean) -> Unit = {},
    onCountdownChange: (Int) -> Unit = {},
    onPresetChange: (PresetType) -> Unit = {},
    onSaveForScript: () -> Unit = {},
    hasScript: Boolean = false,
    onReset: () -> Unit,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Text, 2 = Background, 3 = Layout
    val tabs = listOf("Presets", "Text", "Background", "Layout")
    var showResetConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xF00D1527)) // Translucent dark navy matching Screen 7 in mockup
            .border(
                width = 1.dp,
                color = Color(0x3338BDF8),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented Tab Control: Presets | Text | Background | Layout (Screen 7 in mockup)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131D33))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) ElectricBlue else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab 0: PRESETS
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(PresetType.CREATOR, PresetType.INTERVIEW, PresetType.REELS).forEach { preset ->
                        val isSelected = settings.presetType == preset
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131D33))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ElectricBlue else Color(0xFF203254),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onPresetChange(preset) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.label,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(ElectricBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: TEXT CUSTOMIZATION
            if (selectedTab == 1) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SliderRow(
                        label = "Font Size",
                        value = settings.fontSize.toFloat(),
                        valueRange = 16f..56f,
                        valueLabel = "${settings.fontSize}sp",
                        onValueChange = { onFontSizeChange(it.toInt()) }
                    )

                    SliderRow(
                        label = "Scroll Speed",
                        value = settings.scrollSpeed.toFloat(),
                        valueRange = 10f..100f,
                        valueLabel = "${settings.scrollSpeed}",
                        onValueChange = { onScrollSpeedChange(it.toInt()) }
                    )

                    // Font Family Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Family",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(100.dp)
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(FontStyleType.SANS, "Sans", FontFamily.SansSerif),
                                Triple(FontStyleType.SERIF, "Serif", FontFamily.Serif),
                                Triple(FontStyleType.MONO, "Mono", FontFamily.Monospace)
                            ).forEach { (style, name, family) ->
                                val isChosen = settings.fontStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) ElectricBlue else Color(0xFF1E293B))
                                        .clickable { onFontStyleChange(style) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        fontFamily = family,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Text Alignment (Left, Center, Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alignment",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(100.dp)
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair(TextAlignmentType.LEFT, Icons.Default.FormatAlignLeft),
                                Pair(TextAlignmentType.CENTER, Icons.Default.FormatAlignCenter),
                                Pair(TextAlignmentType.RIGHT, Icons.Default.FormatAlignRight)
                            ).forEach { (align, icon) ->
                                val isChosen = settings.textAlignment == align
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) ElectricBlue else Color(0xFF1E293B))
                                        .clickable { onTextAlignmentChange(align) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = align.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    SliderRow(
                        label = "Line Spacing",
                        value = settings.lineSpacingMultiplier,
                        valueRange = 0.8f..2.0f,
                        valueLabel = String.format(Locale.US, "%.1fx", settings.lineSpacingMultiplier),
                        onValueChange = onLineSpacingChange
                    )

                    SliderRow(
                        label = "Letter Spacing",
                        value = settings.letterSpacingSp,
                        valueRange = -1.0f..3.0f,
                        valueLabel = String.format(Locale.US, "%.1f", settings.letterSpacingSp),
                        onValueChange = onLetterSpacingChange
                    )

                    SliderRow(
                        label = "Text Opacity",
                        value = settings.textOpacity,
                        valueRange = 0.2f..1.0f,
                        valueLabel = "${(settings.textOpacity * 100).toInt()}%",
                        onValueChange = onTextOpacityChange
                    )
                }
            }

            // Tab 2: BACKGROUND
            if (selectedTab == 2) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SliderRow(
                        label = "Overlay Opacity",
                        value = settings.bgOpacity,
                        valueRange = 0f..1f,
                        valueLabel = "${(settings.bgOpacity * 100).toInt()}%",
                        onValueChange = onBgOpacityChange
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(0.0f, 0.25f, 0.50f, 0.75f).forEach { opacity ->
                            val isSelected = Math.abs(settings.bgOpacity - opacity) < 0.05f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (opacity == 0f) Color.Transparent else Color(0xFF1E293B).copy(alpha = opacity)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ElectricBlue else Color(0xFF203254),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onBgOpacityChange(opacity) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (opacity == 0f) "Clear" else "${(opacity * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Tab 3: LAYOUT & BEHAVIOR
            if (selectedTab == 3) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SliderRow(
                        label = "Reading Width",
                        value = settings.readingWidthPercent.toFloat(),
                        valueRange = 50f..100f,
                        valueLabel = "${settings.readingWidthPercent}%",
                        onValueChange = { onReadingWidthChange(it.toInt()) }
                    )

                    SliderRow(
                        label = "Lens Offset",
                        value = settings.verticalPositionPercent.toFloat(),
                        valueRange = 10f..80f,
                        valueLabel = "${settings.verticalPositionPercent}%",
                        onValueChange = { onVerticalPositionChange(it.toInt()) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mirror Mode",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = settings.mirrorText,
                            onCheckedChange = onMirrorTextChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricBlue,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Safe Area Guide",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = settings.showSafeAreaGuide,
                            onCheckedChange = onSafeAreaChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricBlue,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Countdown",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 3, 5, 10).forEach { sec ->
                                val isChosen = settings.countdownSeconds == sec
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) ElectricBlue else Color(0xFF1E293B))
                                        .clickable { onCountdownChange(sec) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (sec == 0) "Off" else "${sec}s",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons: Save for Script (if active script) & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasScript) {
                    Button(
                        onClick = onSaveForScript,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Save for Script", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                TextButton(
                    onClick = { showResetConfirmation = true },
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(
                        text = "Reset to Defaults",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Text(
                    text = "Reset Teleprompter Settings?",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading
                )
            },
            text = {
                Text(
                    text = "This will restore teleprompter speed, font, width, and colors to their default values. Your scripts and recordings will remain untouched.",
                    color = TextDarkMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        onReset()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel", color = TextDarkMuted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = ElectricBlue,
                activeTrackColor = ElectricBlue,
                inactiveTrackColor = Color(0xFF203254)
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = valueLabel,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp)
        )
    }
}
