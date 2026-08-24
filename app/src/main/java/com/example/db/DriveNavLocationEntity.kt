package com.example.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drive_nav_locations")
data class DriveNavLocationEntity(
    @PrimaryKey val id: String, // "home", "work", "custom1", "custom2"
    val label: String,
    val addressOrQuery: String,
    val displayOrder: Int = 0
)

@Dao
interface DriveNavLocationDao {
    @Query("SELECT * FROM drive_nav_locations ORDER BY displayOrder ASC")
    fun getAllNavLocationsFlow(): Flow<List<DriveNavLocationEntity>>

    @Query("SELECT * FROM drive_nav_locations WHERE id = :id")
    suspend fun getNavLocationById(id: String): DriveNavLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNavLocation(location: DriveNavLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllNavLocations(locations: List<DriveNavLocationEntity>)

    @Query("DELETE FROM drive_nav_locations WHERE id = :id")
    suspend fun deleteNavLocation(id: String)
}
