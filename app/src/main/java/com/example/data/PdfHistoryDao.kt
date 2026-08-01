package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfHistoryDao {
    @Query("SELECT * FROM pdf_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PdfHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: PdfHistoryEntity): Long

    @Query("DELETE FROM pdf_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM pdf_history")
    suspend fun clearAllHistory()
}
