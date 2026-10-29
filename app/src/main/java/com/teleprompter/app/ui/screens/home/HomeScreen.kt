package com.teleprompter.app.ui.screens.home

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.CyanGradient
import com.teleprompter.app.core.theme.DeepNavyDark
import com.teleprompter.app.core.theme.DeepNavySurface
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ElectricBlueLight
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.LightSurface
import com.teleprompter.app.core.theme.PinkGradient
import com.teleprompter.app.core.theme.PurpleGradient
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.ui.components.EmptyState
import com.teleprompter.app.ui.components.ScriptCard
import com.teleprompter.app.ui.components.TeleprompterBrandTitle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreateScript: () -> Unit,
    onNavigateToScriptDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToRecordings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPro: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val tabs = listOf("All", "Recent", "Favorites")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightSurface)
            .statusBarsPadding(),
        bottomBar = {
            HomeBottomNavigationBar(
                currentRoute = "home",
                onNavigateHome = {},
                onNavigateRecordings = onNavigateToRecordings,
                onNavigateFolders = onNavigateToFolders,
                onNavigateSearch = onNavigateToSearch,
                onNavigateProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightSurface)
        ) {
            // Header: Centered App Title with premium styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeleprompterBrandTitle(fontSize = 24.sp, isDarkBackground = false)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pro Badge - modern gradient look
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PinkGradient)
                            .clickable(onClick = onNavigateToPro),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 20.sp)
                    }

                    // Search Button - premium pill design
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F2))
                            .clickable(onClick = onNavigateToSearch),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextDarkMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Action Buttons: Modern gradient design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Primary Gradient Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = ElectricBlue)
                        .clickable(onClick = onNavigateToCreateScript),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "New Script",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Outline Button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val text = clip.getItemAt(0).text?.toString().orEmpty()
                            if (text.isNotBlank()) {
                                viewModel.pasteFromClipboard(text) { newId ->
                                    Toast.makeText(context, "Script created from clipboard", Toast.LENGTH_SHORT).show()
                                    onNavigateToScriptDetail(newId)
                                }
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightSurface),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E2))
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = TextDarkHeading,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Paste from Clipboard",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = TextDarkHeading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs - Premium modern look
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.Transparent,
                contentColor = ElectricBlue,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[uiState.selectedTab])
                            .height(3.dp)
                            .width(tabPositions[uiState.selectedTab].width * 0.6f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ElectricBlue)
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFFE5E5E7))
                    )
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = uiState.selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isSelected) ElectricBlue else TextDarkMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Script List / Loading / Empty State
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                uiState.displayedScripts.isEmpty() -> {
                    val emptyMessage = when (uiState.selectedTab) {
                        1 -> Pair("No Recent Scripts", "Scripts you view or edit recently will appear here.")
                        2 -> Pair("No Favorites Yet", "Tap the star icon on any script to quickly access it from favorites.")
                        else -> Pair("No Scripts Created", "Start by writing your first video script or pasting from clipboard.")
                    }
                    EmptyState(
                        title = emptyMessage.first,
                        subtitle = emptyMessage.second,
                        actionLabel = if (uiState.selectedTab == 0) "+ Create Script" else null,
                        onActionClick = if (uiState.selectedTab == 0) onNavigateToCreateScript else null
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.displayedScripts,
                            key = { it.id }
                        ) { script ->
                            ScriptCard(
                                script = script,
                                onClick = { onNavigateToScriptDetail(script.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(script) },
                                onDuplicate = {
                                    viewModel.duplicateScript(script.id) {
                                        Toast.makeText(context, "Script duplicated", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteScript(script.id)
                                    Toast.makeText(context, "Script deleted", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeBottomNavigationBar(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateRecordings: () -> Unit = {},
    onNavigateFolders: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LightSurface)
            .border(width = 0.5.dp, color = Color(0xFFE5E5E7))
            .navigationBarsPadding()
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scripts Tab
            BottomNavItem(
                label = "Scripts",
                icon = Icons.Outlined.Description,
                isSelected = currentRoute == "home",
                onClick = onNavigateHome
            )

            // Recordings Tab
            BottomNavItem(
                label = "Recordings",
                icon = Icons.Default.VideoLibrary,
                isSelected = currentRoute == "recordings",
                onClick = onNavigateRecordings
            )

            // Folders Tab
            BottomNavItem(
                label = "Folders",
                icon = Icons.Outlined.Folder,
                isSelected = currentRoute == "folders",
                onClick = onNavigateFolders
            )

            // Search Tab
            BottomNavItem(
                label = "Search",
                icon = Icons.Outlined.Search,
                isSelected = currentRoute == "search",
                onClick = onNavigateSearch
            )

            // Profile & Sync Tab
            BottomNavItem(
                label = "Profile",
                icon = Icons.Outlined.Person,
                isSelected = currentRoute == "profile",
                onClick = onNavigateProfile
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) ElectricBlue else TextDarkMuted
    val iconTint = if (isSelected) ElectricBlue else TextDarkMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = color
        )
    }
}
