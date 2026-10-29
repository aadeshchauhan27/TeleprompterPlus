package com.teleprompter.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.data.sync.SyncStatus
import com.teleprompter.app.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPro: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit,
    onNavigateToMyScripts: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    val user = uiState.user
    val isGuest = user == null || user.isAnonymous
    val userEmail = user?.email ?: if (isGuest) "Guest Creator (Local Only)" else "creator@teleprompter.app"
    val userName = user?.displayName ?: if (isGuest) "Local Mode" else "Creator"

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
                text = "Cloud Sync & Profile",
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
                .padding(20.dp)
        ) {
            // User Card (Screen 14 in mockup)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, LightBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isGuest) Color(0xFFE2E8F0) else ElectricBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = if (isGuest) Color(0xFF64748B) else ElectricBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGuest) userName else userEmail,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkHeading,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = if (isGuest) "Local-First" else "Cloud Sync Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDarkMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isGuest) {
                        // Sign In with Google / Account button
                        Button(
                            onClick = onNavigateToSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text(
                                text = "Sign in for Cloud Backup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        // Upgrade to Pro Button
                        Button(
                            onClick = onNavigateToPro,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text(
                                text = "Upgrade to Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sync & Backup Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.toggleCloudSync(!uiState.isCloudSyncEnabled)
                        val msg = if (!uiState.isCloudSyncEnabled) "Cloud sync enabled" else "Cloud sync paused (Local-First)"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isCloudSyncEnabled) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.isCloudSyncEnabled) ElectricBlue else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Sync & Backup",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkHeading,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = if (uiState.isCloudSyncEnabled) {
                                "Auto backup scripts & preferences"
                            } else {
                                "Paused (Local storage only)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = uiState.isCloudSyncEnabled,
                    onCheckedChange = {
                        viewModel.toggleCloudSync(it)
                        val msg = if (it) "Cloud sync enabled" else "Cloud sync paused (Local-First)"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }

            // Sync Status & Trigger Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, LightBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (statusText, statusColor, statusIcon) = when (uiState.syncStatus) {
                                SyncStatus.SYNCING -> Triple("Syncing with cloud...", ElectricBlue, Icons.Outlined.CloudUpload)
                                SyncStatus.SUCCESS -> Triple("All scripts up to date", Color(0xFF10B981), Icons.Outlined.CheckCircle)
                                SyncStatus.ERROR -> Triple("Sync offline (cached locally)", Color(0xFFF59E0B), Icons.Outlined.SyncProblem)
                                SyncStatus.OFFLINE -> Triple("Offline mode", Color(0xFF64748B), Icons.Outlined.CloudOff)
                                SyncStatus.DISABLED -> Triple("Sync disabled", Color(0xFF64748B), Icons.Outlined.CloudOff)
                                SyncStatus.IDLE -> Triple("Ready to sync", Color(0xFF64748B), Icons.Outlined.CloudDone)
                            }

                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last synced: ${formatLastSync(uiState.lastSyncTime)}",
                            fontSize = 11.sp,
                            color = TextDarkMuted
                        )
                    }

                    if (uiState.syncStatus == SyncStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ElectricBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        OutlinedButton(
                            onClick = {
                                viewModel.syncNow()
                                Toast.makeText(context, "Synchronizing...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ElectricBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 12.sp, color = ElectricBlue)
                        }
                    }
                }
            }

            // Local-First Guarantee Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Local-First & Privacy Guarantee",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your scripts and teleprompter settings sync safely to your cloud. Video recordings remain 100% private and never leave this device.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            HorizontalDivider(color = LightBorderSubtle, modifier = Modifier.padding(vertical = 4.dp))

            // Storage Meter Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = null,
                                tint = TextDarkHeading,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Cloud Storage Used",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkHeading,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Text(
                        text = if (isGuest) "Local Only" else "12 KB of 1 GB",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextDarkMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { if (isGuest) 0.001f else 0.012f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricBlue,
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round
                )
            }

            HorizontalDivider(color = LightBorderSubtle, modifier = Modifier.padding(vertical = 4.dp))

            // Navigation Rows: My Scripts
            ProfileNavRow(
                icon = Icons.Outlined.Description,
                title = "My Scripts",
                onClick = onNavigateToMyScripts
            )

            HorizontalDivider(color = LightBorderSubtle)

            // Navigation Rows: Trash
            ProfileNavRow(
                icon = Icons.Outlined.Delete,
                title = "Trash",
                onClick = {
                    Toast.makeText(context, "Trash folder is empty", Toast.LENGTH_SHORT).show()
                }
            )

            HorizontalDivider(color = LightBorderSubtle)

            // Navigation Rows: Settings
            ProfileNavRow(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                onClick = onNavigateToAdvancedSettings
            )

            HorizontalDivider(color = LightBorderSubtle)

            // Navigation Rows: Sign Out / Switch Account
            ProfileNavRow(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                title = if (isGuest) "Switch Account / Sign In" else "Sign Out",
                iconTint = if (isGuest) ElectricBlue else Color(0xFFEF4444),
                textColor = if (isGuest) ElectricBlue else Color(0xFFEF4444),
                onClick = {
                    if (isGuest) {
                        onNavigateToSignIn()
                    } else {
                        showSignOutDialog = true
                    }
                }
            )
        }

        // Sign Out Confirmation Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(
                        text = "Sign Out?",
                        fontWeight = FontWeight.Bold,
                        color = TextDarkHeading
                    )
                },
                text = {
                    Text(
                        text = "Your local scripts and recordings will remain safe on this device. Cloud sync will be paused until you sign in again.",
                        color = TextDarkMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutDialog = false
                            viewModel.signOut {
                                Toast.makeText(context, "Signed out. Local data preserved.", Toast.LENGTH_SHORT).show()
                                onSignOut()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Sign Out", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Cancel", color = TextDarkMuted)
                    }
                }
            )
        }
    }
}

private fun formatLastSync(timestamp: Long): String {
    if (timestamp <= 0L) return "Not synced yet"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${(diff / 60_000L).coerceAtLeast(1)}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = TextDarkHeading,
    textColor: Color = TextDarkHeading
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 15.sp
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextDarkMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
