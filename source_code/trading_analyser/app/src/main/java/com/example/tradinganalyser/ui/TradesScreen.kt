package com.example.tradinganalyser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradinganalyser.data.Trade
import com.example.tradinganalyser.ui.components.PnlChart
import com.example.tradinganalyser.ui.components.StatCard
import com.example.tradinganalyser.ui.theme.CardColor
import com.example.tradinganalyser.ui.theme.MutedColor
import com.example.tradinganalyser.ui.theme.NegColor
import com.example.tradinganalyser.ui.theme.PosColor
import com.example.tradinganalyser.ui.theme.TextColor
import com.example.tradinganalyser.util.crv
import com.example.tradinganalyser.util.haltedauer
import com.example.tradinganalyser.util.zielpunkte

// ============================================================
// Übersicht aller Trades:
// Kennzahlen-Kacheln (Win-Rate, Ø CRV, Punkte …), kumuliertes
// Punkte-Diagramm und die Trade-Liste mit berechneten Werten.
// ============================================================
@Composable
fun TradesScreen(viewModel: TradesViewModel = viewModel()) {
    val trades by viewModel.trades.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val cumulative by viewModel.cumulative.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Vergangene Trades", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // Kennzahlen in zwei Reihen à drei Kacheln.
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("${stats.anzahl}", "Trades", Modifier.weight(1f))
                    StatCard("${stats.winRate}%", "Win-Rate", Modifier.weight(1f))
                    StatCard("1 : ${stats.schnittCrv}", "Ø CRV", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "${stats.summePunkte}", "Punkte ges.", Modifier.weight(1f),
                        valueColor = if (stats.summePunkte >= 0) PosColor else NegColor,
                    )
                    StatCard("${stats.gewinner}", "Gewinner", Modifier.weight(1f), valueColor = PosColor)
                    StatCard("${stats.verlierer}", "Verlierer", Modifier.weight(1f), valueColor = NegColor)
                }
            }
        }

        if (trades.isEmpty()) {
            item {
                Text(
                    "Noch keine Trades gespeichert. Werte deinen ersten Screenshot im Tab „Erfassen“ aus.",
                    color = MutedColor,
                )
            }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CardColor)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Kumulierte Punkte", fontWeight = FontWeight.SemiBold)
                        PnlChart(cumulative, Modifier.padding(top = 12.dp))
                    }
                }
            }

            items(trades) { trade ->
                TradeRow(trade)
            }
        }
    }
}

// ------------------------------------------------------------
// Einzelner Trade-Eintrag mit live berechneten Werten.
// ------------------------------------------------------------
@Composable
private fun TradeRow(trade: Trade) {
    val ziel = trade.zielpunkte()
    val crvWert = trade.crv()
    val dauer = trade.haltedauer()

    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "#${trade.id}  ${trade.symbol ?: "—"}  (${trade.richtung ?: "—"})",
                    color = TextColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = trade.resultat ?: "offen",
                    color = when (trade.resultat?.lowercase()) {
                        "gewonnen" -> PosColor
                        "verloren" -> NegColor
                        else -> MutedColor
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "Einstieg: ${trade.einstiegskurs ?: "—"}   SL: ${trade.stop_loss ?: "—"}   TP: ${trade.take_profit ?: "—"}",
                color = MutedColor,
                fontSize = 13.sp,
            )
            Text(
                "Zielpunkte: ${ziel ?: "—"}   CRV: ${crvWert?.let { "1 : $it" } ?: "—"}",
                color = MutedColor,
                fontSize = 13.sp,
            )
            if (dauer != null) {
                Text("Haltedauer: $dauer", color = MutedColor, fontSize = 12.sp)
            }
            trade.einstiegszeit?.let { Text("Ein: $it", color = MutedColor, fontSize = 12.sp) }
            if (!trade.sonstiges.isNullOrBlank()) {
                Text(trade.sonstiges, color = MutedColor, fontSize = 12.sp)
            }
        }
    }
}
