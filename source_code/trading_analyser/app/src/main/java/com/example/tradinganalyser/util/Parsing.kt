package com.example.tradinganalyser.util

// ============================================================
// Hilfsfunktionen zur robusten Verarbeitung von Nutzer- und KI-Eingaben.
// Portierung von levin/app.py:_to_float und analyzer.py:_clean_json_text.
// Eingaben werden grundsätzlich als nicht vertrauenswürdig behandelt.
// ============================================================

// ------------------------------------------------------------
// Wandelt eine Texteingabe robust in ein Double um und toleriert
// deutsche wie englische Zahlenformate (z. B. "25.021,8" -> 25021.8).
// Liefert null bei leerem/ungültigem Wert.
// ------------------------------------------------------------
fun toDoubleDeEn(value: String?): Double? {
    if (value == null) return null
    var text = value.trim()
    if (text.isEmpty() || text.equals("null", ignoreCase = true)) return null

    // Führendes Vorzeichen merken, dann alle Nicht-Zahl-Zeichen entfernen
    // (z. B. %, $, €, Buchstaben, Leerzeichen). So werden Werte wie
    // "+2,5 %" oder "4.252,38 €" korrekt verarbeitet.
    val negative = text.startsWith("-")
    text = text.replace(Regex("[^0-9,.]"), "")
    if (text.isEmpty()) return null

    text = if (text.contains(",") && text.contains(".")) {
        // Punkt = Tausendertrenner, Komma = Dezimaltrenner
        text.replace(".", "").replace(",", ".")
    } else {
        text.replace(",", ".")
    }
    val parsed = text.toDoubleOrNull() ?: return null
    return if (negative) -parsed else parsed
}

// ------------------------------------------------------------
// Bereinigt die rohe Modellantwort, bevor sie als JSON geparst wird.
// Robust gegenüber Reasoning-Modellen, die zusätzlichen Text liefern:
//   1. <think>...</think>-Denkblöcke entfernen,
//   2. Markdown-Code-Fences (```) entfernen,
//   3. das eigentliche JSON-Objekt (erstes "{" bis letztes "}")
//      herausschneiden, falls noch Fließtext drumherum steht.
// ------------------------------------------------------------
fun cleanJsonText(text: String): String {
    // 1. Reasoning-/Denkblöcke entfernen
    var cleaned = text.replace(
        Regex("(?is)<think>.*?</think>"),
        ""
    ).trim()

    // 2. Code-Fences zeilenweise entfernen
    if (cleaned.startsWith("```")) {
        cleaned = cleaned.lineSequence()
            .filterNot { it.trim().startsWith("```") }
            .joinToString("\n")
            .trim()
    }

    // 3. Falls noch Text vor/nach dem JSON steht: Objekt ausschneiden
    val start = cleaned.indexOf('{')
    val end = cleaned.lastIndexOf('}')
    return if (start in 0 until end) cleaned.substring(start, end + 1) else cleaned
}

// Normalisiert die Handelsrichtung auf "long" oder "short".
fun normalizeRichtung(value: String?): String? {
    val v = value?.trim()?.lowercase()
    return when (v) {
        "short" -> "short"
        "long" -> "long"
        null, "", "null" -> null
        else -> v
    }
}
