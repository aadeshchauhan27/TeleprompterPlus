package com.teleprompter.app.ui.screens.editor

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.CyanGradient
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ElectricBlueLight
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.LightSurface
import com.teleprompter.app.core.theme.PurpleGradient
import com.teleprompter.app.core.theme.TextDarkBody
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.ui.screens.editor.SaveStatus

@Composable
fun ScriptEditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightSurface)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top Navigation Bar with premium styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
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

            // Save status pill in center
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (uiState.saveStatus) {
                    SaveStatus.SAVING -> {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", fontSize = 12.sp, color = ElectricBlue)
                    }
                    SaveStatus.SAVED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CyanGradient,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saved", fontSize = 12.sp, color = CyanGradient, fontWeight = FontWeight.SemiBold)
                    }
                    SaveStatus.DIRTY -> {
                        Text("Unsaved changes", fontSize = 12.sp, color = TextDarkMuted)
                    }
                }
            }

            TextButton(
                onClick = {
                    viewModel.saveExplicitly {
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(
                    text = "Save",
                    color = ElectricBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E5E7))

        // Script Title Input Field with premium pill design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE5E5E7), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable { /* Focus handler could be added here */ }
        ) {
            if (uiState.title.isEmpty()) {
                Text(
                    text = "Script Title...",
                    color = TextDarkMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                textStyle = TextStyle(
                    color = TextDarkHeading,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(ElectricBlue),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Editor Toolbar with modern icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F0F2))
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) TextDarkHeading else TextDarkMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) TextDarkHeading else TextDarkMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F0F2))
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Script", uiState.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied script to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextDarkHeading,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text?.toString().orEmpty()
                                if (text.isNotBlank()) {
                                    val merged = if (uiState.content.isBlank()) text else "${uiState.content}\n$text"
                                    viewModel.onContentChanged(merged)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = TextDarkHeading,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE5E5E7))

        // Script Body text area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (uiState.content.isEmpty()) {
                Text(
                    text = "Start typing or pasting your script here...\n\nEvery change saves automatically.",
                    color = TextDarkMuted,
                    fontSize = 17.sp,
                    lineHeight = 26.sp
                )
            }

            BasicTextField(
                value = uiState.content,
                onValueChange = { viewModel.onContentChanged(it) },
                textStyle = TextStyle(
                    color = TextDarkBody,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(ElectricBlue),
                modifier = Modifier.fillMaxSize()
            )

            // Floating Word Count & Reading Time Badge at Bottom Right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ElectricBlue)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val metaText = ScriptUtils.formatScriptMeta(uiState.wordCount, uiState.estimatedReadingSeconds)
                Text(
                    text = metaText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
