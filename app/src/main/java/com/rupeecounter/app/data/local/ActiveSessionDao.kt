package com.rupeecounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveSessionDao {

    @Query("SELECT * FROM active_session_counts")
    fun observeAllActiveCounts(): Flow<List<ActiveSessionEntity>>

    @Query("SELECT * FROM active_session_counts")
    suspend fun getAllActiveCounts(): List<ActiveSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCount(entity: ActiveSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ActiveSessionEntity>)

    @Query("UPDATE active_session_counts SET count = 0, updatedAt = :timestamp")
    suspend fun resetAllToZero(timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM active_session_counts")
    suspend fun deleteAll()

    @Transaction
    suspend fun saveCountsTransaction(counts: Map<String, Long>) {
        val now = System.currentTimeMillis()
        val entities = counts.map { (key, count) ->
            ActiveSessionEntity(
                denominationKey = key,
                count = count,
                updatedAt = now
            )
        }
        upsertAll(entities)
    }
}
