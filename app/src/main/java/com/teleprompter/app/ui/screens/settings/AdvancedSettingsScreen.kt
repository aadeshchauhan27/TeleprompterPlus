package com.teleprompter.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.camera.CameraLens
import com.teleprompter.app.core.model.PresetType
import com.teleprompter.app.core.model.VideoQualityPreference
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.RecordRed
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted

@Composable
fun AdvancedSettingsScreen(
    viewModel: ScriptSettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var showCameraDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDarkHeading
                )
            }
            Text(
                text = "Advanced Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading,
                    fontSize = 20.sp
                )
            )
        }

        HorizontalDivider(color = LightBorderSubtle)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // SECTION 1: CAMERA SETTINGS
            SectionTitle(title = "Camera")

            SettingsValueRow(
                icon = Icons.Outlined.Cameraswitch,
                title = "Default Camera",
                value = if (settings.defaultCameraLens == CameraLens.FRONT) "Front Lens" else "Rear Lens",
                onClick = { showCameraDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsValueRow(
                icon = Icons.Outlined.Videocam,
                title = "Video Quality",
                value = settings.videoQuality.label,
                onClick = { showQualityDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsToggleRow(
                icon = Icons.Outlined.Mic,
                title = "Record Audio",
                description = "Record high-quality sound with video from device microphone",
                isChecked = settings.recordAudio,
                onCheckedChange = { viewModel.setRecordAudio(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsToggleRow(
                icon = Icons.Outlined.FlashOn,
                title = "Flash by Default",
                description = "Enable flash illumination when recording with rear camera",
                isChecked = settings.defaultFlashOn,
                onCheckedChange = { viewModel.setDefaultFlashOn(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: TELEPROMPTER PREFERENCES
            SectionTitle(title = "Teleprompter")

            SettingsValueRow(
                icon = Icons.Outlined.Tune,
                title = "Default Preset",
                value = settings.presetType.label,
                onClick = { showPresetDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsToggleRow(
                icon = Icons.Outlined.Flip,
                title = "Mirror Text (Front Camera)",
                description = "Flips text horizontally for front lens natural eye contact",
                isChecked = settings.mirrorText,
                onCheckedChange = { viewModel.setMirrorText(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsToggleRow(
                icon = Icons.Outlined.CropFree,
                title = "Show Safe Area Guide",
                description = "Shows framing guidelines for centered recording",
                isChecked = settings.showSafeAreaGuide,
                onCheckedChange = { viewModel.setShowSafeAreaGuide(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsToggleRow(
                icon = Icons.Outlined.VisibilityOff,
                title = "Auto-Hide Controls",
                description = "Fades out controls after 3 seconds of smooth reading",
                isChecked = settings.autoHideControls,
                onCheckedChange = { viewModel.setAutoHideControls(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: GENERAL & WORKFLOW
            SectionTitle(title = "General")

            SettingsToggleRow(
                icon = Icons.Outlined.WbSunny,
                title = "Keep Screen Awake",
                description = "Prevents display timeout while reading or recording",
                isChecked = settings.keepScreenAwake,
                onCheckedChange = { viewModel.setKeepScreenAwake(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LightBorderSubtle)

            SettingsValueRow(
                icon = Icons.Outlined.Timer,
                title = "Countdown Timer",
                value = if (settings.countdownSeconds == 0) "Off" else "${settings.countdownSeconds} seconds",
                onClick = { showCountdownDialog = true }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 4: RESET TELEPROMPTER SETTINGS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEF2F2))
                    .clickable { showResetConfirmation = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        tint = RecordRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Teleprompter Settings",
                        color = RecordRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Dialog: Default Camera Lens
    if (showCameraDialog) {
        AlertDialog(
            onDismissRequest = { showCameraDialog = false },
            title = { Text("Default Camera", fontWeight = FontWeight.Bold, color = TextDarkHeading) },
            text = {
                Column {
                    listOf(
                        Pair(CameraLens.FRONT, "Front Camera (Selfie)"),
                        Pair(CameraLens.BACK, "Rear Camera (Main)")
                    ).forEach { (lens, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultCameraLens(lens)
                                    showCameraDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (settings.defaultCameraLens == lens) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.defaultCameraLens == lens) ElectricBlue else TextDarkHeading
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCameraDialog = false }) { Text("Cancel", color = TextDarkMuted) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog: Video Quality
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Video Quality", fontWeight = FontWeight.Bold, color = TextDarkHeading) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        VideoQualityPreference.HIGH,
                        VideoQualityPreference.STANDARD,
                        VideoQualityPreference.ULTRA
                    ).forEach { quality ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.videoQuality == quality) Color(0xFFF1F5F9) else Color.Transparent)
                                .clickable {
                                    viewModel.setVideoQuality(quality)
                                    showQualityDialog = false
                                }
                                .padding(10.dp)
                        ) {
                            Text(
                                text = quality.label,
                                fontSize = 15.sp,
                                fontWeight = if (settings.videoQuality == quality) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (settings.videoQuality == quality) ElectricBlue else TextDarkHeading
                            )
                            Text(
                                text = quality.description,
                                fontSize = 12.sp,
                                color = TextDarkMuted
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showQualityDialog = false }) { Text("Cancel", color = TextDarkMuted) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog: Default Preset
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("Default Preset", fontWeight = FontWeight.Bold, color = TextDarkHeading) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(PresetType.CREATOR, PresetType.INTERVIEW, PresetType.REELS, PresetType.CUSTOM).forEach { preset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.presetType == preset) Color(0xFFF1F5F9) else Color.Transparent)
                                .clickable {
                                    viewModel.applyPreset(preset)
                                    showPresetDialog = false
                                }
                                .padding(10.dp)
                        ) {
                            Text(
                                text = preset.label,
                                fontSize = 15.sp,
                                fontWeight = if (settings.presetType == preset) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (settings.presetType == preset) ElectricBlue else TextDarkHeading
                            )
                            Text(
                                text = preset.description,
                                fontSize = 12.sp,
                                color = TextDarkMuted
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPresetDialog = false }) { Text("Cancel", color = TextDarkMuted) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog: Countdown Timer
    if (showCountdownDialog) {
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text("Countdown Duration", fontWeight = FontWeight.Bold, color = TextDarkHeading) },
            text = {
                Column {
                    listOf(0, 3, 5, 10).forEach { sec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCountdown(sec)
                                    showCountdownDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sec == 0) "Off" else "$sec seconds",
                                fontSize = 15.sp,
                                fontWeight = if (settings.countdownSeconds == sec) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.countdownSeconds == sec) ElectricBlue else TextDarkHeading
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCountdownDialog = false }) { Text("Cancel", color = TextDarkMuted) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog: Reset Confirmation
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Teleprompter Settings?", fontWeight = FontWeight.Bold, color = TextDarkHeading) },
            text = {
                Text(
                    text = "This will reset all teleprompter speed, font size, camera defaults, and window layout preferences to factory defaults. Your scripts and recordings will remain safe.",
                    color = TextDarkMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        viewModel.resetToDefaults()
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
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
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = ElectricBlue,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextDarkHeading,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextDarkHeading,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkMuted,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricBlue
            )
        )
    }
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextDarkHeading,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextDarkHeading,
                    fontSize = 15.sp
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextDarkMuted,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextDarkMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
