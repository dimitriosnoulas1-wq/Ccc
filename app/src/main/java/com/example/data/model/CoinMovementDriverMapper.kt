package com.example.data.model

import java.util.Locale
import kotlin.math.abs

data class CoinMovementRow(
    val key: String,
    val labelEn: String,
    val labelEl: String,
    val intensity: String,
    val bars: Int,
    val isLive: Boolean
)

/**
 * Maps a live [MarketIntelligenceReport] onto the "Why is this coin moving?" meters.
 * Waiting / offline / symbol-mismatch rows stay a dash with zero bars.
 */
object CoinMovementDriverMapper {

    fun isWaiting(detail: String): Boolean {
        val d = detail.lowercase(Locale.US)
        return d.startsWith("waiting") || d.contains("offline")
    }

    fun reportMatchesCoin(reportSymbol: String, coinSymbol: String): Boolean {
        val base = reportSymbol.uppercase(Locale.US)
            .removeSuffix("USDT")
            .removeSuffix("BUSD")
            .removePrefix("1000")
        return base.equals(coinSymbol.trim(), ignoreCase = true)
    }

    fun barsFor(impact: DriverImpact, live: Boolean): Int {
        if (!live) return 0
        return when (impact) {
            DriverImpact.HIGH -> 5
            DriverImpact.MEDIUM -> 3
            DriverImpact.LOW -> 2
            DriverImpact.ABSORPTION -> 2
        }
    }

    fun rows(
        symbol: String,
        change24h: Double,
        hasLiveChange24h: Boolean,
        report: MarketIntelligenceReport?,
        etf: BitcoinEtfFlowData? = null
    ): List<CoinMovementRow> {
        val matched = report?.takeIf { reportMatchesCoin(it.symbol, symbol) }
        val drivers = matched?.drivers.orEmpty()
        fun find(titlePart: String) = drivers.firstOrNull { it.title.contains(titlePart, ignoreCase = true) }

        val spotDriver = find("Spot")
        val spotLive = spotDriver != null && !isWaiting(spotDriver.detail)
        val absChange = abs(change24h)
        val spotRow = when {
            spotLive -> CoinMovementRow(
                key = "spot",
                labelEn = "Spot book / prints",
                labelEl = "Βιβλίο / prints",
                intensity = shortSpotIntensity(spotDriver!!),
                bars = barsFor(spotDriver.impact, true),
                isLive = true
            )
            hasLiveChange24h -> {
                val intensity = when {
                    absChange > 5.0 -> "VERY HIGH"
                    absChange > 2.0 -> "HIGH"
                    absChange > 0.5 -> "MODERATE"
                    else -> "LOW"
                }
                val bars = when {
                    absChange > 5.0 -> 5
                    absChange > 2.0 -> 4
                    absChange > 0.5 -> 3
                    else -> 1
                }
                CoinMovementRow("spot", "24h change", "24ω μεταβολή", intensity, bars, true)
            }
            else -> CoinMovementRow("spot", "24h change", "24ω μεταβολή", "—", 0, false)
        }

        val oiDriver = find("Open Interest")
        val oiLive = matched?.hasLiveOpenInterest == true &&
            oiDriver != null &&
            !isWaiting(oiDriver.detail)
        val oiRow = CoinMovementRow(
            key = "oi",
            labelEn = "Open Interest",
            labelEl = "Open Interest",
            intensity = if (oiLive) compactUsd(matched!!.openInterestUsd) else "—",
            bars = barsFor(oiDriver?.impact ?: DriverImpact.LOW, oiLive),
            isLive = oiLive
        )

        val fundingDriver = find("Funding")
        val fundingLive = matched?.hasLiveFunding == true &&
            fundingDriver != null &&
            !isWaiting(fundingDriver.detail)
        val fundingRow = CoinMovementRow(
            key = "funding",
            labelEn = "Funding (USDT-M)",
            labelEl = "Funding (USDT-M)",
            intensity = if (fundingLive) {
                "%.4f".format(Locale.US, matched!!.fundingRate * 100.0) + "%"
            } else {
                "—"
            },
            bars = barsFor(fundingDriver?.impact ?: DriverImpact.LOW, fundingLive),
            isLive = fundingLive
        )

        val liqDriver = find("Liquidation")
        val liqLive = liqDriver != null && !isWaiting(liqDriver.detail)
        val liqRow = CoinMovementRow(
            key = "liq",
            labelEn = "Liquidations",
            labelEl = "Ρευστοποιήσεις",
            intensity = if (liqLive) liqDriver!!.impact.labelEn else "—",
            bars = barsFor(liqDriver?.impact ?: DriverImpact.LOW, liqLive),
            isLive = liqLive
        )

        val macroDriver = find("Fear")
        val macroLive = macroDriver != null && !isWaiting(macroDriver.detail)
        val macroRow = CoinMovementRow(
            key = "macro",
            labelEn = "Fear & Greed",
            labelEl = "Fear & Greed",
            intensity = if (macroLive) shortMacroIntensity(macroDriver!!) else "—",
            bars = barsFor(macroDriver?.impact ?: DriverImpact.LOW, macroLive),
            isLive = macroLive
        )

        val rows = mutableListOf(spotRow, oiRow, fundingRow, liqRow, macroRow)
        if (symbol.equals("BTC", ignoreCase = true) && etf?.isLive == true) {
            val flow = etf.oneDayNetFlowMillionUsd
            val intensity = (if (flow >= 0.0) "+" else "") +
                "%.1f".format(Locale.US, flow) + "M"
            val bars = when {
                abs(flow) >= 400.0 -> 5
                abs(flow) >= 150.0 -> 4
                abs(flow) >= 40.0 -> 3
                else -> 2
            }
            rows += CoinMovementRow(
                key = "etf",
                labelEn = "US spot ETF 1d",
                labelEl = "ETF ΗΠΑ 1ημ.",
                intensity = intensity,
                bars = bars,
                isLive = true
            )
        }
        return rows
    }

    private fun shortSpotIntensity(driver: MarketDriver): String {
        val share = Regex("""(\d+)%""").find(driver.detail)?.groupValues?.getOrNull(1)
        return if (share != null) "$share%" else driver.impact.labelEn
    }

    private fun shortMacroIntensity(driver: MarketDriver): String {
        val score = Regex("""F&G\s+(\d+)""").find(driver.detail)?.groupValues?.getOrNull(1)
        return score ?: driver.impact.labelEn
    }

    private fun compactUsd(value: Double): String {
        if (value <= 0.0) return "—"
        return if (value >= 1_000_000_000.0) {
            "$" + "%.1f".format(Locale.US, value / 1_000_000_000.0) + "B"
        } else if (value >= 1_000_000.0) {
            "$" + "%.1f".format(Locale.US, value / 1_000_000.0) + "M"
        } else if (value >= 1_000.0) {
            "$" + "%.1f".format(Locale.US, value / 1_000.0) + "K"
        } else {
            "$" + "%.0f".format(Locale.US, value)
        }
    }
}
