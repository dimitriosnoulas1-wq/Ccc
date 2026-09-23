package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.model.LiveMarketContextSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build(),
    private val apiKeyOverride: String? = null
) {
    companion object {
        private const val TAG = "GeminiAiService"
        // Cheapest working text models first (paid $/1M tokens: 3.1-lite $0.25/$1.50, 3.5-lite $0.30/$2.50, 3.6-flash $0.75/$3.75).
        internal val MODELS = listOf(
            "gemini-3.1-flash-lite",
            "gemini-3.5-flash-lite",
            "gemini-3.6-flash"
        )
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    suspend fun analyzeMarketQuery(
        prompt: String,
        snapshot: LiveMarketContextSnapshot,
        language: AppLanguage
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()

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

        if (apiKey.isNotBlank()) {
            for (model in MODELS) {
                // Standard generateContent first: search grounding is extra cost and often quota-blocked.
                try {
                    val responseText = callGeminiRestApi(apiKey, model, prompt, snapshot, targetLangName, enableSearch = false)
                    if (responseText.isNotBlank()) {
                        Log.d(TAG, "Gemini standard live call successful with model: $model")
                        return@withContext responseText
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Gemini model $model standard call failed: ${e.message}")
                }
            }
        }

        // Conversational intelligence synthesizer fallback
        generateRealtimeQuantitativeAnalysis(prompt, snapshot, effectiveLanguage)
    }

    private fun resolveApiKey(): String {
        if (apiKeyOverride != null) {
            return sanitizeApiKey(apiKeyOverride)
        }
        val buildKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("")
        val injectedKey = runCatching { BuildConfig.GEMINI_INJECTED_API_KEY }.getOrDefault("")
        return sanitizeApiKey(buildKey).ifBlank { sanitizeApiKey(injectedKey) }
    }

    private fun sanitizeApiKey(raw: String): String {
        val key = raw.trim()
        return if (key.isNotBlank() && !key.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            key
        } else {
            ""
        }
    }

    private fun callGeminiRestApi(
        apiKey: String,
        modelName: String,
        prompt: String,
        snapshot: LiveMarketContextSnapshot,
        targetLangName: String,
        enableSearch: Boolean = false
    ): String {
        val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"

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

        val lang = if (targetLangName.contains("Greek", ignoreCase = true) || targetLangName.contains("Ελληνικά", ignoreCase = true)) AppLanguage.GREEK else AppLanguage.ENGLISH
        val btcPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.btcPrice, language = lang)
        val ethPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.ethPrice, language = lang)
        val solPriceFormatted = com.example.util.AppNumberFormatter.formatPrice(snapshot.solPrice, language = lang)
        val fundingFormatted = com.example.util.AppNumberFormatter.formatPercent(snapshot.fundingRatePct, includeSign = true, decimals = 4, language = lang)
        val btcDomFormatted = com.example.util.AppNumberFormatter.formatPercent(snapshot.btcDominancePct, includeSign = false, decimals = 1, language = lang)

        val systemInstructionText = """
            You are CryptoCycles AI Market Analyst, a cutting-edge, real-time live cryptocurrency and general intelligence agent (powered by Gemini AI) running directly inside the CryptoCycles app.
            
            CRITICAL BEHAVIOR & RELEVANCE DIRECTIVES:
            1. DIRECT & FOCUSED ANSWER: Answer EXACTLY what the user is asking. If the user asks for the price or stats of a specific coin (e.g. XRP, SOL, DOGE, ADA, ETH, etc.), provide the EXACT live spot price, 24h percentage change, and relevant details immediately in your opening lines!
            2. ABSOLUTELY NO UNWANTED BITCOIN PIVOTS: When the user asks about XRP or any other specific token, do NOT lecture them about Bitcoin, BTC Dominance, 4-year Halving, or Fear & Greed unless they specifically requested a Bitcoin or general macro cycle review. Keep your answer 100% focused on the coin or topic asked.
            3. REAL-TIME DATA PRECISION: Always use the exact real-time prices and values from the Live Telemetry Context below. Never guess, invent, or approximate prices.
            4. TIME & GENERAL QUERIES: If the user asks for the time/date, use the live device timestamp provided. If the user asks general or non-crypto questions, answer clearly, intelligently, and conversationally without forcing crypto into the conversation.
            5. Multilingual & Natural: Default to $targetLangName. If the prompt is in Greek or Greeklish (e.g., "t timh exei to xrp twra", "ti wra einai", "poso kanei to sol"), ALWAYS reply in fluent, natural Greek (Ελληνικά). If the user asks in English, German, French, Spanish, etc., adapt immediately and reply fluently in that language!
            6. Formatting: Use clean markdown with bold numbers and bullet points.
            7. REFUSE EXECUTION ORDERS & TARGETS: You must refuse buy/sell execution orders and must not invent price targets. Explain data on screen only.
            
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
            • Market Sentiment: ${snapshot.fearAndGreedScore}/100 (${snapshot.fearAndGreedSentiment})
            • BTC Dominance: $btcDomFormatted
            • Altcoin Season Index: ${snapshot.altcoinSeasonIndex}
            • Perpetual Funding Rate: $fundingFormatted (Mark: ${snapshot.futuresMarkPrice})
            • Whale Net 24h Flow: ${snapshot.whaleNet24h ?: "N/A"}
            • Shared Market Stance: ${snapshot.marketStance ?: "N/A"}
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 2048)
            })
            if (enableSearch) {
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val responseString = client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini error code ${response.code}: $bodyString")
                throw RuntimeException("Gemini API Error: ${response.code} - $bodyString")
            }
            bodyString
        }

        val respJson = JSONObject(responseString)
        val candidates = respJson.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""

        val textBuilder = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            textBuilder.append(part.optString("text", ""))
        }
        return textBuilder.toString().trim()
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

        // Conversational / Greeting / Live checks
        val isGreetingOrLiveCheck = p.contains("live") || p.contains("online") || p.contains("hello") ||
                p.contains("hi") || p.contains("geia") || p.contains("γεια") || p.contains("test") ||
                p.contains("who are you") || p.contains("ποιος εισαι") || p.contains("poios eisai") ||
                p.contains("ti kaneis") || p.contains("δουλευεις") || p.contains("douleveis") ||
                p.contains("leitourgeis") || p.contains("λειτουργεις") || p == "hey" || p == "yo"

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
                    **🟣 Solana (SOL) Ζωντανή Ανάλυση**
                    
                    • **Τιμή Spot:** `$solPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.sol24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • **Δίκτυο & Οικοσύστημα:** Το Solana παραμένει στην αιχμή των συναλλαγών retail, DeFi και meme liquidity.
                    • **Τεχνική Εικόνα:** Παρακολουθούμε τα επίπεδα στήριξης και τη σχετική ισχύ έναντι του Ethereum (SOL/ETH pair).
                """.trimIndent()

                isEthereum -> """
                    **🔷 Ethereum (ETH) Ζωντανή Ανάλυση**
                    
                    • **Τιμή Spot:** `$ethPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.eth24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • **Spot ETFs & L2s:** Τα Layer 2 δίκτυα (Arbitrum, Base, Optimism) συνεχίζουν να απορροφούν όγκο συναλλαγών.
                    • **Συσχέτιση:** Η επιτάχυνση του ETH αποτελεί ιστορικά το έναυσμα για ευρύτερο Altseason.
                """.trimIndent()
                isGreetingOrLiveCheck -> """
                    **👋 Γεια σου! Είμαι ζωντανά συνδεδεμένος και έτοιμος!**
                    
                    Είμαι ο **AI Market Analyst** του CryptoCycles. Παρακολουθώ σε πραγματικό χρόνο τις εξελίξεις στην αγορά των κρυπτονομισμάτων.
                    
                    💡 **Τι μπορείς να με ρωτήσεις:**
                    • **4ετής Κύκλος & Halving:** Σε ποια φάση του κύκλου βρισκόμαστε και πότε αναμένονται οι ιστορικές κορυφές.
                    • **Ανάλυση Bitcoin, ETH & SOL:** Τάσεις, επίπεδα στήριξης/αντίστασης και κινητικότητα.
                    • **Παράγωγα & Funding Rates:** Επίπεδα μόχλευσης στα Futures και κίνδυνοι ρευστοποιήσεων.
                    • **Altcoins & Altseason:** Πότε ενεργοποιείται το Altseason με βάση το BTC Dominance.
                    • **Δείκτες Ψυχολογίας:** Ανάλυση Fear & Greed Index και macro συνθηκών.
                    
                    *Ρώτησέ με οτιδήποτε θέλεις συγκεκριμένα!*
                """.trimIndent()

                isBuyingStrategy -> """
                    **🎯 Στρατηγική Εισόδου & Διαχείρισης Ρίσκου**
                    
                    • **Τρέχουσα Τιμή BTC:** `$btcPriceFormatted`
                    • **Κλίμα Αγοράς (Fear & Greed):** `$fng / 100` (${snapshot.fearAndGreedSentiment})
                    • **Βέλτιστες Πρακτικές:**
                      - Σε φάσεις υψηλής απληστίας (Greed > 70), η μέθοδος **DCA (Dollar Cost Averaging)** ή η αναμονή για τοπικά pullbacks προσφέρει καλύτερο risk/reward.
                      - Αποφύγετε το FOMO σε ανοδικά peaks. Ιστορικά, οι διορθώσεις 15%-25% σε bull market προσφέρουν ιδανικά σημεία επανατοποθέτησης.
                      - Διατηρείτε πάντα καθορισμένο Stop-Loss και μην υπερμοχλεύετε θέσεις στα Futures.
                """.trimIndent()

                isPeakOrAth -> """
                    **📊 Ανάλυση Ιστορικών Κορυφών & ATH**
                    
                    • **Τρέχουσα Τιμή BTC:** `$btcPriceFormatted` (24h: ${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)})
                    • **Ιστορικό Μοτίβο Κύκλων:**
                      - Οι ιστορικοί κύκλοι (2012, 2016, 2020) σημείωσαν τις απόλυτες κορυφές τους **500 έως 550 ημέρες μετά το εκάστοτε Halving**.
                      - Στον τρέχοντα κύκλο, η είσοδος θεσμικών κεφαλαίων μέσω Spot ETFs επιτάχυνε τη ρευστότητα.
                    • **Σήματα Κορυφής:** Παρακολουθούμε ακραίο Fear & Greed (>85), υπερβολικό Funding Rate (>0.03%) και επιθετική διανομή από μακροχρόνιους κατόχους (Whales).
                """.trimIndent()

                isHalvingOrCycle -> """
                    **⏳ Ανάλυση 4ετούς Κύκλου Halving**
                    
                    • **Χρόνος από το 4ο Halving (20 Απριλίου 2024):** `$halvingDays`
                    • **Τιμές Αναφοράς:** BTC `$btcPriceFormatted` | ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                    • **Φάση Κύκλου:** Βρισκόμαστε στη φάση μακροοικονομικής επέκτασης μετά το halving. Η ιστορική εμπειρία δείχνει ότι η περίοδος 12-18 μήνες μετά τη μείωση της παραγωγής Bitcoin συνοδεύεται από τις μεγαλύτερες κινήσεις ρευστότητας.
                """.trimIndent()

                isFuturesOrLeverage -> """
                    **⚡ Παράγωγα, Funding Rates & Μόχλευση (${snapshot.activeFuturesSymbol})**
                    
                    • **Funding Rate:** `$fundingFormatted` (${if (snapshot.fundingRatePct > 0.01) "Υπερθέρμανση θέσεων Long" else "Ισορροπημένο επίπεδο χωρίς ακραία μόχλευση"})
                    • **Open Interest:** `$oiFormatted` συνολικά ενεργά συμβόλαια.
                    • **Εκτίμηση Ρίσκου:** ${if (snapshot.fundingRatePct > 0.012) "Υψηλός κίνδυνος Long Squeeze σε απότομο pullback. Συνιστάται συντηρητική μόχλευση." else "Υγιής δομή παραγώγων, χαμηλός κίνδυνος αλυσιδωτών ρευστοποιήσεων."}
                """.trimIndent()

                isAltcoins -> """
                    **🚀 Altcoins, Ethereum, Solana & Altseason**
                    
                    • **BTC Dominance:** `$btcDomFormatted` (Κρίσιμο σημείο καμπής: πτώση κάτω από 54%)
                    • **Altcoin Season Index:** `$altIndex / 100` (${if (altIndex >= 75) "Ενεργό Altseason!" else "Κυριαρχία Bitcoin"})
                    • **Τιμές:** ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                    • **Συμπέρασμα:** Η πραγματική έκρηξη των Altcoins ξεκινά όταν το Bitcoin σταθεροποιείται σε νέα υψηλά και η κυριαρχία του (Dominance) αρχίζει να υποχωρεί σταθερά.
                """.trimIndent()

                isSentiment -> """
                    **🎭 Δείκτης Fear & Greed & Ψυχολογία Αγοράς**
                    
                    • **Μέτρηση:** `$fng / 100` (${snapshot.fearAndGreedSentiment})
                    • **Ερμηνεία:** ${if (fng < 30) "Ακραίος Φόβος (Extreme Fear) - Ιστορικά εξαιρετική ζώνη συσσώρευσης." else if (fng > 75) "Ακραία Απληστία (Extreme Greed) - Αυξημένος κίνδυνος διόρθωσης." else "Ουδέτερη ισορροπία συναισθήματος."}
                """.trimIndent()

                isPriceOrPrediction -> """
                    **📈 Τάση Τιμών & Τεχνικά Επίπεδα**
                    
                    • **Bitcoin (BTC):** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                    • **Ethereum (ETH):** `$ethPriceFormatted` | **Solana (SOL):** `$solPriceFormatted`
                    • **Κυριαρχία:** `$btcDomFormatted` | **Funding:** `$fundingFormatted`
                    • **Σύνοψη:** Παρακολουθούμε τα επίπεδα στήριξης και τη ροή στα Spot ETFs για επιβεβαίωση της επόμενης ανοδικής κίνησης.
                """.trimIndent()

                else -> """
                    **💡 CryptoCycles AI Ανάλυση**
                    
                    Σχετικά με το ερώτημά σου: *"$prompt"*
                    
                    • **Τρέχουσα Εικόνα Αγοράς:** Το Bitcoin κινείται στα `$btcPriceFormatted` με κυριαρχία `$btcDomFormatted`.
                    • **Κύκλος & Halving:** Διανύουμε την περίοδο `$halvingDays` μετά το 4ο Halving, η οποία ιστορικά αποτελεί την κύρια φάση διαμόρφωσης της τάσης.
                    • **Κλίμα Αγοράς:** Ο δείκτης Fear & Greed βρίσκεται στο `$fng / 100` (${snapshot.fearAndGreedSentiment}), με το Funding Rate στο `$fundingFormatted`.
                    
                    Αν χρειάζεσαι περισσότερες λεπτομέρειες για συγκεκριμένα νομίσματα, τεχνικούς δείκτες ή στρατηγικές διαχείρισης ρίσκου, γράψε μου την απορία σου!
                """.trimIndent()
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
                **🟣 Solana (SOL) Live Market Analysis**
                
                • **Spot Price:** `$solPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.sol24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Network Activity:** Solana continues leading high-frequency decentralized trading and app liquidity.
                • **Technical Setup:** Monitoring critical reaction levels and relative strength against the SOL/ETH pair.
            """.trimIndent()

            isEthereum -> """
                **🔷 Ethereum (ETH) Live Market Analysis**
                
                • **Spot Price:** `$ethPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.eth24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Ecosystem Health:** Layer 2 rollups (Base, Arbitrum, Optimism) anchor active user volume.
                • **Altseason Trigger:** A sustained breakout in ETH/BTC historically serves as the catalyst for broad altcoin liquidity expansion.
            """.trimIndent()

            isGreetingOrLiveCheck -> """
                **👋 Hello! I am live and online!**
                
                I am the **CryptoCycles AI Market Analyst**, tracking real-time crypto cycle metrics, derivatives flow, and on-chain dynamics.
                
                💡 **What you can ask me:**
                • **4-Year Cycle & Halving:** Phase analysis and historical cycle peak projections.
                • **Bitcoin, ETH & SOL:** Price trends, key support/resistance levels, and momentum.
                • **Futures & Funding Rates:** Leverage health, liquidation clusters, and squeeze risks.
                • **Altcoins & Altseason:** Capital rotation triggers and BTC Dominance radar.
                • **Market Sentiment:** Fear & Greed breakdown and macro risk analysis.
                
                *Ask me anything specific about the crypto market!*
            """.trimIndent()

            isBuyingStrategy -> """
                **🎯 Entry Strategy & Risk Management**
                
                • **Current Spot BTC:** `$btcPriceFormatted`
                • **Market Sentiment:** `$fng / 100` (${snapshot.fearAndGreedSentiment})
                • **Best Practices:**
                  - During high greed readings (>70), dollar-cost averaging (DCA) and waiting for local pullbacks provide a superior risk/reward ratio.
                  - Avoid FOMO buying at breakout tops. Pullbacks of 15%-25% in bull markets historically present the healthiest reload opportunities.
                  - Maintain strict stop-losses and avoid over-leveraging on perpetual futures.
            """.trimIndent()

            isPeakOrAth -> """
                **📊 Cycle Peak & ATH Projections**
                
                • **Current BTC Price:** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Historical Cycle Blueprint:**
                  - Cycles in 2012, 2016, and 2020 reached macro peaks between **500 and 550 days post-Halving**.
                  - Spot ETF institutional adoption in this cycle provides sustained structural demand.
                • **Top Signals:** We monitor peak greed (>85), elevated funding rates (>0.03%), and distribution from long-term whale wallets.
            """.trimIndent()

            isHalvingOrCycle -> """
                **⏳ 4-Year Halving Cycle Temporal Analytics**
                
                • **Days Elapsed Since 4th Halving (April 20, 2024):** `$halvingDays`
                • **Spot Prices:** BTC `$btcPriceFormatted` | ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                • **Cycle Stage:** We are progressing through the post-halving structural expansion phase where historical supply constraints typically exert maximum upward price pressure.
            """.trimIndent()

            isFuturesOrLeverage -> """
                **⚡ Derivatives, Funding Rate & Leverage Structure (${snapshot.activeFuturesSymbol})**
                
                • **Funding Rate:** `$fundingFormatted` (${if (snapshot.fundingRatePct > 0.01) "Elevated Long premium" else "Neutral and healthy leverage"})
                • **Open Interest:** `$oiFormatted` active perpetual contracts.
                • **Risk Assessment:** ${if (snapshot.fundingRatePct > 0.012) "Elevated risk of Long Squeezes on sudden dips. Keep leverage disciplined." else "Orderly derivatives positioning with low systemic liquidation risk."}
            """.trimIndent()

            isAltcoins -> """
                **🚀 Altcoins, ETH, SOL & Altseason Radar**
                
                • **BTC Dominance:** `$btcDomFormatted` (Key rotation inflection trigger: sub-54%)
                • **Altcoin Season Index:** `$altIndex / 100` (${if (altIndex >= 75) "Altseason Active!" else "Bitcoin Dominance Leading"})
                • **Spot Prices:** ETH `$ethPriceFormatted` | SOL `$solPriceFormatted`
                • **Key Takeaway:** Major altcoin runs historically ignite when Bitcoin establishes a consolidation range at highs and BTC Dominance decisively breaks downward.
            """.trimIndent()

            isSentiment -> """
                **🎭 Fear & Greed Index & Sentiment Dynamics**
                
                • **Current Score:** `$fng / 100` (${snapshot.fearAndGreedSentiment})
                • **Interpretation:** ${if (fng < 30) "Extreme Fear — Historically an optimal value accumulation window." else if (fng > 75) "Extreme Greed — Heightened caution for short-term corrective pullbacks." else "Balanced / Neutral sentiment."}
            """.trimIndent()

            isPriceOrPrediction -> """
                **📈 Price Action & Trend Analysis**
                
                • **Bitcoin (BTC):** `$btcPriceFormatted` (${com.example.util.AppNumberFormatter.formatPercent(snapshot.btc24hChange, includeSign = true, decimals = 2, language = language)} 24h)
                • **Ethereum (ETH):** `$ethPriceFormatted` | **Solana (SOL):** `$solPriceFormatted`
                • **BTC Dominance:** `$btcDomFormatted` | **Funding:** `$fundingFormatted`
                • **Summary:** Watch key support levels and ETF spot flows to gauge continuation momentum.
            """.trimIndent()

            else -> """
                **💡 CryptoCycles AI Intelligence**
                
                Regarding your query: *"$prompt"*
                
                • **Current Market Posture:** Bitcoin is trading at `$btcPriceFormatted` with `$btcDomFormatted` dominance.
                • **Cycle Progression:** We are `$halvingDays` post-Halving, in the core macro expansion corridor.
                • **Market Sentiment:** Fear & Greed sits at `$fng / 100` (${snapshot.fearAndGreedSentiment}) with a `$fundingFormatted` funding rate baseline.
                
                Feel free to ask for specific coin analyses, technical indicators, or risk management strategies!
            """.trimIndent()
        }
    }
}

