package com.example.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_books")
data class OfflineBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "Unknown Author",
    val uriString: String,
    val mimeType: String,
    val formatBadge: String = "PDF",
    val currentPage: Int = 0,
    val totalPages: Int = 100,
    val isCompleted: Boolean = false,
    val preferredPackage: String? = null,
    val coverImagePath: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastOpenedTimestamp: Long = System.currentTimeMillis()
) {
    val progressPercentage: Int
        get() = if (totalPages > 0) ((currentPage.toFloat() / totalPages.toFloat()) * 100).coerceIn(0f, 100f).toInt() else 0
}

@Dao
interface OfflineBookDao {
    @Query("SELECT * FROM offline_books ORDER BY lastOpenedTimestamp DESC, addedTimestamp DESC")
    fun getAllOfflineBooksFlow(): Flow<List<OfflineBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineBook(book: OfflineBookEntity): Long

    @Query("DELETE FROM offline_books WHERE id = :id")
    suspend fun deleteOfflineBook(id: Long)

    @Query("UPDATE offline_books SET lastOpenedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: Long, timestamp: Long)

    @Query("UPDATE offline_books SET currentPage = :currentPage, totalPages = :totalPages WHERE id = :id")
    suspend fun updateProgress(id: Long, currentPage: Int, totalPages: Int)

    @Query("UPDATE offline_books SET title = :title, author = :author WHERE id = :id")
    suspend fun renameBook(id: Long, title: String, author: String)

    @Query("UPDATE offline_books SET preferredPackage = :preferredPackage WHERE id = :id")
    suspend fun setPreferredPackage(id: Long, preferredPackage: String?)

    @Query("UPDATE offline_books SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompletion(id: Long, isCompleted: Boolean)
}
