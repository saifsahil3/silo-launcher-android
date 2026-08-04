package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.data.AppInfo
import com.example.ui.LauncherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.DialogProperties
import com.example.util.FocusAudioGenerator
import com.example.util.toImageBitmapSafe
import java.util.Locale

sealed class FocusWidgetData {
    data class SystemWidget(val widgetId: Int, val label: String, val packageName: String = "", var heightDp: Int = 180) : FocusWidgetData()
    data class BuiltInNotes(var content: String) : FocusWidgetData()
    data class BuiltInMantra(var quoteIndex: Int) : FocusWidgetData()
    data class BuiltInTimer(var durationMinutes: Int = 25) : FocusWidgetData()
    data class BuiltInAudio(val title: String = "Rain & Lo-Fi Focus Sound") : FocusWidgetData()
    data class AppShortcut(val packageName: String, val appName: String) : FocusWidgetData()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusModeScreen(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    allowedPackages: Set<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    val appWidgetHost = remember { AppWidgetHost(context, 1024) }
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

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

    // List of added focus widgets
    val activeWidgets = remember {
        mutableStateListOf<FocusWidgetData>(
            FocusWidgetData.BuiltInNotes("Task 1: Finish Deep Work session\nTask 2: Review project pull requests"),
            FocusWidgetData.BuiltInMantra(0)
        )
    }

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
                0 -> FocusAppsPage(
                    viewModel = viewModel,
                    allApps = allApps,
                    allowedPackages = allowedPackages
                )
                1 -> FocusWidgetsPage(
                    allApps = allApps,
                    activeWidgets = activeWidgets,
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager
                )
            }
        }
    }
}

