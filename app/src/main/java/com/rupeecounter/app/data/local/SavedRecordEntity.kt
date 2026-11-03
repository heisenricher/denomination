package com.rupeecounter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_records")
data class SavedRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val grandTotal: Long,
    val totalNotesCount: Long,
    val totalCoinsCount: Long,
    val totalPieces: Long,
    val countsJson: String,
    val remarks: String? = null
)
