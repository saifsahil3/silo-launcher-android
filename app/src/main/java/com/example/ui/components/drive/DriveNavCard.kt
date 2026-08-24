package com.example.ui.components.drive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.DriveNavLocationEntity

/**
 * Navigation Card — 80/20 split primary action row:
 *  Left 80%  → "Start Navigation" button (launches Maps)
 *  Right 20% → Voice Assistant mic button (gradient, same prominence)
 *
 * Below: 4 quick destination shortcut pills (2×2 grid)
 *  - Single tap  → starts navigation to destination
 *  - Long press  → opens destination editor sheet
 */
@Composable
fun DriveNavCard(
    locations: List<DriveNavLocationEntity>,
    onOpenMapsClick: () -> Unit,
    onLocationClick: (DriveNavLocationEntity) -> Unit,
    onLocationLongClick: (DriveNavLocationEntity) -> Unit,
    onLaunchAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeLoc    = locations.find { it.id == "home"    } ?: DriveNavLocationEntity("home",    "Home",    "Home", 0)
    val workLoc    = locations.find { it.id == "work"    } ?: DriveNavLocationEntity("work",    "Work",    "Work", 1)
    val custom1Loc = locations.find { it.id == "custom1" } ?: DriveNavLocationEntity("custom1", "Gym",     "",     2)
    val custom2Loc = locations.find { it.id == "custom2" } ?: DriveNavLocationEntity("custom2", "Airport", "",     3)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        border = BorderStroke(1.dp, Color(0xFF252C3D)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("drive_nav_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Primary Action Row ─────────────────────────────────────────────
            // 80% Start Navigation | 20% Voice Assistant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 80% — Maps Launch Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier
                        .weight(0.8f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenMapsClick() }
                        .testTag("drive_primary_maps_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Navigation",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Start Navigation",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Open Maps & Directions",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // 20% — Voice Assistant Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.2f)
                        .height(68.dp) // matches nav button height
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                            )
                        )
                        .clickable { onLaunchAssistant() }
                        .testTag("drive_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // ── Quick Destination Pills ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK DESTINATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hold to edit",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NavDestinationPill(
                    location = homeLoc,
                    defaultIcon = Icons.Default.Home,
                    accentColor = Color(0xFF10B981),
                    onClick = { onLocationClick(homeLoc) },
                    onLongClick = { onLocationLongClick(homeLoc) },
                    modifier = Modifier.weight(1f)
                )
                NavDestinationPill(
                    location = workLoc,
                    defaultIcon = Icons.Default.Business,
                    accentColor = Color(0xFF3B82F6),
                    onClick = { onLocationClick(workLoc) },
                    onLongClick = { onLocationLongClick(workLoc) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NavDestinationPill(
                    location = custom1Loc,
                    defaultIcon = Icons.Default.Place,
                    accentColor = Color(0xFFF59E0B),
                    onClick = { onLocationClick(custom1Loc) },
                    onLongClick = { onLocationLongClick(custom1Loc) },
                    modifier = Modifier.weight(1f)
                )
                NavDestinationPill(
                    location = custom2Loc,
                    defaultIcon = Icons.Default.LocationOn,
                    accentColor = Color(0xFFA78BFA),
                    onClick = { onLocationClick(custom2Loc) },
                    onLongClick = { onLocationLongClick(custom2Loc) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavDestinationPill(
    location: DriveNavLocationEntity,
    defaultIcon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfigured = location.addressOrQuery.isNotBlank()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1F2D),
        border = BorderStroke(1.dp, if (isConfigured) accentColor.copy(alpha = 0.25f) else Color(0xFF252C3D).copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("nav_pill_${location.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = defaultIcon,
                    contentDescription = location.label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isConfigured) location.addressOrQuery else "Hold to set",
                    fontSize = 10.sp,
                    color = if (isConfigured) Color.White.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
