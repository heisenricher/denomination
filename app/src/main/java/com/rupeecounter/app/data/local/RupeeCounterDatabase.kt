package com.rupeecounter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ActiveSessionEntity::class,
        SavedRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RupeeCounterDatabase : RoomDatabase() {

    abstract fun activeSessionDao(): ActiveSessionDao
    abstract fun savedRecordDao(): SavedRecordDao

    companion object {
        private const val DATABASE_NAME = "rupee_counter.db"

        @Volatile
        private var INSTANCE: RupeeCounterDatabase? = null

        fun getInstance(context: Context): RupeeCounterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): RupeeCounterDatabase {
            return Room.databaseBuilder(
                context,
                RupeeCounterDatabase::class.java,
                DATABASE_NAME
            )
                // Add migrations if schema evolves in future versions
                .addMigrations(*ALL_MIGRATIONS)
                .build()
        }

        // Migration registry for future schema updates
        private val ALL_MIGRATIONS = arrayOf<Migration>()
    }
}
