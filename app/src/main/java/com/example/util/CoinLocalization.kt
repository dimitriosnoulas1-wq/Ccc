package com.example.util

import com.example.data.model.AppLanguage
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.data.model.HistoricalAnalog

object CoinLocalization {

    fun getWhereItMovesNow(coin: CryptoCoin, language: AppLanguage, currency: Currency = Currency.USD): String {
        val currentFormattedPrice = coin.formattedPrice(currency)
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> "Consolidating in a post-ATH accumulation phase near $currentFormattedPrice with steady volume and macro cycle analog alignment."
                "ETH" -> "Consolidating near $currentFormattedPrice with increasing staking ratio and Layer 2 scaling absorption."
                "SOL" -> "Maintaining strength near $currentFormattedPrice with surging DEX volume and active on-chain wallets."
                "XRP" -> "Accumulating near $currentFormattedPrice as cross-border settlement liquidity expands."
                "BNB" -> "Sustaining high network transaction volume near $currentFormattedPrice and consistent deflationary quarterly token burns."
                "DOGE" -> "Forming an accumulation base near $currentFormattedPrice with periodic momentum spikes driven by social volume and memecoin cycle liquidity."
                "ADA" -> "Consolidating near $currentFormattedPrice with expanding smart contract deployments and governance."
                "SUI" -> "Demonstrating high relative momentum near $currentFormattedPrice with parallelized execution adoption and institutional TVL inflows."
                "AVAX" -> "Stabilizing around $currentFormattedPrice with expanding enterprise subnet activity and institutional tokenization pilots."
                "LINK" -> "Consolidating near $currentFormattedPrice while CCIP cross-chain interoperability transaction volume reaches new records."
                "NEAR" -> "Experiencing rising user activity near $currentFormattedPrice powered by chain abstraction and decentralized AI compute network integrations."
                "INJ" -> "Building a consolidation structure near $currentFormattedPrice with high on-chain derivatives trading volume and weekly token burns."
                "KAS" -> "Maintaining high PoW hash rate security near $currentFormattedPrice and rapid 10 BPS block DAG expansion in an accumulation channel."
                "RENDER" -> "Experiencing strong decentralized GPU rendering demand near $currentFormattedPrice across 3D rendering and distributed AI training pipelines."
                "TAO" -> "Consolidating near $currentFormattedPrice as subnet competition and decentralized machine intelligence incentives expand."
                "PEPE" -> "Maintaining strong liquidity depth near $currentFormattedPrice as a premier cultural benchmark in the memecoin category."
                else -> "Consolidating near $currentFormattedPrice with steady trading volume and cycle analog alignment."
            }
            AppLanguage.GERMAN -> when (coin.symbol) {
                "BTC" -> "Befindet sich in einer gesunden Konsolidierungsphase nahe $currentFormattedPrice bei stetigem institutionellem Zufluss."
                "ETH" -> "Akkumulation im Bereich von $currentFormattedPrice bei steigender Staking-Quote und Layer-2-Skalierung."
                "SOL" -> "Stärkt die Marktposition nahe $currentFormattedPrice durch starkes DEX-Volumen und kontinuierliche Netzwerkaktivität."
                "XRP" -> "Konsolidiert nahe $currentFormattedPrice bei wachsender grenzüberschreitender Nutzung."
                "BNB" -> "Stabile Netzwerktransaktionen nahe $currentFormattedPrice und fortlaufende deflationäre Quartals-Burns."
                "DOGE" -> "Bildet eine solide Akkumulationsbasis nahe $currentFormattedPrice mit zyklischen Momentum-Ausbrüchen."
                "ADA" -> "Akkumuliert nahe $currentFormattedPrice mit wachsendem Governance- und Smart-Contract-Ökosystem."
                "SUI" -> "Zeigt starkes relatives Momentum nahe $currentFormattedPrice durch parallele Transaktionsausführung und TVL-Zuflüsse."
                "AVAX" -> "Stabilisierung nahe $currentFormattedPrice mit wachsender Subnet-Adoption und Asset-Tokenisierung."
                "LINK" -> "Konsolidierung nahe $currentFormattedPrice bei Rekordwerten im CCIP-Cross-Chain-Protokoll."
                "NEAR" -> "Wachsende Aktivität nahe $currentFormattedPrice angetrieben durch Chain-Abstraction und dezentrale KI-Infrastruktur."
                "INJ" -> "Konsolidierung nahe $currentFormattedPrice mit starkem Derivate-Handelsvolumen."
                "KAS" -> "Hohe PoW-Hashrate nahe $currentFormattedPrice im stabilen Zykluskanal."
                "RENDER" -> "Hohe Nachfrage nahe $currentFormattedPrice nach dezentraler GPU-Rechenleistung für 3D-Rendering und KI-Training."
                "TAO" -> "Konsolidierung nahe $currentFormattedPrice im Subnetz-Wettbewerb für dezentrale maschinelle Intelligenz."
                "PEPE" -> "Hohe Liquidität nahe $currentFormattedPrice als führender Referenzwert im Meme-Sektor."
                else -> "Konsolidiert nahe $currentFormattedPrice bei stetigem Handelsvolumen und Zyklusausrichtung."
            }
            AppLanguage.FRENCH -> when (coin.symbol) {
                "BTC" -> "En phase de consolidation saine près de $currentFormattedPrice avec un volume d'échange soutenu."
                "ETH" -> "Consolidation près de $currentFormattedPrice avec un ratio de staking croissant et l'absorption des Layer 2."
                "SOL" -> "Reprise de dynamique près de $currentFormattedPrice avec une hausse des volumes DEX."
                "XRP" -> "Accumulation près de $currentFormattedPrice avec l'expansion des canaux de règlement transfrontalier."
                "BNB" -> "Maintien près de $currentFormattedPrice d'un fort volume de transactions et de burns réguliers."
                "DOGE" -> "Formation d'une base d'accumulation près de $currentFormattedPrice avec des poussées de momentum."
                "ADA" -> "Consolidation près de $currentFormattedPrice avec le déploiement de contrats intelligents et la gouvernance."
                "SUI" -> "Forte dynamique relative près de $currentFormattedPrice soutenue par l'exécution parallèle et les flux de TVL."
                "AVAX" -> "Stabilisation près de $currentFormattedPrice avec l'essor des sous-réseaux d'entreprise et de la tokenisation."
                "LINK" -> "Consolidation près de $currentFormattedPrice alors que le protocole CCIP atteint des volumes records."
                "NEAR" -> "Activité soutenue près de $currentFormattedPrice par l'abstraction de chaîne et l'IA décentralisée."
                "INJ" -> "Construction d'une structure de consolidation près de $currentFormattedPrice avec fort volume dérivés."
                "KAS" -> "Maintien d'un taux de hachage élevé près de $currentFormattedPrice en canal d'accumulation."
                "RENDER" -> "Forte demande près de $currentFormattedPrice pour les charges de travail IA et créateurs 3D."
                "TAO" -> "Consolidation près de $currentFormattedPrice avec l'expansion des sous-réseaux d'intelligence machine."
                "PEPE" -> "Forte liquidité près de $currentFormattedPrice en tant que référence culturelle du secteur mémétique."
                else -> "Consolidation près de $currentFormattedPrice avec un volume stable et un alignement cyclique."
            }
            AppLanguage.SPANISH -> when (coin.symbol) {
                "BTC" -> "En fase de consolidación cerca de $currentFormattedPrice con volumen de negociación constante."
                "ETH" -> "Consolidación cerca de $currentFormattedPrice con aumento del ratio de staking y absorción Layer 2."
                "SOL" -> "Recuperando fuerza cerca de $currentFormattedPrice con alto volumen en DEX."
                "XRP" -> "Acumulando cerca de $currentFormattedPrice mientras se expanden las liquidaciones institucionales."
                "BNB" -> "Manteniendo estabilidad cerca de $currentFormattedPrice con quemas deflacionarias trimestrales."
                "DOGE" -> "Formando una base de acumulación cerca de $currentFormattedPrice con picos periódicos de impulso."
                "ADA" -> "Consolidando cerca de $currentFormattedPrice con expansión de contratos inteligentes y gobernanza."
                "SUI" -> "Demostrando un fuerte impulso relativo cerca de $currentFormattedPrice con adopción de ejecución paralela."
                "AVAX" -> "Estabilizándose cerca de $currentFormattedPrice con expansión de subredes corporativas y tokenización."
                "LINK" -> "Consolidando cerca de $currentFormattedPrice mientras el protocolo CCIP alcanza volúmenes récord."
                "NEAR" -> "Creciente actividad cerca de $currentFormattedPrice impulsada por abstracción de cadenas e IA."
                "INJ" -> "Estructura de consolidación cerca de $currentFormattedPrice con fuerte volumen de derivados."
                "KAS" -> "Alto hashrate PoW cerca de $currentFormattedPrice en canal de acumulación del ciclo."
                "RENDER" -> "Fuerte demanda cerca de $currentFormattedPrice para renderizado 3D y modelos de IA."
                "TAO" -> "Consolidando cerca de $currentFormattedPrice ante la expansión de subredes de inteligencia automática."
                "PEPE" -> "Alta liquidez cerca de $currentFormattedPrice dentro del sector memecoin."
                else -> "Consolidando cerca de $currentFormattedPrice con volumen estable y alineación de ciclo."
            }
            AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> "In fase di consolidamento vicino a $currentFormattedPrice con volumi costanti e solido supporto ciclico."
                "ETH" -> "Consolidamento vicino a $currentFormattedPrice con tasso di staking in crescita e adozione Layer 2."
                "SOL" -> "Ripresa del momentum vicino a $currentFormattedPrice con volumi DEX in aumento."
                "XRP" -> "Accumulazione vicino a $currentFormattedPrice con l'espansione dei canali di regolamento istituzionale."
                "BNB" -> "Volume di transazioni solido vicino a $currentFormattedPrice e continui burn trimestrali."
                "DOGE" -> "Base di accumulo vicino a $currentFormattedPrice con picchi di slancio ciclico guidati dai meme."
                "ADA" -> "Consolidamento vicino a $currentFormattedPrice con espansione di smart contract e governance."
                "SUI" -> "Forte slancio relativo vicino a $currentFormattedPrice supportato da esecuzione parallela e TVL."
                "AVAX" -> "Stabilizzazione vicino a $currentFormattedPrice con crescita delle subnet aziendali e tokenizzazione."
                "LINK" -> "Consolidamento vicino a $currentFormattedPrice con volumi record sul protocollo cross-chain CCIP."
                "NEAR" -> "Attività in aumento vicino a $currentFormattedPrice trainata da chain abstraction e calcolo IA."
                "INJ" -> "Struttura di consolidamento vicino a $currentFormattedPrice con alti volumi di derivati on-chain."
                "KAS" -> "Elevato hash rate PoW vicino a $currentFormattedPrice in canale di accumulo ciclico."
                "RENDER" -> "Forte domanda vicino a $currentFormattedPrice per grafica 3D e intelligenza artificiale."
                "TAO" -> "Consolidamento vicino a $currentFormattedPrice con l'espansione delle subnet di intelligenza neurale."
                "PEPE" -> "Elevata liquidità vicino a $currentFormattedPrice nel settore memecoin."
                else -> "Consolidamento vicino a $currentFormattedPrice con volume costante e allineamento al ciclo."
            }
            AppLanguage.GREEK -> "Συσσώρευση στην περιοχή $currentFormattedPrice με σταθερό όγκο συναλλαγών και εναρμόνιση με το ιστορικό μοντέλο κύκλου."
        }
    }

    fun getWhereItMovedPast(coin: CryptoCoin, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> "2010 ($0.07) -> 2011 ($29.5) -> 2013 ($1,150) -> 2017 ($19,800) -> 2021 ($69,000) -> 2024 ($73,750)."
                "ETH" -> "2015 ($0.42) -> 2018 ($1,420) -> 2020 ($90) -> 2021 ($4,891) -> 2024 ($4,090)."
                "SOL" -> "2020 ($0.50) -> 2021 ($260) -> 2022 ($8.00) -> 2024 ($210)."
                "XRP" -> "2014 ($0.003) -> 2017 ($0.30) -> 2018 ($3.84) -> 2021 ($1.96)."
                "BNB" -> "2017 ($0.10) -> 2018 ($24) -> 2021 ($690) -> 2024 ($720)."
                "DOGE" -> "2015 ($0.00008) -> 2017 ($0.018) -> 2021 ($0.737) -> 2024 ($0.22)."
                "ADA" -> "2017 ($0.02) -> 2018 ($1.33) -> 2020 ($0.02) -> 2021 ($3.10)."
                "SUI" -> "2023 ($0.36) -> 2024 ($2.18)."
                "AVAX" -> "2020 ($2.80) -> 2021 ($146) -> 2022 ($10) -> 2024 ($65)."
                "LINK" -> "2017 ($0.12) -> 2018 ($0.30) -> 2021 ($52.8) -> 2024 ($22)."
                "NEAR" -> "2020 ($0.52) -> 2022 ($20.4) -> 2023 ($0.97) -> 2024 ($9.01)."
                "INJ" -> "2020 ($0.65) -> 2021 ($25) -> 2022 ($1.20) -> 2024 ($52.7)."
                "KAS" -> "2022 ($0.00018) -> 2023 ($0.05) -> 2024 ($0.207)."
                "RENDER" -> "2020 ($0.04) -> 2021 ($8.76) -> 2022 ($0.37) -> 2024 ($13.6)."
                "TAO" -> "2023 ($30.4) -> 2024 ($758)."
                "PEPE" -> "2023 ($0.00000006) -> 2024 ($0.000017)."
                else -> coin.whereItMovedPast
            }
            AppLanguage.GERMAN, AppLanguage.FRENCH, AppLanguage.SPANISH, AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> "2010 ($0.07) -> 2011 ($29.5) -> 2013 ($1,150) -> 2017 ($19,800) -> 2021 ($69,000) -> 2024 ($73,750)."
                "ETH" -> "2015 ($0.42) -> 2018 ($1,420) -> 2020 ($90) -> 2021 ($4,891) -> 2024 ($4,090)."
                "SOL" -> "2020 ($0.50) -> 2021 ($260) -> 2022 ($8.00) -> 2024 ($210)."
                "XRP" -> "2014 ($0.003) -> 2017 ($0.30) -> 2018 ($3.84) -> 2021 ($1.96)."
                "BNB" -> "2017 ($0.10) -> 2018 ($24) -> 2021 ($690) -> 2024 ($720)."
                "DOGE" -> "2015 ($0.00008) -> 2017 ($0.018) -> 2021 ($0.737) -> 2024 ($0.22)."
                "ADA" -> "2017 ($0.02) -> 2018 ($1.33) -> 2020 ($0.02) -> 2021 ($3.10)."
                "SUI" -> "2023 ($0.36) -> 2024 ($2.18)."
                "AVAX" -> "2020 ($2.80) -> 2021 ($146) -> 2022 ($10) -> 2024 ($65)."
                "LINK" -> "2017 ($0.12) -> 2018 ($0.30) -> 2021 ($52.8) -> 2024 ($22)."
                "NEAR" -> "2020 ($0.52) -> 2022 ($20.4) -> 2023 ($0.97) -> 2024 ($9.01)."
                "INJ" -> "2020 ($0.65) -> 2021 ($25) -> 2022 ($1.20) -> 2024 ($52.7)."
                "KAS" -> "2022 ($0.00018) -> 2023 ($0.05) -> 2024 ($0.207)."
                "RENDER" -> "2020 ($0.04) -> 2021 ($8.76) -> 2022 ($0.37) -> 2024 ($13.6)."
                "TAO" -> "2023 ($30.4) -> 2024 ($758)."
                "PEPE" -> "2023 ($0.00000006) -> 2024 ($0.000017)."
                else -> coin.whereItMovedPast
            }
            AppLanguage.GREEK -> coin.whereItMovedPast
        }
    }

    fun getNextPredictedMoveNarrative(coin: CryptoCoin, language: AppLanguage): String {
        return getCycleAlignmentNarrative(coin, language)
    }

    fun liveRealizedMoves(coin: CryptoCoin): Triple<String, String, String> {
        val change24 = if (coin.priceUpdatedAtMs > 0L) {
            String.format(java.util.Locale.US, "%+.1f%%", coin.change24h)
        } else "—"
        val spark = coin.sparkline
        val sparkMove = if (spark.size >= 2 && spark.first() > 0.0) {
            val pct = ((spark.last() - spark.first()) / spark.first()) * 100.0
            String.format(java.util.Locale.US, "%+.1f%%", pct)
        } else "—"
        val athMove = if (coin.athUsd > 0.0 && coin.priceUsd > 0.0) {
            String.format(java.util.Locale.US, "%+.1f%%", coin.drawdownPercent)
        } else "—"
        return Triple(change24, sparkMove, athMove)
    }

    fun getProbabilitiesForCoin(coin: CryptoCoin): Triple<Int, Int, Int> {
        // Paid app: never invent win-rate percentages. Callers must use live prints or "—".
        return Triple(0, 0, 0)
    }

    fun getProjected1w(coin: CryptoCoin, @Suppress("UNUSED_PARAMETER") language: AppLanguage): String {
        return liveRealizedMoves(coin).first
    }

    fun getProjected2w(coin: CryptoCoin, @Suppress("UNUSED_PARAMETER") language: AppLanguage): String {
        return liveRealizedMoves(coin).second
    }

    fun getProjected4w(coin: CryptoCoin, @Suppress("UNUSED_PARAMETER") language: AppLanguage): String {
        return liveRealizedMoves(coin).third
    }

    fun getCycleAlignmentNarrative(coin: CryptoCoin, language: AppLanguage): String {
        val name = coin.name
        val price = if (coin.priceUsd > 0.0) com.example.util.AppNumberFormatter.formatPrice(coin.priceUsd) else "—"
        val change = if (coin.priceUpdatedAtMs > 0L) String.format(java.util.Locale.US, "%+.1f%%", coin.change24h) else "—"
        return when (language) {
            AppLanguage.ENGLISH -> "$name is trading at $price ($change 24h) on the live Binance/CoinGecko feed."
            AppLanguage.GERMAN -> "$name handelt bei $price ($change 24h) im Live-Feed."
            AppLanguage.FRENCH -> "$name se négocie à $price ($change 24h) sur le flux live."
            AppLanguage.SPANISH -> "$name cotiza a $price ($change 24h) en el feed en vivo."
            AppLanguage.ITALIAN -> "$name quota $price ($change 24h) sul feed live."
            AppLanguage.GREEK -> "Το $name διαπραγματεύεται στα $price ($change 24ω) από το live feed."
        }
    }

    fun getCycleStatisticsNarrative(coin: CryptoCoin, language: AppLanguage): String {
        val symbol = coin.symbol
        val volume = if (coin.volume24h > 0.0) com.example.util.AppNumberFormatter.formatCompactCurrency(coin.volume24h) else "—"
        val mcap = if (coin.marketCap > 0.0) com.example.util.AppNumberFormatter.formatCompactCurrency(coin.marketCap) else "—"
        return when (language) {
            AppLanguage.ENGLISH -> "$symbol live 24h volume is $volume with market cap $mcap. No invented analog win-rate is shown."
            AppLanguage.GERMAN -> "$symbol Live-24h-Volumen $volume, Marktkapitalisierung $mcap."
            AppLanguage.FRENCH -> "Volume live 24h de $symbol : $volume, capitalisation $mcap."
            AppLanguage.SPANISH -> "Volumen live 24h de $symbol: $volume, capitalización $mcap."
            AppLanguage.ITALIAN -> "Volume live 24h di $symbol: $volume, market cap $mcap."
            AppLanguage.GREEK -> "Live όγκος 24ω του $symbol: $volume, κεφαλαιοποίηση $mcap."
        }
    }

    fun getWhitepaperSummary(coin: CryptoCoin, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> "Bitcoin is a decentralized peer-to-peer electronic cash system enabling direct online payments between parties without trusted financial intermediaries."
                "ETH" -> "Ethereum is a global open-source decentralized computing platform for running programmable smart contracts and decentralized applications (DApps)."
                "SOL" -> "Solana is a high-performance Layer 1 blockchain engineered for widespread global adoption, ultra-low latency, and massive throughput using Proof of History."
                "XRP" -> "XRP Ledger is an open-source decentralized settlement protocol engineered for instant, low-cost international cross-border liquidity and remittances."
                "BNB" -> "BNB Chain powers a robust dual-chain architecture providing high-speed decentralized trading and smart contract execution for Web3 dApps."
                "DOGE" -> "Dogecoin is a decentralized open-source peer-to-peer digital currency designed for friendly, accessible, low-fee everyday tipping and transactions."
                "ADA" -> "Cardano is an evidence-based proof-of-stake blockchain built on peer-reviewed academic research for security, scalability, and sustainability."
                "SUI" -> "Sui is an innovative Layer 1 blockchain engineered with an object-centric data model and parallel transaction execution for lightning-fast latency."
                "AVAX" -> "Avalanche is an ultra-fast smart contract platform with instant finality and customizable sovereign Subnet architecture for global finance."
                "LINK" -> "Chainlink is the industry-standard decentralized computing platform connecting any smart contract to verified real-world data and cross-chain networks."
                "NEAR" -> "NEAR Protocol is a sharded, developer-friendly Layer 1 blockchain designed for seamless chain abstraction and decentralized AI compute."
                "INJ" -> "Injective is a custom Layer 1 blockchain optimized specifically for decentralized finance, high-speed on-chain order books, and derivatives."
                "KAS" -> "Kaspa is an ultra-fast proof-of-work cryptocurrency utilizing the GHOSTDAG blockDAG consensus engine for instantaneous confirmations."
                "RENDER" -> "Render Network connects creators needing GPU compute power with node operators providing distributed rendering capacity."
                "TAO" -> "Bittensor is an open-source protocol powering a decentralized machine learning network with competitive intelligence subnets."
                "PEPE" -> "Pepe is a decentralized deflationary cultural memecoin celebrating the iconic internet cartoon character with deep community liquidity."
                "XLM" -> "Stellar is an open-source, decentralized payment network engineered to connect financial institutions, banks, and payment systems globally with sub-cent transaction fees."
                "HYPE" -> "Hyperliquid is a high-performance perpetual DEX and Layer 1 where on-chain open interest, funding, and liquidations are public."
                else -> "${coin.name} is a leading digital asset operating in the ${coin.category.displayName} sector, providing decentralized protocol functionality."
            }
            AppLanguage.GERMAN -> when (coin.symbol) {
                "BTC" -> "Bitcoin ist ein dezentrales Peer-to-Peer-Zahlungssystem, das direkte Online-Zahlungen ohne Intermediäre ermöglicht."
                "ETH" -> "Ethereum ist eine globale dezentrale Plattform zur Ausführung programmierbarer Smart Contracts und DApps."
                "SOL" -> "Solana ist eine Hochleistungs-Blockchain mit Proof-of-History für maximale Skalierbarkeit und minimale Gebühren."
                "XRP" -> "XRP Ledger ist ein dezentrales Open-Source-Protokoll für sekundenschnelle internationale Abwicklungen."
                "BNB" -> "BNB Chain bietet eine schnelle Plattform für dApps und den BNB-Ecosystem-Handel."
                "DOGE" -> "Dogecoin ist eine dezentrale Open-Source-Kryptowährung für benutzerfreundliche Trinkgelder und weltweite Peer-to-Peer-Zahlungen."
                "ADA" -> "Cardano ist eine wissenschaftlich entwickelte Proof-of-Stake-Blockchain basierend auf Peer-Review-Forschung für maximale Sicherheit und Nachhaltigkeit."
                "SUI" -> "Sui ist eine hochmoderne Layer-1-Blockchain mit objektzentriertem Datenmodell und paralleler Transaktionsausführung."
                "AVAX" -> "Avalanche ist eine extrem schnelle Smart-Contract-Plattform mit Sub-Sekunden-Finalität und anpassbaren souveränen Subnetzen."
                "LINK" -> "Chainlink ist das marktführende dezentrale Orakel-Netzwerk, das Smart Contracts mit realen Daten und kettenübergreifender Interoperabilität (CCIP) verbindet."
                "NEAR" -> "NEAR Protocol ist eine skalierbare Layer-1-Blockchain für nahtlose Chain-Abstraction und dezentrale KI-Infrastruktur."
                "INJ" -> "Injective ist eine maßgeschneiderte Layer-1-Blockchain, die speziell für dezentrale Finanzen, On-Chain-Orderbücher und Derivate optimiert ist."
                "KAS" -> "Kaspa ist eine ultra-schnelle Proof-of-Work-Kryptowährung mit GHOSTDAG-BlockDAG-Konsens für sofortige Bestätigungen."
                "RENDER" -> "Render Network verbindet 3D-Kreative und KI-Entwickler mit dezentralen GPU-Rechenknoten weltweit."
                "TAO" -> "Bittensor ist ein Open-Source-Protokoll, das ein dezentrales maschinelles Lernnetzwerk mit spezialisierten KI-Subnetzen betreibt."
                "PEPE" -> "Pepe ist ein deflationärer Meme-Coin mit tiefgreifender Community-Liquidität und breiter Marktbeachtung."
                "XLM" -> "Stellar ist ein dezentrales Zahlungsnetzwerk, das Finanzinstitute, Banken und Menschen weltweit mit minimalen Transaktionsgebühren verbindet."
                "HYPE" -> "Hyperliquid ist eine hochperformante Perpetual-DEX und Layer-1 mit öffentlichen Open-Interest-, Funding- und Liquidationsdaten."
                else -> "${coin.name} ist ein führendes digitales Krypto-Asset mit dezentralen Netzwerkfunktionen und Smart-Contract-Architektur."
            }
            AppLanguage.FRENCH -> when (coin.symbol) {
                "BTC" -> "Bitcoin est un système d'argent électronique pair-à-pair décentralisé permettant des paiements sans intermédiaire."
                "ETH" -> "Ethereum est une plateforme informatique décentralisée mondiale pour les contrats intelligents et DApps."
                "SOL" -> "Solana est une blockchain haute performance utilisant Proof of History pour un débit élevé et une faible latence."
                "XRP" -> "Le registre XRP est un protocole de règlement décentralisé optimisé pour les transactions transfrontalières."
                "BNB" -> "BNB Chain est une infrastructure pour applications décentralisées et trading Web3 à haute cadence."
                "XLM" -> "Stellar est un réseau de paiement décentralisé reliant les banques et systèmes financiers avec des frais quasi nuls."
                "HYPE" -> "Hyperliquid est un DEX de perpétuels et une Layer 1 haute performance dont l'open interest, le funding et les liquidations sont publics."
                else -> "${coin.name} est un actif numérique du secteur ${coin.category.displayName} offrant des services décentralisés."
            }
            AppLanguage.SPANISH -> when (coin.symbol) {
                "BTC" -> "Bitcoin es un sistema de dinero electrónico peer-to-peer descentralizado que permite pagos directos sin intermediarios."
                "ETH" -> "Ethereum es una plataforma informática descentralizada global para ejecutar contratos inteligentes programables y dApps."
                "SOL" -> "Solana es una blockchain de alto rendimiento con Proof of History para máximo rendimiento y baja latencia."
                "XRP" -> "XRP Ledger es un protocolo de liquidación descentralizado diseñado para pagos transfronterizos instantáneos."
                "BNB" -> "BNB Chain es una infraestructura rápida para aplicaciones Web3 y el ecosistema BNB."
                "XLM" -> "Stellar es una red de pagos descentralizada diseñada para conectar bancos y sistemas financieros con tarifas mínimas."
                "HYPE" -> "Hyperliquid es un DEX de perpetuos y una Layer 1 de alto rendimiento con open interest, funding y liquidaciones públicas."
                else -> "${coin.name} es un criptoactivo en el sector ${coin.category.displayName} con arquitectura descentralizada."
            }
            AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> "Bitcoin è un sistema monetario elettronico peer-to-peer decentralizzato per pagamenti diretti senza intermediari."
                "ETH" -> "Ethereum è una piattaforma informatica decentralizzata per smart contract programmabili e applicazioni Web3."
                "SOL" -> "Solana è una blockchain Layer 1 ad alte prestazioni che sfrutta Proof of History per transazioni veloci ed economiche."
                "XRP" -> "XRP Ledger è un protocollo di regolamento decentralizzato per transazioni internazionali istantanee."
                "BNB" -> "BNB Chain offre una rete veloce e scalabile per contratti intelligenti e finanza decentralizzata."
                "XLM" -> "Stellar è un protocollo di pagamento decentralizzato per connettere banche e sistemi finanziari con costi minimi."
                "HYPE" -> "Hyperliquid è un DEX di perpetual e una Layer 1 ad alte prestazioni con open interest, funding e liquidazioni pubblici."
                else -> "${coin.name} è una risorsa digitale nel settore ${coin.category.displayName} con funzionalità di rete decentralizzata."
            }
            AppLanguage.GREEK -> coin.whitepaperSummary
        }
    }

    fun getTechnologyDetails(coin: CryptoCoin, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> "Employs SHA-256 blockchain consensus, 1MB–4MB block weight (SegWit/Taproot), 10-minute average block time, and Lightning Network Layer 2."
                "ETH" -> "EVM (Ethereum Virtual Machine), Danksharding / EIP-4844 Blobs, 12-second slot time, and Layer 2 rollup scaling."
                "SOL" -> "Proof of History (PoH) coupled with Tower BFT, Sealevel parallel runtime engine, Turbine block propagation, and Gulf Stream mempool-less protocol."
                "XRP" -> "XRP Ledger Consensus Protocol (UNL federated Byzantine agreement), 3–5 second settlement finality, 1,500+ TPS natively without PoW."
                "BNB" -> "Parlia Proof of Staked Authority (PoSA), EVM compatibility, 3-second block generation time, and opBNB optimistic rollup scaling."
                else -> "${coin.consensusMechanism} architecture with decentralized node validation and high cryptographical security."
            }
            AppLanguage.GERMAN -> when (coin.symbol) {
                "BTC" -> "SHA-256 Proof-of-Work, SegWit/Taproot, 10 Minuten Blockzeit und Lightning Network Layer 2."
                "ETH" -> "EVM, EIP-4844 Blob-Transaktionen, 12 Sekunden Slot-Zeit und Layer-2-Rollup-Roadmap."
                "SOL" -> "Proof of History mit Tower BFT und paralleler Sealevel-Transaktionsverarbeitung."
                else -> "${coin.consensusMechanism} Architektur mit dezentraler Knotenvalidierung und hoher Sicherheit."
            }
            AppLanguage.FRENCH -> when (coin.symbol) {
                "BTC" -> "Consensus SHA-256, blocs SegWit/Taproot, temps de bloc moyen de 10 minutes et réseau Lightning Layer 2."
                "ETH" -> "EVM, blobs EIP-4844, temps d'intervalle de 12 secondes et mise à l'échelle via Rollups Layer 2."
                "SOL" -> "Proof of History avec Tower BFT et moteur d'exécution parallèle Sealevel."
                else -> "Architecture ${coin.consensusMechanism} avec validation décentralisée des nœuds."
            }
            AppLanguage.SPANISH -> when (coin.symbol) {
                "BTC" -> "Consenso SHA-256, bloques SegWit/Taproot, tiempo de bloque de 10 minutos y red Lightning Layer 2."
                "ETH" -> "EVM, blobs EIP-4844, tiempo de slot de 12 segundos y escalado centrado en Rollups Layer 2."
                "SOL" -> "Proof of History combinado con Tower BFT y ejecución paralela Sealevel."
                else -> "Arquitectura ${coin.consensusMechanism} con validación descentralizada de nodos."
            }
            AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> "Consenso SHA-256, blocchi SegWit/Taproot, tempo medio del blocco di 10 minuti e Lightning Network Layer 2."
                "ETH" -> "EVM, blob EIP-4844, slot time di 12 secondi e roadmap basata sui rollup Layer 2."
                "SOL" -> "Proof of History combinato con Tower BFT ed esecuzione parallela Sealevel."
                else -> "Architettura ${coin.consensusMechanism} con validazione crittografica e nodi decentralizzati."
            }
            AppLanguage.GREEK -> coin.technologyDetails
        }
    }

    fun getTokenomicsDetails(coin: CryptoCoin, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> "Hard cap of 21,000,000 BTC. Halving occurs every 210,000 blocks (~4 years). Current block reward is 3.125 BTC."
                "ETH" -> "EIP-1559 base fee burning. Dynamic supply with deflationary tendencies during high network utilization; ~3.4% staking APR."
                "SOL" -> "Disinflationary issuance model starting at 8% with 15% annual reduction down to a 1.5% long-term floor. 50% of base tx fees burned."
                "XRP" -> "Fixed total supply of 100 Billion XRP created at genesis. Small transaction fees are permanently burned on every transaction."
                "BNB" -> "Strict programmatic Auto-Burn formula targeting a final supply of 100,000,000 BNB with real-time BEP-95 gas burning."
                else -> "Fixed or disinflationary token economics designed for ecosystem utility and staking incentives."
            }
            AppLanguage.GERMAN -> when (coin.symbol) {
                "BTC" -> "Feste Obergrenze von 21.000.000 BTC. Halving alle 210.000 Blöcke (~4 Jahre). Aktuelle Blockbelohnung: 3,125 BTC."
                "ETH" -> "EIP-1559 Gebührenverbrennung. Dynamisches Angebot mit deflationärer Tendenz bei hoher Netzwerkauslastung."
                "SOL" -> "Disinflationäres Modell mit langfristigem Inflationsziel von 1,5% und 50% Gebühren-Burn."
                else -> "Solide Tokenomics-Struktur für Netzwerknutzen und Protokollanreize."
            }
            AppLanguage.FRENCH -> when (coin.symbol) {
                "BTC" -> "Plafond strict de 21 000 000 BTC. Halving tous les 210 000 blocs (~4 ans). Récompense actuelle: 3,125 BTC."
                "ETH" -> "Destruction des frais de base EIP-1559. Offre dynamique déflationniste lors des pics d'utilisation."
                "SOL" -> "Modèle désinflationniste visant un plancher de 1,5% avec combustion de 50% des frais de base."
                else -> "Structure de tokenomics conçue pour l'utilité du réseau et les incitations du protocole."
            }
            AppLanguage.SPANISH -> when (coin.symbol) {
                "BTC" -> "Límite estricto de 21,000,000 BTC. Halving cada 210,000 bloques (~4 años). Recompensa actual de 3.125 BTC."
                "ETH" -> "Quema de comisiones base EIP-1559. Oferta dinámica con tendencia deflacionaria y staking APR de ~3.4%."
                "SOL" -> "Emisión desinflacionaria con suelo a largo plazo del 1.5% y quema del 50% de comisiones base."
                else -> "Modelo de tokenomics diseñado para incentivos de red y utilidad del ecosistema."
            }
            AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> "Limite fisso di 21.000.000 BTC. Halving ogni 210.000 blocchi (~4 anni). Ricompensa attuale di blocco: 3.125 BTC."
                "ETH" -> "Meccanismo di burning delle commissioni EIP-1559. Offerta dinamica deflazionistica con alta attività di rete."
                "SOL" -> "Emissione disinflazionistica con obiettivo di lungo periodo a 1,5% e burning del 50% delle fee di base."
                else -> "Tokenomics strutturata per sostenere la sicurezza di rete e l'utilità del protocollo."
            }
            AppLanguage.GREEK -> coin.tokenomicsDetails
        }
    }

    fun getUseCases(coin: CryptoCoin, language: AppLanguage): List<String> {
        return when (language) {
            AppLanguage.ENGLISH -> when (coin.symbol) {
                "BTC" -> listOf("Store of Value (Digital Gold)", "Decentralized Settlement Layer", "Censorship-resistant international wealth transfer", "Corporate & Sovereign treasury reserve asset")
                "ETH" -> listOf("Smart Contracts & DApps", "DeFi Collateral & Global Settlement", "Layer 2 Security Base Layer", "NFT & Token issuance standard (ERC-20/721)")
                "SOL" -> listOf("Ultra-fast DEX & Order Book Trading", "High-frequency DeFi & DePIN infrastructure", "Consumer Web3 microtransactions & Gaming", "Tokenized Real-World Assets")
                "XRP" -> listOf("Cross-border bank remittances", "On-Demand Liquidity (ODL) bridge currency", "CBDC and sovereign digital asset issuance", "Instant low-cost global transfers")
                "BNB" -> listOf("Gas fees on BNB Chain and opBNB", "Discounted trading fees on Binance", "DeFi liquidity provision and staking", "Launchpad token sales participation")
                "DOGE" -> listOf("Peer-to-peer tipping and payments", "Microtransactions on social platforms", "Merchant payments and retail settlements", "Decentralized cultural liquidity asset")
                "ADA" -> listOf("Decentralized identity and governance", "Secure smart contract applications", "Enterprise supply chain tracking", "Global financial inclusion systems")
                "SUI" -> listOf("High-throughput Web3 gaming & Dynamic NFTs", "Institutional DeFi trading protocols", "Mass consumer social media dApps", "High-speed parallel commerce payments")
                "AVAX" -> listOf("Custom sovereign Enterprise Subnets", "Institutional asset tokenization (RWA)", "Low-latency DeFi protocols", "Cross-subnet interoperable finance")
                "LINK" -> listOf("Decentralized Price & Data Oracles", "CCIP Cross-Chain Asset Transfers", "Proof of Reserve verification", "Automated smart contract functions")
                else -> listOf("Decentralized network utility", "Staking & governance participation", "Transaction fee settlement", "Ecosystem dApp integration")
            }
            AppLanguage.GERMAN -> when (coin.symbol) {
                "BTC" -> listOf("Wertaufbewahrung (Digitales Gold)", "Dezentrales Settlement-Layer", "Zensurresistente internationale Wertübertragungen", "Unternehmens- & Staatsreserve")
                "ETH" -> listOf("Smart Contracts & DApps", "DeFi-Sicherheiten & Settlement", "Sicherheits-Basis für Layer-2-Rollups", "Token-Standardisierung (ERC-20/721)")
                "SOL" -> listOf("Hochgeschwindigkeits-DEX-Trading", "DeFi- & DePIN-Infrastruktur", "Web3-Mikrotransaktionen & Gaming", "Tokenisierung realer Vermögenswerte")
                else -> listOf("Dezentrale Netzwerk-Utility", "Staking & Governance-Beteiligung", "Transaktionsgebühren-Zahlung", "Ökosystem-Integration")
            }
            AppLanguage.FRENCH -> when (coin.symbol) {
                "BTC" -> listOf("Réserve de valeur (Or numérique)", "Couche de règlement décentralisée", "Transferts internationaux incensurables", "Actif de réserve institutionnel")
                "ETH" -> listOf("Contrats intelligents & DApps", "Garantie DeFi & Règlement global", "Sécurité de base pour Layer 2", "Standard d'émission de jetons")
                "SOL" -> listOf("Trading DEX ultra-rapide", "Infrastructures DeFi & DePIN", "Microtransactions Web3 & Gaming", "Tokenisation d'actifs réels")
                else -> listOf("Utilitaire de réseau décentralisé", "Participation au staking et gouvernance", "Règlement des frais de transaction", "Intégration d'applications Web3")
            }
            AppLanguage.SPANISH -> when (coin.symbol) {
                "BTC" -> listOf("Reserva de valor (Oro digital)", "Capa de liquidación descentralizada", "Transferencias internacionales sin censura", "Activo de reserva corporativo y estatal")
                "ETH" -> listOf("Contratos inteligentes y DApps", "Colateral DeFi y liquidación global", "Capa base de seguridad para Layer 2", "Estándar de emisión de tokens")
                "SOL" -> listOf("Trading DEX de alta velocidad", "Infraestructura DeFi y DePIN", "Microtransacciones Web3 y juegos", "Tokenización de activos del mundo real")
                else -> listOf("Utilidad en red descentralizada", "Participación en staking y gobernanza", "Liquidación de comisiones de red", "Integración con dApps")
            }
            AppLanguage.ITALIAN -> when (coin.symbol) {
                "BTC" -> listOf("Riserva di valore (Oro digitale)", "Layer di regolamento decentralizzato", "Trasferimenti internazionali senza censura", "Attivo di riserva per tesorerie")
                "ETH" -> listOf("Smart contract e applicazioni DApp", "Garanzia DeFi e liquidazione globale", "Sicurezza di base per reti Layer 2", "Standard di emissione token")
                "SOL" -> listOf("Trading ultra-veloce su DEX", "Infrastruttura DeFi e DePIN", "Microtransazioni Web3 e Gaming", "Tokenizzazione di asset reali (RWA)")
                else -> listOf("Utility di rete decentralizzata", "Partecipazione a staking e governance", "Pagamento commissioni di rete", "Integrazione nell'ecosistema")
            }
            AppLanguage.GREEK -> coin.useCases
        }
    }

    fun getDaysAfterHighSubtitle(coin: CryptoCoin, daysAfterAth: Int, typicalDays: Int, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> "We are $daysAfterAth days after ${coin.symbol}'s high. Past ${coin.symbol} cycle corrections lasted about $typicalDays days."
            AppLanguage.GREEK -> "Είμαστε $daysAfterAth ημέρες μετά το ιστορικό υψηλό του ${coin.symbol}. Οι ιστορικές διορθώσεις του ${coin.symbol} διήρκεσαν περίπου $typicalDays ημέρες."
            AppLanguage.GERMAN -> "Wir sind $daysAfterAth Tage nach dem Hoch von ${coin.symbol}. Frühere ${coin.symbol}-Korrekturen dauerten etwa $typicalDays Tage."
            AppLanguage.FRENCH -> "Nous sommes à $daysAfterAth jours du sommet de ${coin.symbol}. Les corrections passées de ${coin.symbol} ont duré environ $typicalDays jours."
            AppLanguage.SPANISH -> "Estamos a $daysAfterAth días del máximo de ${coin.symbol}. Las correcciones pasadas de ${coin.symbol} duraron unos $typicalDays días."
            AppLanguage.ITALIAN -> "Siamo a $daysAfterAth giorni dal massimo di ${coin.symbol}. Le correzioni storiche di ${coin.symbol} sono durate circa $typicalDays giorni."
        }
    }

    fun getFromHighToLowNarrative(
        coin: CryptoCoin,
        daysAfterAth: Int,
        daysToBottom: Int,
        typicalDays: Int,
        isBottomReached: Boolean,
        currency: Currency,
        language: AppLanguage
    ): String {
        val athFormatted = coin.formattedAth(currency)
        return if (!isBottomReached) {
            when (language) {
                AppLanguage.ENGLISH -> "We are $daysAfterAth days after the all-time high of $athFormatted. Historically, pullbacks to the bottom lasted ~$typicalDays days. Estimated $daysToBottom days to a potential bottom zone."
                AppLanguage.GREEK -> "Βρισκόμαστε $daysAfterAth ημέρες μετά το ιστορικό υψηλό των $athFormatted. Ιστορικά, η πορεία στον πυθμένα διήρκεσε ~$typicalDays ημέρες. Απομένουν περίπου $daysToBottom ημέρες για πιθανή ζώνη πυθμένα."
                AppLanguage.GERMAN -> "Wir befinden uns $daysAfterAth Tage nach dem Allzeithoch von $athFormatted. Historisch dauerte der Weg zum Tief ~$typicalDays Tage. Noch ca. $daysToBottom Tage bis zu einem möglichen Tiefpunkt."
                AppLanguage.FRENCH -> "Nous sommes à $daysAfterAth jours du record de $athFormatted. Historiquement, le creux a duré ~$typicalDays jours. Environ $daysToBottom jours avant un creux potentiel."
                AppLanguage.SPANISH -> "Estamos a $daysAfterAth días del máximo de $athFormatted. Históricamente, la caída al suelo duró ~$typicalDays días. Faltan unos $daysToBottom días para un posible suelo."
                AppLanguage.ITALIAN -> "Siamo a $daysAfterAth giorni dal massimo di $athFormatted. Storicamente, il calo al minimo è durato ~$typicalDays giorni. Rimangono circa $daysToBottom giorni per un possibile minimo."
            }
        } else {
            when (language) {
                AppLanguage.ENGLISH -> "We are $daysAfterAth days after the all-time high of $athFormatted. The historical ~$typicalDays-day correction cycle has completed. ${coin.symbol} is currently in the Cycle Accumulation & Expansion phase."
                AppLanguage.GREEK -> "Βρισκόμαστε $daysAfterAth ημέρες μετά το ιστορικό υψηλό των $athFormatted. Ο ιστορικός κύκλος διόρθωσης (~$typicalDays ημ.) έχει ολοκληρωθεί. Το ${coin.symbol} βρίσκεται στη φάση Συσσώρευσης & Προετοιμασίας Ανόδου."
                AppLanguage.GERMAN -> "Wir befinden uns $daysAfterAth Tage nach dem Allzeithoch von $athFormatted. Der historische ~$typicalDays-Tage-Korrekturzyklus ist abgeschlossen. ${coin.symbol} befindet sich in der Akkumulations- & Expansionsphase."
                AppLanguage.FRENCH -> "Nous sommes à $daysAfterAth jours du record de $athFormatted. Le cycle historique de correction (~$typicalDays jours) est terminé. ${coin.symbol} est en phase d'accumulation et d'expansion."
                AppLanguage.SPANISH -> "Estamos a $daysAfterAth días del máximo de $athFormatted. El ciclo histórico de corrección (~$typicalDays días) se ha completado. ${coin.symbol} está en fase de acumulación y expansión."
                AppLanguage.ITALIAN -> "Siamo a $daysAfterAth giorni dal massimo di $athFormatted. Il ciclo storico di correzione (~$typicalDays giorni) è completato. ${coin.symbol} è in fase di accumulazione ed espansione."
            }
        }
    }

    fun getLocalizedPhaseName(analog: HistoricalAnalog, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (analog.cyclePhase) {
                com.example.data.model.CyclePhase.ACCUMULATION -> "Accumulation Phase"
                com.example.data.model.CyclePhase.EXPANSION -> "Halving Expansion Wave"
                com.example.data.model.CyclePhase.EUPHORIA_TOP -> "Euphoria Peak Window"
                com.example.data.model.CyclePhase.BEAR_CAPITULATION -> "Cycle Cooldown Phase"
            }
            AppLanguage.GERMAN -> when (analog.cyclePhase) {
                com.example.data.model.CyclePhase.ACCUMULATION -> "Akkumulationsphase"
                com.example.data.model.CyclePhase.EXPANSION -> "Halving-Expansionswelle"
                com.example.data.model.CyclePhase.EUPHORIA_TOP -> "Euphorie-Spitzenzone"
                com.example.data.model.CyclePhase.BEAR_CAPITULATION -> "Zyklus-Korrekturphase"
            }
            AppLanguage.FRENCH -> when (analog.cyclePhase) {
                com.example.data.model.CyclePhase.ACCUMULATION -> "Phase d'accumulation"
                com.example.data.model.CyclePhase.EXPANSION -> "Vague d'expansion post-Halving"
                com.example.data.model.CyclePhase.EUPHORIA_TOP -> "Zone d'euphorie maximale"
                com.example.data.model.CyclePhase.BEAR_CAPITULATION -> "Phase de refroidissement"
            }
            AppLanguage.SPANISH -> when (analog.cyclePhase) {
                com.example.data.model.CyclePhase.ACCUMULATION -> "Fase de acumulación"
                com.example.data.model.CyclePhase.EXPANSION -> "Ola de expansión post-Halving"
                com.example.data.model.CyclePhase.EUPHORIA_TOP -> "Zona de euforia máxima"
                com.example.data.model.CyclePhase.BEAR_CAPITULATION -> "Fase de reajuste del ciclo"
            }
            AppLanguage.ITALIAN -> when (analog.cyclePhase) {
                com.example.data.model.CyclePhase.ACCUMULATION -> "Fase di accumulazione"
                com.example.data.model.CyclePhase.EXPANSION -> "Onda di espansione post-Halving"
                com.example.data.model.CyclePhase.EUPHORIA_TOP -> "Zona di picco euforico"
                com.example.data.model.CyclePhase.BEAR_CAPITULATION -> "Fase di reset del ciclo"
            }
            AppLanguage.GREEK -> analog.cyclePhaseName
        }
    }
}
