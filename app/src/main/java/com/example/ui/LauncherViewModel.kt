package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppInfo
import com.example.data.AppRepository
import com.example.db.LauncherDatabase
import com.example.db.ModeSettingEntity
import com.example.model.LauncherMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    val isAmbientSoundPlaying: Boolean = false,
    val selectedSoundTrack: String = "Rainfall",
    val windDownTimerMinutes: Int = 15,
    val bedtimeAlarmTime: String = "07:00 AM"
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository()
    private val db = Room.databaseBuilder(
        application,
        LauncherDatabase::class.java,
        "morph_launcher.db"
    )
        .fallbackToDestructiveMigration()
        .build()
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
    private val _focusAllowedPackages = MutableStateFlow<Set<String>>(emptySet())
    val focusAllowedPackages: StateFlow<Set<String>> = _focusAllowedPackages.asStateFlow()

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

    // Battery State
    private val _batteryLevel = MutableStateFlow(85)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(ModeSettingEntity())
    val settings: StateFlow<ModeSettingEntity> = _settings.asStateFlow()

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

    init {
        loadInstalledApps()
        loadSettingsFromDb()
        registerBatteryReceiver()

        // Register internal trigger receiver safely
        try {
            val filter = IntentFilter("com.example.morphlauncher.TRIGGER_EVENT")
            ContextCompat.registerReceiver(
                application,
                triggerReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
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
                        .toSet()

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
                val defaults = apps.take(4).map { it.packageName }.toSet()
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
        val current = _focusAllowedPackages.value.toMutableSet()
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

    private fun saveFocusPackagesToDb(packages: Set<String>) {
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

    fun toggleAmbientSound() {
        _sleepState.value = _sleepState.value.copy(
            isAmbientSoundPlaying = !_sleepState.value.isAmbientSoundPlaying
        )
    }

    fun setAmbientTrack(track: String) {
        _sleepState.value = _sleepState.value.copy(selectedSoundTrack = track)
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerSystemHomePicker(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
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
            val batteryStatus: Intent? = ContextCompat.registerReceiver(
                getApplication<Application>(),
                batteryReceiver,
                batteryFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
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
    }
}
