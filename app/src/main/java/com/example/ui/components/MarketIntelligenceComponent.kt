package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.example.data.model.CryptoCoin
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.MacroMarketSentiment
import com.example.data.model.MarketIntelligenceEngine
import com.example.data.model.MarketIntelligenceReport

@Composable
fun MarketIntelligenceComponent(
    symbol: String,
    coin: CryptoCoin?,
    tickerData: FuturesTickerData?,
    markFunding: FuturesMarkFunding?,
    openInterest: FuturesOpenInterest?,
    bookTicker: FuturesBookTicker? = null,
    recentTrades: List<FuturesTrade> = emptyList(),
    recentLiquidations: List<FuturesLiquidationOrder> = emptyList(),
    macroSentiment: MacroMarketSentiment? = null,
    externalReport: MarketIntelligenceReport? = null,
    modifier: Modifier = Modifier
) {
    if (externalReport != null) {
        MarketIntelligenceCard(
            report = externalReport,
            symbol = symbol,
            coin = coin,
            ticker = tickerData,
            markFunding = markFunding,
            openInterest = openInterest,
            modifier = modifier
        )
        return
    }

    val computedReport = produceState<MarketIntelligenceReport>(
        initialValue = MarketIntelligenceEngine.analyze(
            symbol = symbol,
            ticker = tickerData,
            bookTicker = bookTicker,
            markFunding = markFunding,
            openInterest = openInterest,
            recentTrades = recentTrades,
            recentLiquidations = recentLiquidations,
            macroSentiment = macroSentiment ?: MacroMarketSentiment(),
            coinFallback = coin
        ),
        symbol, tickerData, bookTicker, markFunding, openInterest, recentTrades, recentLiquidations, macroSentiment, coin
    ) {
        value = try {
            MarketIntelligenceEngine.analyze(
                symbol = symbol,
                ticker = tickerData,
                bookTicker = bookTicker,
                markFunding = markFunding,
                openInterest = openInterest,
                recentTrades = recentTrades,
                recentLiquidations = recentLiquidations,
                macroSentiment = macroSentiment ?: MacroMarketSentiment(),
                coinFallback = coin
            )
        } catch (_: Throwable) {
            value
        }
    }.value

    MarketIntelligenceCard(
        report = computedReport,
        symbol = symbol,
        coin = coin,
        ticker = tickerData,
        markFunding = markFunding,
        openInterest = openInterest,
        modifier = modifier
    )
}
