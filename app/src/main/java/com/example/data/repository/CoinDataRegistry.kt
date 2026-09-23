package com.example.data.repository

import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import com.example.data.model.CyclePhase
import com.example.data.model.HistoricalAnalog

object CoinDataRegistry {

    private fun createAnalog(
        date2020: String,
        date2016: String,
        day: Int,
        gain2020: Double,
        gain2016: Double,
        peak: Double,
        bottom: Double,
        phase: CyclePhase,
        phaseName: String,
        progress: Float
    ): HistoricalAnalog = HistoricalAnalog(
        matchingDate2020 = date2020,
        matchingDate2016 = date2016,
        matchingCycleDay = day,
        gainPostMatchingDate2020 = gain2020,
        gainPostMatchingDate2016 = gain2016,
        projectedCyclePeak = peak,
        projectedCycleBottom = bottom,
        cyclePhase = phase,
        cyclePhaseName = phaseName,
        cycleClockProgress = progress,
        historicalCyclePointsCurrent = listOf(0.2f, 0.35f, 0.48f, 0.58f, 0.65f),
        historicalCyclePoints2020 = listOf(0.12f, 0.25f, 0.45f, 0.78f, 1.0f, 0.55f),
        historicalCyclePoints2016 = listOf(0.08f, 0.20f, 0.40f, 0.75f, 1.0f, 0.40f)
    )

