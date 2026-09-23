package com.example.data.repository

import com.example.data.model.BitcoinEtfFlowData
import com.example.data.network.MarketDataClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class BitcoinEtfRepository(
    private val scope: CoroutineScope
) {
    private val _etfFlowData = MutableStateFlow(BitcoinEtfFlowData())
    val etfFlowData: StateFlow<BitcoinEtfFlowData> = _etfFlowData.asStateFlow()

    init {
        refreshEtfFlows()
    }

    fun refreshEtfFlows() {
        scope.launch(Dispatchers.IO) {
            fetchLiveFarsideFlows()
        }
    }

    private fun fetchLiveFarsideFlows() {
        try {
            val body = MarketDataClient.getText("https://farside.co.uk/btc/", attempts = 2)
            if (!body.isNullOrBlank()) {
                val parsed = parseFarsideHtml(body)
                if (parsed != null) {
                    _etfFlowData.value = parsed
                    return
                }
            }
        } catch (_: Throwable) {
        }
        if (!_etfFlowData.value.isLive) {
            _etfFlowData.value = BitcoinEtfFlowData(asOfDate = getFormattedTodayDate())
        }
    }

    private fun parseFarsideHtml(html: String): BitcoinEtfFlowData? {
        try {
            val rowPattern = Pattern.compile("<tr[^>]*>([\\s\\S]*?)</tr>", Pattern.CASE_INSENSITIVE)
            val cellPattern = Pattern.compile("<t[dh][^>]*>([\\s\\S]*?)</t[dh]>", Pattern.CASE_INSENSITIVE)
            val datePattern = Pattern.compile("^\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}$")
            val matcher = rowPattern.matcher(html)
            val dated = mutableListOf<Pair<String, Double>>()
            while (matcher.find()) {
                val row = matcher.group(1) ?: continue
                val cellMatcher = cellPattern.matcher(row)
                val cells = mutableListOf<String>()
                while (cellMatcher.find()) {
                    val text = cellMatcher.group(1)
                        ?.replace(Regex("<[^>]*>"), "")
                        ?.replace("&nbsp;", " ")
                        ?.replace("\\s+".toRegex(), " ")
                        ?.trim()
                        .orEmpty()
                    if (text.isNotBlank()) cells.add(text)
                }
                if (cells.size < 3) continue
                val date = cells.first()
                if (!datePattern.matcher(date).matches()) continue
                val total = parseFlowCell(cells.last()) ?: continue
                dated.add(date to total)
            }
            if (dated.isEmpty()) return null
            val usable = dated.filter { it.second != 0.0 }.ifEmpty { dated }
            val latest = usable.last()
            val fiveDay = usable.takeLast(5).sumOf { it.second }
            return BitcoinEtfFlowData(
                oneDayNetFlowMillionUsd = latest.second,
                fiveDayCumulativeMillionUsd = fiveDay,
                asOfDate = latest.first,
                isAvailable = true,
                isLive = true,
                sourceName = "Farside Investors (Verified Live Feed)"
            )
        } catch (_: Throwable) {
        }
        return null
    }

    private fun parseFlowCell(raw: String): Double? {
        val cleaned = raw.replace("$", "").replace(",", "").trim()
        val negative = cleaned.startsWith("(") && cleaned.endsWith(")")
        val number = cleaned.removePrefix("(").removeSuffix(")").trim().toDoubleOrNull() ?: return null
        return if (negative) -number else number
    }

    private fun getFormattedTodayDate(): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            sdf.format(Date())
        } catch (_: Throwable) {
            "Daily Post-Close"
        }
    }
}
