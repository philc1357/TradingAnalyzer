package com.example.tradinganalyser.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tradinganalyser.network.AnalysisResult
import com.example.tradinganalyser.ui.theme.CardColor
import com.example.tradinganalyser.ui.theme.MutedColor
import com.example.tradinganalyser.ui.theme.NegColor
import com.example.tradinganalyser.util.crv
import com.example.tradinganalyser.util.haltedauer
import com.example.tradinganalyser.util.risikopunkte
import com.example.tradinganalyser.util.toDoubleDeEn
import com.example.tradinganalyser.util.zielpunkte

// ============================================================
// Erfassen-Bildschirm:
// 1. Screenshot wählen  2. KI-Analyse (SL/TP/Einstieg/Zeiten)
// 3. Daten prüfen, Resultat wählen & speichern.
// Punkte/CRV/Haltedauer werden live aus den Eingaben berechnet.
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onSaved: () -> Unit,
    onGoToSettings: () -> Unit,
    viewModel: CaptureViewModel = viewModel(),
) {
    val state = viewModel.state

    var previewUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Editierbare Formularfelder.
    var symbol by remember { mutableStateOf("") }
    var richtung by remember { mutableStateOf("long") }
    var einstiegszeit by remember { mutableStateOf("") }
    var ausstiegszeit by remember { mutableStateOf("") }
    var einstiegskurs by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    var resultat by remember { mutableStateOf("offen") }
    var sonstiges by remember { mutableStateOf("") }

    // Formular mit den KI-Ergebnissen befüllen, sobald sie vorliegen.
    LaunchedEffect(state) {
        if (state is CaptureState.Ready) {
            val r: AnalysisResult = state.result
            symbol = r.symbol.orEmpty()
            richtung = if (r.richtung?.lowercase() == "short") "short" else "long"
            einstiegszeit = r.einstiegszeit.orEmpty()
            ausstiegszeit = r.ausstiegszeit.orEmpty()
            einstiegskurs = r.einstiegskurs.orEmpty()
            stopLoss = r.stop_loss.orEmpty()
            takeProfit = r.take_profit.orEmpty()
            sonstiges = r.sonstiges.orEmpty()
            resultat = "offen"
        }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            previewUri = uri
            viewModel.analyze(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Trade-Screenshot auswerten", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // --- Schritt 1: Bild wählen / Analyse ---
        Card(colors = CardDefaults.cardColors(containerColor = CardColor)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Screenshot hochladen", fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = state !is CaptureState.Loading,
                ) {
                    Text(if (state is CaptureState.Loading) "Wird ausgewertet …" else "Bild wählen & analysieren")
                }

                previewUri?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Vorschau",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }

                when (state) {
                    is CaptureState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.height(20.dp))
                        Text("  Bild wird ausgewertet …", color = MutedColor)
                    }
                    is CaptureState.Error -> {
                        Text(state.message, color = NegColor)
                        if (!viewModel.hasApiKey()) {
                            Button(onClick = onGoToSettings) { Text("Zu den Einstellungen") }
                        }
                    }
                    else -> {}
                }
            }
        }

        // --- Schritt 2: Daten prüfen & speichern ---
        if (state is CaptureState.Ready) {
            Card(colors = CardDefaults.cardColors(containerColor = CardColor)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Daten prüfen & speichern", fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(symbol, { symbol = it }, label = { Text("Symbol") }, modifier = Modifier.fillMaxWidth())

                    Text("Richtung", color = MutedColor, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(richtung == "long", { richtung = "long" }, label = { Text("long") })
                        FilterChip(richtung == "short", { richtung = "short" }, label = { Text("short") })
                    }

                    OutlinedTextField(einstiegskurs, { einstiegskurs = it }, label = { Text("Einstiegskurs (grau)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(stopLoss, { stopLoss = it }, label = { Text("Stop-Loss (rot)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(takeProfit, { takeProfit = it }, label = { Text("Take-Profit (grün)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(einstiegszeit, { einstiegszeit = it }, label = { Text("Einstiegszeit (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(ausstiegszeit, { ausstiegszeit = it }, label = { Text("Ausstiegszeit (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth())

                    Text("Resultat", color = MutedColor, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(resultat == "offen", { resultat = "offen" }, label = { Text("offen") })
                        FilterChip(resultat == "gewonnen", { resultat = "gewonnen" }, label = { Text("gewonnen") })
                        FilterChip(resultat == "verloren", { resultat = "verloren" }, label = { Text("verloren") })
                    }

                    OutlinedTextField(sonstiges, { sonstiges = it }, label = { Text("Sonstiges") }, modifier = Modifier.fillMaxWidth())

                    // Live-Berechnung der abgeleiteten Werte.
                    val ein = toDoubleDeEn(einstiegskurs)
                    val sl = toDoubleDeEn(stopLoss)
                    val tp = toDoubleDeEn(takeProfit)
                    val ziel = zielpunkte(ein, tp)
                    val risiko = risikopunkte(ein, sl)
                    val crvWert = crv(ziel, risiko)
                    val dauer = haltedauer(einstiegszeit, ausstiegszeit)

                    BerechneteWerte(ziel, risiko, crvWert, dauer)

                    Button(
                        onClick = {
                            viewModel.save(
                                symbol, richtung, einstiegszeit, ausstiegszeit,
                                einstiegskurs, stopLoss, takeProfit, resultat, sonstiges,
                            ) {
                                previewUri = null
                                onSaved()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Trade speichern")
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// Anzeige der automatisch berechneten Kennzahlen (read-only).
// ------------------------------------------------------------
@Composable
private fun BerechneteWerte(ziel: Double?, risiko: Double?, crvWert: Double?, dauer: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Berechnet", color = MutedColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("Zielpunkte: ${ziel ?: "—"}", color = MutedColor, fontSize = 13.sp)
        Text("Risikopunkte: ${risiko ?: "—"}", color = MutedColor, fontSize = 13.sp)
        Text("CRV: ${crvWert?.let { "1 : $it" } ?: "—"}", color = MutedColor, fontSize = 13.sp)
        Text("Haltedauer: ${dauer ?: "—"}", color = MutedColor, fontSize = 13.sp)
    }
}
