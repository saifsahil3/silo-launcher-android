package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.util.PassThroughManager
import com.example.service.PassThroughOverlayService
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.LauncherMode
import com.example.ui.LauncherViewModel
import com.example.ui.components.FloatingModeControl
import com.example.ui.components.PassThroughFloatingBanner
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DriveModeScreen
import com.example.ui.screens.EPaperLayout
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.PassThroughModeScreen
import com.example.ui.screens.SleepModeScreen
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.screens.AllAppsScreen
import com.example.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PassThroughManager.setPassThroughActive(this, false)

        setContent {
            MyApplicationTheme {
                SiloLauncherApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PassThroughManager.setPassThroughActive(this, false)
    }

    override fun onResume() {
        super.onResume()
        PassThroughManager.setPassThroughActive(this, false)
        if (viewModel.currentMode.value == LauncherMode.PASS_THROUGH) {
            viewModel.setMode(LauncherMode.FOCUS)
        }
    }

    private fun handlePassThroughIntent(intent: Intent?) {
        PassThroughManager.setPassThroughActive(this, false)
        PassThroughOverlayService.stopService(this)
        viewModel.setMode(LauncherMode.FOCUS)
        intent?.removeExtra("EXIT_PASSTHROUGH")
    }
}

@Composable
fun SiloLauncherApp(viewModel: LauncherViewModel) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val stockLaunchers by viewModel.stockLaunchers.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val focusAllowedPackages by viewModel.focusAllowedPackages.collectAsStateWithLifecycle()
    val driveStats by viewModel.driveStats.collectAsStateWithLifecycle()
    val sleepState by viewModel.sleepState.collectAsStateWithLifecycle()

    val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val isCharging by viewModel.isCharging.collectAsStateWithLifecycle()
    val showPassThroughBanner by viewModel.showPassThroughBanner.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showSettingsScreen by remember { mutableStateOf(false) }

    if (showSettingsScreen) {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettingsScreen = false }
        )
    } else {
        val context = LocalContext.current
        val window = (context as? Activity)?.window
        val isFullScreenMode = currentMode == LauncherMode.FOCUS || currentMode == LauncherMode.E_PAPER

        DisposableEffect(currentMode) {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (isFullScreenMode) {
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                }
            }
            onDispose {
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                }
            }
        }

        val backgroundColor = when (currentMode) {
            LauncherMode.FOCUS -> Color(0xFF0F0F12)
            LauncherMode.ALL_APPS -> Color(0xFF0F0F12)
            LauncherMode.DRIVE -> Color(0xFF121318)
            LauncherMode.SLEEP -> Color(0xFF090A0D)
            LauncherMode.E_PAPER -> Color(0xFFF4F1EA)
            LauncherMode.PASS_THROUGH -> Color(0xFF101218)
        }

        val scaffoldModifier = if (isFullScreenMode) {
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
        } else {
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        }

        Scaffold(
            modifier = scaffoldModifier,
            containerColor = backgroundColor
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Context-Shifting Home Screen Canvas with Swipe Animation Effect
                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        if (isForward) {
                            (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(tween(300)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(tween(300)))
                        }
                    },
                    label = "mode_swipe_transition",
                    modifier = Modifier.fillMaxSize()
                ) { mode ->
                    when (mode) {
                        LauncherMode.FOCUS -> FocusModeScreen(
                            viewModel = viewModel,
                            allApps = allApps,
                            allowedPackages = focusAllowedPackages
                        )
                        LauncherMode.ALL_APPS -> AllAppsScreen(
                            viewModel = viewModel,
                            allApps = allApps,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory
                        )
                        LauncherMode.DRIVE -> DriveModeScreen(
                            viewModel = viewModel,
                            allApps = allApps,
                            driveStats = driveStats
                        )
                        LauncherMode.SLEEP -> SleepModeScreen(
                            viewModel = viewModel,
                            sleepState = sleepState
                        )
                        LauncherMode.E_PAPER -> EPaperLayout(
                            viewModel = viewModel,
                            allApps = allApps
                        )
                        LauncherMode.PASS_THROUGH -> PassThroughModeScreen(
                            viewModel = viewModel,
                            stockLaunchers = stockLaunchers
                        )
                    }
                }

                // Floating Mode Switcher & Settings Icon Button (Bottom Right)
                FloatingModeControl(
                    currentMode = currentMode,
                    enablePassThroughMode = settings.enablePassThroughMode,
                    onModeSelected = { viewModel.setMode(it) },
                    onOpenSettings = { showSettingsScreen = true },
                    onExitToDefaultLauncher = { viewModel.triggerSystemHomePicker(context) },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}