    fun getAllCoins(): List<CryptoCoin> = listOf(
        // 1. BITCOIN
        CryptoCoin(
            id = "bitcoin", symbol = "BTC", name = "Bitcoin", rank = 1,
            priceUsd = 77250.00, athUsd = 73750.07, athDaysAgo = 160, athDate = "14 Mar 2024",
            atlUsd = 0.0707, atlDate = "13 Aug 2010", change24h = -0.32, volume24h = 42800000000.0,
            marketCap = 1520000000000.0, circulatingSupply = 19780000.0, totalSupply = 19780000.0,
            maxSupply = 21000000.0, supplyUnit = "BTC", category = CoinCategory.LAYER1,
            isPro = true,
            analog = createAnalog("21 Oct 2020", "14 Nov 2016", 210, 482.5, 1420.0, 168000.0, 48000.0, CyclePhase.EXPANSION, "Halving Markup Wave", 0.44f),
            sparkline = listOf(75800.0, 76400.0, 76100.0, 77200.0, 76900.0, 77400.0, 77250.0),
            whereItMovesNow = "Συσσώρευση στην ευρύτερη ζώνη στήριξης μετά το ιστορικό ρεκόρ του κύκλου (ATH $73.7k).",
            whereItMovedPast = "2010 ($0.07) -> 2011 ($29.5) -> 2013 ($1,150) -> 2017 ($19,800) -> 2021 ($69,000) -> 2024 ($73,750).",
            nextPredictedMoveNarrative = "Τα ιστορικά μοντέλα του κύκλου μετά το Halving δείχνουν φάση συσσώρευσης και αναμονή ισχυρής διαφυγής προς νέα υψηλά.",
            projectedNextMove1w = "+4.2% (71% πιθανότητα ανόδου)",
            projectedNextMove2w = "+9.5% (79% πιθανότητα ανόδου)",
            projectedNextMove4w = "+21.5% (86% πιθανότητα ανόδου)",
            genesisDate = "03 Jan 2009", founderOrCreator = "Satoshi Nakamoto",
            consensusMechanism = "Proof of Work (SHA-256)",
            whitepaperSummary = "Το Bitcoin είναι ένα αποκεντρωμένο peer-to-peer ψηφιακό σύστημα μετρητών που επιτρέπει απευθείας online πληρωμές χωρίς την ανάγκη εμπιστοσύνης σε χρηματοπιστωτικά ιδρύματα.",
            technologyDetails = "Χρησιμοποιεί αλυσίδα μπλοκ με αλγόριθμο SHA-256, μέγεθος block 1MB-4MB (SegWit/Taproot), μέσο χρόνο μπλοκ 10 λεπτών και Lightning Network για instant Layer 2 συναλλαγές.",
            tokenomicsDetails = "Σκληρό όριο 21.000.000 BTC. Μείωση παραγωγής (Halving) ανά 210.000 blocks (~4 έτη). Τρέχουσα αμοιβή μπλοκ 3.125 BTC.",
            useCases = listOf("Store of Value (Ψηφιακός Χρυσός)", "Αποκεντρωμένο Settlement Layer", "Παγκόσμιες διασυνοριακές μεταφορές χωρίς λογοκρισία", "Εταιρικό/Κρατικό αποθεματικό ενεργητικό")
        ),
        // 2. ETHEREUM
        CryptoCoin(
            id = "ethereum", symbol = "ETH", name = "Ethereum", rank = 2,
            priceUsd = 2650.00, athUsd = 4891.70, athDaysAgo = 1010, athDate = "16 Nov 2021",
            atlUsd = 0.42, atlDate = "21 Oct 2015", change24h = +1.18, volume24h = 18400000000.0,
            marketCap = 319000000000.0, circulatingSupply = 120450000.0, totalSupply = 120450000.0,
            maxSupply = null, supplyUnit = "ETH", category = CoinCategory.LAYER1,
            isPro = true,
            analog = createAnalog("08 Nov 2020", "02 Dec 2016", 202, 820.4, 2100.0, 7400.0, 1650.0, CyclePhase.EXPANSION, "L1 Liquidity Catchup", 0.40f),
            sparkline = listOf(2540.0, 2580.0, 2610.0, 2590.0, 2630.0, 2650.0, 2650.0),
            whereItMovesNow = "Συσσώρευση σε εύρος $2,500-$2,750 με αυξανόμενο staking ratio και Layer 2 blob fee absorption.",
            whereItMovedPast = "2015 ($0.42) -> 2018 ($1,420) -> 2020 ($90) -> 2021 ($4,891) -> 2024 ($4,090).",
            nextPredictedMoveNarrative = "Τα ιστορικά αναλογικά δεδομένα υποδηλώνουν ισχυρή ανοδική εκτόνωση όταν το ETH/BTC ratio ανακάμψει από τη βάση του.",
            projectedNextMove1w = "+4.8% (68% πιθανότητα ανόδου)",
            projectedNextMove2w = "+11.2% (75% πιθανότητα ανόδου)",
            projectedNextMove4w = "+24.8% (82% πιθανότητα ανόδου)",
            genesisDate = "30 Jul 2015", founderOrCreator = "Vitalik Buterin & Co-founders",
            consensusMechanism = "Proof of Stake (Gasper / Casper-FFG)",
            whitepaperSummary = "Το Ethereum είναι ένας παγκόσμιος αποκεντρωμένος υπολογιστής ανοιχτού κώδικα για την εκτέλεση Smart Contracts και αποκεντρωμένων εφαρμογών (DApps).",
            technologyDetails = "EVM (Ethereum Virtual Machine), Danksharding / EIP-4844 Blobs, 12-sec slot time, Layer 2 Rollup-centric scaling roadmap.",
            tokenomicsDetails = "EIP-1559 Base fee burn. Δυναμικό supply με αποπληθωριστική τάση σε υψηλή χρήση δικτύου, Staking yield ~3.4% APR.",
            useCases = listOf("Smart Contracts & DApps", "DeFi Collateral & Settlement", "Layer 2 Security Base Layer", "NFT & Token issuance standard (ERC-20/721/1155)")
        ),
        // 3. SOLANA
        CryptoCoin(
            id = "solana", symbol = "SOL", name = "Solana", rank = 3,
            priceUsd = 145.50, athUsd = 260.06, athDaysAgo = 1020, athDate = "06 Nov 2021",
            atlUsd = 0.50, atlDate = "11 May 2020", change24h = +3.82, volume24h = 4200000000.0,
            marketCap = 68200000000.0, circulatingSupply = 468000000.0, totalSupply = 585000000.0,
            maxSupply = null, supplyUnit = "SOL", category = CoinCategory.LAYER1,
            isPro = false,
            analog = createAnalog("15 Jan 2021", "10 Mar 2017", 245, 1640.0, 3200.0, 380.0, 45.0, CyclePhase.EXPANSION, "High Beta Acceleration", 0.48f),
            sparkline = listOf(138.5, 140.2, 142.1, 141.5, 144.0, 145.5, 145.5),
            whereItMovesNow = "Ανάκτηση ισχύος με ραγδαία αύξηση DEX volume και νέων διευθύνσεων πάνω από τα $140.",
            whereItMovedPast = "2020 ($0.50) -> 2021 ($260) -> 2022 ($8.00) -> 2024 ($210).",
            nextPredictedMoveNarrative = "Σε παρόμοιες φάσεις κύκλου, το SOL παρουσιάζει υπεραπόδοση 2x-3x σε σχέση με το μέσο όρο των L1s.",
            projectedNextMove1w = "+8.4% (74% πιθανότητα ανόδου)",
            projectedNextMove2w = "+18.5% (81% πιθανότητα ανόδου)",
            projectedNextMove4w = "+38.0% (85% πιθανότητα ανόδου)",
            genesisDate = "16 Mar 2020", founderOrCreator = "Anatoly Yakovenko & Raj Gokal",
            consensusMechanism = "Proof of History (PoH) + Tower BFT PoS",
            whitepaperSummary = "Αρχιτεκτονική υψηλής απόδοσης χωρίς sharding, ικανή για 65.000+ TPS και sub-second finality σε παγκόσμια κλίμακα.",
            technologyDetails = "Sealevel parallel runtime, Gulf Stream mempool-less protocol, Turbine block propagation, Firedancer validator client.",
            tokenomicsDetails = "Αρχικός πληθωρισμός 8% με απομείωση 15% ετησίως έως το σταθερό 1.5%. Καύση 50% όλων των transaction fees.",
            useCases = listOf("High-Frequency Trading & DEXs", "DePIN networks (Helium, Hivemapper)", "Web3 Gaming & Consumer Apps", "Micropayments & Stablecoin rails")
        ),
        // 4. BINANCE COIN
        CryptoCoin(
            id = "binancecoin", symbol = "BNB", name = "BNB", rank = 4,
            priceUsd = 584.20, athUsd = 720.67, athDaysAgo = 80, athDate = "06 Jun 2024",
            atlUsd = 0.096, atlDate = "19 Oct 2017", change24h = +1.45, volume24h = 1100000000.0,
            marketCap = 85200000000.0, circulatingSupply = 145800000.0, totalSupply = 145800000.0,
            maxSupply = 200000000.0, supplyUnit = "BNB", category = CoinCategory.LAYER1,
            isPro = true,
            analog = createAnalog("12 Dec 2020", "04 Jan 2017", 215, 1120.0, 2400.0, 1150.0, 320.0, CyclePhase.EXPANSION, "Exchange Rail Dominance", 0.45f),
            sparkline = listOf(565.0, 570.0, 578.0, 572.0, 580.0, 584.2),
            whereItMovesNow = "Σταθερή κίνηση κοντά στα ιστορικά υψηλά με συνεχή launchpool locking demand.",
            whereItMovedPast = "2017 ($0.10) -> 2019 ($39) -> 2021 ($690) -> 2024 ($720).",
            nextPredictedMoveNarrative = "Το τριμηνιαίο auto-burn και τα Launchpools δημιουργούν συνεχή πίεση προσφοράς.",
            projectedNextMove1w = "+3.5% (70% win rate)",
            projectedNextMove2w = "+8.9% (77% win rate)",
            projectedNextMove4w = "+19.4% (83% win rate)",
            genesisDate = "08 Jul 2017", founderOrCreator = "Changpeng Zhao (CZ)",
            consensusMechanism = "Proof of Staked Authority (PoSA)",
            whitepaperSummary = "Το εγγενές token του οικοσυστήματος BNB Chain για πληρωμές τελών, DeFi, και συμμετοχή σε Launchpools.",
            technologyDetails = "BNB Smart Chain (BSC EVM-compatible), opBNB (Optimistic Layer 2), BNB Greenfield αποκεντρωμένη αποθήκευση.",
            tokenomicsDetails = "Αυτόματο τριμηνιαίο Auto-Burn μέχρι η συνολική κυκλοφορία να μειωθεί ακριβώς στα 100.000.000 BNB.",
            useCases = listOf("Trading fee discounts", "BNB Chain Gas fees", "Launchpool & Launchpad staking", "DeFi governance & liquidity")
        ),
        // 5. RIPPLE
        CryptoCoin(
            id = "ripple", symbol = "XRP", name = "Ripple", rank = 5,
            priceUsd = 0.58, athUsd = 3.84, athDaysAgo = 2400, athDate = "04 Jan 2018",
            atlUsd = 0.0028, atlDate = "07 Jul 2014", change24h = +1.92, volume24h = 2900000000.0,
            marketCap = 33000000000.0, circulatingSupply = 57000000000.0, totalSupply = 99990000000.0,
            maxSupply = 100000000000.0, supplyUnit = "XRP", category = CoinCategory.UTILITY,
            isPro = true,
            analog = createAnalog("18 Mar 2021", "22 Apr 2017", 310, 320.0, 4800.0, 5.20, 0.85, CyclePhase.EXPANSION, "Cross-Border Liquidity Surge", 0.52f),
            sparkline = listOf(0.55, 0.56, 0.565, 0.57, 0.575, 0.58),
            whereItMovesNow = "Συσσώρευση γύρω από το επίπεδο $0.55-$0.60 με αυξανόμενο θεσμικό όγκο πληρωμών.",
            whereItMovedPast = "2014 ($0.003) -> 2017 ($0.30) -> 2018 ($3.84) -> 2021 ($1.96).",
            nextPredictedMoveNarrative = "Σε περιόδους θεσμικής ρευστότητας, οι αναλογίες δείχνουν έντονη διατήρηση μομέντουμ.",
            projectedNextMove1w = "+7.2% (69% win rate)",
            projectedNextMove2w = "+16.8% (76% win rate)",
            projectedNextMove4w = "+34.5% (84% win rate)",
            genesisDate = "02 Jun 2012", founderOrCreator = "Chris Larsen, Jed McCaleb, David Schwartz",
            consensusMechanism = "XRP Ledger Consensus Protocol (UNL Consensus)",
            whitepaperSummary = "Εξαιρετικά ταχύ, χαμηλού κόστους και ενεργειακά αποδοτικό πρωτόκολλο για διασυνοριακές πληρωμές και θεσμική ρευστότητα.",
            technologyDetails = "XRPL Federated Byzantine Agreement, 3-5 sec settlement, ενσωματωμένο Decentralized Exchange (DEX), AMM και Escrow logic.",
            tokenomicsDetails = "Σταθερό ανώτατο όριο 100 δις XRP. Τα τέλη συναλλαγών καίγονται ολοσχερώς. 40 δις κλειδωμένα σε Ripple Escrow.",
            useCases = listOf("RippleNet On-Demand Liquidity (ODL)", "Διασυνοριακές τραπεζικές εκκαθαρίσεις", "CBDC Bridging Layer", "Micro-payments")
        )
    )
}
