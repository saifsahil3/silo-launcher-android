package com.example.ui.components.drive

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.util.toImageBitmapSafe

data class AvailableAppShortcut(
    val packageName: String,
    val shortcutId: String,
    val label: String,
    val appName: String,
    val iconDrawable: Drawable? = null
)

/**
 * Bottom Sheet to query & pick official Android App Shortcuts (LauncherApps API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveAppShortcutPickerSheet(
    allApps: List<AppInfo>,
    onSelectShortcut: (packageName: String, shortcutId: String, label: String, appName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val availableShortcuts = remember(allApps, searchQuery) {
        queryAvailableAppShortcuts(context, allApps, searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141722),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add App Shortcut",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search app shortcuts...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF283042)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (availableShortcuts.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No app shortcuts published by installed apps" else "No matching app shortcuts found",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    items(availableShortcuts) { shortcut ->
                        val bitmap = remember(shortcut) {
                            shortcut.iconDrawable?.toImageBitmapSafe() ?: try {
                                context.packageManager.getApplicationIcon(shortcut.packageName).toImageBitmapSafe()
                            } catch (e: Exception) { null }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B202D),
                            border = BorderStroke(1.dp, Color(0xFF283042)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelectShortcut(
                                        shortcut.packageName,
                                        shortcut.shortcutId,
                                        shortcut.label,
                                        shortcut.appName
                                    )
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF242C3D))
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = shortcut.label,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = shortcut.label,
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shortcut.label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (shortcut.appName.isNotBlank()) {
                                        Text(
                                            text = shortcut.appName,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.5f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Queries published LauncherApps shortcuts across all installed applications
 */
private fun queryAvailableAppShortcuts(
    context: Context,
    allApps: List<AppInfo>,
    filterQuery: String
): List<AvailableAppShortcut> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()

    val resultList = mutableListOf<AvailableAppShortcut>()
    try {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return emptyList()
        val userHandle = Process.myUserHandle()

        allApps.forEach { appInfo ->
            try {
                val query = LauncherApps.ShortcutQuery().apply {
                    setPackage(appInfo.packageName)
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                    )
                }
                val shortcuts = launcherApps.getShortcuts(query, userHandle) ?: emptyList()
                shortcuts.forEach { shortcutInfo ->
                    val rawLabel = shortcutInfo.shortLabel?.toString()
                        ?: shortcutInfo.longLabel?.toString()
                        ?: shortcutInfo.id
                    if (rawLabel.isNotBlank()) {
                        val iconDrawable = try {
                            launcherApps.getShortcutIconDrawable(shortcutInfo, context.resources.displayMetrics.densityDpi)
                        } catch (e: Exception) { null }

                        resultList.add(
                            AvailableAppShortcut(
                                packageName = appInfo.packageName,
                                shortcutId = shortcutInfo.id,
                                label = rawLabel,
                                appName = appInfo.label,
                                iconDrawable = iconDrawable
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore restricted package query errors
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (filterQuery.isBlank()) {
        return resultList
    }
    val lower = filterQuery.lowercase()
    return resultList.filter {
        it.label.lowercase().contains(lower) || it.appName.lowercase().contains(lower)
    }
}
