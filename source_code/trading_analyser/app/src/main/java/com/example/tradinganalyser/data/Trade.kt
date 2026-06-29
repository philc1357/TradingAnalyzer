package com.example.tradinganalyser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================
// Datenmodell eines Trades (Room-Entity).
// Punkte-/CRV-Modell: gespeichert werden nur die Rohwerte aus dem
// Screenshot. Punkte, CRV und Haltedauer werden bei der Anzeige
// live berechnet (siehe util/TradeMath.kt).
// ============================================================
@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String? = null,
    val richtung: String? = null,              // "long" / "short"
    val einstiegszeit: String? = null,         // normalisiert "yyyy-MM-dd HH:mm"
    val ausstiegszeit: String? = null,
    val einstiegskurs: Double? = null,         // grau hinterlegte Kurszahl
    val stop_loss: Double? = null,             // rote Kurszahl
    val take_profit: Double? = null,           // grüne Kurszahl
    val resultat: String? = null,              // "offen" / "gewonnen" / "verloren"
    val sonstiges: String? = null,
    val bild_pfad: String? = null,
    val analysiert_am: String? = null,
)

// ============================================================
// Aggregierte Kennzahlen über alle Trades.
// Werden in Kotlin aus der Trade-Liste berechnet (TradeRepository).
// ============================================================
data class TradeStats(
    val anzahl: Int = 0,
    val gewinner: Int = 0,
    val verlierer: Int = 0,
    val winRate: Double = 0.0,
    val schnittCrv: Double = 0.0,
    val summePunkte: Double = 0.0,
)
