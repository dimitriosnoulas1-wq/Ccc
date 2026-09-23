package com.example.data.engine

import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.CycleCommandState
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.MarketRegime
import com.example.data.model.StablecoinLiquidityData

object CycleCommandEngine {

    fun computeCycleCommandState(
        btcPrice: Double,
        etfFlowData: BitcoinEtfFlowData,
        liquidityData: StablecoinLiquidityData,
        futuresMarkFunding: FuturesMarkFunding?,
        fearGreedScore: Int = 55,
        altSeasonScore: Int = 31
    ): CycleCommandState {
        val halvingDays = 508
        val totalDays = 1460

        val fundingRate = futuresMarkFunding?.fundingRate?.times(100.0) ?: 0.011
        val isFundingHeated = fundingRate > 0.035

        val etf5d = etfFlowData.fiveDayCumulativeMillionUsd ?: 892.6
        val stablecoinChangeBillion = liquidityData.change7dUsd / 1_000_000_000.0
        val stablecoinTotalBillion = liquidityData.totalCirculatingUsd / 1_000_000_000.0

        // Determine Market Regime
        val regime = when {
            fearGreedScore >= 80 || fundingRate > 0.05 -> MarketRegime.CYCLE_PEAK_EXIT
            isFundingHeated || (fundingRate > 0.03 && etf5d < 0) -> MarketRegime.LEVERAGE_DISTRIBUTION
            fearGreedScore < 30 || btcPrice < 45000 -> MarketRegime.ACCUMULATION
            else -> MarketRegime.CYCLE_EXPANSION
        }

        // Compute Composite Cycle Score (0 - 100)
        // 0-30 = Deep Accumulation / Value
        // 31-70 = Cycle Expansion
        // 71-85 = Overheated Distribution
        // 86-100 = Peak Euphoria
        var score = 35 // Base mid-cycle

        // Halving timeline contribution (+15)
        val timelineProgress = (halvingDays.toDouble() / totalDays.toDouble()).coerceIn(0.0, 1.0)
        score += (timelineProgress * 25).toInt()

        // ETF 5D flow contribution (+10 if strong inflow, -10 if outflow)
        if (etf5d > 500) score += 8
        else if (etf5d < -300) score -= 8

        // Stablecoin Liquidity contribution (+10 if expanding)
        if (liquidityData.isLiquidityExpanding) score += 6

        // Funding heat (+10 if high leverage)
        if (isFundingHeated) score += 12

        // Fear and Greed normalization
        score = ((score * 0.6) + (fearGreedScore * 0.4)).toInt().coerceIn(10, 95)

        val rainbowBand = when {
            btcPrice < 48000 -> "Fire Sale / Accumulate Floor"
            btcPrice < 68000 -> "Accumulate / Support Base"
            btcPrice < 90000 -> "HODL / Steady Growth Corridor"
            btcPrice < 120000 -> "Is this a bubble?"
            else -> "Maximum Bubble Territory"
        }

        val summaryEn = when (regime) {
            MarketRegime.ACCUMULATION -> "Deep Value Zone. Long-term risk/reward highly asymmetric in favor of spot accumulation."
            MarketRegime.CYCLE_EXPANSION -> "Orderly Bull Expansion. Stablecoin & ETF liquidity supportive. Spot holding favored over high leverage."
            MarketRegime.LEVERAGE_DISTRIBUTION -> "Derivatives Overheating. Elevated funding and open interest create sharp long squeeze vulnerability."
            MarketRegime.CYCLE_PEAK_EXIT -> "Extreme Euphoria Zone. Multiple top indicators flagging. Gradual scale-out take profit strongly recommended."
        }

        val summaryEl = when (regime) {
            MarketRegime.ACCUMULATION -> "Ζώνη Βαθιάς Αξίας. Ο μακροπρόθεσμος λόγος απόδοσης/ρίσκου ευνοεί έντονα τη συσσώρευση Spot."
            MarketRegime.CYCLE_EXPANSION -> "Ομαλή Επέκταση Ταύρων. Η ρευστότητα από Stablecoins & ETFs παραμένει θετική. Συστήνεται διακράτηση Spot."
            MarketRegime.LEVERAGE_DISTRIBUTION -> "Υπερθέρμανση Παραγώγων. Το αυξημένο funding δημιουργεί υψηλό κίνδυνο απότομου Long Squeeze."
            MarketRegime.CYCLE_PEAK_EXIT -> "Ζώνη Ακραίας Ευφορίας. Πολλοί δείκτες κορυφής ενεργοποιούνται. Συστήνεται κλιμακωτή κατοχύρωση κερδών."
        }

        return CycleCommandState(
            regime = regime,
            compositeCycleScore = score,
            halvingDaysElapsed = halvingDays,
            halvingCycleLength = totalDays,
            rainbowBandName = rainbowBand,
            fundingRatePercent = fundingRate,
            isFundingHeated = isFundingHeated,
            etf5dNetFlowMillionUsd = etf5d,
            stablecoinTotalUsdBillion = stablecoinTotalBillion,
            stablecoin7dChangeBillion = stablecoinChangeBillion,
            fearAndGreedIndex = fearGreedScore,
            altcoinSeasonIndex = altSeasonScore,
            keyStanceSummaryEn = summaryEn,
            keyStanceSummaryEl = summaryEl
        )
    }
}
