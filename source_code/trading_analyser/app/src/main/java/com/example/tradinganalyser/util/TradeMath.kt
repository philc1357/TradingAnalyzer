package com.example.tradinganalyser.util

import com.example.tradinganalyser.data.Trade
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// ============================================================
// Handels-Berechnungen rund um das Punkte-/CRV-Modell.
// Reine Funktionen ohne Seiteneffekte: aus den gespeicherten
// Rohwerten (Einstieg, Stop-Loss, Take-Profit, Zeiten, Resultat)
// werden Punkte, Chancen-Risiko-Verhältnis und Haltedauer abgeleitet.
// ============================================================

// Zielpunkte = Abstand Einstieg <-> Take-Profit.
fun zielpunkte(einstieg: Double?, takeProfit: Double?): Double? {
    if (einstieg == null || takeProfit == null) return null
    return round2(abs(takeProfit - einstieg))
}

// Risikopunkte = Abstand Einstieg <-> Stop-Loss.
fun risikopunkte(einstieg: Double?, stopLoss: Double?): Double? {
    if (einstieg == null || stopLoss == null) return null
    return round2(abs(einstieg - stopLoss))
}

// Chancen-Risiko-Verhältnis = Zielpunkte / Risikopunkte.
// Division durch 0 wird abgefangen (-> null).
fun crv(ziel: Double?, risiko: Double?): Double? {
    if (ziel == null || risiko == null || risiko == 0.0) return null
    return round2(ziel / risiko)
}

// Tatsächlich erzielte Punkte je nach Resultat:
// gewonnen -> +Zielpunkte, verloren -> -Risikopunkte, offen -> null.
fun realisiertePunkte(resultat: String?, ziel: Double?, risiko: Double?): Double? {
    return when (resultat?.lowercase()) {
        "gewonnen" -> ziel
        "verloren" -> risiko?.let { -it }
        else -> null
    }
}

// Haltedauer aus Ein-/Ausstiegszeit ("yyyy-MM-dd HH:mm") als "Xh Ym".
// Liefert null, wenn eine Zeit fehlt oder nicht parsebar ist.
fun haltedauer(einstiegszeit: String?, ausstiegszeit: String?): String? {
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY)
    val start = einstiegszeit?.trim()?.takeIf { it.isNotEmpty() }
        ?.let { runCatching { fmt.parse(it) }.getOrNull() } ?: return null
    val end = ausstiegszeit?.trim()?.takeIf { it.isNotEmpty() }
        ?.let { runCatching { fmt.parse(it) }.getOrNull() } ?: return null

    val diffMin = ((end.time - start.time) / 60000L)
    if (diffMin < 0) return null
    val stunden = diffMin / 60
    val minuten = diffMin % 60
    return "${stunden}h ${minuten}m"
}

// Bequeme Ableitungen direkt aus einem Trade-Objekt (für die Liste).
fun Trade.zielpunkte() = zielpunkte(einstiegskurs, take_profit)
fun Trade.risikopunkte() = risikopunkte(einstiegskurs, stop_loss)
fun Trade.crv() = crv(zielpunkte(), risikopunkte())
fun Trade.realisiertePunkte() = realisiertePunkte(resultat, zielpunkte(), risikopunkte())
fun Trade.haltedauer() = haltedauer(einstiegszeit, ausstiegszeit)

private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0