@Composable
private fun FocusAppsPage(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    allowedPackages: Set<String>
) {
    val context = LocalContext.current
    var showCustomizeDialog by remember { mutableStateOf(false) }

    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSecondsRemaining by remember { mutableStateOf(25 * 60) }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSecondsRemaining > 0) {
            delay(1000)
            timerSecondsRemaining -= 1
        }
        if (timerSecondsRemaining <= 0) {
            isTimerRunning = false
        }
    }

    val focusApps = remember(allApps, allowedPackages) {
        allApps.filter { allowedPackages.contains(it.packageName) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Clock & Goal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = currentTimeString.ifEmpty { "10:00" },
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.95f)
            )
            Text(
                text = currentDateString.ifEmpty { "Monday, 3 August" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B1B22)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFFCE93D8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "“Create value instead of consuming noise.”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Apps List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ALLOWED APPS (${focusApps.size}/5)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )

            if (focusApps.isEmpty()) {
                Text(
                    text = "No focus apps selected.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            } else {
                focusApps.forEach { appInfo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.launchApp(context, appInfo) }
                            .padding(vertical = 8.dp)
                            .testTag("focus_app_${appInfo.packageName}")
                    ) {
                        Text(
                            text = appInfo.label.uppercase(Locale.getDefault()),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showCustomizeDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCE93D8)),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("customize_focus_apps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Customize Focus List")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pomodoro Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = Color(0xFFCE93D8)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val mins = timerSecondsRemaining / 60
                        val secs = timerSecondsRemaining % 60
                        val timeText = String.format("%02d:%02d", mins, secs)
                        Text(
                            text = "Focus Timer: $timeText",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTimerRunning) "Deep Focus running..." else "25-min session ready",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { isTimerRunning = !isTimerRunning },
                        modifier = Modifier.testTag("toggle_focus_timer_button")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Timer else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Timer",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            isTimerRunning = false
                            timerSecondsRemaining = 25 * 60
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    if (showCustomizeDialog) {
        Dialog(onDismissRequest = { showCustomizeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Focus Apps (Max 5)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showCustomizeDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allApps) { appInfo ->
                            val isChecked = allowedPackages.contains(appInfo.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.toggleFocusPackage(appInfo.packageName) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = appInfo.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleFocusPackage(appInfo.packageName) },
                                    modifier = Modifier.testTag("checkbox_${appInfo.packageName}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCustomizeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Selection")
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusWidgetsPage(
    allApps: List<AppInfo>,
    activeWidgets: MutableList<FocusWidgetData>,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager
) {
    val context = LocalContext.current
    var showAddWidgetDialog by remember { mutableStateOf(false) }

    var pendingWidgetId by remember { mutableIntStateOf(-1) }
    var pendingWidgetLabel by remember { mutableStateOf("") }
    var pendingWidgetPackage by remember { mutableStateOf("") }

    val configureWidgetLauncher = rememberLauncherForActivityResult(
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
                    FocusWidgetData.SystemWidget(
                        widgetId = pendingWidgetId,
                        label = pendingWidgetLabel.ifEmpty { "System Widget" },
                        packageName = pendingWidgetPackage
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
        pendingWidgetLabel = ""
        pendingWidgetPackage = ""
    }

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
                if (widgetInfo.configure != null) {
                    val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = widgetInfo.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
                    }
                    try {
                        configureWidgetLauncher.launch(configIntent)
                    } catch (e: Throwable) {
                        activeWidgets.add(
                            FocusWidgetData.SystemWidget(
                                widgetId = pendingWidgetId,
                                label = pendingWidgetLabel.ifEmpty { "System Widget" },
                                packageName = pendingWidgetPackage
                            )
                        )
                        pendingWidgetId = -1
                    }
                } else {
                    activeWidgets.add(
                        FocusWidgetData.SystemWidget(
                            widgetId = pendingWidgetId,
                            label = pendingWidgetLabel.ifEmpty { "System Widget" },
                            packageName = pendingWidgetPackage
                        )
                    )
                    pendingWidgetId = -1
                }
            } else {
                try {
                    appWidgetHost.deleteAppWidgetId(pendingWidgetId)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                pendingWidgetId = -1
            }
        } else if (pendingWidgetId > 0) {
            try {
                appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            pendingWidgetId = -1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FOCUS WIDGETS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Surface(
                shape = CircleShape,
                color = Color(0xFFCE93D8),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { showAddWidgetDialog = true }
                    .testTag("add_widget_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Widget",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activeWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1B1B22)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No widgets added yet.\nTap '+' to add widgets.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                activeWidgets.forEachIndexed { index, widgetData ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        when (widgetData) {
                            is FocusWidgetData.BuiltInNotes -> {
                                BuiltInNotesWidgetCard(
                                    content = widgetData.content,
                                    onContentChange = { updated -> widgetData.content = updated },
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                            is FocusWidgetData.BuiltInMantra -> {
                                BuiltInMantraWidgetCard(
                                    quoteIndex = widgetData.quoteIndex,
                                    onNextQuote = { widgetData.quoteIndex += 1 },
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                            is FocusWidgetData.BuiltInTimer -> {
                                BuiltInTimerWidgetCard(
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                            is FocusWidgetData.BuiltInAudio -> {
                                BuiltInAudioWidgetCard(
                                    title = widgetData.title,
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                            is FocusWidgetData.AppShortcut -> {
                                AppShortcutWidgetCard(
                                    packageName = widgetData.packageName,
                                    appName = widgetData.appName,
                                    allApps = allApps,
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                            is FocusWidgetData.SystemWidget -> {
                                SystemWidgetHostCard(
                                    widgetData = widgetData,
                                    appWidgetHost = appWidgetHost,
                                    appWidgetManager = appWidgetManager,
                                    onRemove = { activeWidgets.removeAt(index) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddWidgetDialog) {
        AddFocusWidgetDialog(
            appWidgetManager = appWidgetManager,
            appWidgetHost = appWidgetHost,
            allApps = allApps,
            onDismiss = { showAddWidgetDialog = false },
            onAddBuiltInNotes = {
                activeWidgets.add(FocusWidgetData.BuiltInNotes("Focus tasks & quick notes"))
                showAddWidgetDialog = false
            },
            onAddBuiltInMantra = {
                activeWidgets.add(FocusWidgetData.BuiltInMantra(0))
                showAddWidgetDialog = false
            },
            onAddBuiltInTimer = {
                activeWidgets.add(FocusWidgetData.BuiltInTimer(25))
                showAddWidgetDialog = false
            },
            onAddBuiltInAudio = {
                activeWidgets.add(FocusWidgetData.BuiltInAudio("Rain & White Noise Loop"))
                showAddWidgetDialog = false
            },
            onAddAppShortcut = { app ->
                activeWidgets.add(FocusWidgetData.AppShortcut(app.packageName, app.label))
                showAddWidgetDialog = false
            },
            onAddSystemWidget = { provider ->
                try {
                    val widgetId = appWidgetHost.allocateAppWidgetId()
                    val label = provider.loadLabel(context.packageManager)?.toString() ?: "System Widget"
                    val pkgName = provider.provider.packageName

                    val bound = try {
                        appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, provider.provider)
                    } catch (e: Throwable) {
                        false
                    }

                    if (bound) {
                        if (provider.configure != null) {
                            pendingWidgetId = widgetId
                            pendingWidgetLabel = label
                            pendingWidgetPackage = pkgName
                            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                component = provider.configure
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                            }
                            try {
                                configureWidgetLauncher.launch(configIntent)
                            } catch (e: Throwable) {
                                activeWidgets.add(FocusWidgetData.SystemWidget(widgetId, label, pkgName))
                                pendingWidgetId = -1
                            }
                        } else {
                            activeWidgets.add(FocusWidgetData.SystemWidget(widgetId, label, pkgName))
                        }
                    } else {
                        pendingWidgetId = widgetId
                        pendingWidgetLabel = label
                        pendingWidgetPackage = pkgName

                        val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
                        }
                        try {
                            bindWidgetLauncher.launch(bindIntent)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            try { appWidgetHost.deleteAppWidgetId(widgetId) } catch (err: Throwable) {}
                            pendingWidgetId = -1
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                showAddWidgetDialog = false
            }
        )
    }
}

@Composable
private fun BuiltInNotesWidgetCard(
    content: String,
    onContentChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var text by remember { mutableStateOf(content) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B1B22),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = Color(0xFFCE93D8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Focus Notes / Tasks",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onContentChange(it)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCE93D8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp)
            )
        }
    }
}

@Composable
private fun BuiltInMantraWidgetCard(
    quoteIndex: Int,
    onNextQuote: () -> Unit,
    onRemove: () -> Unit
) {
    val quotes = listOf(
        "“Deep work is the superpower of the 21st century.”",
        "“Clutter is non-essential distraction.”",
        "“Focus on being productive instead of busy.”",
        "“Your focus determines your reality.”"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B1B22),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FOCUS MANTRA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCE93D8),
                    letterSpacing = 1.sp
                )

                Row {
                    IconButton(onClick = onNextQuote, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Next Quote",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quotes[quoteIndex % quotes.size],
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun BuiltInTimerWidgetCard(
    onRemove: () -> Unit
) {
    var timerSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        if (timerSeconds == 0) {
            isRunning = false
        }
    }

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B1B22),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFCE93D8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "POMODORO TIMER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCE93D8),
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFE57373) else Color(0xFFCE93D8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRunning) "Pause" else "Start",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            timerSeconds = 25 * 60
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Reset",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuiltInAudioWidgetCard(
    title: String,
    onRemove: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var selectedTrack by remember { mutableIntStateOf(0) }
    val tracks = remember { listOf("Rain & White Noise", "Deep Focus Waves", "Gentle Wind") }

    val audioGenerator = remember { FocusAudioGenerator() }

    DisposableEffect(Unit) {
        onDispose {
            audioGenerator.stop()
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B1B22),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF81D4FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AMBIENT FOCUS SOUND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81D4FA),
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = {
                        audioGenerator.stop()
                        onRemove()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sound track selection chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tracks.forEachIndexed { index, trackName ->
                    val isSelected = selectedTrack == index
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF81D4FA).copy(alpha = 0.25f) else Color(0xFF252530),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81D4FA)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedTrack = index
                                if (isPlaying) {
                                    audioGenerator.stop()
                                    audioGenerator.startAmbientSound(coroutineScope, selectedTrack)
                                }
                            }
                    ) {
                        Text(
                            text = trackName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF81D4FA) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tracks[selectedTrack], fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text(
                        text = if (isPlaying) "Playing real ambient audio generator" else "Paused • Tap play to start ambient sound",
                        fontSize = 12.sp,
                        color = if (isPlaying) Color(0xFF81D4FA) else Color.White.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = {
                        if (isPlaying) {
                            audioGenerator.stop()
                            isPlaying = false
                        } else {
                            audioGenerator.startAmbientSound(coroutineScope, selectedTrack)
                            isPlaying = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFE57373) else Color(0xFF81D4FA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AppShortcutWidgetCard(
    packageName: String,
    appName: String,
    allApps: List<AppInfo>,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val appInfo = remember(packageName, allApps) {
        allApps.find { it.packageName == packageName }
    }
    val iconBitmap = remember(appInfo, packageName) {
        appInfo?.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B1B22),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = appName,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "APP WIDGET SHORTCUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80CBC4),
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text(text = packageName, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }

                Button(
                    onClick = {
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) {
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SystemWidgetHostCard(
    widgetData: FocusWidgetData.SystemWidget,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onRemove: () -> Unit
) {
    val widgetId = widgetData.widgetId
    val label = widgetData.label
    val packageName = widgetData.packageName

    var isEditing by remember { mutableStateOf(false) }
    var currentHeightDp by remember { mutableIntStateOf(widgetData.heightDp) }

    val widgetInfo = remember(widgetId) {
        if (widgetId <= 0) null
        else {
            try {
                appWidgetManager.getAppWidgetInfo(widgetId)
            } catch (e: Throwable) {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isEditing) 6.dp else 2.dp)
            .then(
                if (isEditing) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = Color(0xFF80CBC4),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141418).copy(alpha = 0.9f))
                        .padding(10.dp)
                } else {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                isEditing = true
                            }
                        )
                    }
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Editing Mode Top Controls Bar
            AnimatedVisibility(visible = isEditing) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = Color(0xFF80CBC4),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label.uppercase(Locale.getDefault()),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80CBC4),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Delete Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF5252).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        try {
                                            if (widgetId > 0) {
                                                appWidgetHost.deleteAppWidgetId(widgetId)
                                            }
                                        } catch (e: Throwable) {
                                            e.printStackTrace()
                                        }
                                        onRemove()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                }
                            }

                            // Done Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF80CBC4),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isEditing = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Done", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }

                    // Height Presets Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Size:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        listOf(120, 180, 260, 360).forEach { size ->
                            val isSel = currentHeightDp == size
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF80CBC4) else Color(0xFF2B2B38),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        currentHeightDp = size
                                        widgetData.heightDp = size
                                    }
                            ) {
                                Text(
                                    text = "${size}dp",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Widget Content View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentHeightDp.dp)
            ) {
                if (widgetInfo != null) {
                    AndroidView(
                        factory = { ctx ->
                            try {
                                appWidgetHost.createView(ctx, widgetId, widgetInfo).apply {
                                    setAppWidget(widgetId, widgetInfo)
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                android.widget.TextView(ctx).apply {
                                    text = "Widget Display: $label"
                                    setPadding(32, 32, 32, 32)
                                    setTextColor(android.graphics.Color.WHITE)
                                }
                            }
                        },
                        update = { view ->
                            if (view is AppWidgetHostView) {
                                try {
                                    view.setAppWidget(widgetId, widgetInfo)
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF252530))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (packageName.isNotEmpty()) "$packageName (Active System Widget)" else "System Widget (ID: $widgetId)",
                                fontSize = 11.sp,
                                color = Color(0xFF80CBC4)
                            )
                        }

                        IconButton(
                            onClick = {
                                try {
                                    if (widgetId > 0) {
                                        appWidgetHost.deleteAppWidgetId(widgetId)
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                                onRemove()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Drag-to-Resize Handle Bar at Bottom
            AnimatedVisibility(visible = isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF80CBC4).copy(alpha = 0.2f))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                val newH = (currentHeightDp + (dragAmount / 2f).toInt()).coerceIn(100, 600)
                                currentHeightDp = newH
                                widgetData.heightDp = newH
                            }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = "Resize",
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HOLD & DRAG TO RESIZE (${currentHeightDp}dp)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF80CBC4),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFocusWidgetDialog(
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    allApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAddBuiltInNotes: () -> Unit,
    onAddBuiltInMantra: () -> Unit,
    onAddBuiltInTimer: () -> Unit,
    onAddBuiltInAudio: () -> Unit,
    onAddAppShortcut: (AppInfo) -> Unit,
    onAddSystemWidget: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val categories = listOf("All", "Focus Tools", "Shortcuts", "System Widgets")

    val systemProviders = remember {
        try {
            appWidgetManager.installedProviders
        } catch (e: Throwable) {
            emptyList<AppWidgetProviderInfo>()
        }
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredSystemProviders = remember(systemProviders, searchQuery) {
        if (searchQuery.isBlank()) systemProviders
        else systemProviders.filter { provider ->
            val label = provider.loadLabel(context.packageManager)?.toString() ?: ""
            val pkg = provider.provider.packageName
            label.contains(searchQuery, ignoreCase = true) || pkg.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141418),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(620.dp)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Widget to Focus Screen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Organized categories with live previews",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF252530), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Input Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search widgets or apps...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFFCE93D8), modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCE93D8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1D1D26),
                        unfocusedContainerColor = Color(0xFF1D1D26),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Category Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { idx, catName ->
                        val isSelected = selectedCategoryIndex == idx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFCE93D8) else Color(0xFF252530),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryIndex = idx }
                        ) {
                            Text(
                                text = catName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of Categories with Previews
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Focus Tools Section
                    if (selectedCategoryIndex == 0 || selectedCategoryIndex == 1) {
                        item {
                            Text(
                                text = "FOCUS TOOLS (BUILT-IN)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCE93D8),
                                letterSpacing = 1.sp
                            )
                        }

                        if (searchQuery.isBlank() || "notes checklist goals scratchpad".contains(searchQuery, ignoreCase = true)) {
                            item {
                                WidgetPreviewCard(
                                    title = "Focus Notes & Checklist",
                                    subtitle = "Minimalist scratchpad for daily tasks & goals",
                                    icon = Icons.AutoMirrored.Filled.Notes,
                                    iconColor = Color(0xFFCE93D8),
                                    onAdd = onAddBuiltInNotes,
                                    previewContent = {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF101014),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("✓ Deep work: Code architecture review", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                                                Text("☐ Finish widget category layout", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        if (searchQuery.isBlank() || "timer pomodoro countdown clock".contains(searchQuery, ignoreCase = true)) {
                            item {
                                WidgetPreviewCard(
                                    title = "Pomodoro Focus Timer",
                                    subtitle = "25-minute deep focus countdown clock",
                                    icon = Icons.Default.Timer,
                                    iconColor = Color(0xFFCE93D8),
                                    onAdd = onAddBuiltInTimer,
                                    previewContent = {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF101014),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("25:00", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFCE93D8)
                                                ) {
                                                    Text("START", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        if (searchQuery.isBlank() || "mantra quote quote mindfulness daily".contains(searchQuery, ignoreCase = true)) {
                            item {
                                WidgetPreviewCard(
                                    title = "Focus Mantra & Quotes",
                                    subtitle = "Daily mindfulness quote card",
                                    icon = Icons.Default.Psychology,
                                    iconColor = Color(0xFFCE93D8),
                                    onAdd = onAddBuiltInMantra,
                                    previewContent = {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF101014),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Text(
                                                text = "“Your focus determines your reality.”",
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Serif,
                                                color = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        if (searchQuery.isBlank() || "audio ambient rain white noise sound lo-fi".contains(searchQuery, ignoreCase = true)) {
                            item {
                                WidgetPreviewCard(
                                    title = "Ambient Focus Audio",
                                    subtitle = "Real synthesized rain, ocean waves & wind player",
                                    icon = Icons.Default.MusicNote,
                                    iconColor = Color(0xFF81D4FA),
                                    onAdd = onAddBuiltInAudio,
                                    previewContent = {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF101014),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Rain & White Noise", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text("Synthesized focus sound track", fontSize = 10.sp, color = Color(0xFF81D4FA))
                                                }
                                                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // App Shortcuts Section
                    if (selectedCategoryIndex == 0 || selectedCategoryIndex == 2) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "APP SHORTCUTS (${filteredApps.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80CBC4),
                                letterSpacing = 1.sp
                            )
                        }

                        if (filteredApps.isEmpty()) {
                            item {
                                Text("No app shortcuts found", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                        } else {
                            items(filteredApps) { app ->
                                AppShortcutPreviewCard(
                                    app = app,
                                    onAdd = { onAddAppShortcut(app) }
                                )
                            }
                        }
                    }

                    // System Widgets Section
                    if (selectedCategoryIndex == 0 || selectedCategoryIndex == 3) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SYSTEM WIDGETS (${filteredSystemProviders.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D),
                                letterSpacing = 1.sp
                            )
                        }

                        if (filteredSystemProviders.isEmpty()) {
                            item {
                                Text("No system widgets detected", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                        } else {
                            items(filteredSystemProviders) { provider ->
                                SystemWidgetPreviewCard(
                                    provider = provider,
                                    onAdd = { onAddSystemWidget(provider) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onAdd: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1E26),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = iconColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(text = subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }

                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ Add", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                }
            }

            previewContent()
        }
    }
}

@Composable
private fun AppShortcutPreviewCard(
    app: AppInfo,
    onAdd: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app) {
        app.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1E26),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF80CBC4).copy(alpha = 0.2f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = Color(0xFF80CBC4), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = app.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(text = app.packageName, fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
                }
            }

            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80CBC4)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ Shortcut", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun SystemWidgetPreviewCard(
    provider: AppWidgetProviderInfo,
    onAdd: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(provider) {
        try {
            provider.loadLabel(pm) ?: "System Widget"
        } catch (e: Throwable) {
            "System Widget"
        }
    }
    val pkgName = provider.provider.packageName
    val providerIcon = remember(provider) {
        try {
            provider.loadIcon(context, 0)?.toImageBitmapSafe() ?: pm.getApplicationIcon(pkgName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1E26),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (providerIcon != null) {
                        Image(
                            bitmap = providerIcon,
                            contentDescription = label,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(text = "$pkgName • Size: ${provider.minWidth}x${provider.minHeight}dp", fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
                    }
                }

                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ Widget", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                }
            }
        }
    }
}

