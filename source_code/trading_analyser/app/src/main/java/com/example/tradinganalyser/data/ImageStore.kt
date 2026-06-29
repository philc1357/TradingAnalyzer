package com.example.tradinganalyser.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

// ============================================================
// Speichert das vom Nutzer gewählte Screenshot-Bild dauerhaft im
// app-internen Verzeichnis und liefert dessen absoluten Pfad zurück.
// Dadurch bleibt das Bild verfügbar, auch wenn die Galerie-URI
// später nicht mehr gültig ist. Alles bleibt lokal auf dem Gerät.
// ============================================================
class ImageStore(private val context: Context) {

    private val dir: File by lazy {
        File(context.filesDir, "images").apply { mkdirs() }
    }

    // Inhalt der übergebenen URI in eine eindeutige Datei kopieren.
    // Gibt den absoluten Dateipfad zurück oder null bei Fehler.
    fun persist(uri: Uri): String? {
        return try {
            val target = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
