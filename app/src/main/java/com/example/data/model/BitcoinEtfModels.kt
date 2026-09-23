package com.example.data.model

data class BitcoinEtfFlowData(
    val oneDayNetFlowMillionUsd: Double = 0.0,
    val fiveDayCumulativeMillionUsd: Double = 0.0,
    val asOfDate: String = "—",
    val isAvailable: Boolean = false,
    val isLive: Boolean = false,
    val sourceName: String = "Farside Investors"
)
