package com.example.ui.components.drive

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DriveDestinationEta
import com.example.ui.DriveManeuverInfo

/**
 * Section 2: Full-Width Embedded Map Card
 * Dimensions: 100% card width, large vertical footprint.
 * Includes:
 *  - 2D Vector Map Canvas with dynamic location marker & route polyline
 *  - Embedded Turn-by-Turn Maneuver Banner at the top
 *  - Quick Navigation Chips with live dynamic ETA labels at the base
 *  - Full card tap opens default navigation application
 */
@Composable
fun DriveMapCard(
    maneuverInfo: DriveManeuverInfo,
    destinationEtas: List<DriveDestinationEta>,
    onCardClick: () -> Unit,
    onDestinationClick: (DriveDestinationEta) -> Unit,
    onManeuverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for GPS Driver Marker
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar_pulse")
    val radarRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_radius"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282D3C)),
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onCardClick() }
            .testTag("drive_map_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: 2D Vector Map Graphic Canvas (Automotive Cockpit Night Theme)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Road grid lines (Stylized vector road network)
                val roadColor = Color(0xFF202636)
                val highwayColor = Color(0xFF2C344A)

                // Background secondary streets
                drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.45f), strokeWidth = 8f)
                drawLine(roadColor, Offset(0f, h * 0.75f), Offset(w, h * 0.65f), strokeWidth = 8f)
                drawLine(roadColor, Offset(w * 0.25f, 0f), Offset(w * 0.2f, h), strokeWidth = 8f)
                drawLine(roadColor, Offset(w * 0.8f, 0f), Offset(w * 0.75f, h), strokeWidth = 8f)

                // Major Arterial Road / Highway
                val highwayPath = Path().apply {
                    moveTo(0f, h * 0.6f)
                    cubicTo(w * 0.3f, h * 0.55f, w * 0.6f, h * 0.75f, w, h * 0.5f)
                }
                drawPath(highwayPath, color = highwayColor, style = Stroke(width = 18f, cap = StrokeCap.Round))

                // Active Navigation Route Overlay (Vibrant Cyan / Blue polyline)
                val routePath = Path().apply {
                    moveTo(w * 0.5f, h * 0.52f)
                    lineTo(w * 0.5f, h * 0.38f)
                    cubicTo(w * 0.5f, h * 0.3f, w * 0.65f, h * 0.28f, w * 0.85f, h * 0.22f)
                }
                drawPath(
                    routePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
                    ),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Center Driver Location Marker with Radar Pulse
                val driverCenter = Offset(w * 0.5f, h * 0.52f)
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = radarAlpha),
                    radius = radarRadius,
                    center = driverCenter
                )
                // Driver Core Dot
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = driverCenter
                )
                drawCircle(
                    color = Color(0xFF00B0FF),
                    radius = 6f,
                    center = driverCenter
                )
            }

            // Layer 2: Content Overlay (Top Turn-by-Turn Banner & Bottom Quick ETA Chips)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Turn-by-Turn Maneuver Instruction Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E2433).copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384158)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onManeuverClick() }
                        .testTag("drive_maneuver_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (maneuverInfo.isNavigating) Color(0xFF2979FF) else Color(0xFF00E5FF).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (maneuverInfo.maneuverType == "LEFT") Icons.Default.TurnLeft else Icons.Default.NearMe,
                                    contentDescription = "Maneuver",
                                    tint = if (maneuverInfo.isNavigating) Color.White else Color(0xFF00E5FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = maneuverInfo.instruction,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = maneuverInfo.roadName,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tap map badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "MAP ↗",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Base: Quick Navigation Chips (Home, Work, Gas with dynamic ETAs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinationEtas.forEach { dest ->
                        NavigationDestinationChip(
                            dest = dest,
                            onClick = { onDestinationClick(dest) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationDestinationChip(
    dest: DriveDestinationEta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (dest.id) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "gas" -> Icons.Default.LocalGasStation
        else -> Icons.Default.Navigation
    }
    val accentColor = when (dest.id) {
        "home" -> Color(0xFF10B981)
        "work" -> Color(0xFF3B82F6)
        "gas" -> Color(0xFFF59E0B)
        else -> Color(0xFFA78BFA)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B202E).copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333B50)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("destination_chip_${dest.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = dest.title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = dest.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${dest.etaMinutes} min • ${dest.distanceMiles} mi",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }
    }
}
