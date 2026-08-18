package com.example.ui

import android.app.Application
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.OpenableColumns
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppInfo
import com.example.data.AppRepository
import com.example.data.ReadingAppDetector
import com.example.db.DatabaseProvider
import com.example.db.LauncherDatabase
import com.example.db.ModeSettingEntity
import com.example.db.CreatorStageConfigEntity
import com.example.db.OfflineBookEntity
import com.example.db.PinnedAppEntity
import com.example.ui.theme.EPaperColorProfile
import com.example.model.LauncherMode
import com.example.model.FocusWidgetData
import com.example.model.focusWidgetsFromJson
import com.example.model.focusWidgetsToJson
import com.example.model.getDefaultFocusWidgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DriveStats(
    val currentSpeedMph: Int = 0,
    val isDrivingDetected: Boolean = false,
    val connectedBluetoothDevice: String? = null
)

data class SleepState(
    val isDndActive: Boolean = false,
    val windDownTimerMinutes: Int = 15,
    val bedtimeAlarmTime: String = "07:00 AM"
)

data class CreatorSessionState(
    val isActive: Boolean = false,
    val currentStageId: String = "shoot",
    val secondsRemaining: Int = 3600,
    val isPaused: Boolean = true,
    val showResumePrompt: Boolean = false,
    val hasTimerBeenStarted: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository()
    private val db = DatabaseProvider.getDatabase(application)
    private val dao = db.modeSettingDao()

    private val _currentMode = MutableStateFlow(LauncherMode.FOCUS)
    val currentMode: StateFlow<LauncherMode> = _currentMode.asStateFlow()

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _stockLaunchers = MutableStateFlow<List<AppInfo>>(emptyList())
    val stockLaunchers: StateFlow<List<AppInfo>> = _stockLaunchers.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Focus Mode
    private val _focusAllowedPackages = MutableStateFlow<List<String>>(emptyList())
    val focusAllowedPackages: StateFlow<List<String>> = _focusAllowedPackages.asStateFlow()

    private val _focusWidgets = MutableStateFlow<List<FocusWidgetData>>(getDefaultFocusWidgets())
    val focusWidgets: StateFlow<List<FocusWidgetData>> = _focusWidgets.asStateFlow()

    private val _focusGoal = MutableStateFlow("Deep Work & Zero Distractions")
    val focusGoal: StateFlow<String> = _focusGoal.asStateFlow()

    // Drive Mode
    private val _driveFavoritePackages = MutableStateFlow<List<String>>(emptyList())
    val driveFavoritePackages: StateFlow<List<String>> = _driveFavoritePackages.asStateFlow()
    private val _driveStats = MutableStateFlow(DriveStats())
    val driveStats: StateFlow<DriveStats> = _driveStats.asStateFlow()

    // Sleep Mode
    private val _sleepState = MutableStateFlow(SleepState())
    val sleepState: StateFlow<SleepState> = _sleepState.asStateFlow()

    // Creator Mode Session
    private val _creatorSessionState = MutableStateFlow(CreatorSessionState())
    val creatorSessionState: StateFlow<CreatorSessionState> = _creatorSessionState.asStateFlow()

    private val _creatorStageConfigs = MutableStateFlow<Map<String, CreatorStageConfigEntity>>(emptyMap())
    val creatorStageConfigs: StateFlow<Map<String, CreatorStageConfigEntity>> = _creatorStageConfigs.asStateFlow()

    // Battery State
    private val _batteryLevel = MutableStateFlow(85)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(ModeSettingEntity())
    val settings: StateFlow<ModeSettingEntity> = _settings.asStateFlow()

    // Offline Books & Reading Progress
    private val offlineBookDao = db.offlineBookDao()
    private val _offlineBooks = MutableStateFlow<List<OfflineBookEntity>>(emptyList())
    val offlineBooks: StateFlow<List<OfflineBookEntity>> = _offlineBooks.asStateFlow()

    private val prefs = application.getSharedPreferences("epaper_reading_prefs", Context.MODE_PRIVATE)
    private val _currentlyReadingBook = MutableStateFlow(prefs.getString("reading_book_title", "Atomic Habits") ?: "Atomic Habits")
    val currentlyReadingBook: StateFlow<String> = _currentlyReadingBook.asStateFlow()

    private val _currentlyReadingPage = MutableStateFlow(prefs.getInt("reading_current_page", 142))
    val currentlyReadingPage: StateFlow<Int> = _currentlyReadingPage.asStateFlow()

    private val _currentlyReadingTotalPages = MutableStateFlow(prefs.getInt("reading_total_pages", 320))
    val currentlyReadingTotalPages: StateFlow<Int> = _currentlyReadingTotalPages.asStateFlow()

    private val _epaperColorProfile = MutableStateFlow(
        try {
            EPaperColorProfile.valueOf(
                prefs.getString("epaper_color_profile", EPaperColorProfile.PAPER_WHITE.name)
                    ?: EPaperColorProfile.PAPER_WHITE.name
            )
        } catch (e: Throwable) {
            EPaperColorProfile.PAPER_WHITE
        }
    )
    val epaperColorProfile: StateFlow<EPaperColorProfile> = _epaperColorProfile.asStateFlow()

    private val _pinnedReadingAppPackages = MutableStateFlow<List<String>>(emptyList())
    val pinnedReadingAppPackages: StateFlow<List<String>> = _pinnedReadingAppPackages.asStateFlow()

    // Pass-Through Banner visibility
    private val _showPassThroughBanner = MutableStateFlow(false)
    val showPassThroughBanner: StateFlow<Boolean> = _showPassThroughBanner.asStateFlow()


    private val triggerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val event = intent?.getStringExtra("event_type") ?: return
            if (event == "POWER_CONNECTED" && _settings.value.autoTriggerChargerSleep) {
                setMode(LauncherMode.SLEEP)
            }
        }
    }
    private var lastScreenOffTime: Long = 0

    private val creatorScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val session = _creatorSessionState.value
            if (!session.isActive) return

            when (action) {
                Intent.ACTION_SCREEN_OFF -> {
                    lastScreenOffTime = System.currentTimeMillis()
                    _creatorSessionState.value = _creatorSessionState.value.copy(isPaused = true)
                }
                Intent.ACTION_USER_PRESENT -> {
                    // Only show resume prompt if timer was actually started by user
                    if (session.hasTimerBeenStarted) {
                        val now = System.currentTimeMillis()
                        if (lastScreenOffTime > 0) {
                            val durationLockedMinutes = (now - lastScreenOffTime) / 1000 / 60
                            if (durationLockedMinutes >= 30) {
                                finishCreatorSession()
                            } else {
                                _creatorSessionState.value = _creatorSessionState.value.copy(
                                    showResumePrompt = true
                                )
                            }
                        } else {
                            _creatorSessionState.value = _creatorSessionState.value.copy(
                                showResumePrompt = true
                            )
                        }
                    }
                }
            }
        }
    }


    init {
        loadInstalledApps()
        loadSettingsFromDb()
        loadCreatorStageConfigs()
        loadOfflineBooks()
        loadPinnedReadingApps()
        registerBatteryReceiver()

        // Register internal trigger receiver safely
        try {
            val filter = IntentFilter("com.example.morphlauncher.TRIGGER_EVENT")
            ContextCompat.registerReceiver(
                getApplication(),
                triggerReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Register screen lock broadcast receiver for Creator Mode
        try {
            val creatorFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            getApplication<Application>().registerReceiver(creatorScreenReceiver, creatorFilter)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    private fun loadSettingsFromDb() {
        viewModelScope.launch {
            try {
                val initial = dao.getSettings()
                if (initial == null) {
                    dao.saveSettings(ModeSettingEntity())
                }

                dao.getSettingsFlow().collectLatest { entity ->
                    val current = entity ?: ModeSettingEntity()
                    _settings.value = current

                    val savedMode = try {
                        LauncherMode.valueOf(current.currentMode)
                    } catch (e: Exception) {
                        LauncherMode.FOCUS
                    }
                    _currentMode.value = savedMode

                    _focusAllowedPackages.value = current.focusAllowedPackages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    _focusWidgets.value = focusWidgetsFromJson(current.focusWidgetsJson)

                    _driveFavoritePackages.value = current.driveFavoritePackages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = appRepository.getInstalledApps(getApplication())
            val stock = appRepository.getInstalledStockLaunchers(getApplication())

            _allApps.value = apps
            _stockLaunchers.value = stock

            // Default focus allowed packages if empty
            if (_focusAllowedPackages.value.isEmpty()) {
                val defaults = apps.take(4).map { it.packageName }
                _focusAllowedPackages.value = defaults
            }

            _isLoadingApps.value = false
        }
    }

    fun setMode(mode: LauncherMode) {
        _currentMode.value = mode
        if (mode == LauncherMode.PASS_THROUGH) {
            _showPassThroughBanner.value = true
        } else {
            _showPassThroughBanner.value = false
        }
        viewModelScope.launch {
            val updated = _settings.value.copy(currentMode = mode.name)
            dao.saveSettings(updated)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFocusPackage(packageName: String) {
        val current = _focusAllowedPackages.value.toMutableList()
        if (current.contains(packageName)) {
            if (current.size > 1) { // Keep at least one app
                current.remove(packageName)
            }
        } else {
            if (current.size < 6) { // Max 6 apps in focus mode
                current.add(packageName)
            }
        }
        _focusAllowedPackages.value = current
        saveFocusPackagesToDb(current)
    }

    fun reorderFocusAllowedPackages(newOrder: List<String>) {
        _focusAllowedPackages.value = newOrder
        saveFocusPackagesToDb(newOrder)
    }

    fun updateFocusWidgets(newList: List<FocusWidgetData>) {
        _focusWidgets.value = newList
        saveFocusWidgetsToDb(newList)
    }

    fun addFocusWidget(widget: FocusWidgetData) {
        val updated = _focusWidgets.value.toMutableList().apply { add(widget) }
        _focusWidgets.value = updated
        saveFocusWidgetsToDb(updated)
    }

    fun removeFocusWidgetAt(index: Int) {
        val current = _focusWidgets.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _focusWidgets.value = current
            saveFocusWidgetsToDb(current)
        }
    }

    fun updateFocusWidgetAt(index: Int, updatedWidget: FocusWidgetData) {
        val current = _focusWidgets.value.toMutableList()
        if (index in current.indices) {
            current[index] = updatedWidget
            _focusWidgets.value = current
            saveFocusWidgetsToDb(current)
        }
    }

    private fun saveFocusWidgetsToDb(widgets: List<FocusWidgetData>) {
        viewModelScope.launch {
            val json = focusWidgetsToJson(widgets)
            val updated = _settings.value.copy(focusWidgetsJson = json)
            dao.saveSettings(updated)
        }
    }

    private fun saveFocusPackagesToDb(packages: List<String>) {
        viewModelScope.launch {
            val stringVal = packages.joinToString(",")
            val updated = _settings.value.copy(focusAllowedPackages = stringVal)
            dao.saveSettings(updated)
        }
    }

    fun toggleDriveFavoritePackage(packageName: String) {
        val current = _driveFavoritePackages.value.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            if (current.size < 6) {
                current.add(packageName)
            }
        }
        _driveFavoritePackages.value = current
        viewModelScope.launch {
            val updated = _settings.value.copy(driveFavoritePackages = current.joinToString(","))
            dao.saveSettings(updated)
        }
    }

    private var locationManager: android.location.LocationManager? = null
    private var locationListener: android.location.LocationListener? = null
    private var isSimulatingSpeed = false

    fun startSpeedTracking() {
        try {
            val app = getApplication<Application>()
            if (androidx.core.content.ContextCompat.checkSelfPermission(app, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(app, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                if (locationManager == null) {
                    locationManager = app.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                }
                if (locationListener == null) {
                    var lastLoc: android.location.Location? = null
                    var lastLocTime: Long = 0L
                    locationListener = object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            if (isSimulatingSpeed) return
                            var speedMph = 0
                            if (location.hasSpeed() && location.speed > 0f) {
                                speedMph = (location.speed * 2.23694f).toInt()
                            } else if (lastLoc != null) {
                                val timeDiffSec = (location.time - lastLocTime) / 1000f
                                if (timeDiffSec > 0.5f) {
                                    val distMeters = lastLoc!!.distanceTo(location)
                                    val speedMps = distMeters / timeDiffSec
                                    speedMph = (speedMps * 2.23694f).toInt()
                                }
                            }
                            lastLoc = location
                            lastLocTime = location.time
                            _driveStats.value = _driveStats.value.copy(
                                currentSpeedMph = speedMph,
                                isDrivingDetected = speedMph > 5
                            )
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                }
                locationListener?.let { listener ->
                    locationManager?.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000L, 0.5f, listener)
                    locationManager?.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 2000L, 1.0f, listener)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun stopSpeedTracking() {
        try {
            locationListener?.let { listener ->
                locationManager?.removeUpdates(listener)
            }
            locationListener = null
            _driveStats.value = _driveStats.value.copy(currentSpeedMph = 0, isDrivingDetected = false)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun toggleSpeedSimulation() {
        isSimulatingSpeed = !isSimulatingSpeed
        if (isSimulatingSpeed) {
            val current = _driveStats.value.currentSpeedMph
            val nextSpeed = when {
                current < 25 -> 25
                current < 45 -> 45
                current < 65 -> 65
                else -> 0
            }
            _driveStats.value = _driveStats.value.copy(
                currentSpeedMph = nextSpeed,
                isDrivingDetected = nextSpeed > 5
            )
        } else {
            _driveStats.value = _driveStats.value.copy(currentSpeedMph = 0, isDrivingDetected = false)
            startSpeedTracking()
        }
    }

    fun updateWindDownTimer(minutes: Int) {
        _sleepState.value = _sleepState.value.copy(windDownTimerMinutes = minutes)
    }

    fun sendMediaKeyEvent(context: Context, keyCode: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            val downEvent = android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode)
            audioManager?.dispatchMediaKeyEvent(downEvent)
            audioManager?.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            try {
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode))
                }
                val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode))
                }
                context.sendOrderedBroadcast(downIntent, null)
                context.sendOrderedBroadcast(upIntent, null)
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
    }

    fun isMediaActive(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.isMusicActive == true
        } catch (e: Exception) {
            false
        }
    }

    fun toggleDnd() {
        _sleepState.value = _sleepState.value.copy(
            isDndActive = !_sleepState.value.isDndActive
        )
    }

    fun updateBedtimeAlarmTime(timeStr: String) {
        _sleepState.value = _sleepState.value.copy(bedtimeAlarmTime = timeStr)
    }

    fun toggleAutoTriggerChargerSleep(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(autoTriggerChargerSleep = enabled)
            dao.saveSettings(updated)
        }
    }

    fun toggleAutoTriggerBluetoothDrive(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(autoTriggerBluetoothDrive = enabled)
            dao.saveSettings(updated)
        }
    }

    fun toggleEnablePassThroughMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(enablePassThroughMode = enabled)
            dao.saveSettings(updated)
            if (!enabled && _currentMode.value == LauncherMode.PASS_THROUGH) {
                setMode(LauncherMode.FOCUS)
            }
        }
    }

    fun launchApp(context: Context, appInfo: AppInfo) {
        try {
            context.startActivity(appInfo.launchIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchPackageName(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                openPlayStore(context, packageName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            openPlayStore(context, packageName)
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun launchOrInstallGoogleNews(context: Context) {
        launchPackageName(context, ReadingAppDetector.GOOGLE_NEWS_PACKAGE)
    }

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
                roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_HOME) == true
            } else {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfo = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                resolveInfo?.activityInfo?.packageName == context.packageName
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    fun triggerSystemHomePicker(context: Context) {
        val activity = context.findActivity()
        val targetContext = activity ?: context

        // 1. Direct System Default Home App Settings (Opens native Home App selector screen across all Android versions & OEMs)
        try {
            val homeSettingsIntent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            if (activity == null) {
                homeSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            targetContext.startActivity(homeSettingsIntent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Direct System Default Apps Settings
        try {
            val defaultAppsIntent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            if (activity == null) {
                defaultAppsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            targetContext.startActivity(defaultAppsIntent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. RoleManager ROLE_HOME intent on Android 10+ (API 29+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                val roleManager = targetContext.getSystemService(android.app.role.RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)) {
                    val roleIntent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                    if (activity == null) {
                        roleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    targetContext.startActivity(roleIntent)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Fallback string intent actions for OEM specific settings
        val oemActions = listOf(
            "android.settings.HOME_SETTINGS",
            "android.settings.DEFAULT_HOME_SETTINGS",
            "android.settings.MANAGE_DEFAULT_APPS_SETTINGS"
        )
        for (action in oemActions) {
            try {
                val intent = Intent(action)
                if (activity == null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                targetContext.startActivity(intent)
                return
            } catch (e: Exception) {
                // keep trying
            }
        }

        // 5. Force system Home intent chooser by resetting preferred activities
        try {
            @Suppress("DEPRECATION")
            context.packageManager.clearPackagePreferredActivities(context.packageName)

            val mainHomeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooserIntent = Intent.createChooser(mainHomeIntent, "Select Default Launcher").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            targetContext.startActivity(chooserIntent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 6. Final fallback: App Details Settings with Toast instruction
        try {
            val appDetailsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                if (activity == null) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            targetContext.startActivity(appDetailsIntent)
            android.widget.Toast.makeText(context, "Please select 'Home App' or 'Defaults' to set Silo as default launcher", android.widget.Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun dismissPassThroughBanner() {
        _showPassThroughBanner.value = false
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { batteryIntent ->
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
                }
                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
    }

    private fun registerBatteryReceiver() {
        try {
            val batteryFilter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            val batteryStatus: Intent? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getApplication<Application>().registerReceiver(batteryReceiver, batteryFilter, Context.RECEIVER_EXPORTED)
            } else {
                getApplication<Application>().registerReceiver(batteryReceiver, batteryFilter)
            }
            batteryStatus?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
                }
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun loadCreatorStageConfigs() {
        viewModelScope.launch {
            try {
                dao.getAllStageConfigsFlow().collectLatest { list ->
                    if (list.isEmpty()) {
                        populateDefaultCreatorConfigs()
                    } else {
                        _creatorStageConfigs.value = list.associateBy { it.stageId }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun populateDefaultCreatorConfigs() {
        val defaultShoot = CreatorStageConfigEntity(
            stageId = "shoot",
            sessionDurationMinutes = 60,
            primaryApps = "com.google.android.GoogleCamera,com.android.camera",
            supportApps = "com.google.android.apps.photos,com.android.documentsui,com.google.android.keep,com.openai.chatgpt"
        )
        val defaultEdit = CreatorStageConfigEntity(
            stageId = "edit",
            sessionDurationMinutes = 60,
            primaryApps = "com.capcut.android,com.lenovo.videoplayer",
            supportApps = "com.google.android.apps.photos,com.google.android.apps.docs,com.android.documentsui,com.google.android.music"
        )
        val defaultPublish = CreatorStageConfigEntity(
            stageId = "publish",
            sessionDurationMinutes = 60,
            primaryApps = "com.instagram.android,com.google.android.apps.youtube.creator",
            supportApps = "com.canva.editor,com.google.android.apps.tachyon,com.google.android.apps.photos,com.google.android.apps.docs"
        )
        dao.saveStageConfig(defaultShoot)
        dao.saveStageConfig(defaultEdit)
        dao.saveStageConfig(defaultPublish)
    }

    private var timerJob: Job? = null

    fun startCreatorSession(stageId: String) {
        val config = _creatorStageConfigs.value[stageId] ?: CreatorStageConfigEntity(stageId)
        val durationSeconds = config.sessionDurationMinutes * 60
        _creatorSessionState.value = CreatorSessionState(
            isActive = true,
            currentStageId = stageId,
            secondsRemaining = durationSeconds,
            isPaused = true,
            showResumePrompt = false
        )
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _creatorSessionState.value
                if (current.isActive && !current.isPaused) {
                    val nextSeconds = current.secondsRemaining - 1
                    if (nextSeconds <= 0) {
                        _creatorSessionState.value = current.copy(
                            secondsRemaining = 0
                        )
                        break
                    } else {
                        _creatorSessionState.value = current.copy(secondsRemaining = nextSeconds)
                    }
                }
            }
        }
    }

    fun pauseCreatorSession() {
        _creatorSessionState.value = _creatorSessionState.value.copy(isPaused = true)
    }

    fun resumeCreatorSession() {
        _creatorSessionState.value = _creatorSessionState.value.copy(
            isPaused = false,
            showResumePrompt = false,
            hasTimerBeenStarted = true
        )
    }

    fun extendCreatorSession(minutes: Int) {
        val current = _creatorSessionState.value
        val newSeconds = current.secondsRemaining + (minutes * 60)
        _creatorSessionState.value = current.copy(
            secondsRemaining = newSeconds,
            isPaused = false,
            showResumePrompt = false,
            hasTimerBeenStarted = true
        )
        startTimerLoop()
    }

    fun finishCreatorSession() {
        timerJob?.cancel()
        _creatorSessionState.value = CreatorSessionState()
    }

    fun switchStage(stageId: String) {
        val config = _creatorStageConfigs.value[stageId] ?: CreatorStageConfigEntity(stageId)
        val durationSeconds = config.sessionDurationMinutes * 60
        _creatorSessionState.value = _creatorSessionState.value.copy(
            currentStageId = stageId,
            secondsRemaining = durationSeconds,
            isPaused = true,
            showResumePrompt = false
        )
        startTimerLoop()
    }

    fun updateStageApps(stageId: String, primary: List<String>, support: List<String>) {
        viewModelScope.launch {
            try {
                val currentConfig = _creatorStageConfigs.value[stageId] ?: CreatorStageConfigEntity(stageId)
                val updatedConfig = currentConfig.copy(
                    primaryApps = primary.joinToString(","),
                    supportApps = support.joinToString(",")
                )
                dao.saveStageConfig(updatedConfig)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun updateStageDuration(stageId: String, minutes: Int) {
        viewModelScope.launch {
            try {
                val currentConfig = _creatorStageConfigs.value[stageId] ?: CreatorStageConfigEntity(stageId)
                val updatedConfig = currentConfig.copy(sessionDurationMinutes = minutes)
                dao.saveStageConfig(updatedConfig)

                // Update active timer immediately if we are configuring the active stage
                val activeSession = _creatorSessionState.value
                if (activeSession.isActive && activeSession.currentStageId == stageId) {
                    _creatorSessionState.value = activeSession.copy(
                        secondsRemaining = minutes * 60
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    // -------------------------------------------------------------
    // E-Paper Sanctuary & Offline Bookshelf Architecture
    // -------------------------------------------------------------

    fun cycleEPaperColorProfile() {
        val next = _epaperColorProfile.value.next()
        _epaperColorProfile.value = next
        prefs.edit().putString("epaper_color_profile", next.name).apply()
    }

    fun setEPaperColorProfile(profile: EPaperColorProfile) {
        _epaperColorProfile.value = profile
        prefs.edit().putString("epaper_color_profile", profile.name).apply()
    }

    private fun loadPinnedReadingApps() {
        viewModelScope.launch {
            try {
                dao.getPinnedAppsForMode(LauncherMode.E_PAPER.name).collectLatest { list ->
                    _pinnedReadingAppPackages.value = list.map { it.packageName }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun pinReadingApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = _pinnedReadingAppPackages.value
                if (!current.contains(packageName)) {
                    dao.insertPinnedApp(
                        PinnedAppEntity(
                            packageName = packageName,
                            modeName = LauncherMode.E_PAPER.name,
                            displayOrder = current.size
                        )
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun unpinReadingApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.deletePinnedApp(packageName, LauncherMode.E_PAPER.name)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun loadOfflineBooks() {
        viewModelScope.launch {
            try {
                offlineBookDao.getAllOfflineBooksFlow().collectLatest { list ->
                    _offlineBooks.value = list
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun addOfflineBook(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Throwable) {
                    // ignore if not persistable
                }

                var rawTitle = "Offline Document"
                val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"

                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) {
                                rawTitle = name
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                val titleLower = rawTitle.lowercase(Locale.ROOT)
                val badge = when {
                    titleLower.endsWith(".epub") || mimeType.contains("epub") -> "EPUB"
                    titleLower.endsWith(".mobi") || mimeType.contains("mobi") -> "MOBI"
                    titleLower.endsWith(".cbz") || mimeType.contains("cbz") -> "CBZ"
                    titleLower.endsWith(".pdf") || mimeType.contains("pdf") -> "PDF"
                    titleLower.endsWith(".txt") || mimeType.contains("text") -> "TXT"
                    else -> "DOC"
                }

                // Clean file extension from title
                var cleanedTitle = rawTitle.replace(Regex("\\.(epub|mobi|cbz|pdf|txt|fb2)$", RegexOption.IGNORE_CASE), "")
                var extractedAuthor = "Unknown Author"

                // Extract Author if format is "Title - Author" or "Author - Title"
                if (cleanedTitle.contains(" - ")) {
                    val parts = cleanedTitle.split(" - ")
                    if (parts.size >= 2) {
                        cleanedTitle = parts[0].trim()
                        extractedAuthor = parts[1].trim()
                    }
                }

                val bookEntity = OfflineBookEntity(
                    title = cleanedTitle,
                    author = extractedAuthor,
                    uriString = uri.toString(),
                    mimeType = mimeType,
                    formatBadge = badge,
                    currentPage = 0,
                    totalPages = 100,
                    isCompleted = false,
                    addedTimestamp = System.currentTimeMillis(),
                    lastOpenedTimestamp = System.currentTimeMillis()
                )
                val newId = offlineBookDao.insertOfflineBook(bookEntity)

                // If currently reading is default placeholder, set this newly imported book as currently reading
                if (_currentlyReadingBook.value == "Atomic Habits" || _currentlyReadingBook.value.isBlank()) {
                    updateReadingProgress(cleanedTitle, 0, 100)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun deleteOfflineBook(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.deleteOfflineBook(id)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun renameOfflineBook(id: Long, newTitle: String, newAuthor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.renameBook(id, newTitle.trim(), newAuthor.trim())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun updateOfflineBookProgress(bookId: Long, page: Int, totalPages: Int) {
        val safeTotal = totalPages.coerceAtLeast(1)
        val safePage = page.coerceIn(0, safeTotal)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.updateProgress(bookId, safePage, safeTotal)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        val book = _offlineBooks.value.find { it.id == bookId }
        if (book != null) {
            updateReadingProgress(book.title, safePage, safeTotal)
        }
    }

    fun setBookPreferredPackage(bookId: Long, packageName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.setPreferredPackage(bookId, packageName)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun toggleBookCompletion(bookId: Long, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.setCompletion(bookId, isCompleted)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun openOfflineBook(context: Context, book: OfflineBookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                offlineBookDao.updateLastOpened(book.id, System.currentTimeMillis())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // Sync to "Now Reading"
        updateReadingProgress(book.title, book.currentPage, book.totalPages)

        try {
            val uri = Uri.parse(book.uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, book.mimeType.ifBlank { "application/pdf" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!book.preferredPackage.isNullOrBlank()) {
                    setPackage(book.preferredPackage)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback without specific package/mime constraint
                val uri = Uri.parse(book.uriString)
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun updateReadingProgress(bookTitle: String, page: Int, totalPages: Int) {
        val safeTotal = totalPages.coerceAtLeast(1)
        val safePage = page.coerceIn(0, safeTotal)
        _currentlyReadingBook.value = bookTitle
        _currentlyReadingPage.value = safePage
        _currentlyReadingTotalPages.value = safeTotal

        prefs.edit()
            .putString("reading_book_title", bookTitle)
            .putInt("reading_current_page", safePage)
            .putInt("reading_total_pages", safeTotal)
            .apply()
    }

    fun resetCreatorConfigurations() {
        viewModelScope.launch {
            try {
                dao.clearAllStageConfigs()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(triggerReceiver)
        } catch (e: Exception) {
            // ignore
        }
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // ignore
        }
        try {
            getApplication<Application>().unregisterReceiver(creatorScreenReceiver)
        } catch (e: Exception) {
            // ignore
        }
        stopSpeedTracking()
        timerJob?.cancel()
    }

}
