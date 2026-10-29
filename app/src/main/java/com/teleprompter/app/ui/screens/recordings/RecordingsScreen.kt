package com.teleprompter.app.ui.screens.recordings

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.RecordRed
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.data.local.entity.RecordingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    viewModel: RecordingsViewModel,
    onNavigateBack: () -> Unit,
    onPlayRecording: (videoUri: Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recordings by viewModel.recordings.collectAsState()

    var recordingToDelete by remember { mutableStateOf<RecordingEntity?>(null) }
    var recordingToRename by remember { mutableStateOf<RecordingEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

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
                text = "My Recordings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (recordings.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${recordings.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkMuted
                    )
                }
            }
        }

        HorizontalDivider(color = LightBorderSubtle)

        if (recordings.isEmpty()) {
            // Clean Empty State (Screen requirements: No dummy data!)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = TextDarkMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No recordings yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkHeading,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "When you record videos using the teleprompter, your saved takes will appear here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextDarkMuted,
                            fontSize = 14.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Recordings List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(recordings, key = { it.id }) { recording ->
                    RecordingCard(
                        recording = recording,
                        onPlay = {
                            onPlayRecording(Uri.parse(recording.videoUri))
                        },
                        onShare = {
                            try {
                                val shareIntent = viewModel.createShareIntent(recording.videoUri)
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Video"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot share video: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRename = {
                            recordingToRename = recording
                            renameText = recording.title
                        },
                        onDelete = {
                            recordingToDelete = recording
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog (Permanent deletion confirmation)
    if (recordingToDelete != null) {
        val item = recordingToDelete!!
        AlertDialog(
            onDismissRequest = { recordingToDelete = null },
            title = {
                Text(
                    text = "Delete Recording?",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading
                )
            },
            text = {
                Text(
                    text = "This will permanently delete \"${item.title}\" and remove the video file from your device.",
                    color = TextDarkMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecording(item.id)
                        recordingToDelete = null
                        Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToDelete = null }) {
                    Text("Cancel", color = TextDarkMuted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Rename Dialog
    if (recordingToRename != null) {
        val item = recordingToRename!!
        AlertDialog(
            onDismissRequest = { recordingToRename = null },
            title = {
                Text(
                    text = "Rename Recording",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkHeading
                )
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameRecording(item.id, renameText)
                        }
                        recordingToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToRename = null }) {
                    Text("Cancel", color = TextDarkMuted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun RecordingCard(
    recording: RecordingEntity,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    val formattedDuration = String.format(
        Locale.US,
        "%02d:%02d",
        recording.durationSeconds / 60,
        recording.durationSeconds % 60
    )

    val formattedDate = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        .format(Date(recording.createdAt))

    val formattedSize = String.format(
        Locale.US,
        "%.1f MB",
        recording.fileSizeBytes / (1024.0 * 1024.0)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LightBorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Play overlay and Duration badge
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 75.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                // Efficient async thumbnail bitmap loader
                AsyncVideoThumbnail(
                    thumbnailPath = recording.thumbnailPath,
                    modifier = Modifier.fillMaxSize()
                )

                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Duration badge pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formattedDuration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details: Title, Date, Size
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recording.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkHeading,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkMuted,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedSize,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }

            // 3-dot Action Menu
            Box {
                IconButton(onClick = { isMenuOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = TextDarkMuted
                    )
                }

                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = { isMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onPlay()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onShare()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onRename()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete", color = RecordRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RecordRed) },
                        onClick = {
                            isMenuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AsyncVideoThumbnail(
    thumbnailPath: String?,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(thumbnailPath) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(thumbnailPath) {
        if (!thumbnailPath.isNullOrBlank() && File(thumbnailPath).exists()) {
            withContext(Dispatchers.IO) {
                try {
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = 1
                    }
                    val decoded = BitmapFactory.decodeFile(thumbnailPath, opts)
                    bitmap = decoded
                } catch (_: Exception) {}
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
