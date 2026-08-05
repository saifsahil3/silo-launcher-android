package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.model.LauncherMode
import com.example.service.PassThroughOverlayService
import com.example.ui.LauncherViewModel
import com.example.util.PassThroughManager
import com.example.util.toImageBitmapSafe

@Composable
fun PassThroughSettingsScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val installedLaunchers = remember {
        PassThroughManager.getInstalledLaunchers(context)
    }

    var selectedPackageName by remember {
        mutableStateOf(PassThroughManager.getSavedPassThroughLauncher(context) ?: "")
    }

    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    // Refresh overlay permission status when returning to app
    DisposableEffect(Unit) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
        onDispose { }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101218))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Architecture Overview Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00897B),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PASS-THROUGH MODE [EXPERIMENTAL]",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB74D),
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Stock Launcher Delegate",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pass-Through Mode is an experimental option that launches your phone's stock launcher package (e.g. Pixel Launcher) while keeping Silo Launcher as default without background overlay services.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }

        // Experimental Caution Note
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF261D18)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Experimental Delegate Launcher Mode",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Zero overlay background services. Launches chosen stock home app. Swiping home on some devices may return to Silo unless default home is changed.",
                            fontSize = 12.sp,
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }
        }

        // Installed Launchers List
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B26)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SELECT TARGET STOCK LAUNCHER (${installedLaunchers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (installedLaunchers.isEmpty()) {
                    Text(
                        text = "No secondary stock launcher packages found.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                } else {
                    installedLaunchers.forEach { launcher ->
                        val isSelected = launcher.packageName == selectedPackageName
                        val imageBitmap = remember(launcher.iconDrawable) {
                            launcher.iconDrawable?.toImageBitmapSafe()
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF003355) else Color(0xFF13151D),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFD1E4FF) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedPackageName = launcher.packageName
                                    PassThroughManager.savePassThroughLauncher(context, launcher.packageName)
                                }
                                .testTag("select_launcher_${launcher.packageName}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedPackageName = launcher.packageName
                                            PassThroughManager.savePassThroughLauncher(context, launcher.packageName)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD1E4FF))
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (imageBitmap != null) {
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = launcher.label,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = null,
                                            tint = Color(0xFF80CBC4),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = launcher.label,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = launcher.packageName,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFD1E4FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Activate Pass-Through Action Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF003355)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Activate Pass-Through",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Switches to selected stock launcher with a floating return handle.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val targetPkg = selectedPackageName.ifEmpty {
                            PassThroughManager.getSavedPassThroughLauncher(context) ?: ""
                        }
                        if (targetPkg.isNotEmpty()) {
                            PassThroughManager.savePassThroughLauncher(context, targetPkg)
                            PassThroughManager.setPassThroughActive(context, true)
                            viewModel.setMode(LauncherMode.PASS_THROUGH)
                            PassThroughManager.launchPassThroughLauncher(context, targetPkg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E4FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activate_passthrough_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF003355),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Stock Launcher Now",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003355)
                    )
                }
            }
        }
    }
}
