package com.example.data.repository

import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin

object CoinDataRegistry {

    fun getAllCoins(): List<CryptoCoin> = catalog().map { it.withoutSeedQuote() }

    private fun catalog(): List<CryptoCoin> = listOf(
        // 1. BITCOIN
        CryptoCoin(
            id = "bitcoin", symbol = "BTC", name = "Bitcoin", rank = 1,
            priceUsd = 77250.00, athUsd = 73750.07, athDaysAgo = 160, athDate = "14 Mar 2024",
            atlUsd = 0.0707, atlDate = "13 Aug 2010", change24h = -0.32, volume24h = 42800000000.0,
            marketCap = 1520000000000.0, circulatingSupply = 19780000.0, totalSupply = 19780000.0,
            maxSupply = 21000000.0, supplyUnit = "BTC", category = CoinCategory.LAYER1,
            isPro = true,
            sparkline = listOf(75800.0, 76400.0, 76100.0, 77200.0, 76900.0, 77400.0, 77250.0),
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
            sparkline = listOf(2540.0, 2580.0, 2610.0, 2590.0, 2630.0, 2650.0, 2650.0),
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
            sparkline = listOf(138.5, 140.2, 142.1, 141.5, 144.0, 145.5, 145.5),
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
            sparkline = listOf(565.0, 570.0, 578.0, 572.0, 580.0, 584.2),
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
            sparkline = listOf(0.55, 0.56, 0.565, 0.57, 0.575, 0.58),
            genesisDate = "02 Jun 2012", founderOrCreator = "Chris Larsen, Jed McCaleb, David Schwartz",
            consensusMechanism = "XRP Ledger Consensus Protocol (UNL Consensus)",
            whitepaperSummary = "Εξαιρετικά ταχύ, χαμηλού κόστους και ενεργειακά αποδοτικό πρωτόκολλο για διασυνοριακές πληρωμές και θεσμική ρευστότητα.",
            technologyDetails = "XRPL Federated Byzantine Agreement, 3-5 sec settlement, ενσωματωμένο Decentralized Exchange (DEX), AMM και Escrow logic.",
            tokenomicsDetails = "Σταθερό ανώτατο όριο 100 δις XRP. Τα τέλη συναλλαγών καίγονται ολοσχερώς. 40 δις κλειδωμένα σε Ripple Escrow.",
            useCases = listOf("RippleNet On-Demand Liquidity (ODL)", "Διασυνοριακές τραπεζικές εκκαθαρίσεις", "CBDC Bridging Layer", "Micro-payments")
        )
    )
}
