package com.example.ui.screens

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.R
import com.example.data.AppInfo
import com.example.db.CreatorStageConfigEntity
import com.example.ui.LauncherViewModel
import com.example.util.toImageBitmapSafe
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreatorModeScreen(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by viewModel.creatorSessionState.collectAsStateWithLifecycle()
    val configs by viewModel.creatorStageConfigs.collectAsStateWithLifecycle()

    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { AppWidgetHost(context, 2048) }

    DisposableEffect(Unit) {
        try {
            appWidgetHost.startListening()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        onDispose {
            try {
                appWidgetHost.stopListening()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    if (!session.isActive) {
        // Entry Screen
        CreatorEntryScreen(
            onStartSession = { stageId ->
                viewModel.startCreatorSession(stageId)
            }
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { 2 })

        // Active Session Screen with Widget Page (Page 1)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F12))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CreatorWorkspacePage(
                        viewModel = viewModel,
                        sessionState = session,
                        configs = configs,
                        allApps = allApps
                    )
                    1 -> CreatorWidgetsPage(
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager
                    )
                }
            }

            // Lock Screen Resume Overlay
            if (session.showResumePrompt) {
                LockScreenResumePrompt(
                    stageId = session.currentStageId,
                    onResume = { viewModel.resumeCreatorSession() },
                    onFinish = { viewModel.finishCreatorSession() }
                )
            }

            // Timer Completion Alert Dialog
            if (session.secondsRemaining <= 0 && session.isActive) {
                SessionCompletionDialog(
                    onExtend = { minutes -> viewModel.extendCreatorSession(minutes) },
                    onFinish = { viewModel.finishCreatorSession() }
                )
            }
        }
    }
}

