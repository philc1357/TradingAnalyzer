package com.example.tradinganalyser.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// ============================================================
// Lokale, verschlüsselte Ablage der App-Einstellungen.
// Speichert ausschließlich den NVIDIA-API-Key – dieser wird vom
// Nutzer eingegeben und NICHT in der App hartkodiert.
// ============================================================
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var apiKey: String
        get() = prefs.getString(KEY_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API, value.trim()).apply()

    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    companion object {
        private const val KEY_API = "nvidia_api_key"
    }
}
