package com.example.data.repository

import com.example.data.derivatives.DerivativesAggregator
import com.example.data.derivatives.DerivativesJson
import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.DerivativesFreshness
import com.example.data.model.VenueDerivativesSnapshot
import com.example.data.network.DerivativesVenueClients
import com.example.data.network.MarketDataClient
import com.example.data.network.MarketHub
import com.example.data.network.SymbolMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object DerivativesRepository {

    suspend fun fetch(symbol: String): AggregatedDerivativesSnapshot = withContext(Dispatchers.IO) {
        val base = SymbolMath.canonical(symbol).first
        if (base.isBlank()) return@withContext emptySnapshot(symbol)
        val fromHub = fetchHub(base)
        if (fromHub != null && fromHub.venues.any { it.ok }) return@withContext fromHub
        fetchVenues(base)
    }

    private fun fetchHub(base: String): AggregatedDerivativesSnapshot? {
        if (!MarketHub.isEnabled()) return null
        val body = MarketDataClient.getDirect(MarketHub.derivativesUrl(base)) ?: return null
        return DerivativesJson.parseHub(body)
    }

    private suspend fun fetchVenues(base: String): AggregatedDerivativesSnapshot = coroutineScope {
        val binance = async(Dispatchers.IO) { DerivativesVenueClients.fetchBinance(base) }
        val bybit = async(Dispatchers.IO) { DerivativesVenueClients.fetchBybit(base) }
        val okx = async(Dispatchers.IO) { DerivativesVenueClients.fetchOkx(base) }
        val venues = listOf(
            runCatching { binance.await() }.getOrElse { DerivativesJson.failed("binance", "${base}USDT", it.message ?: "binance") },
            runCatching { bybit.await() }.getOrElse { DerivativesJson.failed("bybit", "${base}USDT", it.message ?: "bybit") },
            runCatching { okx.await() }.getOrElse { DerivativesJson.failed("okx", "$base-USDT-SWAP", it.message ?: "okx") }
        )
        DerivativesAggregator.assemble(base, venues)
    }

    fun emptySnapshot(symbol: String): AggregatedDerivativesSnapshot {
        val venues = listOf(
            VenueDerivativesSnapshot(venue = "binance", contract = "", ok = false, missing = listOf("all")),
            VenueDerivativesSnapshot(venue = "bybit", contract = "", ok = false, missing = listOf("all")),
            VenueDerivativesSnapshot(venue = "okx", contract = "", ok = false, missing = listOf("all"))
        )
        return DerivativesAggregator.assemble(symbol, venues).copy(freshness = DerivativesFreshness.UNAVAILABLE)
    }
}
