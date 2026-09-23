package com.example.data.derivatives

import com.example.data.model.AggregatedDerivativesMetrics
import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.DerivativesFreshness
import com.example.data.model.VenueDerivativesSnapshot
import org.json.JSONArray
import org.json.JSONObject

object DerivativesJson {

    fun parseHub(body: String): AggregatedDerivativesSnapshot? {
        return try {
            val root = JSONObject(body)
            if (root.optBoolean("ok", false).not() && !root.has("venues")) return null
            val symbol = root.optString("symbol").ifBlank { return null }
            val venues = parseVenueArray(root.opt("venues"))
            if (venues.isEmpty()) return null
            val assembled = DerivativesAggregator.assemble(
                symbol = symbol,
                venues = venues,
                nowMs = root.optLong("asOfMs", System.currentTimeMillis())
            )
            val freshness = parseFreshness(root.optString("freshness")).takeIf {
                root.has("freshness") && root.optString("freshness").isNotBlank()
            }
            assembled.copy(
                asOfMs = root.optLong("asOfMs", assembled.asOfMs),
                freshness = freshness ?: assembled.freshness,
                sourceLabel = root.optString("sourceLabel").ifBlank { assembled.sourceLabel }
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseBinanceBundle(
        contract: String,
        premium: JSONObject?,
        ticker: JSONObject?,
        openInterest: JSONObject?,
        oiHist: JSONArray?,
        taker: JSONArray?,
        longShort: JSONArray?,
        fundingHist: JSONArray?
    ): VenueDerivativesSnapshot {
        val missing = mutableListOf<String>()
        val mark = num(premium, "markPrice")
        val last = num(ticker, "lastPrice")
        val change = num(ticker, "priceChangePercent")
        val funding = num(premium, "lastFundingRate")
        val oiContracts = num(openInterest, "openInterest")
        val priceForOi = mark ?: last
        val oiUsd = if (oiContracts != null && priceForOi != null && priceForOi > 0.0) {
            oiContracts * priceForOi
        } else {
            null
        }
        val oiSeries = numbersFrom(oiHist, "sumOpenInterestValue", "sumOpenInterest")
        val oiChange = lastPct(oiSeries)
        val oiZ = lastZ(pctSeries(oiSeries))
        val fundingSeries = numbersFrom(fundingHist, "fundingRate")
        val fundingZ = lastZ(fundingSeries)
        val takerRatio = firstNum(taker, "buySellRatio")
        val ls = firstNum(longShort, "longShortRatio")
        if (funding == null) missing += "funding"
        if (oiUsd == null) missing += "openInterest"
        if (takerRatio == null) missing += "taker"
        if (ls == null) missing += "longShort"
        missing += "liquidations"
        val asOf = longMs(premium, "time")
            ?: longMs(openInterest, "time")
            ?: longMs(ticker, "closeTime")
            ?: 0L
        val ok = mark != null || last != null || funding != null || oiUsd != null
        return VenueDerivativesSnapshot(
            venue = "binance",
            contract = contract,
            ok = ok,
            asOfMs = asOf,
            markPrice = mark,
            lastPrice = last,
            change24hPct = change,
            fundingRate = funding,
            openInterest = oiContracts,
            openInterestUsd = oiUsd,
            oiChange1hPct = oiChange,
            oiChangeZ = oiZ,
            fundingZ = fundingZ,
            takerBuySellRatio = takerRatio,
            longShortRatio = ls,
            missing = missing,
            error = if (ok) null else "empty_binance_payload"
        )
    }

    fun parseBybitBundle(
        contract: String,
        ticker: JSONObject?,
        oiHist: JSONObject?,
        accountRatio: JSONObject?
    ): VenueDerivativesSnapshot {
        val row = firstListItem(ticker)
        val missing = mutableListOf<String>()
        val last = num(row, "lastPrice")
        val mark = num(row, "markPrice") ?: last
        val changeFrac = num(row, "price24hPcnt")
        val change = changeFrac?.let { it * 100.0 }
        val funding = num(row, "fundingRate")
        val oiContracts = num(row, "openInterest")
        val oiUsd = num(row, "openInterestValue")
            ?: if (oiContracts != null && mark != null) oiContracts * mark else null
        val histSeries = numbersFrom(firstList(oiHist), "openInterest")
        val oiChange = lastPct(histSeries)
        val oiZ = lastZ(pctSeries(histSeries))
        val ratioRow = firstListItem(accountRatio)
        val buy = num(ratioRow, "buyRatio")
        val sell = num(ratioRow, "sellRatio")
        val ls = when {
            buy != null && sell != null && sell > 0.0 -> buy / sell
            buy != null && buy > 0.0 && buy < 1.0 -> buy / (1.0 - buy)
            else -> null
        }
        if (funding == null) missing += "funding"
        if (oiUsd == null) missing += "openInterest"
        missing += "taker"
        missing += "liquidations"
        if (ls == null) missing += "longShort"
        val asOf = longMs(row, "ts") ?: longMs(ratioRow, "timestamp") ?: 0L
        val ok = row != null && (mark != null || last != null || funding != null || oiUsd != null)
        return VenueDerivativesSnapshot(
            venue = "bybit",
            contract = contract,
            ok = ok,
            asOfMs = asOf,
            markPrice = mark,
            lastPrice = last,
            change24hPct = change,
            fundingRate = funding,
            openInterest = oiContracts,
            openInterestUsd = oiUsd,
            oiChange1hPct = oiChange,
            oiChangeZ = oiZ,
            longShortRatio = ls,
            missing = missing,
            error = if (ok) null else "empty_bybit_payload"
        )
    }

    fun parseOkxBundle(
        contract: String,
        mark: JSONObject?,
        funding: JSONObject?,
        openInterest: JSONObject?,
        ticker: JSONObject?,
        longShort: JSONArray?,
        taker: JSONArray?,
        liquidations: JSONObject?
    ): VenueDerivativesSnapshot {
        val markRow = firstData(mark)
        val fundRow = firstData(funding)
        val oiRow = firstData(openInterest)
        val tickRow = firstData(ticker)
        val missing = mutableListOf<String>()
        val markPx = num(markRow, "markPx")
        val last = num(tickRow, "last")
        val open24h = num(tickRow, "open24h")
        val change = if (last != null && open24h != null && open24h > 0.0) {
            (last - open24h) / open24h * 100.0
        } else {
            null
        }
        val fundingRate = num(fundRow, "fundingRate")
        val oiContracts = num(oiRow, "oi")
        val oiUsd = num(oiRow, "oiUsd")
        val contractUsd = if (oiContracts != null && oiContracts > 0.0 && oiUsd != null) {
            oiUsd / oiContracts
        } else {
            null
        }
        val ls = firstPairSecond(longShort)
        val takerRatio = okxTakerRatio(taker)
        val (longLiq, shortLiq) = okxLiquidations(liquidations, contractUsd, markPx ?: last)
        if (fundingRate == null) missing += "funding"
        if (oiUsd == null) missing += "openInterest"
        if (takerRatio == null) missing += "taker"
        if (ls == null) missing += "longShort"
        if (longLiq == null && shortLiq == null) missing += "liquidations"
        val asOf = longMs(markRow, "ts") ?: longMs(oiRow, "ts") ?: longMs(tickRow, "ts") ?: 0L
        val ok = markPx != null || last != null || fundingRate != null || oiUsd != null
        return VenueDerivativesSnapshot(
            venue = "okx",
            contract = contract,
            ok = ok,
            asOfMs = asOf,
            markPrice = markPx,
            lastPrice = last,
            change24hPct = change,
            fundingRate = fundingRate,
            openInterest = oiContracts,
            openInterestUsd = oiUsd,
            takerBuySellRatio = takerRatio,
            longShortRatio = ls,
            longLiqUsd = longLiq,
            shortLiqUsd = shortLiq,
            missing = missing,
            error = if (ok) null else "empty_okx_payload"
        )
    }

    fun failed(venue: String, contract: String, error: String): VenueDerivativesSnapshot {
        return VenueDerivativesSnapshot(
            venue = venue,
            contract = contract,
            ok = false,
            missing = listOf("all"),
            error = error
        )
    }

    fun hubPayload(snapshot: AggregatedDerivativesSnapshot): JSONObject {
        val venues = JSONArray()
        snapshot.venues.forEach { venues.put(venueJson(it)) }
        return JSONObject()
            .put("ok", snapshot.venues.any { it.ok })
            .put("symbol", snapshot.symbol)
            .put("asOfMs", snapshot.asOfMs)
            .put("freshness", snapshot.freshness.name)
            .put("sourceLabel", snapshot.sourceLabel)
            .put("venues", venues)
            .put("aggregated", metricsJson(snapshot.aggregated))
            .put("score", JSONObject()
                .put("value", snapshot.score.value ?: JSONObject.NULL)
                .put("redistributed", snapshot.score.redistributed)
                .put("formula", snapshot.score.formula)
            )
    }

    private fun venueJson(venue: VenueDerivativesSnapshot): JSONObject {
        return JSONObject()
            .put("venue", venue.venue)
            .put("contract", venue.contract)
            .put("ok", venue.ok)
            .put("asOfMs", venue.asOfMs)
            .put("markPrice", nullable(venue.markPrice))
            .put("lastPrice", nullable(venue.lastPrice))
            .put("change24hPct", nullable(venue.change24hPct))
            .put("fundingRate", nullable(venue.fundingRate))
            .put("openInterest", nullable(venue.openInterest))
            .put("openInterestUsd", nullable(venue.openInterestUsd))
            .put("oiChange1hPct", nullable(venue.oiChange1hPct))
            .put("oiChangeZ", nullable(venue.oiChangeZ))
            .put("fundingZ", nullable(venue.fundingZ))
            .put("takerBuySellRatio", nullable(venue.takerBuySellRatio))
            .put("longShortRatio", nullable(venue.longShortRatio))
            .put("longLiqUsd", nullable(venue.longLiqUsd))
            .put("shortLiqUsd", nullable(venue.shortLiqUsd))
            .put("missing", JSONArray(venue.missing))
            .put("error", venue.error ?: JSONObject.NULL)
    }

    private fun metricsJson(metrics: AggregatedDerivativesMetrics): JSONObject {
        return JSONObject()
            .put("markPrice", nullable(metrics.markPrice))
            .put("lastPrice", nullable(metrics.lastPrice))
            .put("change24hPct", nullable(metrics.change24hPct))
            .put("fundingRate", nullable(metrics.fundingRate))
            .put("openInterestUsd", nullable(metrics.openInterestUsd))
            .put("oiChange1hPct", nullable(metrics.oiChange1hPct))
            .put("oiChangeZ", nullable(metrics.oiChangeZ))
            .put("fundingZ", nullable(metrics.fundingZ))
            .put("takerBuySellRatio", nullable(metrics.takerBuySellRatio))
            .put("longShortRatio", nullable(metrics.longShortRatio))
            .put("longLiqUsd", nullable(metrics.longLiqUsd))
            .put("shortLiqUsd", nullable(metrics.shortLiqUsd))
            .put("venueCount", metrics.venueCount)
    }

    private fun parseVenueArray(raw: Any?): List<VenueDerivativesSnapshot> {
        val out = mutableListOf<VenueDerivativesSnapshot>()
        when (raw) {
            is JSONArray -> {
                for (i in 0 until raw.length()) {
                    val item = raw.optJSONObject(i) ?: continue
                    out += parseVenue(item)
                }
            }
            is JSONObject -> {
                val keys = raw.keys()
                while (keys.hasNext()) {
                    val item = raw.optJSONObject(keys.next()) ?: continue
                    out += parseVenue(item)
                }
            }
        }
        return out
    }

    private fun parseVenue(obj: JSONObject): VenueDerivativesSnapshot {
        val missing = mutableListOf<String>()
        val missingArr = obj.optJSONArray("missing")
        if (missingArr != null) {
            for (i in 0 until missingArr.length()) missing += missingArr.optString(i)
        }
        return VenueDerivativesSnapshot(
            venue = obj.optString("venue"),
            contract = obj.optString("contract"),
            ok = obj.optBoolean("ok", false),
            asOfMs = obj.optLong("asOfMs", 0L),
            markPrice = optNum(obj, "markPrice"),
            lastPrice = optNum(obj, "lastPrice"),
            change24hPct = optNum(obj, "change24hPct"),
            fundingRate = optNum(obj, "fundingRate"),
            openInterest = optNum(obj, "openInterest"),
            openInterestUsd = optNum(obj, "openInterestUsd"),
            oiChange1hPct = optNum(obj, "oiChange1hPct"),
            oiChangeZ = optNum(obj, "oiChangeZ"),
            fundingZ = optNum(obj, "fundingZ"),
            takerBuySellRatio = optNum(obj, "takerBuySellRatio"),
            longShortRatio = optNum(obj, "longShortRatio"),
            longLiqUsd = optNum(obj, "longLiqUsd"),
            shortLiqUsd = optNum(obj, "shortLiqUsd"),
            missing = missing,
            error = obj.optString("error").takeIf { it.isNotBlank() && it != "null" }
        )
    }

    private fun parseFreshness(raw: String): DerivativesFreshness {
        return try {
            DerivativesFreshness.valueOf(raw.trim().uppercase())
        } catch (_: Exception) {
            DerivativesFreshness.UNAVAILABLE
        }
    }

    private fun num(obj: JSONObject?, key: String): Double? = optNum(obj, key)

    private fun optNum(obj: JSONObject?, key: String): Double? {
        if (obj == null || !obj.has(key) || obj.isNull(key)) return null
        val raw = obj.opt(key)
        return when (raw) {
            is Number -> raw.toDouble().takeIf { it.isFinite() }
            is String -> raw.toDoubleOrNull()?.takeIf { it.isFinite() }
            else -> null
        }
    }

    private fun longMs(obj: JSONObject?, key: String): Long? {
        val n = optNum(obj, key) ?: return null
        return n.toLong().takeIf { it > 0L }
    }

    private fun firstNum(array: JSONArray?, key: String): Double? {
        if (array == null || array.length() == 0) return null
        val last = array.optJSONObject(array.length() - 1)
        val first = array.optJSONObject(0)
        return num(last, key) ?: num(first, key)
    }

    private fun numbersFrom(array: JSONArray?, vararg keys: String): List<Double> {
        if (array == null) return emptyList()
        val out = ArrayList<Double>(array.length())
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val value = keys.firstNotNullOfOrNull { num(item, it) } ?: continue
            out += value
        }
        return out
    }

    private fun firstList(obj: JSONObject?): JSONArray? {
        if (obj == null) return null
        val result = obj.optJSONObject("result")
        return result?.optJSONArray("list") ?: obj.optJSONArray("list")
    }

    private fun firstListItem(obj: JSONObject?): JSONObject? {
        val list = firstList(obj) ?: return null
        return list.optJSONObject(0)
    }

    private fun firstData(obj: JSONObject?): JSONObject? {
        if (obj == null) return null
        val data = obj.optJSONArray("data") ?: return if (obj.has("markPx") || obj.has("oi")) obj else null
        return data.optJSONObject(0)
    }

    private fun firstPairSecond(array: JSONArray?): Double? {
        if (array == null || array.length() == 0) return null
        val row = array.optJSONArray(0) ?: return null
        if (row.length() < 2) return null
        return when (val raw = row.opt(1)) {
            is Number -> raw.toDouble().takeIf { it.isFinite() }
            is String -> raw.toDoubleOrNull()?.takeIf { it.isFinite() }
            else -> null
        }
    }

    private fun okxTakerRatio(array: JSONArray?): Double? {
        if (array == null || array.length() == 0) return null
        val row = array.optJSONArray(0) ?: return null
        if (row.length() < 3) return null
        val sell = when (val raw = row.opt(1)) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        } ?: return null
        val buy = when (val raw = row.opt(2)) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        } ?: return null
        if (sell <= 0.0) return null
        return buy / sell
    }

