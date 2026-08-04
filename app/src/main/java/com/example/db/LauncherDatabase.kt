package com.example.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mode_settings")
data class ModeSettingEntity(
    @PrimaryKey val id: Int = 1,
    val currentMode: String = "FOCUS",
    val focusAllowedPackages: String = "com.google.android.dialer,com.google.android.apps.messaging,com.google.android.keep",
    val driveFavoritePackages: String = "com.google.android.apps.maps,com.google.android.music,com.google.android.dialer",
    val sleepAllowedPackages: String = "com.google.android.deskclock,com.google.android.dialer",
    val passThroughLauncherPackage: String = "",
    val autoTriggerBluetoothDrive: Boolean = true,
    val autoTriggerChargerSleep: Boolean = false,
    val bedtimeStartHour: Int = 22,
    val bedtimeEndHour: Int = 7
)

@Entity(tableName = "pinned_apps")
data class PinnedAppEntity(
    @PrimaryKey val packageName: String,
    val modeName: String, // NORMAL, FOCUS, DRIVE, SLEEP
    val displayOrder: Int = 0,
    val customLabel: String? = null
)

@Dao
interface ModeSettingDao {
    @Query("SELECT * FROM mode_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<ModeSettingEntity?>

    @Query("SELECT * FROM mode_settings WHERE id = 1")
    suspend fun getSettings(): ModeSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ModeSettingEntity)

    @Query("SELECT * FROM pinned_apps WHERE modeName = :mode ORDER BY displayOrder ASC")
    fun getPinnedAppsForMode(mode: String): Flow<List<PinnedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinnedApp(app: PinnedAppEntity)

    @Query("DELETE FROM pinned_apps WHERE packageName = :packageName AND modeName = :mode")
    suspend fun deletePinnedApp(packageName: String, mode: String)
}

@Database(
    entities = [ModeSettingEntity::class, PinnedAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun modeSettingDao(): ModeSettingDao
}
