package com.example.tradinganalyser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ============================================================
// Lokale SQLite-Datenbank (Room). Speichert alle Trades dauerhaft
// auf dem Gerät – es gibt keinen Server. Singleton-Zugriff über get().
// ============================================================
@Database(entities = [Trade::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trades.db"
                )
                    // Schema-Wechsel auf das Punkte-/CRV-Modell: alte Test-Daten
                    // dürfen verworfen werden (App ist noch im Aufbau).
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
