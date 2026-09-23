package com.example.data.repository

import com.example.data.model.CycleDayComparativeReport
import com.example.data.model.HistoricalCrashEvent
import com.example.data.model.HistoricalCycleComparison
import com.example.util.HalvingCycleUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CycleComparativeRepository {

    // Anchor: Bitcoin Bull Market Peak of Current Cycle (06 Oct 2025)
    companion object {
        const val BTC_CYCLE_ATH_TIMESTAMP = 1759708800000L // 06 Oct 2025 00:00 UTC
        const val BTC_CYCLE_ATH_USD = 126500.0
        const val BTC_CYCLE_ATH_DATE = "06 Oct 2025"

        // Historical bottom day offsets post-ATH
        const val BOTTOM_OFFSET_2022 = 376 // 21 Nov 2022 ($15,476)
        const val BOTTOM_OFFSET_2018 = 363 // 15 Dec 2018 ($3,122)
        const val BOTTOM_OFFSET_2015 = 410 // 14 Jan 2015 ($152)
        const val HISTORICAL_AVG_BOTTOM_DAY = 375 // ~365 - 385 days (Almost exactly 1 year post-ATH)
    }

    fun getLiveDaysSinceAth(): Int {
        val now = System.currentTimeMillis()
        val diff = now - BTC_CYCLE_ATH_TIMESTAMP
        return (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    fun getLiveDaysSinceHalving(): Int {
        return HalvingCycleUtils.getDaysSince4thHalving()
    }

    /**
     * Generates a complete comparative report for any chosen day offset post-ATH.
     * If [dayOffset] is null, defaults to today's live offset from Oct 6, 2025 (~332 days).
     */
    fun getComparativeReport(
        dayOffset: Int? = null,
        currentBtcPrice: Double = 58240.0
    ): CycleDayComparativeReport {
        val liveDay = getLiveDaysSinceAth()
        val targetDay = dayOffset ?: liveDay
        val isToday = (targetDay == liveDay)
        val daysSinceHalving = getLiveDaysSinceHalving()
        val daysUntilHistoricalBottom = (HISTORICAL_AVG_BOTTOM_DAY - targetDay)

        val currentDrawdown = ((currentBtcPrice - BTC_CYCLE_ATH_USD) / BTC_CYCLE_ATH_USD) * 100.0

        val cycle2022 = calculateCycle2022ForDay(targetDay)
        val cycle2018 = calculateCycle2018ForDay(targetDay)
        val cycle2015 = calculateCycle2015ForDay(targetDay)

        val comparisons = listOf(
            cycle2022,
            cycle2018,
            cycle2015
        )

        val phase = determineCyclePhase(targetDay)
        val crashesInPeriod = getHistoricalCrashesForDayWindow(targetDay)
        val allEvents = getAllHistoricalCrashEvents()

        val altcoinSummaryEn = getAltcoinSummaryEn(targetDay)
        val altcoinSummaryEl = getAltcoinSummaryEl(targetDay)

        return CycleDayComparativeReport(
            targetDayOffset = targetDay,
            isTodayLive = isToday,
            daysSinceAth = liveDay,
            daysSinceHalving = daysSinceHalving,
            currentBtcPrice = currentBtcPrice,
            currentBtcAthPrice = BTC_CYCLE_ATH_USD,
            currentAthDate = BTC_CYCLE_ATH_DATE,
            currentDrawdownPercent = currentDrawdown,
            daysUntilHistoricalBottom = daysUntilHistoricalBottom,
            historicalBottomWindow = "Days 360 - 390 (Mid Oct - Late Nov)",
            phaseNameEn = phase.first,
            phaseNameEl = phase.second,
            phaseDiagnosisEn = phase.third,
            phaseDiagnosisEl = phase.fourth,
            comparisons = comparisons,
            altcoinComparisonSummaryEn = altcoinSummaryEn,
            altcoinComparisonSummaryEl = altcoinSummaryEl,
            crashesInPeriod = crashesInPeriod,
            allHistoricalCrashTimeline = allEvents
        )
    }

    private fun calculateCycle2022ForDay(day: Int): HistoricalCycleComparison {
        // ATH: 10 Nov 2021 ($68,789)
        // Bottom: 21 Nov 2022 ($15,476) at Day 376 (-77.5%)
        val athTime = 1636502400000L // 10 Nov 2021
        val dateOnDay = formatDateWithOffset(athTime, day)

        val priceOnDay = when {
            day <= 30 -> 56800.0
            day <= 60 -> 42500.0
            day <= 120 -> 38900.0
            day <= 180 -> 29500.0 // Pre-Luna
            day <= 220 -> 19800.0 // 3AC / Celsius plunge
            day <= 300 -> 20200.0 // Summer-Autumn chop
            day in 320..350 -> 19450.0 // Exactly ~Day 330: tight consolidation around $19.4k
            day in 351..376 -> 15800.0 // FTX collapse to cycle bottom ($15,476 on day 376)
            day in 377..430 -> 16800.0 // Post-FTX baseline accumulation
            else -> 23200.0 // 2023 breakout
        }

        val athPrice = 68789.0
        val drawdown = ((priceOnDay - athPrice) / athPrice) * 100.0

        return HistoricalCycleComparison(
            cycleName = "2021 - 2022 Cycle",
            cyclePeriod = "Nov 2021 ➔ Nov 2022",
            athDate = "10 Nov 2021",
            athPriceUsd = athPrice,
            dateOnDay = dateOnDay,
            priceOnDay = priceOnDay,
            drawdownPercentOnDay = drawdown,
            statusSummaryEn = if (day in 320..350) {
                "BTC was consolidating in a false calm at $19,450 (-71.7%), 40 days before the FTX liquidity hole detonated."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "BTC trading at ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr from ATH)."
            },
            statusSummaryEl = if (day in 320..350) {
                "Το BTC συσσωρευόταν σε μια παραπλανητική ηρεμία στα $19.450 (-71,7%), 40 ημέρες πριν σκάσει η τρύπα ρευστότητας της FTX."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "Το BTC διαπραγματευόταν στα ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr από το ATH)."
            },
            altcoinPerformanceEn = if (day in 320..350) {
                "Altcoins had already lost 80-85% post-Luna/3AC. ETH held $1,300, but Solana was about to drop from $35 to $8 during the FTX crash."
            } else {
                "Altcoin drawdowns averaged -75% to -90% across top caps."
            },
            altcoinPerformanceEl = if (day in 320..350) {
                "Τα Altcoins είχαν ήδη χάσει 80-85% μετά τα Luna/3AC. Το ETH κρατούσε τα $1.300, αλλά το Solana επρόκειτο να πέσει από τα $35 στα $8 με το κράχ της FTX."
            } else {
                "Οι απώλειες των altcoins κυμαίνονταν κατά μέσο όρο μεταξύ -75% και -90%."
            },
            bottomDayOffset = BOTTOM_OFFSET_2022,
            bottomDate = "21 Nov 2022",
            bottomPriceUsd = 15476.0,
            bottomMaxDrawdownPercent = -77.5,
            daysRemainingToBottom = (BOTTOM_OFFSET_2022 - day).coerceAtLeast(0)
        )
    }

    private fun calculateCycle2018ForDay(day: Int): HistoricalCycleComparison {
        // ATH: 17 Dec 2017 ($19,783)
        // Bottom: 15 Dec 2018 ($3,122) at Day 363 (-84.2%)
        val athTime = 1513468800000L // 17 Dec 2017
        val dateOnDay = formatDateWithOffset(athTime, day)

        val priceOnDay = when {
            day <= 30 -> 11800.0
            day <= 60 -> 8200.0
            day <= 120 -> 6800.0
            day <= 180 -> 6450.0
            day <= 250 -> 6300.0
            day in 310..340 -> 6320.0 // Exactly ~Day 330: The famous "$6k unbreakable floor"
            day in 341..363 -> 3250.0 // BCH Hash War capitulation to $3,122
            day in 364..420 -> 3600.0 // Bottom accumulation floor
            else -> 4100.0 // 2019 recovery
        }

        val athPrice = 19783.0
        val drawdown = ((priceOnDay - athPrice) / athPrice) * 100.0

        return HistoricalCycleComparison(
            cycleName = "2017 - 2018 Cycle",
            cyclePeriod = "Dec 2017 ➔ Dec 2018",
            athDate = "17 Dec 2017",
            athPriceUsd = athPrice,
            dateOnDay = dateOnDay,
            priceOnDay = priceOnDay,
            drawdownPercentOnDay = drawdown,
            statusSummaryEn = if (day in 310..340) {
                "BTC spent 6 months defending the $6,000-$6,400 floor (-68.1%). Market sentiment believed the bottom was in before the Hash War cracked it."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "BTC trading at ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr from ATH)."
            },
            statusSummaryEl = if (day in 310..340) {
                "Το BTC υπερασπιζόταν επί 6 μήνες τη στήριξη των $6.000-$6.400 (-68,1%). Η αγορά θεωρούσε ότι το bottom είχε ήδη σχηματιστεί πριν τον καταστροφικό Hash War."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "Το BTC διαπραγματευόταν στα ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr από το ATH)."
            },
            altcoinPerformanceEn = if (day in 310..340) {
                "Altcoins were down -88% to -95%. ICO projects were desperately dumping ETH treasuries for operational cash, sending ETH down from $1,400 to $82."
            } else {
                "Altcoins endured relentless bleeding as ICO funding dried up completely."
            },
            altcoinPerformanceEl = if (day in 310..340) {
                "Τα Altcoins είχαν καταγράψει πτώση -88% έως -95%. Τα ICO projects ξεπουλούσαν πανικόβλητα τα αποθεματικά τους σε ETH για ρευστότητα, οδηγώντας το ETH από $1.400 στα $82."
            } else {
                "Τα altcoins βίωσαν αδιάκοπη αιμορραγία καθώς τα κεφάλαια των ICOs εξαντλήθηκαν ολοκληρωτικά."
            },
            bottomDayOffset = BOTTOM_OFFSET_2018,
            bottomDate = "15 Dec 2018",
            bottomPriceUsd = 3122.0,
            bottomMaxDrawdownPercent = -84.2,
            daysRemainingToBottom = (BOTTOM_OFFSET_2018 - day).coerceAtLeast(0)
        )
    }

    private fun calculateCycle2015ForDay(day: Int): HistoricalCycleComparison {
        // ATH: 30 Nov 2013 ($1,163)
        // Bottom: 14 Jan 2015 ($152) at Day 410 (-86.9%)
        val athTime = 1385769600000L // 30 Nov 2013
        val dateOnDay = formatDateWithOffset(athTime, day)

        val priceOnDay = when {
            day <= 45 -> 850.0
            day <= 90 -> 560.0 // Mt. Gox collapse
            day <= 180 -> 460.0
            day <= 250 -> 480.0
            day in 310..350 -> 352.0 // Day ~330
            day in 351..410 -> 190.0 // Bitstamp hack / final flush to $152
            else -> 230.0
        }

        val athPrice = 1163.0
        val drawdown = ((priceOnDay - athPrice) / athPrice) * 100.0

        return HistoricalCycleComparison(
            cycleName = "2013 - 2015 Cycle",
            cyclePeriod = "Nov 2013 ➔ Jan 2015",
            athDate = "30 Nov 2013",
            athPriceUsd = athPrice,
            dateOnDay = dateOnDay,
            priceOnDay = priceOnDay,
            drawdownPercentOnDay = drawdown,
            statusSummaryEn = if (day in 310..350) {
                "BTC was drifting downward at $352 (-69.7%), under continuous sell pressure from Mt. Gox liquidation overhang."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "BTC trading at ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr from ATH)."
            },
            statusSummaryEl = if (day in 310..350) {
                "Το BTC διολίσθαινε πτωτικά στα $352 (-69,7%), υπό συνεχείς πιέσεις πωλήσεων λόγω της εκκαθάρισης του Mt. Gox."
            } else {
                val ddStr = com.example.util.AppNumberFormatter.formatPercent(drawdown, includeSign = true, decimals = 1)
                "Το BTC διαπραγματευόταν στα ${com.example.util.AppNumberFormatter.formatRawPrice(priceOnDay, decimals = 0)} ($ddStr από το ATH)."
            },
            altcoinPerformanceEn = "Litecoin and Peercoin lost 95% of value. No liquidity existed for minor tokens.",
            altcoinPerformanceEl = "Το Litecoin και το Peercoin έχασαν το 95% της αξίας τους. Δεν υπήρχε ρευστότητα για μικρά tokens.",
            bottomDayOffset = BOTTOM_OFFSET_2015,
            bottomDate = "14 Jan 2015",
            bottomPriceUsd = 152.0,
            bottomMaxDrawdownPercent = -86.9,
            daysRemainingToBottom = (BOTTOM_OFFSET_2015 - day).coerceAtLeast(0)
        )
    }

    private fun determineCyclePhase(day: Int): Quadruple<String, String, String, String> {
        return when {
            day < 60 -> Quadruple(
                "Euphoria Unwind & Initial Bull Trap",
                "Αποκλιμάκωση Ευφορίας & Πρώτη Παγίδα Ταύρων",
                "First steep decline followed by a relief bounce. Retail dip-buyers enter too early.",
                "Απότομη πρώτη πτώση που ακολουθείται από αναπήδηση ανακούφισης. Οι μικροεπενδυτές αγοράζουν πρόωρα."
            )
            day in 60..150 -> Quadruple(
                "Denial & Macro Deleveraging",
                "Φάση Άρνησης & Μακροοικονομική Απομόχλευση",
                "Institutional deleveraging, interest rate sensitivity, and breaking of major moving averages.",
                "Θεσμική απομόχλευση, ευαισθησία στα επιτόκια και διάσπαση των βασικών κινητών μέσων όρων."
            )
            day in 151..240 -> Quadruple(
                "Insolvency Cascades & Black Swans",
                "Ντόμινο Χρεοκοπιών & Μαύροι Κύκνοι",
                "Unsustainable yields and over-leveraged hedge funds implode (e.g. Terra/Luna, 3AC, Celsius in 2022).",
                "Κατάρρευση μη βιώσιμων αποδόσεων και υπερχρεωμένων funds (π.χ. Terra/Luna, 3AC, Celsius το 2022)."
            )
            day in 241..325 -> Quadruple(
                "Exhaustion & 'Fake Floor' Chop",
                "Εξάντληση & Ψευδής Πυθμένας (Chop)",
                "Low volatility apathy. Market forms a deceptive floor ($6k in 2018, $19k in 2022) convincing traders the bottom is in.",
                "Χαμηλή μεταβλητότητα και απάθεια. Η αγορά σχηματίζει μια παραπλανητική βάση ($6k το 2018, $19k το 2022) πείθοντας ότι ο πάτος βρέθηκε."
            )
            day in 326..385 -> Quadruple(
                "The 1-Year Bottom Window & Final Capitulation",
                "Το Παράθυρο Πυθμένα 1 Έτους & Τελική Συνθηκολόγηση",
                "CRITICAL HISTORICAL ZONE: Exactly 11-13 months post-ATH. Previous cycles saw final black swans (BCH Hash War Day 335, FTX Day 363) establishing the macro cycle low.",
                "ΚΡΙΣΙΜΗ ΙΣΤΟΡΙΚΗ ΖΩΝΗ: Ακριβώς 11-13 μήνες μετά το ATH. Στους προηγούμενους κύκλους σημειώθηκαν τα τελικά καταλυτικά σοκ (BCH Hash War Ημέρα 335, FTX Ημέρα 363) που διαμόρφωσαν το απόλυτο χαμηλό του κύκλου."
            )
            else -> Quadruple(
                "Post-Bottom Accumulation & Disbelief",
                "Συσσώρευση Μετά τον Πυθμένα & Δυσπιστία",
                "Silent institutional accumulation. Sharp bear-market rally shocks remaining skeptics.",
                "Αθόρυβη θεσμική συσσώρευση. Απότομο ράλι ανάκαμψης που αιφνιδιάζει τους δύσπιστους πωλητές."
            )
        }
    }

    private fun getHistoricalCrashesForDayWindow(day: Int): List<HistoricalCrashEvent> {
        val all = getAllHistoricalCrashEvents()
        return all.filter {
            it.exactDayOffset in (day - 45)..(day + 45) || (day in 320..385 && it.exactDayOffset in 330..385)
        }
    }

    fun getAllHistoricalCrashEvents(): List<HistoricalCrashEvent> {
        return listOf(
            HistoricalCrashEvent(
                id = "bch_hash_war_2018",
                cycleYear = "2018",
                dayOffsetRange = "Day 335 ➔ 363 post-ATH",
                exactDayOffset = 335,
                approxDate = "15 Nov 2018",
                titleEn = "The Bitcoin Cash Hash War & Collapse of the $6,000 Floor",
                titleEl = "Ο Πόλεμος Hash του Bitcoin Cash & Η Κατάρρευση των $6.000",
                severity = "CRITICAL",
                btcDropSummary = "$6,320 ➔ $3,122 (-50.6% in 30 days)",
                altcoinDropSummary = "ETH $210 ➔ $82 (-61%), Altcoins bled another 60-80%",
                whatHappenedEn = "On November 15, 2018 (Day 335 post-ATH), a bitter civil war erupted inside Bitcoin Cash between BCH ABC (Roger Ver) and BCH SV (Craig Wright). Both factions redirected massive SHA-256 hash power away from BTC to wage an expensive hash war.",
                whatHappenedEl = "Στις 15 Νοεμβρίου 2018 (Ημέρα 335 μετά το ATH), ξέσπασε εμφύλιος πόλεμος στο Bitcoin Cash μεταξύ BCH ABC (Roger Ver) και BCH SV (Craig Wright). Και οι δύο πλευρές απέσυραν τεράστια υπολογιστική ισχύ hash από το BTC για να πολεμήσουν με ζημίες.",
                whyItCrashedEn = "1) Miners sold tens of thousands of BTC reserves onto spot order books to subsidize unprofitable mining on the forked chains. 2) The legendary '$6,000 support floor' that held for 6 months shattered, triggering automated stop-loss cascades and margin calls across BitMEX. 3) General panic caused capitulation of long-term holders.",
                whyItCrashedEl = "1) Οι miners πούλησαν δεκάδες χιλιάδες BTC αποθεματικών στα spot βιβλία εντολών για να χρηματοδοτήσουν την ασύμφορη εξόρυξη των διασπασμένων αλυσίδων. 2) Το θρυλικό 'πάτωμα των $6.000' που άντεξε 6 μήνες έσπασε, προκαλώντας αυτόματο ντόμινο stop-loss και ρευστοποιήσεων στο BitMEX. 3) Γενικευμένος πανικός και τελική συνθηκολόγηση μακροχρόνιων επενδυτών.",
                altcoinImpactEn = "ETH collapsed from $210 to a cycle low of $82 (-94% from ATH). Many 2017 ICO tokens went down 99% or shuttered operations entirely.",
                altcoinImpactEl = "Το ETH κατέρρευσε από τα $210 στο χαμηλό των $82 (-94% από το ATH). Πολλά ICO tokens του 2017 έχασαν το 99% της αξίας τους ή έκλεισαν οριστικά.",
                marketPsychologyEn = "Traders who survived the entire bear market gave up in despair at $3,500, believing Bitcoin was heading to zero.",
                marketPsychologyEl = "Επενδυτές που είχαν αντέξει ολόκληρη τη bear market λύγισαν σε απόγνωση στα $3.500, πιστεύοντας ότι το Bitcoin θα μηδενίσει.",
                survivalLessonEn = "The final flush in a bear market happens when the strongest perceived support breaks. That exact capitulation marked the cycle bottom on Day 363 ($3,122).",
                survivalLessonEl = "Το τελικό ξεκαθάρισμα σε μια bear market συμβαίνει όταν σπάει η ισχυρότερη στήριξη. Αυτή η ακριβώς συνθηκολόγηση διαμόρφωσε τον πυθμένα στην Ημέρα 363 ($3.122)."
            ),

            HistoricalCrashEvent(
                id = "ftx_alameda_implosion_2022",
                cycleYear = "2022",
                dayOffsetRange = "Day 363 ➔ 376 post-ATH",
                exactDayOffset = 363,
                approxDate = "08 Nov 2022",
                titleEn = "The FTX & Alameda Research Insolvency Bank Run",
                titleEl = "Η Χρεοκοπία της FTX & Alameda Research (Bank Run)",
                severity = "CRITICAL",
                btcDropSummary = "$20,600 ➔ $15,476 (-25% flush to ultimate bottom)",
                altcoinDropSummary = "Solana $36 ➔ $8 (-78%), FTT wiped 98%, Altcoins down 40-70%",
                whatHappenedEn = "On November 2, 2022, CoinDesk revealed Alameda Research's balance sheet was artificially inflated with billions in illiquid FTT tokens. On November 6, Binance announced it would liquidate its entire FTT holdings. A $6B bank run ensued in 72 hours, revealing an $8 Billion hole in customer deposits.",
                whatHappenedEl = "Στις 2 Νοεμβρίου 2022, το CoinDesk αποκάλυψε ότι ο ισολογισμός της Alameda Research ήταν τεχνητά φουσκωμένος με δισεκατομμύρια σε μη ρευστά FTT tokens. Στις 6 Νοεμβρίου, η Binance ανακοίνωσε την πώληση όλων των FTT της. Ακολούθησε τραπεζικός πανικός $6 δισ. σε 72 ώρες, αποκαλύπτοντας τρύπα $8 δισεκατομμυρίων σε καταθέσεις πελατών.",
                whyItCrashedEn = "1) SBF illegally lent customer deposits to Alameda to cover bad loans after the Terra/Luna crash. 2) The second-largest exchange on Earth froze withdrawals, wiping out retail and market-maker capital. 3) Massive forced liquidation of Alameda's holdings (Solana, Aptos, BTC, ETH) overwhelmed market depth.",
                whyItCrashedEl = "1) Ο SBF δάνειζε παράνομα καταθέσεις πελατών στην Alameda για να καλύψει επισφάλειες από το κραχ της Luna. 2) Το δεύτερο μεγαλύτερο ανταλλακτήριο στον πλανήτη πάγωσε τις αναλήψεις, εξαφανίζοντας κεφάλαια ιδιωτών και market makers. 3) Μαζικές αναγκαστικές πωλήσεις των αποθεμάτων της Alameda (Solana, Aptos, BTC, ETH) διέλυσαν τη ρευστότητα.",
                altcoinImpactEn = "Solana plunged to $8.40 due to Alameda's direct tie-ins. DeFi TVL fell 40%. The entire lending market (Genesis, BlockFi) froze and filed for bankruptcy.",
                altcoinImpactEl = "Το Solana βυθίστηκε στα $8,40 λόγω της στενής σύνδεσης με την Alameda. Το TVL στο DeFi έπεσε 40%. Ολόκληρη η αγορά δανεισμού (Genesis, BlockFi) πάγωσε και πτώχευσε.",
                marketPsychologyEn = "Maximal institutional panic. Mainstream media proclaimed crypto was dead forever. This was the exact day Bitcoin bottomed at $15,476.",
                marketPsychologyEl = "Απόλυτος θεσμικός πανικός. Τα παγκόσμια ΜΜΕ κήρυξαν τον οριστικό θάνατο των κρυπτονομισμάτων. Αυτή ήταν ακριβώς η ημέρα που το Bitcoin έπιασε πάτο στα $15.476.",
                survivalLessonEn = "Major exchange collapses mark historical generational bottoms (Mt. Gox in 2014, FTX in 2022). Buying blood during fraud fallout is historically the highest-yielding trade.",
                survivalLessonEl = "Οι καταρρεύσεις κορυφαίων ανταλλακτηρίων σηματοδοτούν ιστορικούς πυθμένες γενεών (Mt. Gox 2014, FTX 2022). Η αγορά στον απόλυτο φόβο αποτελεί ιστορικά την πιο κερδοφόρα κίνηση."
            ),

            HistoricalCrashEvent(
                id = "terra_luna_death_spiral_2022",
                cycleYear = "2022",
                dayOffsetRange = "Day 180 ➔ 190 post-ATH",
                exactDayOffset = 180,
                approxDate = "09 May 2022",
                titleEn = "The $40 Billion Terra/Luna Algorithmic Death Spiral",
                titleEl = "Η Αλγοριθμική Κατάρρευση $40 Δισ. των Terra / Luna & UST",
                severity = "CRITICAL",
                btcDropSummary = "$39,500 ➔ $26,700 (-32%)",
                altcoinDropSummary = "LUNA $85 ➔ $0.00001 (-100%), UST lost $1 peg to $0.05",
                whatHappenedEn = "On May 8, 2022, coordinated liquidity drains pulled millions from Curve's UST pool, breaking the $1 peg of the algorithmic stablecoin UST. The mint/burn mechanism hyperinflated LUNA from 350M tokens to 6.5 Trillion tokens in under 72 hours.",
                whatHappenedEl = "Στις 8 Μαΐου 2022, συντονισμένες αναλήψεις ρευστότητας από την Curve έσπασαν τη σύνδεση του $1 του αλγοριθμικού stablecoin UST. Ο μηχανισμός mint/burn υπερπληθώρισε το LUNA από 350 εκατ. σε 6,5 τρισεκατομμύρια tokens σε λιγότερο από 72 ώρες.",
                whyItCrashedEn = "1) Anchor Protocol promised an unsustainable 20% APY funded by investor deposits. 2) Luna Foundation Guard (LFG) dumped 80,000 Bitcoins onto spot exchanges in a futile attempt to defend the UST peg, crashing the entire market. 3) Complete loss of confidence in algorithmic stablecoins.",
                whyItCrashedEl = "1) Το Anchor Protocol υποσχόταν μη βιώσιμο 20% APY που επιδοτούνταν από νέες καταθέσεις. 2) Το Luna Foundation Guard (LFG) ξεπούλησε 80.000 Bitcoins στα spot ανταλλακτήρια για να σώσει το peg, κατακρημνίζοντας ολόκληρη την αγορά. 3) Ολοκληρωτική απώλεια εμπιστοσύνης στα αλγοριθμικά stablecoins.",
                altcoinImpactEn = "$40 Billion of market cap vanished overnight. Retail life savings were erased. The contagion fatally wounded Celsius, Three Arrows Capital, and Voyager.",
                altcoinImpactEl = "40 δισεκατομμύρια δολάρια κεφαλαιοποίησης εξαϋλώθηκαν μέσα σε μια νύχτα. Η μόλυνση τραυμάτισε θανάσιμα τα Celsius, Three Arrows Capital και Voyager.",
                marketPsychologyEn = "Shock and disbelief that a top-5 cryptocurrency could drop to absolute zero in 3 days.",
                marketPsychologyEl = "Σοκ και αδυναμία κατανόησης του πώς ένα κρυπτονόμισμα του Top-5 μπόρεσε να μηδενίσει σε 3 ημέρες.",
                survivalLessonEn = "Never trust uncollateralized or circular algorithmic tokenomics offering guaranteed high APYs during a macro tightening cycle.",
                survivalLessonEl = "Μην εμπιστεύεστε ποτέ αλγοριθμικά μοντέλα χωρίς πραγματικό αντίκρισμα που προσφέρουν εγγυημένες υψηλές αποδόσεις σε φάση νομισματικής σύσφιξης."
            ),

            HistoricalCrashEvent(
                id = "three_arrows_celsius_cascade_2022",
                cycleYear = "2022",
                dayOffsetRange = "Day 210 ➔ 230 post-ATH",
                exactDayOffset = 215,
                approxDate = "13 Jun 2022",
                titleEn = "Celsius Withdrawal Freeze & 3AC Liquidation Contagion",
                titleEl = "Πάγωμα Αναλήψεων Celsius & Ντόμινο Ρευστοποιήσεων 3AC",
                severity = "HIGH",
                btcDropSummary = "$31,000 ➔ $17,500 (-43% in 10 days)",
                altcoinDropSummary = "ETH $1,900 ➔ $880 (-53%), DeFi tokens down 60%",
                whatHappenedEn = "On Sunday night, June 12, 2022, Celsius Network halted all customer transfers and withdrawals citing 'extreme market conditions'. Days later, Three Arrows Capital (3AC), a $10B mega-fund, failed to meet margin calls and ghosted counterparties.",
                whatHappenedEl = "Το βράδυ της Κυριακής, 12 Ιουνίου 2022, η Celsius Network ανέστειλε όλες τις αναλήψεις πελατών επικαλούμενη 'ακραίες συνθήκες αγοράς'. Λίγες ημέρες μετά, η Three Arrows Capital (3AC), ένα mega-fund $10 δισ., απέτυχε να καλύψει margin calls και εξαφανίστηκε.",
                whyItCrashedEn = "1) Massive hidden re-hypothecation: 3AC borrowed uncollateralized funds from every CeFi lender. 2) stETH de-pegged on Curve, trapping leveraged Lido staking loops. 3) Lenders forcefully liquidated thousands of mortgaged BTC and ETH contracts simultaneously.",
                whyItCrashedEl = "1) Τεράστια κρυφή υπερμόχλευση: Η 3AC δανειζόταν ακάλυπτα κεφάλαια από όλα τα CeFi ιδρύματα. 2) Το stETH έχασε το 1:1 με το ETH στην Curve, εγκλωβίζοντας μοχλευμένες θέσεις. 3) Οι δανειστές ρευστοποίησαν αναγκαστικά δεκάδες χιλιάδες συμβόλαια BTC και ETH ταυτόχρονα.",
                altcoinImpactEn = "ETH plunged to $880. DeFi protocols suffered forced liquidations. Babel Finance, Voyager, and CoinFLEX all halted operations.",
                altcoinImpactEl = "Το ETH έπεσε στα $880. Τα πρωτόκολλα DeFi υπέστησαν μαζικές ρευστοποιήσεις. Τα Babel Finance, Voyager και CoinFLEX έκλεισαν τις πλατφόρμες τους.",
                marketPsychologyEn = "Widespread fear that every lending platform in crypto was insolvent and that Bitcoin had no support.",
                marketPsychologyEl = "Επικράτηση του φόβου ότι κάθε πλατφόρμα δανεισμού ήταν χρεοκοπημένη και ότι το Bitcoin δεν είχε κανένα δίχτυ ασφαλείας.",
                survivalLessonEn = "Not your keys, not your crypto. High CeFi deposit yields come from lending your coins to opaque, leveraged gamblers.",
                survivalLessonEl = "Αν δεν κατέχεις τα ιδιωτικά κλειδιά, δεν κατέχεις τα νομίσματα. Οι υψηλές αποδόσεις στα CeFi προέρχονταν από δανεισμό σε αδιαφανείς κερδοσκόπους."
            ),

            HistoricalCrashEvent(
                id = "bitconnect_regulatory_fud_2018",
                cycleYear = "2018",
                dayOffsetRange = "Day 30 ➔ 60 post-ATH",
                exactDayOffset = 45,
                approxDate = "17 Jan 2018",
                titleEn = "Bitconnect Ponzi Shutdown & Asian Regulatory Crackdowns",
                titleEl = "Κλείσιμο Bitconnect & Ρυθμιστικές Απαγορεύσεις στην Ασία",
                severity = "HIGH",
                btcDropSummary = "$19,783 ➔ $6,000 (-69% in 7 weeks)",
                altcoinDropSummary = "BCC went to $0, Altcoins plunged 50-70% from January peak",
                whatHappenedEn = "In mid-January 2018, Texas and North Carolina regulators issued cease-and-desist orders against Bitconnect, causing the multi-billion dollar Ponzi to shut down overnight. Concurrently, South Korea's Justice Ministry threatened an outright ban on cryptocurrency exchanges, and Coincheck was hacked for $530M of NEM.",
                whatHappenedEl = "Στα μέσα Ιανουαρίου 2018, οι ρυθμιστικές αρχές των ΗΠΑ εξέδωσαν εντολές παύσης κατά του Bitconnect, κλείνοντας την πολυδιαφημισμένη πυραμίδα. Παράλληλα, το Υπουργείο Δικαιοσύνης της Νότιας Κορέας απείλησε με απαγόρευση των ανταλλακτηρίων, ενώ το Coincheck έπεσε θύμα υποκλοπής $530 εκατ. σε NEM.",
                whyItCrashedEn = "1) Euphoric retail leverage got completely wiped out on the first sharp pullback. 2) South Korea 'Kimchi Premium' arbitrage collapsed under police raid rumors. 3) Bitconnect dumped hundreds of millions of user BTC on market.",
                whyItCrashedEl = "1) Η υπερβολική μόχλευση των ιδιωτών εξαλείφθηκε στο πρώτο απότομο pullback. 2) Το premium 'Kimchi' της Κορέας κατέρρευσε υπό φήμες επιδρομών της αστυνομίας. 3) Οι δημιουργοί του Bitconnect ξεπούλησαν εκατοντάδες εκατομμύρια σε BTC.",
                altcoinImpactEn = "The January 2018 altcoin bubble burst violently; tokens that had gained 10,000% dropped 70% in weeks.",
                altcoinImpactEl = "Η φούσκα των altcoins του Ιανουαρίου 2018 έσκασε βίαια. Tokens με κέρδη 10.000% έχασαν το 70% μέσα σε εβδομάδες.",
                marketPsychologyEn = "Disbelief turned into panic selling as mainstream media declared the crypto bubble officially popped.",
                marketPsychologyEl = "Η άρνηση μετατράπηκε σε πανικόβλητες πωλήσεις καθώς τα διεθνή μέσα κήρυξαν το επίσημο σκάσιμο της φούσκας.",
                survivalLessonEn = "Do not chase parabolic vertical spikes at the end of a multi-year bull cycle.",
                survivalLessonEl = "Μην κυνηγάτε παραβολικές κατακόρυφες ανόδους στο τέλος ενός πολυετούς ανοδικού κύκλου."
            ),

            HistoricalCrashEvent(
                id = "bitstamp_mtgox_exhaustion_2015",
                cycleYear = "2015",
                dayOffsetRange = "Day 400 ➔ 410 post-ATH",
                exactDayOffset = 410,
                approxDate = "14 Jan 2015",
                titleEn = "The Bitstamp 19,000 BTC Hack & Mt. Gox Exhaustion",
                titleEl = "Το Hack των 19.000 BTC στο Bitstamp & Ο Πυθμένας Mt. Gox",
                severity = "MODERATE",
                btcDropSummary = "$320 ➔ $152 (-52% final capitulation wick)",
                altcoinDropSummary = "Litecoin dropped to $1.15, Peercoin and Namecoin died",
                whatHappenedEn = "On January 4, 2015, Bitstamp was hacked for 19,000 BTC ($5.1M at the time) via an employee phishing attack. This hit an already exhausted market still recovering from the Mt. Gox bankruptcy (850,000 BTC stolen in early 2014).",
                whatHappenedEl = "Στις 4 Ιανουαρίου 2015, το Bitstamp παραβιάστηκε και εκλάπησαν 19.000 BTC μέσω phishing επίθεσης σε υπάλληλο. Αυτό χτύπησε μια ήδη εξαντλημένη αγορά που προσπαθούσε να ανακάμψει από τη χρεοκοπία του Mt. Gox (850.000 BTC).",
                whyItCrashedEn = "1) Complete lack of liquidity and order book depth. 2) Miner revenue fell below electricity costs, forcing hardware shutdowns. 3) Fear that no exchange could be trusted with custody.",
                whyItCrashedEl = "1) Πλήρης έλλειψη ρευστότητας και βάθους στο βιβλίο εντολών. 2) Τα έσοδα των miners έπεσαν κάτω από το κόστος ρεύματος, αναγκάζοντας κλείσιμο μηχανημάτων. 3) Φόβος ότι κανένα ανταλλακτήριο δεν ήταν αξιόπιστο.",
                altcoinImpactEn = "The first generation of altcoins (Litecoin, Namecoin, Peercoin) suffered catastrophic 95-98% drawdowns.",
                altcoinImpactEl = "Η πρώτη γενιά altcoins υπέστη καταστροφικές απώλειες της τάξης του 95-98%.",
                marketPsychologyEn = "Total capitulation. The Bitcoin Reddit forum was empty, trading volume was near zero, and interest hit an all-time low.",
                marketPsychologyEl = "Ολική συνθηκολόγηση. Το Reddit ήταν άδειο, ο όγκος συναλλαγών κοντά στο μηδέν και το ενδιαφέρον στο ναδίρ.",
                survivalLessonEn = "Maximum boredom and lowest volume combined with security hacks consistently pinpoint the deepest cyclical bottom.",
                survivalLessonEl = "Η μέγιστη ανία και ο χαμηλότερος όγκος σε συνδυασμό με hacks ασφαλείας εντοπίζουν σταθερά τον απόλυτο κυκλικό πυθμένα."
            )
        )
    }

    private fun getAltcoinSummaryEn(day: Int): String {
        return when {
            day in 310..365 -> "At Day $day post-ATH, Altcoins in past cycles (2018 & 2022) were in a brutal liquidity drain. During the 2018 BCH Hash War, ETH plunged from $210 to $82 (-61%), while in 2022 the FTX crash crushed Solana from $36 to $8 (-78%). Altcoins experienced their deepest final capitulation right alongside Bitcoin's 1-year macro bottom."
            day < 120 -> "Altcoins experienced an early fake rally followed by a heavy bleed as Bitcoin Dominance began consolidating."
            day in 120..250 -> "Extreme solvency risk phase: In 2022, Terra Luna and 3AC wiped out $40B+ in altcoin capital in this exact window."
            else -> "Altcoins trade at 85% to 95% discounts from cycle highs, presenting asymmetric long-term risk/reward for surviving high-utility networks."
        }
    }

    private fun getAltcoinSummaryEl(day: Int): String {
        return when {
            day in 310..365 -> "Στην Ημέρα $day μετά το ATH, τα Altcoins στους προηγούμενους κύκλους (2018 & 2022) βίωναν βίαιη εξάντληση ρευστότητας. Κατά τον Hash War του 2018 το ETH κατακρημνίστηκε από τα $210 στα $82 (-61%), ενώ το 2022 το κραχ της FTX συνέτριψε το Solana από τα $36 στα $8 (-78%). Τα altcoins κατέγραψαν την τελική τους συνθηκολόγηση ταυτόχρονα με τον πυθμένα 1 έτους του Bitcoin."
            day < 120 -> "Τα Altcoins κατέγραψαν μια πρώιμη ψευδή αναπήδηση και στη συνέχεια αιμορραγούσαν καθώς η κυριαρχία του Bitcoin (BTC Dominance) εδραιωνόταν."
            day in 120..250 -> "Φάση ακραίου κινδύνου χρεοκοπιών: Το 2022, τα Terra Luna και 3AC εξαΰλωσαν πάνω από $40 δισ. σε κεφάλαια altcoins σε αυτό ακριβώς το παράθυρο."
            else -> "Τα Altcoins διαπραγματεύονται με εκπτώσεις 85% έως 95% από τα υψηλά του κύκλου, προσφέροντας ασύμμετρη μακροπρόθεσμη σχέση ρίσκου/απόδοσης για δίκτυα με πραγματική χρησιμότητα."
        }
    }

    private fun formatDateWithOffset(baseTimestamp: Long, daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = baseTimestamp
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(cal.time)
    }

    private fun formatPrice(price: Double): String {
        return com.example.util.AppNumberFormatter.formatRawPrice(price, decimals = if (price >= 1000) 0 else 2)
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
