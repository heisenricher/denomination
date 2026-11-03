package com.rupeecounter.app.ui.records

import com.rupeecounter.app.data.local.SavedRecordEntity
import com.rupeecounter.app.data.model.CalculationResult

data class RecordsUiState(
    val records: List<SavedRecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRecord: SavedRecordEntity? = null,
    val selectedRecordCalculation: CalculationResult? = null,
    val recordToDelete: SavedRecordEntity? = null,
    val userMessage: String? = null
)
