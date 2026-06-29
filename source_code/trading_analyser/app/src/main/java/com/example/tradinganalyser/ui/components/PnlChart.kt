package com.example.tradinganalyser.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tradinganalyser.ui.theme.AccentColor
import com.example.tradinganalyser.ui.theme.BorderColor

// ============================================================
// Liniendiagramm des kumulierten Gewinn/Verlusts, direkt mit
// Compose-Canvas gezeichnet (ersetzt Chart.js aus der Referenz).
// Skaliert automatisch auf den Min-/Max-Bereich der Werte.
// ============================================================
@Composable
fun PnlChart(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (values.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val padding = 8f

        val minV = minOf(values.min(), 0.0)
        val maxV = maxOf(values.max(), 0.0)
        val range = (maxV - minV).takeIf { it != 0.0 } ?: 1.0

        // Hilfsfunktion: Datenpunkt-Index/Wert in Pixelkoordinaten umrechnen.
        fun pointX(i: Int): Float {
            if (values.size == 1) return w / 2f
            return padding + (w - 2 * padding) * (i.toFloat() / (values.size - 1))
        }
        fun pointY(v: Double): Float {
            val norm = (v - minV) / range
            return (h - padding) - (h - 2 * padding) * norm.toFloat()
        }

        // Nulllinie als Orientierung zeichnen.
        val zeroY = pointY(0.0)
        drawLine(
            color = BorderColor,
            start = Offset(padding, zeroY),
            end = Offset(w - padding, zeroY),
            strokeWidth = 1f,
        )

        // Verlaufslinie zeichnen.
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = pointX(i)
            val y = pointY(v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = AccentColor,
            style = Stroke(width = 4f),
        )
    }
}
