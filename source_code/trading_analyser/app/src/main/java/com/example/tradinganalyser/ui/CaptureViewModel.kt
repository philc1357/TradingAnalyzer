package com.example.tradinganalyser.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradinganalyser.data.AppDatabase
import com.example.tradinganalyser.data.ImageStore
import com.example.tradinganalyser.data.SettingsStore
import com.example.tradinganalyser.data.Trade
import com.example.tradinganalyser.data.TradeRepository
import com.example.tradinganalyser.network.AnalysisResult
import com.example.tradinganalyser.network.AnalyzerService
import com.example.tradinganalyser.util.normalizeRichtung
import com.example.tradinganalyser.util.toDoubleDeEn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================
// ViewModel für den Erfassen-Bildschirm.
// Steuert: Bild auswählen → KI-Analyse → Speichern.
// Kapselt Netzwerk-/DB-Zugriffe von der UI ab.
// ============================================================

// Zustand der Analyse-Phase.
sealed interface CaptureState {
    data object Idle : CaptureState
    data object Loading : CaptureState
    data class Ready(val result: AnalysisResult) : CaptureState
    data class Error(val message: String) : CaptureState
}

class CaptureViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TradeRepository(AppDatabase.get(app).tradeDao())
    private val settings = SettingsStore(app)
    private val imageStore = ImageStore(app)
    private val analyzer = AnalyzerService()

    var state by mutableStateOf<CaptureState>(CaptureState.Idle)
        private set

    // Pfad des aktuell gespeicherten Bildes (wird mit dem Trade verknüpft).
    var bildPfad by mutableStateOf<String?>(null)
        private set

    fun hasApiKey(): Boolean = settings.hasApiKey()

    // ------------------------------------------------------------
    // Gewähltes Bild lokal sichern und an die KI schicken.
    // ------------------------------------------------------------
    fun analyze(uri: Uri) {
        if (!settings.hasApiKey()) {
            state = CaptureState.Error("Kein API-Key hinterlegt. Bitte in den Einstellungen eintragen.")
            return
        }
        state = CaptureState.Loading
        viewModelScope.launch {
            try {
                // Bild lokal persistieren und Bytes für die Analyse lesen.
                val context = getApplication<Application>()
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("Bild konnte nicht gelesen werden.")

                bildPfad = imageStore.persist(uri)

                val result = analyzer.analyze(bytes, settings.apiKey)
                state = CaptureState.Ready(result)
            } catch (e: Exception) {
                state = CaptureState.Error(e.message ?: "Auswertung fehlgeschlagen.")
            }
        }
    }

    // ------------------------------------------------------------
    // Vom Nutzer geprüfte Formularwerte casten/validieren und
    // als Trade lokal speichern. Es werden nur die Rohwerte abgelegt;
    // Punkte/CRV/Haltedauer werden bei der Anzeige berechnet.
    // ------------------------------------------------------------
    fun save(
        symbol: String,
        richtung: String,
        einstiegszeit: String,
        ausstiegszeit: String,
        einstiegskurs: String,
        stopLoss: String,
        takeProfit: String,
        resultat: String,
        sonstiges: String,
        onSaved: () -> Unit,
    ) {
        val trade = Trade(
            symbol = symbol.trim().ifBlank { null },
            richtung = normalizeRichtung(richtung),
            einstiegszeit = einstiegszeit.trim().ifBlank { null },
            ausstiegszeit = ausstiegszeit.trim().ifBlank { null },
            einstiegskurs = toDoubleDeEn(einstiegskurs),
            stop_loss = toDoubleDeEn(stopLoss),
            take_profit = toDoubleDeEn(takeProfit),
            resultat = resultat.trim().lowercase().ifBlank { "offen" },
            sonstiges = sonstiges.trim().ifBlank { null },
            bild_pfad = bildPfad,
        )
        viewModelScope.launch {
            repository.save(trade)
            reset()
            onSaved()
        }
    }

    // Bildschirm nach dem Speichern zurücksetzen.
    fun reset() {
        state = CaptureState.Idle
        bildPfad = null
    }
}
