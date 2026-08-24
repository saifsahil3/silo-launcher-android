package com.example.ui.components.drive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.ui.DriveAppPair
import com.example.ui.DriveQuickShortcut
import com.example.util.toImageBitmapSafe
import coil.compose.rememberAsyncImagePainter

enum class DriveDrawerTab {
    APPS, SHORTCUTS, APP_PAIRS
}

/**
 * Categorized Driving App & Shortcut Drawer with Classy Edit Mode.
 * Features 3 tabs: [ Apps ], [ Shortcuts ], [ App Pairs ]
 */
@Composable
fun DriveAppDrawerCard(
    pinnedPackages: List<String>,
    allApps: List<AppInfo>,
    appPairs: List<DriveAppPair>,
    quickShortcuts: List<DriveQuickShortcut>,
    onLaunchApp: (AppInfo) -> Unit,
    onLaunchAppPair: (DriveAppPair) -> Unit,
    onLaunchQuickShortcut: (DriveQuickShortcut) -> Unit,
    onAddAppClick: () -> Unit,
    onAddAppPairClick: () -> Unit,
    onAddShortcutClick: () -> Unit,
    onRemoveAppClick: (String) -> Unit,
    onRemoveAppPairClick: (String) -> Unit,
    onRemoveShortcutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(DriveDrawerTab.APPS) }
    var isEditMode by remember { mutableStateOf(false) }

    val pinnedAppInfos = remember(pinnedPackages, allApps) {
        pinnedPackages.mapNotNull { pkg ->
            allApps.find { it.packageName == pkg }
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        border = BorderStroke(1.dp, Color(0xFF252C3D)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("drive_app_drawer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header + Classy Done ✓ Button ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COCKPIT LAUNCHER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDDD6FE),
                    letterSpacing = 1.sp
                )

                if (isEditMode) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isEditMode = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isEditMode = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Categorized Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B202E))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerTabButton(
                    label = "Apps",
                    icon = Icons.Default.GridView,
                    isSelected = activeTab == DriveDrawerTab.APPS,
                    onClick = { activeTab = DriveDrawerTab.APPS },
                    modifier = Modifier.weight(1f)
                )
                DrawerTabButton(
                    label = "Shortcuts",
                    icon = Icons.Default.FlashOn,
                    isSelected = activeTab == DriveDrawerTab.SHORTCUTS,
                    onClick = { activeTab = DriveDrawerTab.SHORTCUTS },
                    modifier = Modifier.weight(1f)
                )
                DrawerTabButton(
                    label = "App Pairs",
                    icon = Icons.Default.Layers,
                    isSelected = activeTab == DriveDrawerTab.APP_PAIRS,
                    onClick = { activeTab = DriveDrawerTab.APP_PAIRS },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Tab Content ───────────────────────────────────────────────────
            when (activeTab) {
                DriveDrawerTab.APPS -> {
                    val totalItems: List<AppInfo?> = pinnedAppInfos + listOf(null)
                    val chunkedRows = totalItems.chunked(4)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (rowItems in chunkedRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (appItem in rowItems) {
                                    if (appItem != null) {
                                        PinnedAppTile(
                                            appInfo = appItem,
                                            isEditMode = isEditMode,
                                            onClick = {
                                                if (isEditMode) {
                                                    onRemoveAppClick(appItem.packageName)
                                                } else {
                                                    onLaunchApp(appItem)
                                                }
                                            },
                                            onLongClick = { isEditMode = true },
                                            onRemoveClick = { onRemoveAppClick(appItem.packageName) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        AddTile(
                                            label = "Add App",
                                            onClick = onAddAppClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                val remaining = 4 - rowItems.size
                                if (remaining > 0) {
                                    repeat(remaining) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                DriveDrawerTab.SHORTCUTS -> {
                    val totalItems: List<DriveQuickShortcut?> = quickShortcuts + listOf(null)
                    val chunkedRows = totalItems.chunked(4)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (rowItems in chunkedRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (shortcutItem in rowItems) {
                                    if (shortcutItem != null) {
                                        QuickShortcutTile(
                                            shortcut = shortcutItem,
                                            isEditMode = isEditMode,
                                            onClick = {
                                                if (isEditMode) {
                                                    onRemoveShortcutClick(shortcutItem.id)
                                                } else {
                                                    onLaunchQuickShortcut(shortcutItem)
                                                }
                                            },
                                            onLongClick = { isEditMode = true },
                                            onRemoveClick = { onRemoveShortcutClick(shortcutItem.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        AddTile(
                                            label = "Add Shortcut",
                                            onClick = onAddShortcutClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                val remaining = 4 - rowItems.size
                                if (remaining > 0) {
                                    repeat(remaining) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                DriveDrawerTab.APP_PAIRS -> {
                    val totalItems: List<DriveAppPair?> = appPairs + listOf(null)
                    val chunkedRows = totalItems.chunked(2)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (rowItems in chunkedRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (pairItem in rowItems) {
                                    if (pairItem != null) {
                                        AppPairTile(
                                            appPair = pairItem,
                                            allApps = allApps,
                                            isEditMode = isEditMode,
                                            onClick = {
                                                if (isEditMode) {
                                                    onRemoveAppPairClick(pairItem.id)
                                                } else {
                                                    onLaunchAppPair(pairItem)
                                                }
                                            },
                                            onLongClick = { isEditMode = true },
                                            onRemoveClick = { onRemoveAppPairClick(pairItem.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        AddTile(
                                            label = "Add Pair",
                                            onClick = onAddAppPairClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                val remaining = 2 - rowItems.size
                                if (remaining > 0) {
                                    repeat(remaining) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF8B5CF6) else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedAppTile(
    appInfo: AppInfo,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageBitmap = remember(appInfo) {
        appInfo.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appInfo.packageName).toImageBitmapSafe()
        } catch (e: Throwable) { null }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1F2D),
        border = BorderStroke(1.dp, if (isEditMode) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF283042)),
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("pinned_app_${appInfo.packageName}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF242C3D))
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = appInfo.label,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = appInfo.label,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = appInfo.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            if (isEditMode) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onRemoveClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove App",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickShortcutTile(
    shortcut: DriveQuickShortcut,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIconDrawable = remember(shortcut.packageName) {
        try {
            context.packageManager.getApplicationIcon(shortcut.packageName)
        } catch (e: Exception) { null }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1F2D),
        border = BorderStroke(1.dp, if (isEditMode) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF3B82F6).copy(alpha = 0.25f)),
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF242C3D))
                ) {
                    if (appIconDrawable != null) {
                        val painter = rememberAsyncImagePainter(appIconDrawable)
                        Image(
                            painter = painter,
                            contentDescription = shortcut.label,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = shortcut.label,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = shortcut.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            if (isEditMode) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onRemoveClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Shortcut",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppPairTile(
    appPair: DriveAppPair,
    allApps: List<AppInfo>,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app1 = allApps.find { it.packageName == appPair.package1 }
    val app2 = allApps.find { it.packageName == appPair.package2 }

    val icon1 = remember(appPair.package1) {
        app1?.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appPair.package1).toImageBitmapSafe()
        } catch (e: Exception) { null }
    }
    val icon2 = remember(appPair.package2) {
        app2?.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appPair.package2).toImageBitmapSafe()
        } catch (e: Exception) { null }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B1E2D),
        border = BorderStroke(1.dp, if (isEditMode) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF8B5CF6).copy(alpha = 0.35f)),
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dual-Icon Overlap Box
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Icon 1 (Left / Primary)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color(0xFF252D40))
                            .border(1.dp, Color(0xFF8B5CF6), CircleShape)
                    ) {
                        if (icon1 != null) {
                            Image(bitmap = icon1, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Icon 2 (Right / Secondary)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2536))
                            .border(1.dp, Color(0xFF10B981), CircleShape)
                    ) {
                        if (icon2 != null) {
                            Image(bitmap = icon2, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appPair.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Split-Screen",
                        fontSize = 10.sp,
                        color = Color(0xFFA78BFA)
                    )
                }
            }

            if (isEditMode) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onRemoveClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove App Pair",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171A24),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = label,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
