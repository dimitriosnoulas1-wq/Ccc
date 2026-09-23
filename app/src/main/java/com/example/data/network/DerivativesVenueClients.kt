package com.example.data.network

import com.example.data.derivatives.DerivativesJson
import com.example.data.model.VenueDerivativesSnapshot
import org.json.JSONArray
import org.json.JSONObject

internal object DerivativesVenueClients {

    fun fetchBinance(base: String): VenueDerivativesSnapshot {
        val contract = "${base}USDT"
        return try {
            val premium = getJsonObject(
                "https://www.binance.com/fapi/v1/premiumIndex?symbol=$contract",
                "https://fapi.binance.com/fapi/v1/premiumIndex?symbol=$contract"
            )
            val ticker = getJsonObject(
                "https://www.binance.com/fapi/v1/ticker/24hr?symbol=$contract",
                "https://fapi.binance.com/fapi/v1/ticker/24hr?symbol=$contract"
            )
            val openInterest = getJsonObject(
                "https://www.binance.com/fapi/v1/openInterest?symbol=$contract",
                "https://fapi.binance.com/fapi/v1/openInterest?symbol=$contract"
            )
            val oiHist = getJsonArray(
                "https://www.binance.com/futures/data/openInterestHist?symbol=$contract&period=1h&limit=30",
                "https://fapi.binance.com/futures/data/openInterestHist?symbol=$contract&period=1h&limit=30"
            )
            val taker = getJsonArray(
                "https://www.binance.com/futures/data/takerlongshortRatio?symbol=$contract&period=1h&limit=1",
                "https://fapi.binance.com/futures/data/takerlongshortRatio?symbol=$contract&period=1h&limit=1"
            )
            val longShort = getJsonArray(
                "https://www.binance.com/futures/data/globalLongShortAccountRatio?symbol=$contract&period=1h&limit=1",
                "https://fapi.binance.com/futures/data/globalLongShortAccountRatio?symbol=$contract&period=1h&limit=1"
            )
            val fundingHist = getJsonArray(
                "https://www.binance.com/fapi/v1/fundingRate?symbol=$contract&limit=30",
                "https://fapi.binance.com/fapi/v1/fundingRate?symbol=$contract&limit=30"
            )
            DerivativesJson.parseBinanceBundle(
                contract = contract,
                premium = premium,
                ticker = ticker,
                openInterest = openInterest,
                oiHist = oiHist,
                taker = taker,
                longShort = longShort,
                fundingHist = fundingHist
            )
        } catch (err: Exception) {
            DerivativesJson.failed("binance", contract, err.message ?: "binance_error")
        }
    }

    fun fetchBybit(base: String): VenueDerivativesSnapshot {
        val contract = "${base}USDT"
        return try {
            val ticker = getJsonObject(
                "https://api.bybit.com/v5/market/tickers?category=linear&symbol=$contract",
                "https://api.bytick.com/v5/market/tickers?category=linear&symbol=$contract"
            )
            if (ticker != null && ticker.optInt("retCode", -1) != 0) {
                return DerivativesJson.failed("bybit", contract, ticker.optString("retMsg", "bybit_ret"))
            }
            val oiHist = getJsonObject(
                "https://api.bybit.com/v5/market/open-interest?category=linear&symbol=$contract&intervalTime=1h&limit=30",
                "https://api.bytick.com/v5/market/open-interest?category=linear&symbol=$contract&intervalTime=1h&limit=30"
            )
            val accountRatio = getJsonObject(
                "https://api.bybit.com/v5/market/account-ratio?category=linear&symbol=$contract&period=1h&limit=1",
                "https://api.bytick.com/v5/market/account-ratio?category=linear&symbol=$contract&period=1h&limit=1"
            )
            DerivativesJson.parseBybitBundle(
                contract = contract,
                ticker = ticker,
                oiHist = oiHist,
                accountRatio = accountRatio
            )
        } catch (err: Exception) {
            DerivativesJson.failed("bybit", contract, err.message ?: "bybit_error")
        }
    }

    fun fetchOkx(base: String): VenueDerivativesSnapshot {
        val contract = "$base-USDT-SWAP"
        val uly = "$base-USDT"
        return try {
            val mark = getJsonObject("https://www.okx.com/api/v5/public/mark-price?instType=SWAP&instId=$contract")
            val funding = getJsonObject("https://www.okx.com/api/v5/public/funding-rate?instId=$contract")
            val openInterest = getJsonObject("https://www.okx.com/api/v5/public/open-interest?instType=SWAP&instId=$contract")
            val ticker = getJsonObject("https://www.okx.com/api/v5/market/ticker?instId=$contract")
            val longShortRoot = getJsonObject("https://www.okx.com/api/v5/rubik/stat/contracts/long-short-account-ratio?ccy=$base&period=1H")
            val takerRoot = getJsonObject("https://www.okx.com/api/v5/rubik/stat/taker-volume?ccy=$base&instType=CONTRACTS&period=1H")
            val liquidations = getJsonObject("https://www.okx.com/api/v5/public/liquidation-orders?instType=SWAP&uly=$uly&state=filled")
            DerivativesJson.parseOkxBundle(
                contract = contract,
                mark = mark,
                funding = funding,
                openInterest = openInterest,
                ticker = ticker,
                longShort = longShortRoot?.optJSONArray("data"),
                taker = takerRoot?.optJSONArray("data"),
                liquidations = liquidations
            )
        } catch (err: Exception) {
            DerivativesJson.failed("okx", contract, err.message ?: "okx_error")
        }
    }

    private fun getJsonObject(vararg urls: String): JSONObject? {
        val body = MarketDataClient.getText(urls.toList()) ?: return null
        return try {
            val obj = JSONObject(body)
            if (isBlocked(obj, body)) null else obj
        } catch (_: Exception) {
            null
        }
    }

    private fun getJsonArray(vararg urls: String): JSONArray? {
        val body = MarketDataClient.getText(urls.toList()) ?: return null
        return try {
            val trimmed = body.trim()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (isBlocked(obj, body)) return null
            }
            JSONArray(trimmed)
        } catch (_: Exception) {
            null
        }
    }

    private fun isBlocked(obj: JSONObject, raw: String): Boolean {
        val msg = obj.optString("msg") + obj.optString("message") + obj.optString("error")
        if (msg.contains("restricted location", ignoreCase = true)) return true
        if (msg.contains("block access from your country", ignoreCase = true)) return true
        if (raw.contains("CloudFront", ignoreCase = true) && raw.contains("country", ignoreCase = true)) return true
        return false
    }
}
