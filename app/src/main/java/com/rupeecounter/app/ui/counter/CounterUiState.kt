package com.rupeecounter.app.ui.counter

import com.rupeecounter.app.data.model.CalculationResult
import com.rupeecounter.app.data.model.Denomination

data class CounterUiState(
    val counts: Map<Denomination, Long> = Denomination.entries.associateWith { 0L },
    val calculation: CalculationResult = CalculationResult.EMPTY,
    val isLoading: Boolean = true,
    val showResetDialog: Boolean = false,
    val showSaveDialog: Boolean = false,
    val userMessage: String? = null
)
