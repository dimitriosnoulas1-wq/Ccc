package com.example.util

import com.example.data.model.RainbowBand
import com.example.data.model.RainbowPoint
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

object RainbowCalculator {

    const val GENESIS_MILLIS: Long = 1230940800000L // 2009-01-03 UTC

    fun getDaysSinceGenesis(timeMillis: Long = System.currentTimeMillis()): Int {
        val diff = timeMillis - GENESIS_MILLIS
        return max(1, (diff / (1000L * 60 * 60 * 24)).toInt())
    }

    fun getCurrentFractionalYear(timeMillis: Long = System.currentTimeMillis()): Double {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = timeMillis
        }
        val year = calendar.get(java.util.Calendar.YEAR)
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        val daysInYear = if (isLeap) 366.0 else 365.0
        return year + (dayOfYear.toDouble() / daysInYear)
    }

    /**
     * BlockchainCenter Official Regression Formula:
     * basePrice = 10^(-17.01 + 5.84 * log10(daysSinceGenesis))
     */
    fun calculateBasePrice(days: Int): Double {
        val d = days.toDouble().coerceAtLeast(1.0)
        val exponent = -17.01 + 5.84 * log10(d)
        return 10.0.pow(exponent).coerceAtLeast(0.001)
    }

    fun calculateBandPrice(days: Int, band: RainbowBand): Double {
        val base = calculateBasePrice(days)
        return (base * band.multiplier).coerceAtLeast(0.001)
    }

    fun getAllBandPrices(days: Int): List<Double> {
        return RainbowBand.entries.sortedBy { it.id }.map { band ->
            calculateBandPrice(days, band)
        }
    }

    fun getActiveBandForPrice(btcPrice: Double, days: Int = getDaysSinceGenesis()): RainbowBand {
        val effectivePrice = resolveEffectivePrice(btcPrice)
        val bands = RainbowBand.entries.sortedBy { it.id }
        for (i in bands.indices) {
            val bandPrice = calculateBandPrice(days, bands[i])
            if (effectivePrice <= bandPrice) {
                return bands[i]
            }
        }
        return RainbowBand.MAXIMUM_BUBBLE
    }

    fun resolveEffectivePrice(price: Double): Double {
        return if (price > 0.0) price else 0.0
    }

    fun generateRainbowChartSeries(
        currentBtcPrice: Double,
        sparkline: List<Double> = emptyList(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): List<RainbowPoint> {
        val points = mutableListOf<RainbowPoint>()
        val effectiveBtcPrice = resolveEffectivePrice(currentBtcPrice)
        val currentFractionalYear = getCurrentFractionalYear(currentTimeMillis)

        // Authentic historical price milestones up to the present moment only.
        // No future or invented price points for 2025/2026.
        val historicalPriceMap = mutableMapOf(
            2010.50 to 0.08,
            2010.75 to 0.25,
            2011.00 to 0.30,
            2011.45 to 29.00,
            2011.85 to 2.20,
            2012.00 to 5.27,
            2012.50 to 6.65,
            2012.89 to 12.25,  // Halving 1: Nov 28, 2012
            2013.15 to 30.00,
            2013.28 to 230.00,
            2013.45 to 85.00,
            2013.88 to 750.00,
            2013.92 to 1150.00, // Dec 2013 Top
            2014.25 to 500.00,
            2014.85 to 370.00,
            2015.04 to 210.00, // Jan 2015 Capitulation Bottom
            2015.50 to 280.00,
            2015.90 to 430.00,
            2016.40 to 670.00,
            2016.52 to 650.00, // Halving 2: July 9, 2016
            2016.95 to 960.00,
            2017.35 to 2500.00,
            2017.65 to 4400.00,
            2017.96 to 19700.00, // Dec 2017 Top
            2018.15 to 6800.00,
            2018.50 to 6300.00,
            2018.96 to 3250.00, // Dec 2018 Bottom
            2019.49 to 13800.00,
            2019.95 to 7200.00,
            2020.20 to 5000.00, // March 2020 Covid Bottom
            2020.36 to 8800.00, // Halving 3: May 11, 2020
            2020.88 to 18000.00,
            2021.05 to 40000.00,
            2021.28 to 64800.00, // April 2021 Top 1
            2021.55 to 29800.00, // July 2021 Dip
            2021.86 to 69000.00, // Nov 2021 Top 2
            2022.38 to 29000.00,
            2022.48 to 19000.00,
            2022.87 to 15600.00, // Nov 2022 FTX Bottom
            2023.20 to 28000.00,
            2023.50 to 31000.00,
            2023.65 to 26000.00,
            2023.80 to 34000.00,
            2023.95 to 43000.00,
            2024.03 to 42500.00, // Jan 2024 ETF Approval phase
            2024.12 to 51800.00, // Feb 2024 Breakout
            2024.18 to 62500.00, // Pre-ATH acceleration
            2024.20 to 73750.00, // March 14, 2024: Peak 1 of the "M" (ATH in yellow/orange)
            2024.24 to 61500.00, // Pullback
            2024.28 to 71200.00, // Recovery
            2024.30 to 63800.00, // April 19, 2024: Halving 4 dip
            2024.38 to 71500.00, // May 21, 2024: Peak 2 of the "M" (Completes Double-Top)
            2024.44 to 65000.00, // June decline begins
            2024.48 to 60200.00, // Mid-June breakdown
            2024.51 to 54000.00, // July 5, 2024: German Gov selloff dip
            2024.55 to 67800.00, // July 28, 2024: Counter-rally
            2024.59 to 49150.00, // August 5, 2024: Deep capitulation crash into "Basically a Fire Sale"
            2024.63 to 64200.00, // Mid-August sharp relief bounce
            2024.67 to 53300.00, // September 6, 2024: Secondary retest dip
            2024.70 to 58200.00  // Mid-September upward recovery turn
        )

        // Remove any historical milestone that sits ahead of the current fractional year
        historicalPriceMap.keys.toList().forEach { k ->
            if (k >= currentFractionalYear) historicalPriceMap.remove(k)
        }

        // Anchor the live real-time price directly at the exact current date
        historicalPriceMap[currentFractionalYear] = effectiveBtcPrice

        val stepSize = 0.012 // High-resolution step for smooth curves (~4.4 days per point)
        var year = 2012.0
        var currentPriceInjected = false

        while (year <= 2032.02) {
            if (!currentPriceInjected && year > currentFractionalYear) {
                val curDays = ((currentFractionalYear - 2009.0) * 365.25).toInt()
                points.add(
                    RainbowPoint(
                        year = currentFractionalYear,
                        dayNumber = curDays,
                        btcPrice = effectiveBtcPrice,
                        bandPrices = getAllBandPrices(curDays)
                    )
                )
                currentPriceInjected = true
            }

            val yearOffset = year - 2009.0
            val days = (yearOffset * 365.25).toInt()
            val bands = getAllBandPrices(days)

            val btcPrice: Double? = if (year <= currentFractionalYear + 0.001) {
                interpolatePrice(year, historicalPriceMap, effectiveBtcPrice, currentFractionalYear)
            } else {
                null // Future projection beyond today has no price line
            }

            points.add(
                RainbowPoint(
                    year = year,
                    dayNumber = days,
                    btcPrice = btcPrice,
                    bandPrices = bands
                )
            )
            year += stepSize
        }

        return points
    }

    private fun interpolatePrice(
        year: Double,
        map: Map<Double, Double>,
        liveCurrentPrice: Double,
        currentYear: Double
    ): Double {
        if (year >= currentYear) return liveCurrentPrice
        val sortedKeys = map.keys.sorted()
        if (year <= sortedKeys.first()) return map[sortedKeys.first()] ?: 0.10
        if (year >= sortedKeys.last()) return liveCurrentPrice

        var lowerKey = sortedKeys.first()
        var upperKey = sortedKeys.last()

        for (k in sortedKeys) {
            if (k <= year) lowerKey = k
            if (k >= year) {
                upperKey = k
                break
            }
        }

        if (lowerKey == upperKey) return map[lowerKey] ?: liveCurrentPrice

        val p0 = map[lowerKey] ?: 0.10
        val p1 = map[upperKey] ?: liveCurrentPrice
        val t = (year - lowerKey) / (upperKey - lowerKey)

        val logP0 = ln(max(0.01, p0))
        val logP1 = ln(max(0.01, p1))
        val logInterp = logP0 + t * (logP1 - logP0)
        return exp(logInterp)
    }
}
