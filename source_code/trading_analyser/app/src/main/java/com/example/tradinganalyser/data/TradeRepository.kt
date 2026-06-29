package com.example.tradinganalyser.data

import com.example.tradinganalyser.util.crv
import com.example.tradinganalyser.util.realisiertePunkte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// Repository: bündelt den DB-Zugriff und berechnet die Kennzahlen.
// Kapselt die Datenschicht von den ViewModels ab.
// ============================================================
class TradeRepository(private val dao: TradeDao) {

    // Reaktive Liste aller Trades (neueste zuerst).
    val tradesFlow: Flow<List<Trade>> = dao.getAllFlow()

    // Reaktive Kennzahlen, abgeleitet aus der Trade-Liste.
    val statsFlow: Flow<TradeStats> = tradesFlow.map { computeStats(it) }

    // Trade speichern und dabei den Analyse-Zeitstempel setzen (falls leer).
    suspend fun save(trade: Trade): Long {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(Date())
        return dao.insert(trade.copy(analysiert_am = trade.analysiert_am ?: now))
    }

    companion object {
        // ----------------------------------------------------
        // Kennzahlen über alle Trades berechnen:
        // Anzahl, Gewinner/Verlierer, Win-Rate, durchschnittliches CRV
        // und Summe der realisierten Punkte.
        // ----------------------------------------------------
        fun computeStats(trades: List<Trade>): TradeStats {
            val anzahl = trades.size
            val gewinner = trades.count { it.resultat?.lowercase() == "gewonnen" }
            val verlierer = trades.count { it.resultat?.lowercase() == "verloren" }
            val abgeschlossen = gewinner + verlierer
            val winRate = if (abgeschlossen > 0) (gewinner.toDouble() / abgeschlossen) * 100.0 else 0.0

            val crvWerte = trades.mapNotNull { it.crv() }
            val schnittCrv = if (crvWerte.isNotEmpty()) crvWerte.sum() / crvWerte.size else 0.0

            return TradeStats(
                anzahl = anzahl,
                gewinner = gewinner,
                verlierer = verlierer,
                winRate = round1(winRate),
                schnittCrv = round2(schnittCrv),
                summePunkte = round2(realisiertePunkteSumme(trades)),
            )
        }

        // Summe der realisierten Punkte über alle Trades (+Ziel / -Risiko).
        private fun realisiertePunkteSumme(trades: List<Trade>): Double =
            trades.mapNotNull { it.realisiertePunkte() }.sum()

        // Kumulierter Verlauf der realisierten Punkte (chronologisch).
        // 'trades' kommt neueste-zuerst herein, daher umgekehrt durchlaufen.
        fun cumulativePoints(trades: List<Trade>): List<Double> {
            var summe = 0.0
            return trades.reversed().map {
                summe += it.realisiertePunkte() ?: 0.0
                round2(summe)
            }
        }

        private fun round2(v: Double) = Math.round(v * 100.0) / 100.0
        private fun round1(v: Double) = Math.round(v * 10.0) / 10.0
    }
}
