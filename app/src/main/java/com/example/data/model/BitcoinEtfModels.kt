package com.example.data.model

data class BitcoinEtfFlowData(
    val oneDayNetFlowMillionUsd: Double = 184.2,
    val fiveDayCumulativeMillionUsd: Double = 892.6,
    val asOfDate: String = "Post-NYSE Close",
    val isAvailable: Boolean = true,
    val sourceName: String = "Farside Investors (Public Institutional Flow)"
)
