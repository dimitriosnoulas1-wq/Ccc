package com.example.data.derivatives

/**
 * Single config for the public-derivatives score. Weights are parts of 100.
 * A missing component is dropped and its weight is redistributed over the rest.
 */
object DerivativesWeights {
    const val FUNDING = 20.0
    const val OPEN_INTEREST = 25.0
    const val LIQUIDATIONS = 20.0
    const val TAKER = 20.0
    const val LONG_SHORT = 15.0

    const val TOTAL = FUNDING + OPEN_INTEREST + LIQUIDATIONS + TAKER + LONG_SHORT

    const val FORMULA =
        "score = sum(component * usedWeight) / sum(usedWeight); " +
            "funding 20 (crowded +funding → negative); " +
            "oi 25 (1h change z-score, signed by 24h price); " +
            "liq 20 ((shortLiq-longLiq)/(short+long)); " +
            "taker 20 ((ratio-1)/0.3); " +
            "ls 15 ((1-ratio)/0.8); " +
            "missing weights redistribute; never fabricate a venue field"

    init {
        require(TOTAL == 100.0) { "DerivativesWeights must sum to 100, was $TOTAL" }
    }
}
