package com.rupeecounter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session_counts")
data class ActiveSessionEntity(
    @PrimaryKey
    val denominationKey: String,
    val count: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
