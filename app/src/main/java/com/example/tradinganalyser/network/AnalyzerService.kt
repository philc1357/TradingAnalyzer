package com.example.tradinganalyser.network

import android.util.Base64
import com.example.tradinganalyser.BuildConfig
import com.example.tradinganalyser.util.cleanJsonText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// ============================================================
// KI-Auswertung der Trading-Screenshots.
// Kapselt den NVIDIA-Vision-Aufruf (≙ levin/analyzer.py). Die App
// ruft die Cloud-API direkt vom Gerät auf; ein eigener Server
// existiert nicht. Liefert die extrahierten Rohwerte als Strings,
// die im Formular angezeigt und vom Nutzer korrigiert werden.
// ============================================================

// Vom Modell extrahierte Rohdaten (noch ungeprüfte Strings).
data class AnalysisResult(
    val symbol: String?,
    val richtung: String?,
    val einstiegszeit: String?,
    val ausstiegszeit: String?,
    val einstiegskurs: String?,
    val stop_loss: String?,
    val take_profit: String?,
    val sonstiges: String?,
)

class AnalyzerService {

    // Read-Timeout großzügig, da Reasoning-Modelle deutlich länger
    // brauchen (das Modell "denkt" vor der eigentlichen Antwort).
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    // ------------------------------------------------------------
    // Bild an das NVIDIA-Vision-Modell schicken und die JSON-Antwort
    // parsen. Wirft IllegalStateException mit klarer Meldung bei
    // Netzwerk-/API-/Parse-Fehlern, damit die UI sie anzeigen kann.
    // ------------------------------------------------------------
    suspend fun analyze(imageBytes: ByteArray, apiKey: String): AnalysisResult =
        withContext(Dispatchers.IO) {
            val imageB64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Nachrichten-Inhalt: Prompt-Text + eingebettetes Bild als Data-URL.
            val content = JSONArray()
                .put(JSONObject().put("type", "text").put("text", PROMPT_TEXT))
                .put(
                    JSONObject()
                        .put("type", "image_url")
                        .put(
                            "image_url",
                            JSONObject().put("url", "data:image/jpeg;base64,$imageB64")
                        )
                )

            // KI-Parameter stammen aus dem Modell-Flavor (BuildConfig),
            // damit jede APK ihr eigenes Modell + passende Werte nutzt.
            val payload = JSONObject()
                .put("model", BuildConfig.AI_MODEL)
                .put("temperature", BuildConfig.AI_TEMPERATURE.toDouble())
                .put("top_p", BuildConfig.AI_TOP_P.toDouble())
                .put("max_tokens", BuildConfig.AI_MAX_TOKENS)
                .put(
                    "messages",
                    JSONArray().put(
                        JSONObject().put("role", "user").put("content", content)
                    )
                )

            // Reasoning-Modelle: erweitertes "Thinking" aktivieren und das
            // Denk-Budget setzen (NVIDIA-NIM-Schalter auf dem
            // OpenAI-kompatiblen Endpunkt).
            if (BuildConfig.AI_REASONING) {
                payload.put(
                    "chat_template_kwargs",
                    JSONObject().put("enable_thinking", true)
                )
                payload.put("reasoning_budget", BuildConfig.AI_REASONING_BUDGET)
            }

            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    if (response.code == 401 || response.code == 403) {
                        throw IllegalStateException(
                            "API-Fehler ${response.code}: Der NVIDIA-API-Key ist ungültig, " +
                                "abgelaufen oder falsch eingetragen. Bitte in den Einstellungen " +
                                "prüfen.\n$body"
                        )
                    }
                    throw IllegalStateException("API-Fehler ${response.code}: $body")
                }
                parseResponse(body)
            }
        }

    // Antwort-Hülle der Chat-Completions-API auspacken und das
    // eigentliche JSON der Trade-Daten herauslösen.
    private fun parseResponse(body: String): AnalysisResult {
        val raw = try {
            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            throw IllegalStateException("Unerwartetes API-Format: $body")
        }

        val json = try {
            JSONObject(cleanJsonText(raw))
        } catch (e: Exception) {
            throw IllegalStateException("KI-Antwort ist kein gültiges JSON: $raw")
        }

        fun str(key: String): String? {
            if (!json.has(key) || json.isNull(key)) return null
            val v = json.getString(key).trim()
            return if (v.isEmpty() || v.equals("null", ignoreCase = true)) null else v
        }

        return AnalysisResult(
            symbol = str("symbol"),
            richtung = str("richtung"),
            einstiegszeit = str("einstiegszeit"),
            ausstiegszeit = str("ausstiegszeit"),
            einstiegskurs = str("einstiegskurs"),
            stop_loss = str("stop_loss"),
            take_profit = str("take_profit"),
            sonstiges = str("sonstiges"),
        )
    }

    companion object {
        private const val BASE_URL = "https://integrate.api.nvidia.com/v1"
        private val JSON_MEDIA = "application/json".toMediaType()

        // Prompt mit klaren Fundort-Regeln für TradingView-Screenshots
        // mit eingezeichnetem Positions-Tool (rotes Risiko-/grünes Ziel-Feld).
        private val PROMPT_TEXT = """
            Analysiere diesen TradingView-Trading-Screenshot mit eingezeichnetem
            Positions-Tool und extrahiere die Werte nach folgenden Regeln.

            Fundorte der Kurszahlen an der rechten Preisachse:
            - Die ROT hinterlegte Kurszahl ist der Stop-Loss (stop_loss).
            - Die GRÜN hinterlegte Kurszahl ist der Take-Profit (take_profit).
            - Die GRAU hinterlegte Kurszahl ist der Einstiegskurs (einstiegskurs).
            - Ignoriere die farbige Box mit dem AKTUELLEN Kurs (meist oben, oft mit
              einer Countdown-Uhrzeit dahinter) – das ist NICHT der Einstieg.

            Richtung (richtung):
            - "long", wenn der Take-Profit ÜBER dem Einstiegskurs liegt,
            - sonst "short".

            Zeiten (einstiegszeit, ausstiegszeit):
            - Stehen unten zwei BLAUE Zeit-Felder (z. B. "Fr 19 Jun '26 08:45"), dann
              ist das linke die Einstiegszeit, das rechte die Ausstiegszeit.
            - Fehlen die blauen Felder, schätze die Zeiten anhand der linken bzw.
              rechten Kante des Positions-Felds gegenüber der unteren Zeitachse.
            - Gib beide Zeiten IMMER im Format "yyyy-MM-dd HH:mm" zurück, sonst null.

            Antworte AUSSCHLIESSLICH mit validem JSON, keine Erklaerungen, kein Markdown:

            {
              "symbol": "gehandeltes Asset/Symbol, z. B. XAUUSD",
              "richtung": "long oder short",
              "einstiegszeit": "yyyy-MM-dd HH:mm oder null",
              "ausstiegszeit": "yyyy-MM-dd HH:mm oder null",
              "einstiegskurs": "graue Kurszahl als Zahl",
              "stop_loss": "rote Kurszahl als Zahl",
              "take_profit": "gruene Kurszahl als Zahl",
              "sonstiges": "weitere relevante Infos, sonst null"
            }
        """.trimIndent()
    }
}
