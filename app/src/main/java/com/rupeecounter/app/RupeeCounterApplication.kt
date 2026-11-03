package com.rupeecounter.app

import android.app.Application
import com.rupeecounter.app.data.local.RupeeCounterDatabase
import com.rupeecounter.app.data.repository.CounterRepository
import com.rupeecounter.app.data.repository.CounterRepositoryImpl
import com.rupeecounter.app.domain.usecase.CalculateTotalUseCase

class RupeeCounterApplication : Application() {

    lateinit var database: RupeeCounterDatabase
        private set

    lateinit var repository: CounterRepository
        private set

    val calculateTotalUseCase: CalculateTotalUseCase by lazy {
        CalculateTotalUseCase()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = RupeeCounterDatabase.getInstance(this)
        repository = CounterRepositoryImpl(
            activeSessionDao = database.activeSessionDao(),
            savedRecordDao = database.savedRecordDao(),
            calculateTotalUseCase = calculateTotalUseCase
        )
    }

    companion object {
        lateinit var instance: RupeeCounterApplication
            private set
    }
}
