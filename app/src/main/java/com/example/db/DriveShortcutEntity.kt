package com.example.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drive_shortcuts")
data class DriveShortcutEntity(
    @PrimaryKey val slotIndex: Int, // 0 = Speed Dial, 1..3 = Customizable App Slots
    val type: String = "APP",       // "SPEED_DIAL" or "APP"
    val label: String = "",
    val packageName: String = "",
    val phoneNumber: String = "",
    val customIcon: String = ""
)

@Dao
interface DriveShortcutDao {
    @Query("SELECT * FROM drive_shortcuts ORDER BY slotIndex ASC")
    fun getAllShortcutsFlow(): Flow<List<DriveShortcutEntity>>

    @Query("SELECT * FROM drive_shortcuts WHERE slotIndex = :slotIndex")
    suspend fun getShortcutBySlot(slotIndex: Int): DriveShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveShortcut(shortcut: DriveShortcutEntity)

    @Query("DELETE FROM drive_shortcuts WHERE slotIndex = :slotIndex")
    suspend fun deleteShortcut(slotIndex: Int)
}
