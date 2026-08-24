package com.example.ui.components.drive

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Telephony
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneInTalk
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.db.DriveCommShortcutEntity

/**
 * Ultra-Minimalist Quick Contacts Card — Single Horizontal Row with Classy Edit Mode.
 *
 *  • Single horizontal row of 4 contact tiles
 *  • 56dp circular avatar (Contact Photo or installed App Icon)
 *  • Classy Edit Mode: Long press or tap 'Edit' brings up edit overlay badges & 'Done ✓' checkmark tick
 */
@Composable
fun DriveCommShortcutsCard(
    shortcuts: List<DriveCommShortcutEntity>,
    onShortcutClick: (DriveCommShortcutEntity) -> Unit,
    onShortcutLongClick: (Int, DriveCommShortcutEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }

    val slotList = (0..3).map { index ->
        shortcuts.find { it.slotIndex == index } ?: DriveCommShortcutEntity(slotIndex = index)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        border = BorderStroke(1.dp, Color(0xFF252C3D)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("drive_comm_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header Bar with Classy Done ✓ Tick ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUICK CONTACTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD),
                        letterSpacing = 1.sp
                    )
                }

                if (isEditMode) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3B82F6),
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
                        color = Color(0xFF60A5FA),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isEditMode = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // ── Single Horizontal Row (4 Contact Items) ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slotList.forEach { shortcut ->
                    CommContactHorizontalTile(
                        shortcut = shortcut,
                        isEditMode = isEditMode,
                        onClick = {
                            if (isEditMode) {
                                onShortcutLongClick(shortcut.slotIndex, shortcut)
                            } else if (shortcut.phoneNumber.isNotBlank()) {
                                onShortcutClick(shortcut)
                            } else {
                                onShortcutLongClick(shortcut.slotIndex, shortcut)
                            }
                        },
                        onLongClick = {
                            isEditMode = true
                        },
                        onEditClick = {
                            onShortcutLongClick(shortcut.slotIndex, shortcut)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommContactHorizontalTile(
    shortcut: DriveCommShortcutEntity,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAssigned = shortcut.phoneNumber.isNotBlank()
    val channelInfo = getChannelBadge(shortcut.channelType)
    val accentColor = channelInfo.badgeColor

    val appIconDrawable = remember(shortcut.channelType) {
        getChannelAppDrawable(context, shortcut.channelType)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAssigned) Color(0xFF181E2D) else Color(0xFF141920),
        border = BorderStroke(
            1.dp,
            if (isEditMode && isAssigned) Color(0xFF3B82F6) else if (isAssigned) accentColor.copy(alpha = 0.25f) else Color(0xFF242A3B).copy(alpha = 0.5f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("comm_slot_${shortcut.slotIndex}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isAssigned) {
                    // Avatar Box (56dp) with App Badge at bottom-right
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val photoUriString = shortcut.photoUri
                        val hasPhoto = !photoUriString.isNullOrBlank()

                        if (hasPhoto) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(if (photoUriString!!.startsWith("file://")) Uri.parse(photoUriString) else photoUriString)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = shortcut.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, accentColor.copy(alpha = 0.7f), CircleShape)
                            )
                        } else if (appIconDrawable != null) {
                            val appIconPainter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(appIconDrawable)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = appIconPainter,
                                contentDescription = shortcut.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, accentColor.copy(alpha = 0.7f), CircleShape)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(2.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = channelInfo.icon,
                                    contentDescription = shortcut.name,
                                    tint = accentColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // 20dp Overlay Badge at Bottom-Right
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .border(1.5.dp, Color(0xFF141722), CircleShape)
                        ) {
                            if (appIconDrawable != null) {
                                val badgePainter = rememberAsyncImagePainter(appIconDrawable)
                                Image(
                                    painter = badgePainter,
                                    contentDescription = channelInfo.label,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = channelInfo.icon,
                                    contentDescription = channelInfo.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = shortcut.name.ifBlank { "Contact" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Empty Slot: Dashed Circle with '+'
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "+ Add",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.45f),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Overlay Edit Badge in Edit Mode
            if (isEditMode && isAssigned) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .clickable { onEditClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Contact",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

private data class CommChannelBadge(
    val label: String,
    val shortLabel: String,
    val icon: ImageVector,
    val badgeColor: Color
)

private fun getChannelBadge(channelType: String): CommChannelBadge {
    return when (channelType.uppercase()) {
        "WHATSAPP" -> CommChannelBadge("WhatsApp",  "WA",   Icons.AutoMirrored.Filled.Chat,     Color(0xFF25D366))
        "SMS"      -> CommChannelBadge("SMS",        "SMS",  Icons.AutoMirrored.Filled.Message,  Color(0xFF0284C7))
        "TELEGRAM" -> CommChannelBadge("Telegram",   "TG",   Icons.AutoMirrored.Filled.Send,     Color(0xFF229ED9))
        else       -> CommChannelBadge("Phone Call", "CALL", Icons.Default.PhoneInTalk,           Color(0xFF10B981))
    }
}

private fun getChannelAppDrawable(context: Context, channelType: String): Drawable? {
    val pm = context.packageManager
    val packageName = when (channelType.uppercase()) {
        "WHATSAPP" -> "com.whatsapp"
        "TELEGRAM" -> "org.telegram.messenger"
        "SMS"      -> Telephony.Sms.getDefaultSmsPackage(context) ?: "com.google.android.apps.messaging"
        else -> {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.resolveActivity(pm)?.packageName ?: "com.google.android.dialer"
        }
    }

    return try {
        pm.getApplicationIcon(packageName)
    } catch (e: Exception) {
        try {
            if (channelType.uppercase() == "WHATSAPP") pm.getApplicationIcon("com.whatsapp.w4b") else null
        } catch (ex: Exception) {
            null
        }
    }
}
