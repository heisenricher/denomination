package com.rupeecounter.app.data.repository

import com.rupeecounter.app.data.local.ActiveSessionDao
import com.rupeecounter.app.data.local.ActiveSessionEntity
import com.rupeecounter.app.data.local.SavedRecordDao
import com.rupeecounter.app.data.local.SavedRecordEntity
import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CounterRepositoryTest {

    private lateinit var fakeActiveDao: FakeActiveSessionDao
    private lateinit var fakeSavedDao: FakeSavedRecordDao
    private lateinit var repository: CounterRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        fakeActiveDao = FakeActiveSessionDao()
        fakeSavedDao = FakeSavedRecordDao()
        repository = CounterRepositoryImpl(
            activeSessionDao = fakeActiveDao,
            savedRecordDao = fakeSavedDao,
            calculateTotalUseCase = CalculateTotalUseCase(),
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun testActiveSessionSavingAndRestoration() = runTest(testDispatcher) {
        // Initial state is all 0
        val initial = repository.getActiveCounts()
        for (denom in Denomination.entries) {
            assertEquals(0L, initial[denom])
        }

        // User enters counts matching the spec restoration example
        repository.updateCount(Denomination.NOTE_500, 20L)
        repository.updateCount(Denomination.NOTE_200, 10L)
        repository.updateCount(Denomination.NOTE_100, 15L)
        repository.updateCount(Denomination.NOTE_50, 5L)
        repository.updateCount(Denomination.NOTE_20, 10L)
        repository.updateCount(Denomination.NOTE_10, 5L)
        repository.updateCount(Denomination.COIN_5, 10L)

        // Simulate app kill and restore session
        val restored = repository.getActiveCounts()
        assertEquals(20L, restored[Denomination.NOTE_500])
        assertEquals(10L, restored[Denomination.NOTE_200])
        assertEquals(15L, restored[Denomination.NOTE_100])
        assertEquals(5L, restored[Denomination.NOTE_50])
        assertEquals(10L, restored[Denomination.NOTE_20])
        assertEquals(5L, restored[Denomination.NOTE_10])
        assertEquals(10L, restored[Denomination.COIN_5])
        assertEquals(0L, restored[Denomination.COIN_1]) // untouched remain 0
    }

    @Test
    fun testResetActiveCountsSetsCountsToZero() = runTest(testDispatcher) {
        repository.updateCount(Denomination.NOTE_500, 50L)
        repository.updateCount(Denomination.COIN_10, 20L)

        // Reset
        repository.resetActiveCounts()

        val restored = repository.getActiveCounts()
        assertEquals(0L, restored[Denomination.NOTE_500])
        assertEquals(0L, restored[Denomination.COIN_10])
    }

    @Test
    fun testSavingHistoricalRecordDoesNotAlterActiveSession() = runTest(testDispatcher) {
        // Setup active session
        repository.updateCount(Denomination.NOTE_500, 10L) // 5000
        repository.updateCount(Denomination.NOTE_200, 5L)  // 1000
        // Grand total = 6000

        // Save as historical record
        val recordId = repository.saveCurrentAsRecord("Evening Drawer Cash")
        assertTrue(recordId > 0)

        // Verify record is saved in historical records
        val saved = repository.getSavedRecordById(recordId)
        assertNotNull(saved)
        assertEquals(6000L, saved?.grandTotal)
        assertEquals(15L, saved?.totalPieces)
        assertEquals(15L, saved?.totalNotesCount)
        assertEquals(0L, saved?.totalCoinsCount)
        assertEquals("Evening Drawer Cash", saved?.remarks)

        // CRITICAL CHECK: Active session MUST NOT be reset or altered!
        val active = repository.getActiveCounts()
        assertEquals(10L, active[Denomination.NOTE_500])
        assertEquals(5L, active[Denomination.NOTE_200])
    }

    @Test
    fun testDeleteHistoricalRecord() = runTest(testDispatcher) {
        repository.updateCount(Denomination.NOTE_100, 10L)
        val recordId = repository.saveCurrentAsRecord(null)

        val success = repository.deleteSavedRecord(recordId)
        assertTrue(success)

        val retrieved = repository.getSavedRecordById(recordId)
        assertEquals(null, retrieved)
    }

    @Test
    fun testJsonSerializationRoundTrip() {
        val original = mapOf(
            Denomination.NOTE_500 to 20L,
            Denomination.NOTE_200 to 10L,
            Denomination.COIN_5 to 5L
        )

        val json = repository.serializeCounts(original)
        val deserialized = repository.parseCountsJson(json)

        assertEquals(20L, deserialized[Denomination.NOTE_500])
        assertEquals(10L, deserialized[Denomination.NOTE_200])
        assertEquals(5L, deserialized[Denomination.COIN_5])
        assertEquals(0L, deserialized[Denomination.NOTE_100])
    }
}

class FakeActiveSessionDao : ActiveSessionDao {
    private val data = mutableMapOf<String, ActiveSessionEntity>()
    private val flow = MutableStateFlow<List<ActiveSessionEntity>>(emptyList())

    override fun observeAllActiveCounts(): Flow<List<ActiveSessionEntity>> = flow

    override suspend fun getAllActiveCounts(): List<ActiveSessionEntity> = data.values.toList()

    override suspend fun upsertCount(entity: ActiveSessionEntity) {
        data[entity.denominationKey] = entity
        flow.value = data.values.toList()
    }

    override suspend fun upsertAll(entities: List<ActiveSessionEntity>) {
        for (e in entities) {
            data[e.denominationKey] = e
        }
        flow.value = data.values.toList()
    }

    override suspend fun resetAllToZero(timestamp: Long) {
        for (key in data.keys) {
            data[key] = ActiveSessionEntity(key, 0L, timestamp)
        }
        flow.value = data.values.toList()
    }

    override suspend fun deleteAll() {
        data.clear()
        flow.value = emptyList()
    }
}

class FakeSavedRecordDao : SavedRecordDao {
    private val records = mutableMapOf<Long, SavedRecordEntity>()
    private val flow = MutableStateFlow<List<SavedRecordEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAllRecords(): Flow<List<SavedRecordEntity>> = flow.map { list ->
        list.sortedByDescending { it.timestamp }
    }

    override suspend fun getAllRecords(): List<SavedRecordEntity> = records.values.sortedByDescending { it.timestamp }

    override suspend fun getRecordById(id: Long): SavedRecordEntity? = records[id]

    override suspend fun insertRecord(entity: SavedRecordEntity): Long {
        val id = if (entity.id == 0L) nextId++ else entity.id
        val newRecord = entity.copy(id = id)
        records[id] = newRecord
        flow.value = records.values.toList()
        return id
    }

    override suspend fun deleteRecordById(id: Long): Int {
        val removed = records.remove(id) != null
        if (removed) flow.value = records.values.toList()
        return if (removed) 1 else 0
    }

    override suspend fun deleteAll(): Int {
        val count = records.size
        records.clear()
        flow.value = emptyList()
        return count
    }

    override suspend fun getRecordCount(): Int = records.size
}
