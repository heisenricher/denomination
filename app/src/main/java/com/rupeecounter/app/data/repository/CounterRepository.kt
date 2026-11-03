package com.rupeecounter.app.data.repository

import com.rupeecounter.app.data.local.ActiveSessionDao
import com.rupeecounter.app.data.local.ActiveSessionEntity
import com.rupeecounter.app.data.local.SavedRecordDao
import com.rupeecounter.app.data.local.SavedRecordEntity
import com.rupeecounter.app.data.model.CalculationResult
import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface CounterRepository {
    fun observeActiveCounts(): Flow<Map<Denomination, Long>>
    suspend fun getActiveCounts(): Map<Denomination, Long>
    suspend fun updateCount(denomination: Denomination, count: Long)
    suspend fun resetActiveCounts()
    suspend fun saveActiveSession(counts: Map<Denomination, Long>)

    fun observeSavedRecords(): Flow<List<SavedRecordEntity>>
    suspend fun getSavedRecordById(id: Long): SavedRecordEntity?
    suspend fun saveCurrentAsRecord(remarks: String?): Long
    suspend fun deleteSavedRecord(id: Long): Boolean

    fun parseCountsJson(json: String): Map<Denomination, Long>
    fun serializeCounts(counts: Map<Denomination, Long>): String
}

class CounterRepositoryImpl(
    private val activeSessionDao: ActiveSessionDao,
    private val savedRecordDao: SavedRecordDao,
    private val calculateTotalUseCase: CalculateTotalUseCase = CalculateTotalUseCase(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CounterRepository {

    override fun observeActiveCounts(): Flow<Map<Denomination, Long>> {
        return activeSessionDao.observeAllActiveCounts().map { entities ->
            val map = mutableMapOf<Denomination, Long>()
            // Default all to 0
            for (denom in Denomination.entries) {
                map[denom] = 0L
            }
            // Populate from DB
            for (entity in entities) {
                val denom = Denomination.fromKey(entity.denominationKey)
                if (denom != null) {
                    map[denom] = entity.count
                }
            }
            map
        }.flowOn(ioDispatcher)
    }

    override suspend fun getActiveCounts(): Map<Denomination, Long> = withContext(ioDispatcher) {
        val entities = activeSessionDao.getAllActiveCounts()
        val map = mutableMapOf<Denomination, Long>()
        for (denom in Denomination.entries) {
            map[denom] = 0L
        }
        for (entity in entities) {
            val denom = Denomination.fromKey(entity.denominationKey)
            if (denom != null) {
                map[denom] = entity.count
            }
        }
        map
    }

    override suspend fun updateCount(denomination: Denomination, count: Long) = withContext(ioDispatcher) {
        val safeCount = count.coerceAtLeast(0L)
        activeSessionDao.upsertCount(
            ActiveSessionEntity(
                denominationKey = denomination.key,
                count = safeCount,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun resetActiveCounts() = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val zeroList = Denomination.entries.map { denom ->
            ActiveSessionEntity(
                denominationKey = denom.key,
                count = 0L,
                updatedAt = now
            )
        }
        activeSessionDao.upsertAll(zeroList)
    }

    override suspend fun saveActiveSession(counts: Map<Denomination, Long>) = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val entities = counts.map { (denom, count) ->
            ActiveSessionEntity(
                denominationKey = denom.key,
                count = count.coerceAtLeast(0L),
                updatedAt = now
            )
        }
        activeSessionDao.upsertAll(entities)
    }

    override fun observeSavedRecords(): Flow<List<SavedRecordEntity>> {
        return savedRecordDao.observeAllRecords().flowOn(ioDispatcher)
    }

    override suspend fun getSavedRecordById(id: Long): SavedRecordEntity? = withContext(ioDispatcher) {
        savedRecordDao.getRecordById(id)
    }

    override suspend fun saveCurrentAsRecord(remarks: String?): Long = withContext(ioDispatcher) {
        val counts = getActiveCounts()
        val calculation = calculateTotalUseCase.execute(counts)
        val countsJson = serializeCounts(counts)

        val record = SavedRecordEntity(
            timestamp = System.currentTimeMillis(),
            grandTotal = calculation.grandTotal,
            totalNotesCount = calculation.totalNotesCount,
            totalCoinsCount = calculation.totalCoinsCount,
            totalPieces = calculation.totalPieces,
            countsJson = countsJson,
            remarks = remarks?.trim()?.ifEmpty { null }
        )

        savedRecordDao.insertRecord(record)
    }

    override suspend fun deleteSavedRecord(id: Long): Boolean = withContext(ioDispatcher) {
        savedRecordDao.deleteRecordById(id) > 0
    }

    override fun serializeCounts(counts: Map<Denomination, Long>): String {
        val entries = counts.filter { it.value > 0L }
            .map { "\"${it.key.key}\":${it.value}" }
        return "{" + entries.joinToString(",") + "}"
    }

    override fun parseCountsJson(json: String): Map<Denomination, Long> {
        val map = mutableMapOf<Denomination, Long>()
        for (denom in Denomination.entries) {
            map[denom] = 0L
        }
        if (json.isBlank()) return map

        val regex = "\"([A-Z0-9_]+)\"\\s*:\\s*([0-9]+)".toRegex()
        for (match in regex.findAll(json)) {
            val (key, countStr) = match.destructured
            val denom = Denomination.fromKey(key)
            if (denom != null) {
                map[denom] = countStr.toLongOrNull() ?: 0L
            }
        }
        return map
    }
}
