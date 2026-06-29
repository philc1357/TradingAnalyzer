package com.example.tradinganalyser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradinganalyser.data.SettingsStore
import com.example.tradinganalyser.ui.theme.AccentColor
import com.example.tradinganalyser.ui.theme.MutedColor

// ============================================================
// Einstellungen: Eingabe und lokale (verschlüsselte) Speicherung
// des NVIDIA-API-Keys. Der Key wird NICHT in der App hartkodiert.
// ============================================================
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }

    var key by remember { mutableStateOf(store.apiKey) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Einstellungen", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Der NVIDIA-API-Key wird ausschließlich lokal und verschlüsselt auf diesem " +
                "Gerät gespeichert und nur für die Screenshot-Analyse verwendet.",
            color = MutedColor,
            fontSize = 13.sp,
        )

        OutlinedTextField(
            value = key,
            onValueChange = { key = it; saved = false },
            label = { Text("NVIDIA-API-Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                store.apiKey = key
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Speichern")
        }

        if (saved) {
            Text("Gespeichert.", color = AccentColor)
        }
    }
}