@Composable
private fun CreatorEntryScreen(
    onStartSession: (String) -> Unit
) {
    val context = LocalContext.current
    val logoBitmap = remember(context) {
        try {
            androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)?.toImageBitmapSafe()
                ?: androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12))
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF161622))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap,
                    contentDescription = "Silo Launcher Logo",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Silo Launcher Logo",
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Creator Mode",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "What are you working on?",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        CreatorStageOptionButton(
            title = "Shoot",
            subtitle = "Capture photos, videos, or audio",
            icon = Icons.Default.Camera,
            onClick = { onStartSession("shoot") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreatorStageOptionButton(
            title = "Editing",
            subtitle = "Refine, trim, and assemble draft content",
            icon = Icons.Default.Movie,
            onClick = { onStartSession("edit") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreatorStageOptionButton(
            title = "Publish",
            subtitle = "Upload, format, and push to audience",
            icon = Icons.Default.Publish,
            onClick = { onStartSession("publish") }
        )
    }
}

@Composable
private fun CreatorStageOptionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
            .testTag("creator_entry_stage_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3F51B5).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF5C6BC0),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun CreatorWorkspacePage(
    viewModel: LauncherViewModel,
    sessionState: com.example.ui.CreatorSessionState,
    configs: Map<String, CreatorStageConfigEntity>,
    allApps: List<AppInfo>
) {
    val context = LocalContext.current
    var showSwitchDialog by remember { mutableStateOf(false) }
    var showPrimaryPicker by remember { mutableStateOf(false) }
    var showSupportPicker by remember { mutableStateOf(false) }
    var showDurationSettings by remember { mutableStateOf(false) }

    val config = configs[sessionState.currentStageId] ?: CreatorStageConfigEntity(sessionState.currentStageId)

    val primaryPackages = remember(config.primaryApps) {
        config.primaryApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val supportPackages = remember(config.supportApps) {
        config.supportApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val primaryApps = remember(allApps, primaryPackages) {
        primaryPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
    }
    val supportApps = remember(allApps, supportPackages) {
        supportPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stageIcon = when (sessionState.currentStageId) {
                "shoot" -> Icons.Default.Camera
                "edit" -> Icons.Default.Movie
                "publish" -> Icons.Default.Publish
                else -> Icons.Default.Palette
            }

            val stageTitleDisplay = if (sessionState.currentStageId == "edit") "EDITING" else sessionState.currentStageId.uppercase()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDurationSettings = true }
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = stageIcon,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stageTitleDisplay,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Duration",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Gentle Timer countdown & Play/Pause button
            val minutes = sessionState.secondsRemaining / 60
            val seconds = sessionState.secondsRemaining % 60
            val timerText = String.format("%02d:%02d", minutes, seconds)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDurationSettings = true }
                        .background(Color(0xFF161622))
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = timerText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        if (sessionState.isPaused) {
                            viewModel.resumeCreatorSession()
                        } else {
                            viewModel.pauseCreatorSession()
                        }
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (sessionState.isPaused) Color(0xFF3F51B5) else Color(0xFFEF5350).copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = if (sessionState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (sessionState.isPaused) "Start Timer" else "Pause Timer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Primary Apps Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRIMARY APPS (${primaryApps.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = { showPrimaryPicker = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Primary Apps",
                    tint = Color(0xFF5C6BC0),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (primaryApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161622)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Primary Apps configured",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                primaryApps.forEachIndexed { idx, app ->
                    AppShortcutCard(
                        appInfo = app,
                        highlighted = (idx == 0),
                        onClick = { viewModel.launchApp(context, app) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Support Apps Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUPPORT APPS (${supportApps.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = { showSupportPicker = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Support Apps",
                    tint = Color(0xFF5C6BC0),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (supportApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161622)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Support Apps configured",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                supportApps.forEach { app ->
                    AppShortcutCard(
                        appInfo = app,
                        highlighted = false,
                        onClick = { viewModel.launchApp(context, app) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showSwitchDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Switch Stage")
            }

            Button(
                onClick = { viewModel.finishCreatorSession() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Finish Session")
            }
        }
    }

    if (showPrimaryPicker) {
        CreatorStageAppPickerDialog(
            stageId = sessionState.currentStageId,
            title = "Select Primary Apps",
            allApps = allApps,
            selectedPackages = primaryPackages.toSet(),
            onDismiss = { showPrimaryPicker = false },
            onSave = { selected ->
                viewModel.updateStageApps(sessionState.currentStageId, selected.toList(), supportPackages)
                showPrimaryPicker = false
            }
        )
    }

    if (showSupportPicker) {
        CreatorStageAppPickerDialog(
            stageId = sessionState.currentStageId,
            title = "Select Support Apps",
            allApps = allApps,
            selectedPackages = supportPackages.toSet(),
            onDismiss = { showSupportPicker = false },
            onSave = { selected ->
                viewModel.updateStageApps(sessionState.currentStageId, primaryPackages, selected.toList())
                showSupportPicker = false
            }
        )
    }

    if (showDurationSettings) {
        StageDurationSettingsDialog(
            currentDuration = config.sessionDurationMinutes,
            onDismiss = { showDurationSettings = false },
            onSave = { duration ->
                viewModel.updateStageDuration(sessionState.currentStageId, duration)
                showDurationSettings = false
            }
        )
    }

    // Switch Stage Dialog
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Switch stage workflow?", color = Color.White) },
            text = { Text("This will terminate your current stage session and start the new one immediately.", color = Color.White.copy(alpha = 0.7f)) },
            containerColor = Color(0xFF1E1E24),
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val remainingStages = listOf("shoot", "edit", "publish").filter { it != sessionState.currentStageId }
                    remainingStages.forEach { stage ->
                        val stageButtonLabel = if (stage == "edit") "EDITING" else stage.uppercase()
                        Button(
                            onClick = {
                                viewModel.switchStage(stage)
                                showSwitchDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stageButtonLabel)
                        }
                    }
                    OutlinedButton(
                        onClick = { showSwitchDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun AppShortcutCard(
    appInfo: AppInfo,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageBitmap = remember(appInfo) {
        appInfo.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appInfo.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .testTag("app_shortcut_${appInfo.packageName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) Color(0xFF3F51B5).copy(alpha = 0.15f) else Color(0xFF161622)
        ),
        border = BorderStroke(
            1.dp,
            if (highlighted) Color(0xFF3F51B5) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0F12)),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = appInfo.label,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = appInfo.label,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = appInfo.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            if (highlighted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3F51B5))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRIMARY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Widget Workspace Page (Swipe Right Page)
@Composable
private fun CreatorWidgetsPage(
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager
) {
    val context = LocalContext.current
    val activeWidgets = remember { mutableStateListOf<SystemWidgetConfig>() }

    var pendingWidgetId by remember { mutableStateOf(-1) }

    val bindWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingWidgetId > 0) {
            val widgetInfo = try {
                appWidgetManager.getAppWidgetInfo(pendingWidgetId)
            } catch (e: Throwable) {
                null
            }
            if (widgetInfo != null) {
                activeWidgets.add(
                    SystemWidgetConfig(
                        widgetId = pendingWidgetId,
                        label = widgetInfo.loadLabel(context.packageManager) ?: "System Widget",
                        packageName = widgetInfo.provider.packageName
                    )
                )
            } else {
                try {
                    appWidgetHost.deleteAppWidgetId(pendingWidgetId)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } else if (pendingWidgetId > 0) {
            try {
                appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        pendingWidgetId = -1
    }

    val pickWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (widgetId > 0) {
                val widgetInfo = try {
                    appWidgetManager.getAppWidgetInfo(widgetId)
                } catch (e: Throwable) {
                    null
                }
                if (widgetInfo != null) {
                    activeWidgets.add(
                        SystemWidgetConfig(
                            widgetId = widgetId,
                            label = widgetInfo.loadLabel(context.packageManager) ?: "System Widget",
                            packageName = widgetInfo.provider.packageName
                        )
                    )
                } else {
                    try {
                        appWidgetHost.deleteAppWidgetId(widgetId)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            val data = result.data
            val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (widgetId > 0) {
                try {
                    appWidgetHost.deleteAppWidgetId(widgetId)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WIDGET WORKSPACE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = {
                    try {
                        val widgetId = appWidgetHost.allocateAppWidgetId()
                        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        }
                        pickWidgetLauncher.launch(pickIntent)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF3F51B5))
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Widget", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Widget List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Built-in Notes Widget (Standard Default)
            item {
                BuiltInNotesCard()
            }

            itemsIndexed(activeWidgets) { index, widgetConfig ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val widgetInfo = remember(widgetConfig.widgetId) {
                            try {
                                appWidgetManager.getAppWidgetInfo(widgetConfig.widgetId)
                            } catch (e: Throwable) {
                                null
                            }
                        }

                        if (widgetInfo != null) {
                            AndroidView(
                                factory = { ctx ->
                                    try {
                                        appWidgetHost.createView(ctx, widgetConfig.widgetId, widgetInfo).apply {
                                            setAppWidget(widgetConfig.widgetId, widgetInfo)
                                        }
                                    } catch (e: Throwable) {
                                        android.widget.TextView(ctx).apply {
                                            text = "Display: ${widgetConfig.label}"
                                            setTextColor(android.graphics.Color.WHITE)
                                            setPadding(16, 16, 16, 16)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error loading system widget:\n${widgetConfig.label}",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Remove Widget Action Button (top right)
                        IconButton(
                            onClick = {
                                try {
                                    appWidgetHost.deleteAppWidgetId(widgetConfig.widgetId)
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                                activeWidgets.removeAt(index)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Widget",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuiltInNotesCard() {
    var notesText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFF5C6BC0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "QUICK NOTES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = {
                    Text(
                        text = "Jot down ideas, scripts, shoot details, or publish drafts...",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Transparent),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )
        }
    }
}

data class SystemWidgetConfig(
    val widgetId: Int,
    val label: String,
    val packageName: String
)

@Composable
private fun LockScreenResumePrompt(
    stageId: String,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF5C6BC0),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Resume stage session?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome back. Continue your ${stageId.uppercase()} session or finish it early.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Resume Session")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finish Session")
                }
            }
        }
    }
}

@Composable
private fun SessionCompletionDialog(
    onExtend: (Int) -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Session Complete",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Still creating? You can add more time or finish your session.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onExtend(15) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161622)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+15 minutes", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onExtend(30) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161622)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+30 minutes", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onExtend(60) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161622)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+60 minutes", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finish Session")
                }
            }
        }
    }
}

@Composable
private fun CreatorStageAppPickerDialog(
    stageId: String,
    title: String,
    allApps: List<AppInfo>,
    selectedPackages: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedPackages) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181A20),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
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
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${tempSelected.size} app(s) selected",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF161622),
                        unfocusedContainerColor = Color(0xFF161622),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps) { appInfo ->
                        val isChecked = tempSelected.contains(appInfo.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val current = tempSelected.toMutableSet()
                                    if (current.contains(appInfo.packageName)) {
                                        current.remove(appInfo.packageName)
                                    } else {
                                        current.add(appInfo.packageName)
                                    }
                                    tempSelected = current
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val context = LocalContext.current
                                val imageBitmap = remember(appInfo) {
                                    appInfo.iconDrawable?.toImageBitmapSafe() ?: try {
                                        context.packageManager.getApplicationIcon(appInfo.packageName).toImageBitmapSafe()
                                    } catch (e: Throwable) {
                                        null
                                    }
                                }
                                if (imageBitmap != null) {
                                    Image(
                                        bitmap = imageBitmap,
                                        contentDescription = appInfo.label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = appInfo.label,
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = appInfo.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSave(tempSelected) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Selection", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StageDurationSettingsDialog(
    currentDuration: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(currentDuration) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181A20),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Session Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Configure the duration of this stage session.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                val durations = listOf(30, 45, 60, 90, 120)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    durations.forEach { duration ->
                        val isSelected = duration == selectedDuration
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF3F51B5) else Color(0xFF161622))
                                .border(1.dp, if (isSelected) Color(0xFF3F51B5) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { selectedDuration = duration },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$duration",
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(selectedDuration) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

