package com.example.data.network

import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.LiveMarketContextSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build(),
    // null = the build's MarketHub URL; "" = no hub, answer locally (used by tests).
    private val hubBaseUrl: String? = null
) {
    companion object {
        private const val TAG = "GeminiAiService"
    }

    suspend fun analyzeMarketQuery(
        prompt: String,
        snapshot: LiveMarketContextSnapshot,
        language: AppLanguage
    ): String = withContext(Dispatchers.IO) {
        val isGreeklishOrGreek = prompt.any { it in '\u0370'..'\u03ff' } ||
                prompt.contains("pes", ignoreCase = true) ||
                prompt.contains("meres", ignoreCase = true) ||
                prompt.contains("prin", ignoreCase = true) ||
                prompt.contains("htan", ignoreCase = true) ||
                prompt.contains("poso", ignoreCase = true) ||
                prompt.contains("poses", ignoreCase = true) ||
                prompt.contains("pote", ignoreCase = true) ||
                prompt.contains("ti einai", ignoreCase = true) ||
                prompt.contains("poio", ignoreCase = true) ||
                prompt.contains("poia", ignoreCase = true) ||
                prompt.contains("kykl", ignoreCase = true) ||
                prompt.contains("top", ignoreCase = true) ||
                prompt.contains("korifi", ignoreCase = true) ||
                prompt.contains("geia", ignoreCase = true) ||
                prompt.contains("eisai", ignoreCase = true)

        val effectiveLanguage = if (isGreeklishOrGreek && language == AppLanguage.ENGLISH) {
            AppLanguage.GREEK
        } else {
            language
        }

        val targetLangName = when (effectiveLanguage) {
            AppLanguage.ENGLISH -> "English"
            AppLanguage.GREEK -> "Greek (Ελληνικά)"
            AppLanguage.GERMAN -> "German (Deutsch)"
            AppLanguage.FRENCH -> "French (Français)"
            AppLanguage.SPANISH -> "Spanish (Español)"
            AppLanguage.ITALIAN -> "Italian (Italiano)"
        }

        if (isGreetingOnly(prompt)) {
            return@withContext presenceReply(effectiveLanguage)
        }

        // The AI keys live only on the hub; the app never holds them.
        val hub = (hubBaseUrl ?: MarketHub.baseUrl).trim().trimEnd('/')
        if (hub.isNotBlank()) {
            try {
                val responseText = callHub(hub, prompt, snapshot, targetLangName)
                if (responseText.isNotBlank()) return@withContext responseText
            } catch (e: Exception) {
                Log.w(TAG, "Hub AI call failed: ${e.message}")
            }
        }

        // Conversational intelligence synthesizer fallback
        generateRealtimeQuantitativeAnalysis(prompt, snapshot, effectiveLanguage)
    }

    internal fun presenceReply(language: AppLanguage): String = when (language) {
        AppLanguage.GREEK -> "Είμαι εδώ. Τι θα θέλατε να δούμε;"
        AppLanguage.GERMAN -> "Ich bin da. Was möchten Sie uns ansehen?"
        AppLanguage.FRENCH -> "Je suis là. Que souhaitez-vous voir ?"
        AppLanguage.SPANISH -> "Estoy aquí. ¿Qué te gustaría ver?"
        AppLanguage.ITALIAN -> "Sono qui. Cosa vorresti vedere?"
        AppLanguage.ENGLISH -> "I'm here. What would you like to see?"
    }

    internal fun isGreetingOnly(prompt: String): Boolean {
        val p = prompt.lowercase(Locale.ROOT).trim()
            .trim('!', '?', '.', ',', ';', '…')
            .replace(Regex("\\s+"), " ")
        if (p.isEmpty()) return true
        if (hasMarketIntent(p)) return false

        val exact = setOf(
            "hi", "hey", "yo", "hello", "helloo", "hellooo",
            "geia", "geia sou", "geia sas", "γεια", "γεια σου", "γεια σας",
            "test", "testing",
            "hey there", "hi there",
            "eimai edw", "είμαι εδώ", "eisai ekei", "είσαι εκεί",
            "kalimera", "καλημερα", "καλημέρα",
            "kalispera", "καλησπερα", "καλησπέρα"
        )
        if (p in exact) return true

        val presence = listOf(
            "who are you", "ποιος εισαι", "ποιος είσαι", "poios eisai",
            "ti kaneis", "τι κανεις", "τι κάνεις",
            "douleveis", "δουλευεις", "δουλεύεις",
            "leitourgeis", "λειτουργεις", "λειτουργείς",
            "are you live", "are you livee", "are you online", "are you there",
            "eisai live", "είσαι live", "eisai online", "είσαι online"
        )
        if (presence.any { p == it || p.startsWith("$it?") || p.startsWith("$it !") }) return true

        return p.matches(Regex("^(hi+|hey+|hello+|geia+|γεια+)(\\s+(sou|sas|there))?$"))
    }

    private fun hasMarketIntent(normalizedPrompt: String): Boolean {
        val needles = listOf(
            "btc", "eth", "sol", "xrp", "price", "τιμ", "timh", "cycle", "κυκλ", "kykl",
            "halving", "funding", "futures", "whale", "alt", "doge", "ada", "chart",
            "support", "resistance", "buy", "sell", "αγορ", "πουλ"
        )
        return needles.any { normalizedPrompt.contains(it) }
    }

    private fun callHub(
        hub: String,
        prompt: String,
        snapshot: LiveMarketContextSnapshot,
        targetLangName: String
    ): String {
        val jsonBody = JSONObject().apply {
            put("system", buildAnalystSystemInstruction(snapshot, targetLangName))
            put("prompt", prompt.take(2_000))
        }
        val request = Request.Builder()
            .url("$hub/v1/ai")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        return client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("Hub AI error ${response.code}")
            }
            JSONObject(bodyString).optString("text").trim()
        }
    }

    private fun buildAnalystSystemInstruction(
        snapshot: LiveMarketContextSnapshot,
        targetLangName: String
    ): String {
        val timeLine = if (snapshot.currentTimeString.isNotBlank()) {
            "• Current Live Device Timestamp / System Time: ${snapshot.currentTimeString}"
        } else ""

        val targetedCoinSection = if (!snapshot.targetedCoinInfo.isNullOrBlank()) {
            """
            🎯 TARGETED COIN DATA REQUESTED BY USER:
            ${snapshot.targetedCoinInfo}
            """.trimIndent()
        } else ""

        val allMarketPricesLine = if (snapshot.allTrackedCoinsSummary.isNotBlank()) {
            "• Live Spot Prices of Cryptocurrencies in App Database:\n  ${snapshot.allTrackedCoinsSummary}"
        } else if (snapshot.topMarketPricesSummary.isNotBlank()) {
            "• Live Top Market Prices:\n  ${snapshot.topMarketPricesSummary}"
        } else ""

        val lang = if (targetLangName.contains("Greek", ignoreCase = true) || targetLangName.contains("Ελληνικά", ignoreCase = true)) {
            AppLanguage.GREEK
        } else {
            AppLanguage.ENGLISH
        }
        val btcPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.btcPrice, language = lang)
        val ethPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.ethPrice, language = lang)
        val solPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.solPrice, language = lang)
        val fundingFormatted = com.example.util.AppNumberFormatter.formatPercent(snapshot.fundingRatePct, includeSign = true, decimals = 4, language = lang)
        val btcDomFormatted = com.example.util.AppNumberFormatter.formatPercent(snapshot.btcDominancePct, includeSign = false, decimals = 1, language = lang)

        return """
            You are CryptoCycles AI Market Analyst, a real-time cryptocurrency and general intelligence agent running directly inside the CryptoCycles app.
            
            CRITICAL BEHAVIOR & RELEVANCE DIRECTIVES:
            1. DIRECT & FOCUSED ANSWER: Answer EXACTLY what the user is asking. If the user asks for the price or stats of a specific coin (e.g. XRP, SOL, DOGE, ADA, ETH, etc.), provide the EXACT live spot price, 24h percentage change, and relevant details immediately in your opening lines!
            2. ABSOLUTELY NO UNWANTED BITCOIN PIVOTS: When the user asks about XRP or any other specific token, do NOT lecture them about Bitcoin, BTC Dominance, 4-year Halving, or Fear & Greed unless they specifically requested a Bitcoin or general macro cycle review. Keep your answer 100% focused on the coin or topic asked.
            3. REAL-TIME DATA PRECISION: Always use the exact real-time prices and values from the Live Telemetry Context below. Never guess, invent, or approximate prices.
            4. TIME & GENERAL QUERIES: If the user asks for the time/date, use the live device timestamp provided. If the user asks general or non-crypto questions, answer clearly, intelligently, and conversationally without forcing crypto into the conversation.
            5. Multilingual & Natural: Default to $targetLangName. If the prompt is in Greek or Greeklish (e.g., "t timh exei to xrp twra", "ti wra einai", "poso kanei to sol"), ALWAYS reply in fluent, natural Greek (Ελληνικά). If the user asks in English, German, French, Spanish, etc., adapt immediately and reply fluently in that language!
            6. Formatting: Use clean markdown with bold numbers and bullet points. Keep replies short unless the user asked for detail.
            7. REFUSE EXECUTION ORDERS & TARGETS: You must refuse buy/sell execution orders and must not invent price targets. Explain data on screen only. Never recommend a position, entry, exit, DCA, or a climax price.
            8. GREETINGS STAY EMPTY OF ANALYSIS: If the user only says hello / hi / γεια / test / "are you there", reply with ONE clean sentence such as "I'm here. What would you like to see?" or "Είμαι εδώ. Τι θα θέλατε να δούμε;". Do not dump prices, cycle lectures, or a menu of topics.
            9. MISSING NUMBERS: If a snapshot field is blank, N/A, or zero when a live feed is required, say that number is missing. Do not invent it.
            
            Real-Time Live Telemetry Context:
            $timeLine
            $targetedCoinSection
            $allMarketPricesLine
            • Spot BTC: $btcPriceFormatted (${if (snapshot.btc24hChange >= 0) "+" else ""}${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = false, decimals = 2, language = lang)} 24h, ts: ${snapshot.btcTs})
            • Spot ETH: $ethPriceFormatted (${if (snapshot.eth24hChange >= 0) "+" else ""}${com.example.util.AppNumberFormatter.formatPercent(snapshot.eth24hChange, includeSign = false, decimals = 2, language = lang)} 24h, ts: ${snapshot.ethTs})
            • Spot SOL: $solPriceFormatted (${if (snapshot.sol24hChange >= 0) "+" else ""}${com.example.util.AppNumberFormatter.formatPercent(snapshot.sol24hChange, includeSign = false, decimals = 2, language = lang)} 24h, ts: ${snapshot.solTs})
            • Cycle Phase: ${snapshot.cyclePhase ?: "N/A"}
            • Cycle Day / Days Since Halving: Day ${snapshot.cycleDay} (${snapshot.daysSinceHalving} days since halving)
            • Rainbow Band: ${snapshot.rainbowBand ?: "N/A"}
            • 200W SMA Distance: ${snapshot.distance200w ?: "N/A"}
            • Pi Cycle Gap: ${snapshot.piCycleGap ?: "N/A"}
            • Market Sentiment: ${if (snapshot.fearAndGreedScore > 0) "${snapshot.fearAndGreedScore}/100 (${snapshot.fearAndGreedSentiment})" else "missing"}
            • BTC Dominance: $btcDomFormatted
            • Altcoin Season Index: ${snapshot.altcoinSeasonIndex}
            • Perpetual Funding Rate: $fundingFormatted (Mark: ${snapshot.futuresMarkPrice})
            • Whale Net 24h Flow: ${snapshot.whaleNet24h ?: "N/A"}
            • Shared Market Stance: ${snapshot.marketStance ?: "N/A"}
        """.trimIndent()
    }

    private fun generateRealtimeQuantitativeAnalysis(
        prompt: String,
        snapshot: LiveMarketContextSnapshot,
        language: AppLanguage
    ): String {
        val now = System.currentTimeMillis()
        val halvingTimestamp = 1713571200000L // April 20, 2024

        val daysSinceHalvingCalc = ((now - halvingTimestamp) / (1000L * 60 * 60 * 24)).coerceAtLeast(1)

        val btcPriceFormatted = if (snapshot.btcPrice > 0) com.example.util.AppNumberFormatter.formatPrice(snapshot.btcPrice, language = language) else "—"
        val ethPriceFormatted = if (snapshot.ethPrice > 0) com.example.util.AppNumberFormatter.formatPrice(snapshot.ethPrice, language = language) else "—"
        val solPriceFormatted = if (snapshot.solPrice > 0) com.example.util.AppNumberFormatter.formatPrice(snapshot.solPrice, language = language) else "—"
        val fundingFormatted = if (snapshot.futuresMarkPrice > 0.0 || snapshot.openInterestUsd > 0.0 || snapshot.fundingRatePct != 0.0) {
            com.example.util.AppNumberFormatter.formatPercent(snapshot.fundingRatePct, includeSign = true, decimals = 4, language = language)
        } else {
            "—"
        }
        val oiFormatted = if (snapshot.openInterestUsd > 0) com.example.util.AppNumberFormatter.formatCompactCurrency(snapshot.openInterestUsd, language = language) else "—"
        val btcDomFormatted = com.example.util.AppNumberFormatter.formatPercent(snapshot.btcDominancePct, includeSign = false, decimals = 1, language = language)
        val halvingDays = if (snapshot.daysSinceHalving > 0) "${snapshot.daysSinceHalving}d" else "${daysSinceHalvingCalc}d"
        val altIndex = snapshot.altcoinSeasonIndex
        val fng = snapshot.fearAndGreedScore

        val isGreek = language == AppLanguage.GREEK
        val p = prompt.lowercase(Locale.ROOT).trim()

        // Time / Date check
        val isTimeQuery = p.contains("wra") || p.contains("ώρα") || p.contains("time") ||
                p.contains("hmera") || p.contains("ημερα") || p.contains("date") ||
                p.contains("ti mera") || p.contains("ti wra") || p.contains("clock") ||
                p.contains("what time") || p.contains("current time")

        // Specific coins
        val isSolana = p.contains("solana") || p.contains("sol") || p.contains("σολανα")
        val isEthereum = p.contains("ethereum") || p.contains("eth") || p.contains("αιθεριο")

        val isGreetingOrLiveCheck = isGreetingOnly(prompt)

        val isPeakOrAth = p.contains("ath") || p.contains("all time high") || p.contains("peak") ||
                p.contains("top") || p.contains("κορυφ") || p.contains("υψηλο") || p.contains("korifi")

        val isHalvingOrCycle = p.contains("halving") || p.contains("cycle") || p.contains("κυκλ") ||
                p.contains("meres") || p.contains("days") || p.contains("ημερ") || p.contains("4-year") ||
                p.contains("τετραετ")

        val isFuturesOrLeverage = p.contains("funding") || p.contains("futures") || p.contains("leverage") ||
                p.contains("μόχλευση") || p.contains("παράγωγα") || p.contains("oi") || p.contains("liquidation") ||
                p.contains("long") || p.contains("short") || p.contains("ρευστοποι")

        val isAltcoins = p.contains("alt") || p.contains("dominance") || p.contains("altseason")

        val isSentiment = p.contains("fear") || p.contains("greed") || p.contains("sentiment") ||
                p.contains("φοβ") || p.contains("απληστ") || p.contains("synaisthima")

        val isBuyingStrategy = p.contains("buy") || p.contains("sell") || p.contains("αγορασ") ||
                p.contains("πουλησ") || p.contains("agoraso") || p.contains("pouliso") || p.contains("dca") ||
                p.contains("entry") || p.contains("εισοδ") || p.contains("επενδυ")

        val isPriceOrPrediction = p.contains("price") || p.contains("τιμη") || p.contains("timh") ||
                p.contains("target") || p.contains("προβλεψ") || p.contains("predict")

        val timeString = snapshot.currentTimeString.ifBlank {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).format(java.util.Date())
        }

        if (isGreek) {
            if (!snapshot.targetedCoinInfo.isNullOrBlank()) {
                return """
                    **📊 Ζωντανά Στοιχεία Αγοράς**
                    
                    ${snapshot.targetedCoinInfo}
                    
                    *Τα δεδομένα αντλούνται ζωντανά σε πραγματικό χρόνο.*
                """.trimIndent()
            }

            return when {
                isTimeQuery -> """
                    **🕒 Ζωντανή Ώρα & Ημερομηνία Συσκευής**
                    
                    • **Τρέχουσα Ώρα / Timestamp:** `$timeString`
                    • **Κατάσταση AI Agent:** 🟢 Ζωντανός σε πραγματικό χρόνο (Live Streaming Telemetry)
                    
                    Μπορείς να με ρωτήσεις οτιδήποτε για την ώρα, τιμές, δείκτες κύκλων ή ό,τι άλλο επιθυμείς!
                """.trimIndent()

                isSolana -> """
                    **🟣 Solana (SOL) στην οθόνη**
                    
                    • **Τιμή Spot:** `$solPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.sol24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • Διαβάζω μόνο ό,τι είναι στο snapshot. Δεν εφευρίσκω στήριξη, στόχο ή πρόβλεψη.
                """.trimIndent()

                isEthereum -> """
                    **🔷 Ethereum (ETH) στην οθόνη**
                    
                    • **Τιμή Spot:** `$ethPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.eth24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • Διαβάζω μόνο ό,τι είναι στο snapshot. Δεν εφευρίσκω στήριξη, στόχο ή πρόβλεψη.
                """.trimIndent()
                isGreetingOrLiveCheck -> presenceReply(language)

                isBuyingStrategy -> """
                    **Οθόνη, όχι εντολή**
                    
                    • **Τιμή BTC στην οθόνη:** `$btcPriceFormatted`
                    • Διαβάζω μόνο ό,τι φαίνεται. Δεν προτείνω αγορά, πώληση ή στόχο.
                    • ${com.example.util.CycleReadingText.DISCLAIMER_EL}
                """.trimIndent()

                isPeakOrAth -> """
                    **Στην οθόνη, όχι πρόβλεψη κορυφής**
                    
                    • **BTC:** `$btcPriceFormatted` (24h: ${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)})
                    • **Ημέρες από το 4ο halving:** `$halvingDays`
                    • Δεν εφευρίσκω ημέρα κορυφής, ATH-στόχο ή σήμα πώλησης.
                """.trimIndent()

                isHalvingOrCycle -> """
                    **Ρολόι κύκλου στην οθόνη**
                    
                    • **Ημέρες από το 4ο Halving (20 Απριλίου 2024):** `$halvingDays`
                    • **Τιμές:** BTC `$btcPriceFormatted` | ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                    • Καταγραφή ημερών μόνο. Δεν προβλέπω 12-18 μήνες ή κορυφή.
                """.trimIndent()

                isFuturesOrLeverage -> """
                    **Παράγωγα στην οθόνη (${snapshot.activeFuturesSymbol})**
                    
                    • **Funding:** `$fundingFormatted`
                    • **Open Interest:** `$oiFormatted`
                    • Χωρίς συμβουλή μόχλευσης. Αν λείπει αριθμός, λείπει.
                """.trimIndent()

                isAltcoins -> """
                    **Altcoins στην οθόνη**
                    
                    • **BTC Dominance:** `$btcDomFormatted`
                    • **Altcoin Season Index:** ${if (altIndex > 0) "`$altIndex / 100`" else "`—`"}
                    • **Τιμές:** ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                    • Δεν εφευρίσκω σημείο καμπής dominance ούτε προβλέπω altseason.
                """.trimIndent()

                isSentiment -> """
                    **Fear & Greed στην οθόνη**
                    
                    • **Μέτρηση:** ${if (fng > 0) "`$fng / 100` (${snapshot.fearAndGreedSentiment})" else "`—`"}
                    • Καταγραφή μόνο. Δεν είναι εντολή συσσώρευσης ή πώλησης.
                """.trimIndent()

                isPriceOrPrediction -> """
                    **Τιμές στην οθόνη, όχι στόχος**
                    
                    • **Bitcoin (BTC):** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • **Ethereum (ETH):** `$ethPriceFormatted` | **Solana (SOL):** `$solPriceFormatted`
                    • **Κυριαρχία:** `$btcDomFormatted` | **Funding:** `$fundingFormatted`
                    • Δεν εφευρίσκω στήριξη, αντίσταση ή πρόβλεψη.
                """.trimIndent()

                else -> presenceReply(language)
            }
        }

        // English & Default
        if (!snapshot.targetedCoinInfo.isNullOrBlank()) {
            return """
                **📊 Live Market Telemetry**
                
                ${snapshot.targetedCoinInfo}
                
                *Data updated in real-time from active exchange price feeds.*
            """.trimIndent()
        }

        return when {
            isTimeQuery -> """
                **🕒 Live Device Timestamp & System Time**
                
                • **Current Local Time / Timestamp:** `$timeString`
                • **AI Agent Status:** 🟢 Live streaming telemetry connected
                
                Feel free to ask me for current time, token prices, cycle metrics, or any market question!
            """.trimIndent()

            isSolana -> """
                **🟣 Solana (SOL) on screen**
                
                • **Spot Price:** `$solPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.sol24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • I only read this snapshot. I do not invent support, a target, or a forecast.
            """.trimIndent()

            isEthereum -> """
                **🔷 Ethereum (ETH) on screen**
                
                • **Spot Price:** `$ethPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.eth24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • I only read this snapshot. I do not invent support, a target, or a forecast.
            """.trimIndent()

            isGreetingOrLiveCheck -> presenceReply(language)

            isBuyingStrategy -> """
                **On-screen reading only**
                
                • **BTC on screen:** `$btcPriceFormatted`
                • I only read what is on the screen. I do not recommend a buy, a sell, or a target.
                • ${com.example.util.CycleReadingText.DISCLAIMER_EN}
            """.trimIndent()

            isPeakOrAth -> """
                **On-screen reading, not a peak forecast**
                
                • **BTC:** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Days since the 4th halving:** `$halvingDays`
                • I do not invent a peak day, an ATH target, or a sell signal.
            """.trimIndent()

            isHalvingOrCycle -> """
                **Cycle clock on screen**
                
                • **Days since the 4th Halving (April 20, 2024):** `$halvingDays`
                • **Spot prices:** BTC `$btcPriceFormatted` | ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                • Day count only. I do not forecast a 12–18 month window or a top.
            """.trimIndent()

            isFuturesOrLeverage -> """
                **Derivatives on screen (${snapshot.activeFuturesSymbol})**
                
                • **Funding:** `$fundingFormatted`
                • **Open Interest:** `$oiFormatted`
                • No leverage advice. If a number is missing, it is missing.
            """.trimIndent()

            isAltcoins -> """
                **Altcoins on screen**
                
                • **BTC Dominance:** `$btcDomFormatted`
                • **Altcoin Season Index:** ${if (altIndex > 0) "`$altIndex / 100`" else "`—`"}
                • **Spot prices:** ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                • I do not invent a dominance trigger or an altseason call.
            """.trimIndent()

            isSentiment -> """
                **Fear & Greed on screen**
                
                • **Score:** ${if (fng > 0) "`$fng / 100` (${snapshot.fearAndGreedSentiment})" else "`—`"}
                • A reading only. Not an accumulate or sell order.
            """.trimIndent()

            isPriceOrPrediction -> """
                **On-screen prices, not a target**
                
                • **Bitcoin (BTC):** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Ethereum (ETH):** `$ethPriceFormatted` | **Solana (SOL):** `$solPriceFormatted`
                • **BTC Dominance:** `$btcDomFormatted` | **Funding:** `$fundingFormatted`
                • I do not invent support, resistance, or a forecast.
            """.trimIndent()

            else -> presenceReply(language)
        }
    }
}

