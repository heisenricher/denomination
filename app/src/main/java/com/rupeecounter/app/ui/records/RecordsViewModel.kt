package com.rupeecounter.app.ui.records

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rupeecounter.app.data.local.SavedRecordEntity
import com.rupeecounter.app.data.repository.CounterRepository
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase
import com.rupeecounter.app.export.PdfReportGenerator
import com.rupeecounter.app.export.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val repository: CounterRepository,
    private val calculateTotalUseCase: CalculateTotalUseCase = CalculateTotalUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            repository.observeSavedRecords().collect { list ->
                _uiState.update {
                    it.copy(
                        records = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSelectRecord(record: SavedRecordEntity) {
        val parsedCounts = repository.parseCountsJson(record.countsJson)
        val calculation = calculateTotalUseCase.execute(parsedCounts)
        _uiState.update {
            it.copy(
                selectedRecord = record,
                selectedRecordCalculation = calculation
            )
        }
    }

    fun dismissDetail() {
        _uiState.update {
            it.copy(
                selectedRecord = null,
                selectedRecordCalculation = null
            )
        }
    }

    fun requestDeleteRecord(record: SavedRecordEntity) {
        _uiState.update { it.copy(recordToDelete = record) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(recordToDelete = null) }
    }

    fun confirmDeleteRecord() {
        val target = _uiState.value.recordToDelete ?: return
        viewModelScope.launch {
            try {
                repository.deleteSavedRecord(target.id)
                _uiState.update {
                    it.copy(
                        recordToDelete = null,
                        selectedRecord = if (it.selectedRecord?.id == target.id) null else it.selectedRecord,
                        selectedRecordCalculation = if (it.selectedRecord?.id == target.id) null else it.selectedRecordCalculation,
                        userMessage = "Record deleted"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        recordToDelete = null,
                        userMessage = "Failed to delete: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun exportRecordPdf(context: Context, record: SavedRecordEntity) {
        viewModelScope.launch {
            try {
                val parsedCounts = repository.parseCountsJson(record.countsJson)
                val calc = calculateTotalUseCase.execute(parsedCounts)
                val generator = PdfReportGenerator(context)
                val file = generator.generateReport(
                    calculation = calc,
                    timestamp = record.timestamp,
                    remarks = record.remarks
                )
                ShareHelper.sharePdf(context, file, "Rupee Counter Report - Record #${record.id}")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = "PDF export failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun shareRecordText(context: Context, record: SavedRecordEntity) {
        try {
            val parsedCounts = repository.parseCountsJson(record.countsJson)
            val calc = calculateTotalUseCase.execute(parsedCounts)
            val text = ShareHelper.buildTextSummary(
                calculation = calc,
                timestamp = record.timestamp,
                remarks = record.remarks
            )
            ShareHelper.shareText(context, text, "Rupee Counter - Record #${record.id}")
        } catch (e: Exception) {
            _uiState.update {
                it.copy(userMessage = "Sharing failed: ${e.localizedMessage}")
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun provideFactory(
            repository: CounterRepository,
            calculateTotalUseCase: CalculateTotalUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecordsViewModel(repository, calculateTotalUseCase) as T
            }
        }
    }
}
