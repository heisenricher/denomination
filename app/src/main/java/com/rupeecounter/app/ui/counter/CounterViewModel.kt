package com.rupeecounter.app.ui.counter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.data.repository.CounterRepository
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase
import com.rupeecounter.app.domain.usecase.IndianCurrencyFormatter
import com.rupeecounter.app.export.PdfReportGenerator
import com.rupeecounter.app.export.ShareHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CounterViewModel(
    private val repository: CounterRepository,
    private val calculateTotalUseCase: CalculateTotalUseCase = CalculateTotalUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null
    private var isSavingHistorical: Boolean = false

    init {
        restoreSession()
    }

    /**
     * Automatic session restoration from Room DB.
     * Restores exact state after app closure, process death, device restart.
     */
    private fun restoreSession() {
        viewModelScope.launch {
            try {
                val persistedCounts = repository.getActiveCounts()
                val calculation = calculateTotalUseCase.execute(persistedCounts)
                _uiState.update {
                    it.copy(
                        counts = persistedCounts,
                        calculation = calculation,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                // Safe fallback to zero counts if database read encounters unexpected error
                val defaultCounts = Denomination.entries.associateWith { 0L }
                val calculation = calculateTotalUseCase.execute(defaultCounts)
                _uiState.update {
                    it.copy(
                        counts = defaultCounts,
                        calculation = calculation,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onCountChanged(denomination: Denomination, newCount: Long) {
        val safeCount = newCount.coerceIn(0L, IndianCurrencyFormatter.MAX_ALLOWED_COUNT)
        val currentCounts = _uiState.value.counts.toMutableMap()
        if (currentCounts[denomination] == safeCount) return

        currentCounts[denomination] = safeCount
        val calculation = calculateTotalUseCase.execute(currentCounts)

        _uiState.update {
            it.copy(
                counts = currentCounts,
                calculation = calculation
            )
        }

        // Persist to Room database immediately
        persistActiveCount(denomination, safeCount)
    }

    fun onCountInputStringChanged(denomination: Denomination, rawInput: String) {
        val parsed = IndianCurrencyFormatter.parseInputCount(rawInput)
        onCountChanged(denomination, parsed)
    }

    fun onIncrement(denomination: Denomination, delta: Long = 1L) {
        val current = _uiState.value.counts[denomination] ?: 0L
        val next = (current + delta).coerceIn(0L, IndianCurrencyFormatter.MAX_ALLOWED_COUNT)
        onCountChanged(denomination, next)
    }

    fun onDecrement(denomination: Denomination, delta: Long = 1L) {
        val current = _uiState.value.counts[denomination] ?: 0L
        val next = (current - delta).coerceAtLeast(0L)
        onCountChanged(denomination, next)
    }

    private fun persistActiveCount(denomination: Denomination, count: Long) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                repository.updateCount(denomination, count)
            } catch (_: Exception) {
                // Silently safeguard local DB write; in-memory state remains intact
            }
        }
    }

    fun requestReset() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun confirmReset() {
        viewModelScope.launch {
            try {
                repository.resetActiveCounts()
                val zeroCounts = Denomination.entries.associateWith { 0L }
                val calculation = calculateTotalUseCase.execute(zeroCounts)
                _uiState.update {
                    it.copy(
                        counts = zeroCounts,
                        calculation = calculation,
                        showResetDialog = false,
                        userMessage = "Counts reset to zero"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showResetDialog = false,
                        userMessage = "Failed to reset: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun requestSaveRecord() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun dismissSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }

    fun confirmSaveRecord(remarks: String?) {
        if (isSavingHistorical) return
        isSavingHistorical = true

        viewModelScope.launch {
            try {
                val recordId = repository.saveCurrentAsRecord(remarks)
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userMessage = "Record #$recordId saved successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userMessage = "Failed to save record: ${e.localizedMessage}"
                    )
                }
            } finally {
                isSavingHistorical = false
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun exportAndSharePdf(context: Context, remarks: String? = null) {
        viewModelScope.launch {
            try {
                val calculation = _uiState.value.calculation
                val generator = PdfReportGenerator(context)
                val pdfFile = generator.generateReport(
                    calculation = calculation,
                    timestamp = System.currentTimeMillis(),
                    remarks = remarks
                )
                ShareHelper.sharePdf(context, pdfFile, "Rupee Counter Report")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = "PDF generation failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun shareTextSummary(context: Context, remarks: String? = null) {
        try {
            val calculation = _uiState.value.calculation
            val summary = ShareHelper.buildTextSummary(
                calculation = calculation,
                timestamp = System.currentTimeMillis(),
                remarks = remarks
            )
            ShareHelper.shareText(context, summary, "Rupee Counter Summary")
        } catch (e: Exception) {
            _uiState.update {
                it.copy(userMessage = "Sharing failed: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: CounterRepository,
            calculateTotalUseCase: CalculateTotalUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CounterViewModel(repository, calculateTotalUseCase) as T
            }
        }
    }
}
