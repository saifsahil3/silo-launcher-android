package com.example.ui.components.drive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

/**
 * Bottom Sheet to create a Split-Screen App Pair for Drive Mode with App Search & Pair Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveAppPairCreateSheet(
    allApps: List<AppInfo>,
    onCreatePair: (label: String, package1: String, package2: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedApp1 by remember { mutableStateOf<AppInfo?>(allApps.find { it.packageName.contains("maps") } ?: allApps.firstOrNull()) }
    var selectedApp2 by remember { mutableStateOf<AppInfo?>(allApps.find { it.packageName.contains("spotify") || it.packageName.contains("music") } ?: allApps.getOrNull(1)) }
    var searchQuery by remember { mutableStateOf("") }
    var customLabel  by remember { mutableStateOf("") }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            val q = searchQuery.lowercase()
            allApps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
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
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Split-Screen App Pair",
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
                placeholder = { Text("Filter apps...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF283042)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Pair Preview Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1B202D),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // App 1 Preview
                    AppPreviewBox(app = selectedApp1, label = "App 1 (Left/Top)", color = Color(0xFF8B5CF6))

                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )

                    // App 2 Preview
                    AppPreviewBox(app = selectedApp2, label = "App 2 (Right/Bottom)", color = Color(0xFF10B981))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App 1 Selector
            Text(
                text = "Primary App (Left / Top):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            AppHorizontalPickerRow(
                apps = filteredApps,
                selectedApp = selectedApp1,
                onSelectApp = { selectedApp1 = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // App 2 Selector
            Text(
                text = "Secondary App (Right / Bottom):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            AppHorizontalPickerRow(
                apps = filteredApps,
                selectedApp = selectedApp2,
                onSelectApp = { selectedApp2 = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pair Name Label Input
            val defaultName = remember(selectedApp1, selectedApp2) {
                val p1 = selectedApp1?.label ?: "App 1"
                val p2 = selectedApp2?.label ?: "App 2"
                "$p1 + $p2"
            }

            OutlinedTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = { Text("App Pair Label") },
                placeholder = { Text(defaultName) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF283042)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Create Button
            Button(
                onClick = {
                    val app1 = selectedApp1 ?: return@Button
                    val app2 = selectedApp2 ?: return@Button
                    val finalLabel = customLabel.ifBlank { defaultName }
                    onCreatePair(finalLabel, app1.packageName, app2.packageName)
                    onDismiss()
                },
                enabled = selectedApp1 != null && selectedApp2 != null && selectedApp1 != selectedApp2,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_app_pair_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save App Pair",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AppPreviewBox(
    app: AppInfo?,
    label: String,
    color: Color
) {
    val context = LocalContext.current
    val bitmap = remember(app) {
        if (app == null) null
        else app.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmapSafe()
        } catch (e: Exception) { null }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = app?.label,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column {
            Text(
                text = app?.label ?: "Select",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AppHorizontalPickerRow(
    apps: List<AppInfo>,
    selectedApp: AppInfo?,
    onSelectApp: (AppInfo) -> Unit
) {
    val context = LocalContext.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(apps) { app ->
            val isSelected = app.packageName == selectedApp?.packageName
            val bitmap = remember(app) {
                app.iconDrawable?.toImageBitmapSafe() ?: try {
                    context.packageManager.getApplicationIcon(app.packageName).toImageBitmapSafe()
                } catch (e: Exception) { null }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color(0xFF1C2230),
                border = BorderStroke(1.5.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF2B3448)),
                modifier = Modifier
                    .width(76.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectApp(app) }
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = app.label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
