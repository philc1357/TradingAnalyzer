package com.example.tradinganalyser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tradinganalyser.ui.CaptureScreen
import com.example.tradinganalyser.ui.SettingsScreen
import com.example.tradinganalyser.ui.TradesScreen
import com.example.tradinganalyser.ui.theme.TradingAnalyzerTheme

// ============================================================
// Einstiegspunkt der App. Setzt das Compose-Theme und die
// Bottom-Navigation mit den drei Bereichen Erfassen/Trades/Einstellungen.
// ============================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradingAnalyzerTheme {
                AppRoot()
            }
        }
    }
}

// Verfügbare Tabs der unteren Navigationsleiste.
private enum class Tab(val label: String, val icon: ImageVector) {
    Capture("Erfassen", Icons.Filled.AddCircle),
    Trades("Trades", Icons.AutoMirrored.Filled.List),
    Settings("Einstellungen", Icons.Filled.Settings),
}

@Composable
private fun AppRoot() {
    var current by remember { mutableStateOf(Tab.Capture) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab,
                        onClick = { current = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        // Inhalt je nach gewähltem Tab.
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (current) {
            Tab.Capture -> androidx.compose.foundation.layout.Box(contentModifier) {
                CaptureScreen(
                    onSaved = { current = Tab.Trades },
                    onGoToSettings = { current = Tab.Settings },
                )
            }
            Tab.Trades -> androidx.compose.foundation.layout.Box(contentModifier) {
                TradesScreen()
            }
            Tab.Settings -> androidx.compose.foundation.layout.Box(contentModifier) {
                SettingsScreen()
            }
        }
    }
}
