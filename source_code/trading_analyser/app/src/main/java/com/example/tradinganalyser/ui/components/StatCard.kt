package com.example.tradinganalyser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradinganalyser.ui.theme.BorderColor
import com.example.tradinganalyser.ui.theme.CardColor
import com.example.tradinganalyser.ui.theme.MutedColor
import com.example.tradinganalyser.ui.theme.TextColor

// ============================================================
// Kennzahlen-Kachel für die Übersicht (≙ .stat-card aus style.css).
// ============================================================
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextColor,
) {
    Column(
        modifier = modifier
            .background(CardColor, RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = MutedColor,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
