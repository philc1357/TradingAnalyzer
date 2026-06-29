package com.example.tradinganalyser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ============================================================
// Datenzugriffs-Objekt (DAO) für Trades.
// Alle Queries laufen parametrisiert über Room (Bind-Parameter);
// es werden niemals Nutzereingaben in SQL-Strings eingesetzt.
// ============================================================
@Dao
interface TradeDao {

    // Einen Trade speichern; liefert die neue Zeilen-ID.
    @Insert
    suspend fun insert(trade: Trade): Long

    // Alle Trades als reaktiver Stream, neueste zuerst (für die Übersicht).
    @Query("SELECT * FROM trades ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Trade>>
}
