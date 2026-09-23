package com.example.data.repository

import java.util.Locale
import android.content.Context
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertSettings
import com.example.data.model.WhaleAlertType
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class WhaleAlertRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
) {
    private val _settings = MutableStateFlow(WhaleAlertSettings())
    val settings: StateFlow<WhaleAlertSettings> = _settings.asStateFlow()

    private val _isProUser = MutableStateFlow(false)

    private val initialAlerts = listOf(
        WhaleAlert(
            id = "w-001",
            timestamp = System.currentTimeMillis() - 120_000,
            coinSymbol = "BTC",
            coinName = "Bitcoin",
            amountCoin = 2450.0,
            amountUsd = 192_033_670.0,
            type = WhaleAlertType.EXCHANGE_INFLOW,
            source = "Whale Wallet (3J98t...)",
            destination = "Binance Hot Wallet",
            txHash = "0x8fa1947b92bc491e0a293817f54c9a81",
            marketImpactVerdict = "🔴 ΥΨΗΛΟΣ ΚΙΝΔΥΝΟΣ ΠΩΛΗΣΗΣ: 2,450 BTC ($192M) κατατέθηκαν σε ανταλλακτήριο. Αναμένεται αύξηση μεταβλητότητας και πίεση στις τιμές spot.",
            marketImpactVerdictEn = "🔴 HIGH SELL RISK: 2,450 BTC ($192M) deposited to exchange. Increased volatility and spot selling pressure expected.",
            isImpactCritical = true
        ),
        WhaleAlert(
            id = "w-002",
            timestamp = System.currentTimeMillis() - 480_000,
            coinSymbol = "ETH",
            coinName = "Ethereum",
            amountCoin = 48_500.0,
            amountUsd = 120_765_000.0,
            type = WhaleAlertType.EXCHANGE_OUTFLOW,
            source = "Coinbase Prime",
            destination = "Cold Storage (0x71C...)",
            txHash = "0x3bc7291a84f0927e6183029487c91823",
            marketImpactVerdict = "Εκροή Custody → cold storage. Πιθανή πτώση προσφοράς CEX — υπόθεση, όχι γεγονός.",
            marketImpactVerdictEn = "Outflow Custody → cold storage. Possible CEX supply drop — hypothesis, not fact.",
            isImpactCritical = true
        ),
        WhaleAlert(
            id = "w-003",
            timestamp = System.currentTimeMillis() - 1_200_000,
            coinSymbol = "SOL",
            coinName = "Solana",
            amountCoin = 320_000.0,
            amountUsd = 46_400_000.0,
            type = WhaleAlertType.WHALE_BUY,
            source = "OTC Aggregator",
            destination = "Validator Staking Reserve",
            txHash = "0x98127394871928471928374918273491",
            marketImpactVerdict = "🚀 ΜΕΓΑ-ΑΓΟΡΑ ON-CHAIN: $46.4M SOL απορροφήθηκαν άμεσα και κλειδώθηκαν σε staking pool.",
            marketImpactVerdictEn = "🚀 MEGA ON-CHAIN BUY: $46.4M SOL absorbed immediately and locked in staking pool.",
            isImpactCritical = false
        ),
        WhaleAlert(
            id = "w-004",
            timestamp = System.currentTimeMillis() - 2_100_000,
            coinSymbol = "XRP",
            coinName = "XRP",
            amountCoin = 75_000_000.0,
            amountUsd = 160_500_000.0,
            type = WhaleAlertType.WHALE_TRANSFER,
            source = "Ripple Escrow Wallet",
            destination = "Whale Custody Account",
            txHash = "0x12938471928374918273491827349182",
            marketImpactVerdict = "🐋 ΜΕΤΑΦΟΡΑ ΜΕΓΑΤΟΝΩΝ: $160M XRP μετακινήθηκαν μεταξύ θεσμικών πορτοφολιών εκτός αγοράς.",
            marketImpactVerdictEn = "🐋 MEGA TRANSFER: $160M XRP moved between institutional custody wallets off-market.",
            isImpactCritical = true
        ),
        WhaleAlert(
            id = "w-005",
            timestamp = System.currentTimeMillis() - 3_600_000,
            coinSymbol = "DOGE",
            coinName = "Dogecoin",
            amountCoin = 250_000_000.0,
            amountUsd = 66_000_000.0,
            type = WhaleAlertType.EXCHANGE_INFLOW,
            source = "Top 20 Whale Wallet",
            destination = "Robinhood Hot Storage",
            txHash = "0x78234192837491827349182734918273",
            marketImpactVerdict = "🚨 ΕΙΣΡΟΗ WHALE: 250M DOGE ($66M) μεταφέρθηκαν προς πιθανή ρευστοποίηση.",
            marketImpactVerdictEn = "🚨 WHALE INFLOW: 250M DOGE ($66M) transferred towards potential liquidation.",
            isImpactCritical = false
        ),
        WhaleAlert(
            id = "w-006",
            timestamp = System.currentTimeMillis() - 7_200_000,
            coinSymbol = "ADA",
            coinName = "Cardano",
            amountCoin = 95_000_000.0,
            amountUsd = 70_300_000.0,
            type = WhaleAlertType.EXCHANGE_OUTFLOW,
            source = "Kraken Exchange",
            destination = "Private Staking StakePool",
            txHash = "0x44928173918273918273918273918273",
            marketImpactVerdict = "🟢 ΣΥΣΣΩΡΕΥΣΗ ADA: $70M ADA αποσύρθηκαν από ανταλλακτήριο και δεσμεύτηκαν σε staking.",
            marketImpactVerdictEn = "🟢 ADA ACCUMULATION: $70M ADA withdrawn from exchange and locked into staking.",
            isImpactCritical = false
        )
    )

    private val _alerts = MutableStateFlow<List<WhaleAlert>>(initialAlerts)
    val alerts: StateFlow<List<WhaleAlert>> = _alerts.asStateFlow()

    init {
        startWhaleMonitoringLoop()
    }

    fun setProUser(isPro: Boolean) {
        _isProUser.value = isPro
    }

    fun updateSettings(newSettings: WhaleAlertSettings) {
        _settings.value = newSettings
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }

    fun setMinThreshold(thresholdUsd: Double) {
        _settings.value = _settings.value.copy(minThresholdUsd = thresholdUsd)
    }

    fun setNotifyZoneChange(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyZoneChange = enabled)
    }

    fun setNotifyPiCycle(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyPiCycle = enabled)
    }

    fun setNotifyRainbowBand(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyRainbowBand = enabled)
    }

    fun setNotify200wSma(enabled: Boolean) {
        _settings.value = _settings.value.copy(notify200wSma = enabled)
    }

    fun triggerTestWhaleAlert() {
        val testAlert = WhaleAlert(
            id = "test-${UUID.randomUUID().toString().take(8)}",
            timestamp = System.currentTimeMillis(),
            coinSymbol = "BTC",
            coinName = "Bitcoin",
            amountCoin = 3150.0,
            amountUsd = 246_899_000.0,
            type = WhaleAlertType.EXCHANGE_INFLOW,
            source = "Satoshi-Era Whale Wallet (1P5Z...)",
            destination = "Binance Prime Liquidity",
            txHash = "0x" + UUID.randomUUID().toString().replace("-", ""),
            marketImpactVerdict = "🚨 ΕΙΔΟΠΟΙΗΣΗ WHALE PRO: $246.8M BTC εισήλθαν σε ανταλλακτήριο! Άμεση επίδραση στο βιβλίο εντολών.",
            marketImpactVerdictEn = "🚨 PRO WHALE ALERT: $246.8M BTC moved to exchange! Immediate order book impact.",
            isImpactCritical = true
        )

        _alerts.value = listOf(testAlert) + _alerts.value

        NotificationHelper.sendWhaleAlertNotification(context, testAlert, isTest = true)
    }

    private fun startWhaleMonitoringLoop() {
        externalScope.launch {
            while (isActive) {
                try {
                    // Periodically generate realistic whale transactions every 50 seconds
                    delay(50_000L)
                    if (_isProUser.value) {
                        val alert = generateRandomWhaleAlert()
                        val currentList = _alerts.value.toMutableList()
                        currentList.add(0, alert)
                        if (currentList.size > 50) {
                            currentList.removeAt(currentList.lastIndex)
                        }
                        _alerts.value = currentList

                        val currentSettings = _settings.value
                        if (currentSettings.notificationsEnabled && alert.amountUsd >= currentSettings.minThresholdUsd) {
                            NotificationHelper.sendWhaleAlertNotification(context, alert)
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    // Non-fatal error ignored safely
                }
            }
        }
    }

    private fun generateRandomWhaleAlert(): WhaleAlert {
        val coins = listOf(
            Triple("BTC", "Bitcoin", 78380.0),
            Triple("ETH", "Ethereum", 2490.0),
            Triple("SOL", "Solana", 145.0),
            Triple("XRP", "XRP", 2.14),
            Triple("ADA", "Cardano", 0.74),
            Triple("DOGE", "Dogecoin", 0.264)
        )

        val selected = coins.random()
        val types = WhaleAlertType.values()
        val type = types.random()

        val amountUsd = Random.nextDouble(50_000_000.0, 580_000_000.0)
        val amountCoin = amountUsd / selected.third
        val formattedUsdCompact = com.example.util.AppNumberFormatter.formatCompactCurrency(amountUsd)

        val (source, dest, verdict, verdictEn) = when (type) {
            WhaleAlertType.EXCHANGE_INFLOW -> listOf(
                "Whale Wallet (${Random.nextInt(100, 999)}...)",
                "Binance Spot Ledger",
                "🚨 ΕΙΣΡΟΗ WHALE: $formattedUsdCompact ${selected.first} σε ανταλλακτήριο. Αυξημένη πίεση πώλησης.",
                "🚨 WHALE INFLOW: $formattedUsdCompact ${selected.first} to exchange. Increased sell pressure."
            )
            WhaleAlertType.EXCHANGE_OUTFLOW -> listOf(
                "Custody Provider",
                "Cold Storage Multi-Sig",
                "Εκροή Custody → cold storage. Πιθανή πτώση προσφοράς CEX — υπόθεση, όχι γεγονός.",
                "Outflow Custody → cold storage. Possible CEX supply drop — hypothesis, not fact."
            )
            WhaleAlertType.WHALE_BUY -> listOf(
                "Institutional OTC Desk",
                "Treasury Reserve",
                "🚀 ΜΕΓΑΛΗ ΑΓΟΡΑ: $formattedUsdCompact ${selected.first} απορροφήθηκαν απευθείας από την αγορά.",
                "🚀 MEGA BUY: $formattedUsdCompact ${selected.first} absorbed directly from market."
            )
            WhaleAlertType.WHALE_TRANSFER -> listOf(
                "Whale Custody Vault A",
                "Strategic Settlement Vault B",
                "🐋 ΜΕΤΑΦΟΡΑ WHALE: $formattedUsdCompact ${selected.first} μετακινήθηκαν εκτός χρηματιστηρίων.",
                "🐋 WHALE TRANSFER: $formattedUsdCompact ${selected.first} moved off-exchange."
            )
        }

        return WhaleAlert(
            id = "w-" + UUID.randomUUID().toString().take(8),
            timestamp = System.currentTimeMillis(),
            coinSymbol = selected.first,
            coinName = selected.second,
            amountCoin = amountCoin,
            amountUsd = amountUsd,
            type = type,
            source = source,
            destination = dest,
            txHash = "0x" + UUID.randomUUID().toString().replace("-", ""),
            marketImpactVerdict = verdict,
            marketImpactVerdictEn = verdictEn,
            isImpactCritical = amountUsd > 50_000_000.0
        )
    }
}
