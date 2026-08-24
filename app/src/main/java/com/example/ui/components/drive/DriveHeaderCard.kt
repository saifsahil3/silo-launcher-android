package com.example.ui.components.drive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DriveStats

/**
 * Section 1: Header (Drive Status & Speed)
 * Full-width header card showing vehicle drive status indicator and integrated digital speedometer.
 */
@Composable
fun DriveHeaderCard(
    driveStats: DriveStats,
    onToggleSpeedSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B24)),
        border = BorderStroke(1.dp, Color(0xFF282D3C)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleSpeedSimulation() }
            .testTag("speedometer_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Vehicle drive status indicator
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Vehicle Status",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DRIVE MODE ACTIVE",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF9800),
                            fontSize = 13.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (driveStats.currentSpeedMph > 0) Color(0xFF10B981) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (driveStats.connectedBluetoothDevice != null) {
                                    "BT: ${driveStats.connectedBluetoothDevice}"
                                } else if (driveStats.currentSpeedMph > 0) {
                                    "Driving Detected • Live GPS"
                                } else {
                                    "Cockpit Ready • Tap to test speed"
                                },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Right: Integrated digital speedometer
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF222634))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speedometer",
                    tint = if (driveStats.currentSpeedMph > 0) Color(0xFFFF9800) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(bottom = 4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${driveStats.currentSpeedMph}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (driveStats.currentSpeedMph > 0) Color(0xFFFFB74D) else Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = " MPH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
