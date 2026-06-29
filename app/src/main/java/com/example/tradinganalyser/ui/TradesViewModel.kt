package com.example.tradinganalyser.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradinganalyser.data.AppDatabase
import com.example.tradinganalyser.data.Trade
import com.example.tradinganalyser.data.TradeRepository
import com.example.tradinganalyser.data.TradeStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ============================================================
// ViewModel für die Trade-Übersicht.
// Liefert die reaktive Trade-Liste, Kennzahlen und den kumulierten
// PnL-Verlauf für das Diagramm.
// ============================================================
class TradesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TradeRepository(AppDatabase.get(app).tradeDao())

    val trades = repository.tradesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeStats())

    // Kumulierter Verlauf der realisierten Punkte (chronologisch) für das Diagramm.
    val cumulative = repository.tradesFlow
        .map { TradeRepository.cumulativePoints(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
