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
        fearGreedScore: Int? = null,
        altSeasonScore: Int = -1
    ): CycleCommandState {
        val halvingDays = com.example.util.HalvingCycleUtils.getDaysSince4thHalving()
        val totalDays = (
            (com.example.util.HalvingCycleUtils.HALVING_5TH_TIMESTAMP - com.example.util.HalvingCycleUtils.HALVING_4TH_TIMESTAMP) /
                86_400_000L
            ).toInt().coerceAtLeast(1)

        val fundingIsLive = futuresMarkFunding?.fundingRate != null
        val fundingRate = if (fundingIsLive) futuresMarkFunding?.fundingRate?.times(100.0) ?: 0.0 else 0.0
        val isFundingHeated = fundingIsLive && fundingRate > 0.035

        val etf5d = if (etfFlowData.isLive) etfFlowData.fiveDayCumulativeMillionUsd else 0.0
        val stablecoinChangeBillion = if (liquidityData.isLive) liquidityData.change7dUsd / 1_000_000_000.0 else 0.0
        val stablecoinTotalBillion = if (liquidityData.isLive) liquidityData.totalCirculatingUsd / 1_000_000_000.0 else 0.0

        // Determine Market Regime
        val regime = when {
            (fearGreedScore != null && fearGreedScore >= 80) || (fundingIsLive && fundingRate > 0.05) -> MarketRegime.CYCLE_PEAK_EXIT
            isFundingHeated || (fundingIsLive && fundingRate > 0.03 && etfFlowData.isLive && etf5d < 0) -> MarketRegime.LEVERAGE_DISTRIBUTION
            (fearGreedScore != null && fearGreedScore < 30) || (btcPrice > 0.0 && btcPrice < 45000) -> MarketRegime.ACCUMULATION
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

        // ETF 5D flow contribution only after a verified Farside parse
        if (etfFlowData.isLive) {
            if (etf5d > 500) score += 8
            else if (etf5d < -300) score -= 8
        }

        // Stablecoin Liquidity contribution only from a live DefiLlama read
        if (liquidityData.isLive && liquidityData.isLiquidityExpanding) score += 6

        // Funding heat (+10 if high leverage)
        if (isFundingHeated) score += 12

        // Fear and Greed normalization
        score = if (fearGreedScore != null) {
            ((score * 0.6) + (fearGreedScore * 0.4)).toInt().coerceIn(10, 95)
        } else {
            score.coerceIn(10, 95)
        }

        val rainbowBand = if (btcPrice <= 0.0) {
            "Waiting for live price"
        } else {
            val band = com.example.ui.rainbow.RainbowModel.bandIndex(com.example.ui.rainbow.DateUtil.today(), btcPrice)
            com.example.ui.rainbow.RainbowModel.BANDS[band].name
        }

        val dayLine = "Day $halvingDays of $totalDays since the 4th halving."
        val etfLine = if (etfFlowData.isLive) {
            " ETF 5d net ${"%.1f".format(etf5d)}M."
        } else {
            " ETF flow offline."
        }
        val stablesLine = if (liquidityData.isLive) {
            " Stables ${"%.1f".format(stablecoinTotalBillion)}B, 7d ${"%.1f".format(stablecoinChangeBillion)}B."
        } else {
            " Stables offline."
        }
        val fgLine = if (fearGreedScore != null) {
            " F&G $fearGreedScore."
        } else {
            " F&G offline."
        }
        val fundingLine = if (fundingIsLive) {
            " Funding ${"%.4f".format(fundingRate)}%."
        } else {
            " Funding offline."
        }
        val summaryEn = "$dayLine$etfLine$stablesLine$fgLine$fundingLine No trade call."
        val summaryEl = "Ημέρα $halvingDays από $totalDays μετά το 4ο halving." +
            (if (etfFlowData.isLive) " ETF 5ημ. ${"%.1f".format(etf5d)} εκ." else " ETF εκτός σύνδεσης.") +
            (if (liquidityData.isLive) " Stables ${"%.1f".format(stablecoinTotalBillion)} δισ., 7ημ. ${"%.1f".format(stablecoinChangeBillion)} δισ." else " Stables εκτός σύνδεσης.") +
            (if (fearGreedScore != null) " F&G $fearGreedScore." else " F&G εκτός σύνδεσης.") +
            (if (fundingIsLive) " Funding ${"%.4f".format(fundingRate)}%." else " Funding εκτός σύνδεσης.") +
            " Χωρίς εντολή συναλλαγής."

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
            fearAndGreedIndex = fearGreedScore ?: -1,
            altcoinSeasonIndex = altSeasonScore,
            etfFlowIsLive = etfFlowData.isLive,
            fundingIsLive = fundingIsLive,
            keyStanceSummaryEn = summaryEn,
            keyStanceSummaryEl = summaryEl
        )
    }
}
