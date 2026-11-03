package com.rupeecounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecordDao {

    @Query("SELECT * FROM saved_records ORDER BY timestamp DESC")
    fun observeAllRecords(): Flow<List<SavedRecordEntity>>

    @Query("SELECT * FROM saved_records ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<SavedRecordEntity>

    @Query("SELECT * FROM saved_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): SavedRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(entity: SavedRecordEntity): Long

    @Query("DELETE FROM saved_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long): Int

    @Query("DELETE FROM saved_records")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM saved_records")
    suspend fun getRecordCount(): Int
}
