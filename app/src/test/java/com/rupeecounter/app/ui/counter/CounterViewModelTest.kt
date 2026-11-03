package com.rupeecounter.app.ui.counter

import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.data.repository.CounterRepositoryImpl
import com.rupeecounter.app.data.repository.FakeActiveSessionDao
import com.rupeecounter.app.data.repository.FakeSavedRecordDao
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeActiveDao: FakeActiveSessionDao
    private lateinit var fakeSavedDao: FakeSavedRecordDao
    private lateinit var repository: CounterRepositoryImpl
    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeActiveDao = FakeActiveSessionDao()
        fakeSavedDao = FakeSavedRecordDao()
        repository = CounterRepositoryImpl(
            activeSessionDao = fakeActiveDao,
            savedRecordDao = fakeSavedDao,
            calculateTotalUseCase = CalculateTotalUseCase(),
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSessionRestorationOnViewModelInit() = runTest(testDispatcher) {
        // Pre-populate repository with active session data
        repository.updateCount(Denomination.NOTE_500, 10L) // 5000
        repository.updateCount(Denomination.COIN_5, 10L)   // 50
        advanceUntilIdle()

        // Initialize ViewModel
        viewModel = CounterViewModel(repository, CalculateTotalUseCase())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(10L, state.counts[Denomination.NOTE_500])
        assertEquals(10L, state.counts[Denomination.COIN_5])
        assertEquals(5050L, state.calculation.grandTotal)
        assertEquals("₹5,050", state.calculation.formattedGrandTotal)
    }

    @Test
    fun testCountChangedUpdatesCalculationImmediately() = runTest(testDispatcher) {
        viewModel = CounterViewModel(repository, CalculateTotalUseCase())
        advanceUntilIdle()

        viewModel.onCountChanged(Denomination.NOTE_500, 4L) // 2000
        viewModel.onCountChanged(Denomination.NOTE_200, 5L) // 1000

        val state = viewModel.uiState.value
        assertEquals(3000L, state.calculation.grandTotal)
        assertEquals("₹3,000", state.calculation.formattedGrandTotal)
        assertEquals(9L, state.calculation.totalPieces)
        assertEquals(9L, state.calculation.totalNotesCount)
        assertEquals(0L, state.calculation.totalCoinsCount)
    }

    @Test
    fun testIncrementAndDecrement() = runTest(testDispatcher) {
        viewModel = CounterViewModel(repository, CalculateTotalUseCase())
        advanceUntilIdle()

        viewModel.onIncrement(Denomination.NOTE_100, 5L)
        assertEquals(5L, viewModel.uiState.value.counts[Denomination.NOTE_100])
        assertEquals(500L, viewModel.uiState.value.calculation.grandTotal)

        viewModel.onDecrement(Denomination.NOTE_100, 2L)
        assertEquals(3L, viewModel.uiState.value.counts[Denomination.NOTE_100])
        assertEquals(300L, viewModel.uiState.value.calculation.grandTotal)

        // Decrement below zero is safely clamped to 0
        viewModel.onDecrement(Denomination.NOTE_100, 10L)
        assertEquals(0L, viewModel.uiState.value.counts[Denomination.NOTE_100])
        assertEquals(0L, viewModel.uiState.value.calculation.grandTotal)
    }

    @Test
    fun testResetFlow() = runTest(testDispatcher) {
        viewModel = CounterViewModel(repository, CalculateTotalUseCase())
        advanceUntilIdle()

        viewModel.onCountChanged(Denomination.NOTE_500, 10L)
        assertEquals(5000L, viewModel.uiState.value.calculation.grandTotal)

        // Request reset
        viewModel.requestReset()
        assertTrue(viewModel.uiState.value.showResetDialog)

        // Confirm reset
        viewModel.confirmReset()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showResetDialog)
        assertEquals(0L, viewModel.uiState.value.calculation.grandTotal)
        assertEquals("₹0", viewModel.uiState.value.calculation.formattedGrandTotal)
    }

    @Test
    fun testSaveRecordFlow() = runTest(testDispatcher) {
        viewModel = CounterViewModel(repository, CalculateTotalUseCase())
        advanceUntilIdle()

        viewModel.onCountChanged(Denomination.NOTE_500, 10L)

        // Request save dialog
        viewModel.requestSaveRecord()
        assertTrue(viewModel.uiState.value.showSaveDialog)

        // Confirm save
        viewModel.confirmSaveRecord("Deposit Cash")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showSaveDialog)
        assertTrue(viewModel.uiState.value.userMessage?.contains("saved successfully") == true)

        // Verify active counts still present
        assertEquals(10L, viewModel.uiState.value.counts[Denomination.NOTE_500])
        assertEquals(5000L, viewModel.uiState.value.calculation.grandTotal)
    }
}