    private fun okxLiquidations(
        root: JSONObject?,
        contractUsd: Double?,
        fallbackPrice: Double?
    ): Pair<Double?, Double?> {
        val data = root?.optJSONArray("data") ?: return null to null
        var longUsd = 0.0
        var shortUsd = 0.0
        var seen = false
        for (i in 0 until data.length()) {
            val block = data.optJSONObject(i) ?: continue
            val details = block.optJSONArray("details") ?: continue
            for (j in 0 until details.length()) {
                val row = details.optJSONObject(j) ?: continue
                val sz = num(row, "sz") ?: continue
                val px = num(row, "bkPx") ?: fallbackPrice
                val usd = when {
                    contractUsd != null && contractUsd > 0.0 -> sz * contractUsd
                    px != null && px > 0.0 -> sz * px
                    else -> continue
                }
                if (usd <= 0.0) continue
                seen = true
                when (row.optString("posSide").lowercase()) {
                    "long" -> longUsd += usd
                    "short" -> shortUsd += usd
                }
            }
        }
        if (!seen) return null to null
        return longUsd to shortUsd
    }

    private fun lastPct(series: List<Double>): Double? {
        if (series.size < 2) return null
        return DerivativesMath.pctChange(series[series.lastIndex - 1], series.last())
    }

    private fun pctSeries(series: List<Double>): List<Double> {
        if (series.size < 2) return emptyList()
        val out = ArrayList<Double>(series.size - 1)
        for (i in 1 until series.size) {
            val change = DerivativesMath.pctChange(series[i - 1], series[i]) ?: continue
            out += change
        }
        return out
    }

    private fun lastZ(series: List<Double>): Double? {
        if (series.isEmpty()) return null
        return DerivativesMath.zScore(series, series.last())
    }

    private fun nullable(value: Double?): Any = value ?: JSONObject.NULL
}
