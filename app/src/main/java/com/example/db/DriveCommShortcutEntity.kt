package com.example.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drive_comm_shortcuts")
data class DriveCommShortcutEntity(
    @PrimaryKey val slotIndex: Int, // 0..3
    val name: String = "",
    val phoneNumber: String = "",
    val channelType: String = "CALL", // "CALL", "WHATSAPP", "SMS", "TELEGRAM"
    val photoUri: String? = null
)

@Dao
interface DriveCommShortcutDao {
    @Query("SELECT * FROM drive_comm_shortcuts ORDER BY slotIndex ASC")
    fun getAllCommShortcutsFlow(): Flow<List<DriveCommShortcutEntity>>

    @Query("SELECT * FROM drive_comm_shortcuts WHERE slotIndex = :slotIndex")
    suspend fun getCommShortcutBySlot(slotIndex: Int): DriveCommShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCommShortcut(shortcut: DriveCommShortcutEntity)

    @Query("DELETE FROM drive_comm_shortcuts WHERE slotIndex = :slotIndex")
    suspend fun deleteCommShortcut(slotIndex: Int)
}
