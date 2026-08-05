package com.example.ui.screens

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import com.example.ui.LauncherViewModel
import com.example.util.PassThroughManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val focusAllowedPackages by viewModel.focusAllowedPackages.collectAsState()
    val stockLaunchers by viewModel.stockLaunchers.collectAsState()

    var showFocusAppDialog by remember { mutableStateOf(false) }
    var selectedStockPackage by remember {
        mutableStateOf(PassThroughManager.getSavedPassThroughLauncher(context) ?: "")
    }

    // High contrast switch colors for clear visibility on dark background
    val highContrastSwitchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = Color(0xFF7C3AED),
        uncheckedThumbColor = Color(0xFF94A3B8),
        uncheckedTrackColor = Color(0xFF262933),
        uncheckedBorderColor = Color(0xFF475569)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Silo Settings",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Preferences & Automation",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1015)
                )
            )
        },
        containerColor = Color(0xFF0F1015),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: Focus Mode App Shortcuts
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262933)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF262933),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "FOCUS MODE APPS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA),
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Allowed App Shortcuts",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { showFocusAppDialog = true },
                            modifier = Modifier.testTag("edit_focus_apps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Apps",
                                tint = Color(0xFFA78BFA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Selected apps (${focusAllowedPackages.size}/6) visible on Focus screen:",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val selectedAppInfos = remember(allApps, focusAllowedPackages) {
                        allApps.filter { focusAllowedPackages.contains(it.packageName) }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selectedAppInfos.forEach { app ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF111318))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = app.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = app.category,
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Automation Triggers
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262933)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AUTOMATION TRIGGERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Auto-Mode Transitions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Charger Auto Sleep Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Power,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Sleep on Charger",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Switches to Sleep Mode when charging",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                        Switch(
                            checked = settings.autoTriggerChargerSleep,
                            onCheckedChange = { viewModel.toggleAutoTriggerChargerSleep(it) },
                            colors = highContrastSwitchColors,
                            modifier = Modifier.testTag("charger_sleep_switch")
                        )
                    }

                    HorizontalDivider(
                        color = Color(0xFF262933),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Bluetooth Auto Drive Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Drive on Bluetooth",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Switches to Drive Mode when connected to car BT",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                        Switch(
                            checked = settings.autoTriggerBluetoothDrive,
                            onCheckedChange = { viewModel.toggleAutoTriggerBluetoothDrive(it) },
                            colors = highContrastSwitchColors,
                            modifier = Modifier.testTag("bluetooth_drive_switch")
                        )
                    }
                }
            }

            // SECTION 3: Experimental Features
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262933)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF262933),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "EXPERIMENTAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA),
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Pass-Through Mode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Switch(
                            checked = settings.enablePassThroughMode,
                            onCheckedChange = { viewModel.toggleEnablePassThroughMode(it) },
                            colors = highContrastSwitchColors,
                            modifier = Modifier.testTag("enable_passthrough_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enables an experimental option in the mode selector to delegate home screen control to your phone's stock launcher package.",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 16.sp
                    )

                    if (settings.enablePassThroughMode) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "TARGET STOCK LAUNCHER PACKAGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (stockLaunchers.isEmpty()) {
                            Text(
                                text = "No secondary stock launcher packages detected.",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        } else {
                            stockLaunchers.forEach { launcher ->
                                val isSelected = launcher.packageName == selectedStockPackage
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF231D38) else Color(0xFF111318),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFA78BFA) else Color(0xFF1E212B)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            selectedStockPackage = launcher.packageName
                                            PassThroughManager.savePassThroughLauncher(context, launcher.packageName)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedStockPackage = launcher.packageName
                                                PassThroughManager.savePassThroughLauncher(context, launcher.packageName)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA78BFA))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = launcher.label,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = launcher.packageName,
                                                fontSize = 10.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: System Launcher Role (Moved towards end as requested)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262933)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF262933),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SYSTEM LAUNCHER CONTROL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA78BFA),
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Default Home App Role",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Open the system home app selection screen to set Silo as default launcher or return to your stock home screen.",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.triggerSystemHomePicker(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_default_launcher_setting_button")
                    ) {
                        Text(
                            text = "Switch Default Launcher (Exit Silo)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // SECTION 5: Branding Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Silo Launcher v1.0",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Built with Jetpack Compose & Clean Architecture",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Focus Apps Selection Dialog
    if (showFocusAppDialog) {
        Dialog(onDismissRequest = { showFocusAppDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF181A20),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262933)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Focus Apps (Max 6)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showFocusAppDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allApps) { appInfo ->
                            val isChecked = focusAllowedPackages.contains(appInfo.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleFocusPackage(appInfo.packageName) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = appInfo.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleFocusPackage(appInfo.packageName) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showFocusAppDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Selection", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
