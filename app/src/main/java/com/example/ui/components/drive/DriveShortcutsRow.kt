package com.example.ui.components.drive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.db.DriveShortcutEntity
import com.example.util.toImageBitmapSafe

/**
 * Section 4: Quick Shortcuts Row (Customizable Slot Area)
 * Slot 1: Speed Dial / Phone Favorite (1-tap direct dial, long-press to edit contact)
 * Slots 2-4: User-customizable app tiles (or [+] Add Shortcut tile if unassigned)
 */
@Composable
fun DriveShortcutsRow(
    shortcuts: List<DriveShortcutEntity>,
    allApps: List<AppInfo>,
    onSpeedDialClick: (DriveShortcutEntity?) -> Unit,
    onSpeedDialLongClick: () -> Unit,
    onAppSlotClick: (Int, DriveShortcutEntity?) -> Unit,
    onAppSlotLongClick: (Int, DriveShortcutEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    val speedDialShortcut = remember(shortcuts) {
        shortcuts.find { it.slotIndex == 0 }
    }
    val customSlots = remember(shortcuts) {
        (1..3).map { slotIndex ->
            shortcuts.find { it.slotIndex == slotIndex }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Slot 1 (Fixed Function: Speed Dial / Favorite Contact)
        SpeedDialTile(
            shortcut = speedDialShortcut,
            onClick = { onSpeedDialClick(speedDialShortcut) },
            onLongClick = onSpeedDialLongClick,
            modifier = Modifier.weight(1f)
        )

        // Slots 2–4 (Customizable Slots)
        customSlots.forEachIndexed { index, shortcut ->
            val slotIndex = index + 1
            val assignedApp = remember(shortcut, allApps) {
                if (shortcut != null && shortcut.packageName.isNotBlank()) {
                    allApps.find { it.packageName == shortcut.packageName }
                } else null
            }

            CustomShortcutTile(
                slotIndex = slotIndex,
                shortcut = shortcut,
                appInfo = assignedApp,
                onClick = { onAppSlotClick(slotIndex, shortcut) },
                onLongClick = { onAppSlotLongClick(slotIndex, shortcut) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeedDialTile(
    shortcut: DriveShortcutEntity?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasContact = shortcut != null && shortcut.phoneNumber.isNotBlank()
    val contactName = if (hasContact) shortcut.label.ifBlank { "Favorite" } else "Speed Dial"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15803D).copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.35f)),
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("drive_speed_dial_slot")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E).copy(alpha = 0.25f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Speed Dial",
                    tint = Color(0xFF4ADE80),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = contactName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasContact) "Tap to call" else "Tap to set",
                fontSize = 9.sp,
                color = Color(0xFF86EFAC),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomShortcutTile(
    slotIndex: Int,
    shortcut: DriveShortcutEntity?,
    appInfo: AppInfo?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAssigned = shortcut != null && shortcut.packageName.isNotBlank()

    val imageBitmap = remember(appInfo, shortcut) {
        appInfo?.iconDrawable?.toImageBitmapSafe() ?: try {
            if (shortcut != null && shortcut.packageName.isNotBlank()) {
                context.packageManager.getApplicationIcon(shortcut.packageName).toImageBitmapSafe()
            } else null
        } catch (e: Throwable) {
            null
        }
    }

    val labelText = if (isAssigned) {
        shortcut?.label?.ifBlank { appInfo?.label ?: "App" } ?: "App"
    } else {
        "Add"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAssigned) Color(0xFF181B24) else Color(0xFF141720)
        ),
        border = BorderStroke(
            1.dp,
            if (isAssigned) Color(0xFF282D3C) else Color(0xFF282D3C).copy(alpha = 0.5f)
        ),
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("drive_shortcut_slot_$slotIndex")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAssigned) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242836))
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = labelText,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = labelText,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Shortcut",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = labelText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAssigned) Color.White else Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isAssigned) "Hold to edit" else "Slot $slotIndex",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
