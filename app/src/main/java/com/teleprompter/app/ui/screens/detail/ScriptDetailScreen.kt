package com.teleprompter.app.ui.screens.detail

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.CyanGradient
import com.teleprompter.app.core.theme.DeepNavyCard
import com.teleprompter.app.core.theme.DeepNavyDark
import com.teleprompter.app.core.theme.DeepNavySurface
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ElectricBlueLight
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.LightSurface
import com.teleprompter.app.core.theme.PurpleGradient
import com.teleprompter.app.core.theme.TextDarkBody
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.ui.components.TeleprompterBrandTitle

@Composable
fun ScriptDetailScreen(
    viewModel: ScriptDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    onStartTeleprompter: (String) -> Unit,
    onStartRecording: (String) -> Unit = {},
    onNavigateToRecordings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val script = uiState.script

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize().background(LightSurface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ElectricBlue)
        }
        return
    }

    if (script == null) {
        Box(
            modifier = modifier.fillMaxSize().background(LightSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("Script not found", color = TextDarkMuted)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightSurface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDarkHeading,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { /* Bookmark / Tag action */ }) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Tag",
                    tint = TextDarkMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and Metadata Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = script.title.ifBlank { "Untitled Script" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val metaText = ScriptUtils.formatScriptMeta(script.wordCount, script.estimatedReadingTime)
            Text(
                text = "$metaText • ${script.wordCount} words",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextDarkMuted,
                    fontSize = 15.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Quick Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = { onNavigateToEditor(script.id) },
                gradient = listOf(ElectricBlue, CyanGradient)
            )

            QuickActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, viewModel.getExportText())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Script"))
                },
                gradient = listOf(CyanGradient, PurpleGradient)
            )

            QuickActionButton(
                icon = Icons.Default.Timer,
                label = "Timer",
                onClick = {
                    Toast.makeText(context, "Reading time: ${ScriptUtils.formatReadingTime(script.estimatedReadingTime)}", Toast.LENGTH_SHORT).show()
                },
                gradient = listOf(PurpleGradient, ElectricBlue)
            )

            QuickActionButton(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                onClick = { viewModel.openFolderDialog() },
                gradient = listOf(ElectricBlueLight, ElectricBlue)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Premium Primary Action Buttons
        PremiumActionListItem(
            icon = Icons.Default.PlayArrow,
            iconTint = ElectricBlue,
            iconBg = ElectricBlueLight,
            title = "Start Teleprompter",
            onClick = { onStartTeleprompter(script.id) }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.Videocam,
            iconTint = TextDarkHeading,
            iconBg = Color(0xFFF0F0F2),
            title = "Start Recording",
            onClick = {
                onStartRecording(script.id)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.VideoLibrary,
            iconTint = ElectricBlue,
            iconBg = ElectricBlueLight,
            title = "View Recordings",
            onClick = {
                onNavigateToRecordings()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.ContentCopy,
            iconTint = TextDarkHeading,
            iconBg = Color(0xFFF0F0F2),
            title = "Duplicate Script",
            onClick = {
                viewModel.duplicateScript {
                    Toast.makeText(context, "Script duplicated", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add to Favorites Switch Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleFavorite() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (script.isFavorite) ElectricBlueLight else Color(0xFFF0F0F2))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (script.isFavorite) PurpleGradient else TextDarkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Add to Favorites",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextDarkHeading
                    )
                )
            }

            Switch(
                checked = script.isFavorite,
                onCheckedChange = { viewModel.toggleFavorite() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                )
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.Folder,
            iconTint = TextDarkHeading,
            iconBg = Color(0xFFF0F0F2),
            title = "Move to Folder",
            trailingText = uiState.folderName ?: "None",
            hasChevron = true,
            onClick = { viewModel.openFolderDialog() }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.Description,
            iconTint = TextDarkHeading,
            iconBg = Color(0xFFF0F0F2),
            title = "Export as Text",
            hasChevron = true,
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, script.title)
                    putExtra(Intent.EXTRA_TEXT, viewModel.getExportText())
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export as Text"))
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.Share,
            iconTint = TextDarkHeading,
            iconBg = Color(0xFFF0F0F2),
            title = "Share Script",
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, viewModel.getExportText())
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Script"))
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E5E7))

        PremiumActionListItem(
            icon = Icons.Default.Delete,
            iconTint = Color(0xFFEF4444),
            iconBg = Color(0xFFFEF2F2),
            title = "Delete Script",
            titleColor = Color(0xFFEF4444),
            onClick = { viewModel.openDeleteDialog() }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Move to Folder Dialog
    if (uiState.showFolderDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFolderDialog() },
            title = { Text("Move to Folder", fontWeight = FontWeight.Bold, color = ElectricBlue) },
            text = {
                Column {
                    Text(
                        text = "Select a folder for this script:",
                        color = TextDarkMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.moveToFolder(null) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No Folder (Uncategorized)",
                            fontWeight = if (script.folderId == null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (script.folderId == null) ElectricBlue else TextDarkHeading
                        )
                    }

                    uiState.availableFolders.forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.moveToFolder(folder.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(android.graphics.Color.parseColor(folder.colorHex)), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = folder.name,
                                fontWeight = if (script.folderId == folder.id) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (script.folderId == folder.id) ElectricBlue else TextDarkHeading
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFolderDialog() }) {
                    Text("Cancel", color = TextDarkMuted)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Script", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = { Text("Are you sure you want to delete '${script.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteScript {
                            Toast.makeText(context, "Script deleted", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel", color = TextDarkMuted)
                }
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    gradient: List<Color>
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(gradient))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PremiumActionListItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    titleColor: Color = TextDarkHeading,
    trailingText: String? = null,
    hasChevron: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg)
                    .padding(12.dp)
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
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    fontSize = 15.sp
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDarkMuted,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            if (hasChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextDarkMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
