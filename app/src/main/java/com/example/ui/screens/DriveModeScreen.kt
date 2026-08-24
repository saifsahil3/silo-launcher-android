package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppInfo
import com.example.db.DriveCommShortcutEntity
import com.example.db.DriveNavLocationEntity
import com.example.ui.DriveStats
import com.example.ui.LauncherViewModel
import com.example.ui.components.drive.DriveAdaptiveMediaCard
import com.example.ui.components.drive.DriveAppDrawerCard
import com.example.ui.components.drive.DriveAppPickerSheet
import com.example.ui.components.drive.DriveCommContactEditSheet
import com.example.ui.components.drive.DriveCommShortcutsCard
import com.example.ui.components.drive.DriveNavCard
import com.example.ui.components.drive.DriveNavLocationEditSheet
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Modern Minimalist Drive Mode Dashboard
 */
@Composable
fun DriveModeScreen(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    driveStats: DriveStats,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // State collections
    val navLocations by viewModel.driveNavLocations.collectAsStateWithLifecycle()
    val commShortcuts by viewModel.driveCommShortcuts.collectAsStateWithLifecycle()
    val mediaTrack by viewModel.driveMediaTrack.collectAsStateWithLifecycle()
    val driveFavoritePackages by viewModel.driveFavoritePackages.collectAsStateWithLifecycle()
    val appPairs by viewModel.driveAppPairs.collectAsStateWithLifecycle()
    val quickShortcuts by viewModel.driveQuickShortcuts.collectAsStateWithLifecycle()

    var pendingCallShortcut by remember { mutableStateOf<DriveCommShortcutEntity?>(null) }
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingCallShortcut != null) {
            viewModel.launchDriveCommShortcut(context, pendingCallShortcut!!)
            pendingCallShortcut = null
        }
    }

    var isNotificationAccessGranted by remember {
        mutableStateOf(viewModel.isNotificationListenerGranted(context))
    }

    // Observe lifecycle ON_RESUME so granted permission updates dynamically without restarting
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationAccessGranted = viewModel.isNotificationListenerGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Filter media apps
    val installedMediaApps = remember(allApps) {
        viewModel.getInstalledMediaApps()
    }

    // Modal sheet states
    var editingNavLocation by remember { mutableStateOf<DriveNavLocationEntity?>(null) }
    var editingCommSlotIndex by remember { mutableIntStateOf(-1) }
    var editingCommShortcut by remember { mutableStateOf<DriveCommShortcutEntity?>(null) }
    var isAppPickerVisible by remember { mutableStateOf(false) }
    var isAppPairPickerVisible by remember { mutableStateOf(false) }
    var isShortcutPickerVisible by remember { mutableStateOf(false) }

    // Start real-time audio playback listener when entering screen
    androidx.compose.runtime.DisposableEffect(Unit) {
        viewModel.startMediaPlaybackTracking(context)
        onDispose {
            viewModel.stopMediaPlaybackTracking()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D13)) // Deep minimal cockpit dark theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Minimalist Navigation Card (Maps Launch + 4 Destination Pills)
            DriveNavCard(
                locations = navLocations,
                onOpenMapsClick = { viewModel.launchDriveNavigation(context, null) },
                onLocationClick = { location ->
                    if (location.addressOrQuery.isNotBlank()) {
                        viewModel.launchDriveNavLocation(context, location)
                    } else {
                        editingNavLocation = location
                    }
                },
                onLocationLongClick = { location ->
                    editingNavLocation = location
                },
                onLaunchAssistant = { viewModel.launchDriveVoiceAssistant(context) }
            )

            // 2. Adaptive Dual-State Media Card (Music Apps List <-> Streamlined Player with pause persistence)
            DriveAdaptiveMediaCard(
                trackInfo = mediaTrack,
                mediaApps = installedMediaApps,
                onLaunchMediaApp = { app -> viewModel.launchApp(context, app) },
                onMediaKeyClick = { keyCode -> viewModel.sendMediaKeyEvent(context, keyCode) },
                onOpenCurrentMediaApp = { viewModel.launchDriveMediaApp(context, mediaTrack.activeAppPackage) },
                onEnableNotificationAccess = { viewModel.openNotificationListenerSettings(context) },
                isNotificationAccessGranted = isNotificationAccessGranted
            )

            // 3. Simplified Favorite Communication Shortcuts Card (Call/Message with side badge)
            DriveCommShortcutsCard(
                shortcuts = commShortcuts,
                onShortcutClick = { shortcut ->
                    viewModel.launchDriveCommShortcut(
                        context = context,
                        shortcut = shortcut,
                        onRequestCallPermission = {
                            pendingCallShortcut = shortcut
                            callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                        }
                    )
                },
                onShortcutLongClick = { slotIndex, shortcut ->
                    editingCommSlotIndex = slotIndex
                    editingCommShortcut = shortcut
                }
            )

            // 4. Categorized Driving App & Shortcut Drawer (Apps, Shortcuts, App Pairs)
            DriveAppDrawerCard(
                pinnedPackages = driveFavoritePackages,
                allApps = allApps,
                appPairs = appPairs,
                quickShortcuts = quickShortcuts,
                onLaunchApp = { app -> viewModel.launchApp(context, app) },
                onLaunchAppPair = { pair -> viewModel.launchAppPair(context, pair) },
                onLaunchQuickShortcut = { shortcut -> viewModel.launchDriveQuickShortcut(context, shortcut) },
                onAddAppClick = { isAppPickerVisible = true },
                onAddAppPairClick = { isAppPairPickerVisible = true },
                onAddShortcutClick = { isShortcutPickerVisible = true },
                onRemoveAppClick = { pkg -> viewModel.toggleDriveFavorite(pkg) },
                onRemoveAppPairClick = { id -> viewModel.removeDriveAppPair(id) },
                onRemoveShortcutClick = { id -> viewModel.removeDriveQuickShortcut(id) }
            )

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Voice Assistant is now embedded in DriveNavCard (80/20 row)

        // Sheet 1: Navigation Destination Editor Sheet
        editingNavLocation?.let { location ->
            DriveNavLocationEditSheet(
                location = location,
                onSearchSuggestions = { query -> viewModel.searchPlaceSuggestions(context, query) },
                onSave = { label, address ->
                    viewModel.saveDriveNavLocation(location.id, label, address)
                    editingNavLocation = null
                },
                onDismiss = { editingNavLocation = null }
            )
        }

        // Sheet 2: Communication Contact Shortcut Editor Sheet (with native Contact Picker)
        if (editingCommSlotIndex >= 0) {
            DriveCommContactEditSheet(
                slotIndex = editingCommSlotIndex,
                existingShortcut = editingCommShortcut,
                onSave = { name, phone, channel, photoUri ->
                    viewModel.saveDriveCommShortcut(
                        slotIndex = editingCommSlotIndex,
                        name = name,
                        phoneNumber = phone,
                        channelType = channel,
                        photoUri = photoUri
                    )
                    editingCommSlotIndex = -1
                    editingCommShortcut = null
                },
                onDelete = {
                    viewModel.deleteDriveCommShortcut(editingCommSlotIndex)
                    editingCommSlotIndex = -1
                    editingCommShortcut = null
                },
                onDismiss = {
                    editingCommSlotIndex = -1
                    editingCommShortcut = null
                }
            )
        }

        // Sheet 3: App Shortcut Picker Sheet
        if (isAppPickerVisible) {
            DriveAppPickerSheet(
                allApps = allApps,
                onSelectApp = { app ->
                    viewModel.toggleDriveFavorite(app.packageName)
                    isAppPickerVisible = false
                },
                onDismiss = { isAppPickerVisible = false }
            )
        }

        // Sheet 4: App Pair Creator Sheet
        if (isAppPairPickerVisible) {
            com.example.ui.components.drive.DriveAppPairCreateSheet(
                allApps = allApps,
                onCreatePair = { label, pkg1, pkg2 ->
                    viewModel.addDriveAppPair(label, pkg1, pkg2)
                    isAppPairPickerVisible = false
                },
                onDismiss = { isAppPairPickerVisible = false }
            )
        }

        // Sheet 5: Android App Shortcut Picker Sheet (LauncherApps API)
        if (isShortcutPickerVisible) {
            com.example.ui.components.drive.DriveAppShortcutPickerSheet(
                allApps = allApps,
                onSelectShortcut = { packageName, shortcutId, label, appName ->
                    viewModel.addDriveQuickShortcut(packageName, shortcutId, label, appName)
                    isShortcutPickerVisible = false
                },
                onDismiss = { isShortcutPickerVisible = false }
            )
        }
    }
}
