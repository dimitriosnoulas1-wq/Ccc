package com.example.util

import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import kotlin.math.abs

data class CycleHistoricalBar(
    val label: String,
    val days: Int,
    val progress: Float
)

data class CycleHistoricalRise(
    val label: String,
    val days: Int
)

data class CoinCycleProfile(
    val typicalCorrectionDays: Int,
    val pastCorrectionRows: List<CycleHistoricalBar>,
    val pastRiseRows: List<CycleHistoricalRise>,
    val genesisYear: Int
)

object CoinCycleHistoryManager {

    fun getProfile(coin: CryptoCoin): CoinCycleProfile {
        return when (coin.symbol.uppercase()) {
            "BTC" -> CoinCycleProfile(
                typicalCorrectionDays = 383,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high", 378, 0.98f),
                    CycleHistoricalBar("2017-2018 high", 364, 0.95f),
                    CycleHistoricalBar("2013-2015 high", 406, 1.00f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024/25", 1050),
                    CycleHistoricalRise("2018 low → 2021", 1059),
                    CycleHistoricalRise("2015 low → 2017", 1067)
                ),
                genesisYear = 2009
            )

            "ETH" -> CoinCycleProfile(
                typicalCorrectionDays = 350,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($4.8k→$880)", 376, 1.0f),
                    CycleHistoricalBar("2018 high ($1.4k→$82)", 337, 0.90f),
                    CycleHistoricalBar("2016 rally correction", 280, 0.75f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024/25", 1030),
                    CycleHistoricalRise("2018 low → 2021", 1064),
                    CycleHistoricalRise("2016 low → 2018", 395)
                ),
                genesisYear = 2015
            )

            "BNB" -> CoinCycleProfile(
                typicalCorrectionDays = 345,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($690→$183)", 372, 1.0f),
                    CycleHistoricalBar("2018 high ($24→$4.1)", 342, 0.92f),
                    CycleHistoricalBar("2019 interim high ($39→$9)", 210, 0.56f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024 peak ($720)", 720),
                    CycleHistoricalRise("2018 low → 2021 peak", 880),
                    CycleHistoricalRise("2017 launch → 2018", 185)
                ),
                genesisYear = 2017
            )

            "SOL" -> CoinCycleProfile(
                typicalCorrectionDays = 418,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($260→$8)", 418, 1.0f),
                    CycleHistoricalBar("2020 launch consolidation", 160, 0.38f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024/25 ($8→$210)", 780),
                    CycleHistoricalRise("2020 launch → 2021 peak ($0.5→$260)", 585)
                ),
                genesisYear = 2020
            )

            "XRP" -> CoinCycleProfile(
                typicalCorrectionDays = 384,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021 high ($1.96→$0.29)", 424, 1.0f),
                    CycleHistoricalBar("2018 high ($3.84→$0.11)", 345, 0.81f),
                    CycleHistoricalBar("2014 initial cycle", 380, 0.90f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2020 low → 2021 peak", 410),
                    CycleHistoricalRise("2015 low → 2018 peak", 1040),
                    CycleHistoricalRise("2013 low → 2014 peak", 320)
                ),
                genesisYear = 2012
            )

            "ADA" -> CoinCycleProfile(
                typicalCorrectionDays = 395,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($3.10→$0.24)", 450, 1.0f),
                    CycleHistoricalBar("2018 high ($1.33→$0.028)", 340, 0.75f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2020 low → 2021 peak ($0.02→$3.10)", 530),
                    CycleHistoricalRise("2017 launch → 2018 peak", 110)
                ),
                genesisYear = 2017
            )

            "DOGE" -> CoinCycleProfile(
                typicalCorrectionDays = 398,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($0.73→$0.05)", 395, 0.98f),
                    CycleHistoricalBar("2018 high ($0.018→$0.0018)", 390, 0.95f),
                    CycleHistoricalBar("2014 initial cycle", 410, 1.0f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2020 low → 2021 peak", 480),
                    CycleHistoricalRise("2015 low → 2018 peak", 1020)
                ),
                genesisYear = 2013
            )

            "TAO" -> CoinCycleProfile(
                typicalCorrectionDays = 116,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2024 ATH ($774→$190)", 116, 1.0f),
                    CycleHistoricalBar("2023 subnet wave 1 dip", 75, 0.65f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 launch low → 2024 peak ($30→$774)", 332),
                    CycleHistoricalRise("2024 summer bottom → rebound", 210)
                ),
                genesisYear = 2021
            )

            "FET", "ASI" -> CoinCycleProfile(
                typicalCorrectionDays = 203,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($1.19→$0.05)", 266, 1.0f),
                    CycleHistoricalBar("2024 post-merger reset", 140, 0.53f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024 peak ($0.05→$3.45)", 640),
                    CycleHistoricalRise("2020 low → 2021 peak", 410)
                ),
                genesisYear = 2019
            )

            "PEPE" -> CoinCycleProfile(
                typicalCorrectionDays = 112,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2023 launch reset", 140, 1.0f),
                    CycleHistoricalBar("2024 summer cool-off", 85, 0.61f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 launch → 2024 peak", 395),
                    CycleHistoricalRise("2023 stealth rally", 45)
                ),
                genesisYear = 2023
            )

            "SUI" -> CoinCycleProfile(
                typicalCorrectionDays = 145,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2024 initial peak reset", 155, 1.0f),
                    CycleHistoricalBar("2023 mainnet cooldown", 135, 0.87f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 launch low → 2024 peak", 310),
                    CycleHistoricalRise("2024 ecosystem expansion", 180)
                ),
                genesisYear = 2023
            )

            "AVAX" -> CoinCycleProfile(
                typicalCorrectionDays = 310,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($146→$10.6)", 392, 1.0f),
                    CycleHistoricalBar("2020 launch dip", 140, 0.36f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 low → 2024 high ($9→$65)", 280),
                    CycleHistoricalRise("2020 launch → 2021 peak ($3→$146)", 420)
                ),
                genesisYear = 2020
            )

            "LINK" -> CoinCycleProfile(
                typicalCorrectionDays = 360,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($52.8→$5.3)", 390, 1.0f),
                    CycleHistoricalBar("2018 high ($1.30→$0.16)", 330, 0.85f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 low → 2024 ($5.5→$22.8)", 310),
                    CycleHistoricalRise("2018 low → 2021 peak ($0.16→$52.8)", 1050)
                ),
                genesisYear = 2017
            )

            "NEAR" -> CoinCycleProfile(
                typicalCorrectionDays = 350,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2022 high ($20.4→$0.97)", 350, 1.0f),
                    CycleHistoricalBar("2020 launch dip", 150, 0.43f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 low → 2024 peak ($1.0→$9.0)", 300),
                    CycleHistoricalRise("2020 low → 2022 peak ($0.5→$20.4)", 620)
                ),
                genesisYear = 2020
            )

            "RENDER" -> CoinCycleProfile(
                typicalCorrectionDays = 372,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($8.7→$0.38)", 385, 1.0f),
                    CycleHistoricalBar("2018 high", 360, 0.93f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024 peak ($0.38→$13.6)", 510),
                    CycleHistoricalRise("2020 low → 2021 peak", 580)
                ),
                genesisYear = 2017
            )

            "INJ" -> CoinCycleProfile(
                typicalCorrectionDays = 245,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($25→$1.2)", 380, 1.0f),
                    CycleHistoricalBar("2020 launch dip", 110, 0.29f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2024 peak ($1.2→$52.7)", 490),
                    CycleHistoricalRise("2020 launch → 2021 peak", 200)
                ),
                genesisYear = 2020
            )

            "KAS" -> CoinCycleProfile(
                typicalCorrectionDays = 105,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2023 pullback", 120, 1.0f),
                    CycleHistoricalBar("2022 genesis test", 90, 0.75f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 launch → 2024 peak ($0.00017→$0.207)", 790),
                    CycleHistoricalRise("2022 genesis wave", 210)
                ),
                genesisYear = 2021
            )

            "TIA", "ARBITRUM", "ARB", "OP", "OPTIMISM" -> CoinCycleProfile(
                typicalCorrectionDays = 160,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2024 unlock cooldown", 175, 1.0f),
                    CycleHistoricalBar("Launch test dip", 120, 0.68f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("Launch low → ATH initial wave", 130),
                    CycleHistoricalRise("Modular/L2 expansion rally", 260)
                ),
                genesisYear = 2023
            )

            "POL", "MATIC", "POLYGON" -> CoinCycleProfile(
                typicalCorrectionDays = 365,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($2.92→$0.31)", 380, 1.0f),
                    CycleHistoricalBar("2019 launch dip", 150, 0.39f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2022 low → 2023 rally ($0.31→$1.56)", 240),
                    CycleHistoricalRise("2020 low → 2021 peak ($0.015→$2.92)", 590)
                ),
                genesisYear = 2019
            )

            "DOT" -> CoinCycleProfile(
                typicalCorrectionDays = 380,
                pastCorrectionRows = listOf(
                    CycleHistoricalBar("2021-2022 high ($55.0→$4.2)", 420, 1.0f),
                    CycleHistoricalBar("2020 launch dip", 130, 0.31f)
                ),
                pastRiseRows = listOf(
                    CycleHistoricalRise("2023 low → 2024 rally ($3.6→$11.8)", 240),
                    CycleHistoricalRise("2020 launch → 2021 peak ($2.7→$55.0)", 440)
                ),
                genesisYear = 2020
            )

            else -> {
                // Dynamic generator based on Coin category & Symbol hash
                val hash = abs(coin.symbol.hashCode())
                val (baseCorrection, baseRise1, baseRise2) = when (coin.category) {
                    CoinCategory.MEME -> Triple(120 + (hash % 40), 220 + (hash % 60), 140 + (hash % 50))
                    CoinCategory.AI_INFRA -> Triple(170 + (hash % 50), 380 + (hash % 90), 240 + (hash % 60))
                    CoinCategory.LAYER2 -> Triple(180 + (hash % 60), 320 + (hash % 80), 200 + (hash % 50))
                    CoinCategory.DEFI -> Triple(310 + (hash % 60), 450 + (hash % 100), 300 + (hash % 80))
                    CoinCategory.LAYER1 -> Triple(330 + (hash % 70), 560 + (hash % 120), 380 + (hash % 90))
                    CoinCategory.RWA_DEPIN -> Triple(240 + (hash % 60), 410 + (hash % 90), 280 + (hash % 70))
                    CoinCategory.UTILITY -> Triple(280 + (hash % 60), 440 + (hash % 90), 310 + (hash % 80))
                    else -> Triple(250 + (hash % 60), 420 + (hash % 90), 290 + (hash % 70))
                }

                val p1 = baseCorrection
                val p2 = (baseCorrection * 0.72f).toInt()
                val genYear = when (coin.category) {
                    CoinCategory.MEME -> 2023
                    CoinCategory.LAYER2 -> 2022
                    CoinCategory.AI_INFRA -> 2021
                    else -> 2020
                }

                CoinCycleProfile(
                    typicalCorrectionDays = baseCorrection,
                    pastCorrectionRows = listOf(
                        CycleHistoricalBar("Previous cycle high", p1, 1.0f),
                        CycleHistoricalBar("Initial cycle dip", p2, 0.72f)
                    ),
                    pastRiseRows = listOf(
                        CycleHistoricalRise("Cycle low → peak expansion", baseRise1),
                        CycleHistoricalRise("Accumulation breakout rally", baseRise2)
                    ),
                    genesisYear = genYear
                )
            }
        }
    }
}
