package com.example.util

import androidx.compose.runtime.compositionLocalOf
import com.example.data.model.AppLanguage
import com.example.data.model.CoinCategory

open class AppStrings(
    open val language: AppLanguage = AppLanguage.ENGLISH
) {
    // Navigation & Core
    open val navMarkets: String = "Cycle"
    open val navCoins: String = "Coins"
    open val navSignals: String = "Tape"
    open val navLearn: String = "Learn"
    open val navSettings: String = "Settings"
    open val appTitle: String = "CryptoCycles"
    open val proBadge: String = "Pro"
    open val proActive: String = "PRO ACTIVE"
    open val upgradeToPro: String = "Upgrade to Pro"
    open val viewDetails: String = "View details"
    open val searchPlaceholder: String = "Search 100+ coins..."
    open val cryptocurrenciesCount: String = "CRYPTOCURRENCIES"
    open val cryptocurrencySingular: String = "CRYPTOCURRENCY"
    open val rankPrefix: String = "Rank"
    open val closeButton: String = "Close"
    open val institutionalTerminal: String = "Professional Cycle & Derivatives Terminal"
    open val latencyMs: String = "%dms latency"
    open val realtimeFeed: String = "REALTIME FEED"
    open val refreshTooltip: String = "Refresh market feeds"

    // Coin Categories
    open val catAll: String = "100+"
    open val catLayer1: String = "Layer 1"
    open val catLayer2: String = "Layer 2"
    open val catDefi: String = "DeFi"
    open val catAiInfra: String = "AI & Infra"
    open val catMeme: String = "Memes"
    open val catRwaDepin: String = "RWA & DePIN"
    open val catUtility: String = "Utility"
    open val catFavorites: String = "Favorites"

    // Markets Screen & Teasers
    open val marketsHeader: String = "Market Overview"
    open val marketsSubtitle: String = "Where each coin is moving, past historical patterns, future projections & whitepapers."
    open val currentPrice: String = "Current Price"
    open val analogMatch: String = "Analog Match"
    open val cyclePhase: String = "Cycle Phase"
    open val daysSinceAth: String = "Days since ATH"
    

    // Historical Cycle Analog
    open val historicalAnalogHeader: String = "HISTORICAL CYCLE ANALOG"
    open val historicalAnalogProTitle: String = "Historical Cycle Analog"
    open val historicalAnalogProUnlockedDesc: String = "Live exchange history and realized moves are active."
    open val historicalAnalogProDesc: String = "Unlock live history charts and realized move cards for all coins."
    open val movementAnalysisHeader: String = "MOVEMENT ANALYSIS & CYCLE HISTORY"
    open val nextMoveHeader: String = "LIVE REALIZED MOVES"
    open val cycleFractalLabel: String = "Cycle Fractal Day"
    open val historicalMatchLabel: String = "How price moved from one halving to the next"
    open val cycleChartTapToInspect: String = "Tap or drag to read a cycle day"
    open val cycleChartDayLabel: String = "Day"
    open val cycleChartModeCurrent: String = "2024"
    open val cycleChartModeAll: String = "All"
    open val cycleChartModeProjection: String = "Projected Corridor"
    open val cycleChartCurrentPoint: String = "2024"
    open val cycleChartProjectedPath: String = "Projected Fractal Path"
    open val proAnalyticsDesc: String = "Unlock the 2016 and 2020 lines on the same day-count for"

    // Market Stats & Token Supply
    open val marketStatsHeader: String = "MARKET STATS & TOKEN SUPPLY"
    open val circulatingSupplyLabel: String = "Circulating Supply:"
    open val circulatingShort: String = "Circulating"
    open val infiniteDeflationary: String = "Infinite (Deflationary)"
    open val marketCapMetric: String = "MARKET CAP"
    open val volume24hMetric: String = "24H VOLUME"
    open val tradingVolumeSub: String = "Trading volume"
    open val fdvMetric: String = "FDV (VALUATION)"
    open val fdvSub: String = "Fully diluted value"
    open val athMetric: String = "ALL-TIME HIGH"
    open val atlMetric: String = "ALL-TIME LOW"
    open val drawdownAthMetric: String = "ATH DRAWDOWN"
    open val fromPeakSub: String = "From apex peak"
    open val allTimeLowMetric: String = "All-time low"
    open val allTimeHighMetric: String = "All-time high"
    open val startMetric: String = "Start"
    open val daysSinceHighMetric: String = "Days since the high"

    // Whitepaper & Technology Blueprint
    open val whitepaperSectionHeader: String = "WHITEPAPER & TECHNOLOGY BLUEPRINT"
    open val founderLabel: String = "CREATOR / FOUNDER"
    open val genesisDateLabel: String = "GENESIS DATE"
    open val whatCoinDoesLabel: String = "What the coin does (Core Whitepaper Blueprint)"
    open val tokenomicsLabel: String = "Tokenomics, Halving & Emission"
    open val useCasesLabel: String = "Core Ecosystem Use Cases"

    // Cycle Analysis Deep Dive
    open val weekOfFallTitle: String = "Weeks since the all-time high"
    open val cycleAnalysisHeader: String = "Cycle analysis"
    open val fromHighToLowTitle: String = "FROM THE HIGH TO A POSSIBLE LOW"
    open val daysAfterAthLabel: String = "days after the all-time high"
    open val daysToBottomLabel: String = "days, past corrections (avg)"
    open val highMarker: String = "High"
    open val fromHighToLowDisclaimer: String = "This is what past cycles did. It is not a promise — only a clock."
    open val thisCycleVsLastTitle: String = "THIS CYCLE VS THE LAST ONES"
    open val daysAfterHighTitle: String = "Days after the high"
    open val nowLabel: String = "Now"
    open val daysOfRiseTitle: String = "Days of a rise, historically"
    open val daysOfRiseSub: String = "Past rises from a low to the next high lasted about 1059 days."
    open val halvingTitle: String = "BITCOIN HALVING"
    open val halvingDaysLabel: String = "days to next halving"
    open val halvingHoursLabel: String = "hours remaining"
    open val whyWeSayThisTitle: String = "Live Market Context"

    // Signals Screen
    open val signalsCoinSelectorTitle: String = "Cycle Asset"
    open val signalsAllCoins: String = "All 100+ Coins"
    open val timeframe1D: String = "1D"
    open val timeframe1W: String = "1W"
    open val timeframe1M: String = "1M"
    open val timeframe1Y: String = "1Y"
    open val timeframeCycle: String = "Macro Cycle"
    open val signalBullish: String = "24h UP"
    open val signalBearish: String = "24h DOWN"

    // Settings Screen
    open val settingsHeader: String = "Settings"
    open val languageTitle: String = "Language"
    open val languageDesc: String = "Select your preferred language"
    open val optionsTitle: String = "Options"
    open val soundEffectsTitle: String = "Crypto Sound Effects (SFX)"
    open val soundEffectsSub: String = "Short sounds when you tap, switch and navigate"
    open val soundTestTitle: String = "Test Audio FX"
    open val soundTestLaunch: String = "Major Scale Ascent 🎵"
    open val soundTestClick: String = "Tech Tap"
    open val soundTestToggle: String = "Toggle Chirp"
    open val soundTestChime: String = "Coin Reward Chime"
    open val logChartsTitle: String = "Logarithmic Charts"
    open val logChartsSub: String = "Display historical prices on log scaling to normalize percentage changes"
    open val proMembershipTitle: String = "Pro Membership"
    open val manageProSubscription: String = "Manage Pro Subscription"
    open val proTapeTitle: String = "PRO TAPE READING"
    open val proTapeDesc: String = "Unlock the live RSI, EMA, ATR, funding and ETF reading from daily exchange closes. Not a forecast."
    open val proBenefit1: String = "2016 and 2020 lines on the same cycle day"
    open val proBenefit2: String = "Daily log of the live numbers the app showed"
    open val proBenefit3: String = "Optional day-count alert that opens the chart"
    open val proBenefit4: String = "Live tape of daily closes, funding, and ETF — not a forecast"
    open val proBenefit5: String = "AI that reads only the on-screen snapshot"
    open val proBenefit6: String = "Extra lessons on leverage, funding, and the order book — no trade call"
    open val currencyDisplayTitle: String = "Currency Display"
    open val howWeReadItTitle: String = "How we read it — in plain words"
    open val plainItem1Title: String = "1. History repeats in rhythms, not guarantees"
    open val plainItem1Desc: String = "Crypto follows roughly 4-year halving cycles. We line up today with the same day after the 2012, 2016 and 2020 halvings."
    open val plainItem2Title: String = "2. Historical analogs"
    open val plainItem2Desc: String = "On the cycle chart the violet curve is 2020, the amber curve is 2016, and the cyan line is the current cycle."
    open val plainItem3Title: String = "3. Historical Cycle Zones"
    open val plainItem3Desc: String = "Using logarithmic regression and diminishing return curves, we calculate past upper and lower zones for study. All tools are educational and not financial advice."
    open val plainItem4Title: String = "4. Fundamental Whitepaper Blueprints"
    open val plainItem4Desc: String = "Every coin profile includes its verified consensus model, tokenomics, founder origins, and real-world utility architecture."
    open val plainItem5Title: String = "5. Risk Disclaimer"
    open val plainItem5Desc: String = "Past performance is no guarantee of future returns. Crypto markets are volatile. Never invest funds you cannot afford to lose."
    open val privacyPolicyTitle: String = "Terms of Use & Privacy Policy"
    open val privacyPolicyView: String = "View our data collection & storage policy"

    // Pro Modal & Subscriptions
    open val proModalTitle: String = "CryptoCycles Pro"
    open val proModalSubtitle: String = "You pay for the 2016/2020 lines, the daily log, the day alert, the live tape, and the extra lessons. Price, cap, Fear & Greed, and ETF 1-day stay free. Not a forecast."
    open val proModalFeature1: String = "2016 and 2020 lines on the same cycle day"
    open val proModalFeature2: String = "Daily log of the live numbers the app showed"
    open val proModalFeature3: String = "Optional day-count alert that opens the chart"
    open val proModalFeature4: String = "Live tape of daily closes, funding, and ETF — not a forecast"
    open val proModalFeature5: String = "AI that reads only the on-screen snapshot"
    open val proModalFeature6: String = "Extra lessons on leverage, funding, and the order book — no trade call"
    open val planMonthlyTitle: String = "Monthly"
    open val planMonthlyPeriod: String = "/ month"
    open val planAnnualTitle: String = "Yearly"
    open val planAnnualPeriod: String = "/ year"
    open val restorePurchases: String = "Restore Purchases"
    open val restoringPurchases: String = "Restoring purchases from Google Play..."
    open val playTermsDisclosure: String = "Payment is charged to your Google Play account upon purchase confirmation. Subscriptions auto-renew unless canceled at least 24 hours prior to the end of the current period in Google Play Subscriptions. Manage or cancel anytime in Google Play Store > Subscriptions."
    open val financialDisclaimerTitle: String = "Financial & Regulatory Disclaimer"
    open val financialDisclaimerText: String = "Educational history of past cycles. Not a forecast and not advice. CryptoCycles does not exchange, custody, or tell you to buy or sell."
    open val privacySheetTitle: String = "CryptoCycles Privacy"
    open val privSection1Title: String = "1. Data Collection & Analytics Privacy"
    open val privSection1Body: String = "CryptoCycles does not collect, sell, or monetize personal identifying information. Market metrics, cycle overlays, and order book telemetry are aggregated anonymously from public blockchain and exchange feeds. All technical indicators and cycle charts are strictly for educational and analytical reference and do not constitute financial advice."
    open val privSection2Title: String = "2. Children's Privacy"
    open val privSection2Body: String = "CryptoCycles is not directed at children under 13 (or the equivalent age in your country)."
    open val privSection3Title: String = "3. Contact & Inquiries"
    open val privSection3Body: String = "For a privacy request, use the contact email on the CryptoCycles Google Play listing. We will respond within 30 days."
    open val privSection4Title: String = "4. Local-First Architecture"
    open val privSection4Body: String = "CryptoCycles does not create user accounts and does not sell personal data. Themes, favorites, and Pro states are persisted locally on your device."

    // Signals Pro Locked Banner
    open val signalsProLockedTitle: String = "PRO cycle lines and live tape reading"
    open val signalsProLockedSubtitle: String = "You pay for the 2016 and 2020 overlays and the live tape. Price and 24h stay free. Not a forecast."
    open val signalsProLockedFeature2: String = "Live tape of daily closes, funding, and ETF"
    open val signalsProLockedFeature3: String = "Daily log of the live numbers the app showed"
    open val signalsGateTitle: String = "Cycle overlays and tape reading"
    open val signalsGateDesc: String = "Unlock the 2016 and 2020 lines and the live tape of daily closes. Not a signal and not a forecast."
    open val signalsGateBadge: String = "Live daily closes · live funding · live ETF"

    // Weekly Briefing Strings
    open val weeklyBriefingTitle: String = "WEEKLY MACRO BRIEFING"
    open val weeklyBriefingSub: String = "Weekly institutional cycle synthesis & risk posture"
    open val weeklyBriefingCurrentZone: String = "Current Macro Zone"
    open val weeklyBriefingActionTitle: String = "Recommended Posture"
    open val weeklyBriefingReasonTitle: String = "3-Line Synthesis"
    open val weeklyBriefingProUnlockText: String = "Unlock institutional Weekly Briefing, cycle playbook, and macro analogs with Pro."

    open val historicalAnalogProUnlockText: String = "Unlock the 2016 and 2020 lines on the same day-count. History only — no projected path."

    // Push Alerts Settings Strings
    open val alertSettingsHeader: String = "Cycle & Indicator Push Alerts"
    open val alertSettingsSub: String = "Instant on-device notifications for critical market cycle thresholds"
    open val alertZoneChangeTitle: String = "Zone Change Alert"
    open val alertZoneChangeDesc: String = "Notify when the market-mood zone changes (Fear, Neutral, Hot funding, Extreme), from Fear & Greed and funding"
    open val alertPiCycleTitle: String = "Pi Cycle Top Gap & Cross"
    open val alertPiCycleDesc: String = "Notify when the 111DMA comes within 10% of the 350DMA × 2, crosses it, or moves away. Checked daily"
    open val alertRainbowTitle: String = "Rainbow Band Migration"
    open val alertRainbowDesc: String = "Notify when Bitcoin transitions between Rainbow valuation bands"
    open val alert200WTitle: String = "200-Week SMA Distance"
    open val alert200WDesc: String = "Notify when price moves below the 200W SMA, within 10% above it, or 2.5× above it. Checked daily"
    open val alertFreeLimitNotice: String = "Free tier includes up to 2 active alerts. Upgrade to Pro for unlimited alerts."
    open val alertProUnlockNotice: String = "PRO feature: Unlimited cycle, Pi Cycle, and Rainbow band push alerts."

    // Whale Radar & Live Order Flow
    open val whaleRadarTitle: String = "LARGE BINANCE USDT-M PRINTS (PRO)"
    open val whaleRadarSub: String = "Binance USDT-M futures prints from $100k. Not on-chain wallet transfers."
    open val whaleNotificationsTitle: String = "Whale Alert Push Notifications"
    open val whaleNotificationsDesc: String = "Push for a single Binance USDT-M futures trade above your size, while the app is open."
    open val whaleNotificationsProLockedDesc: String = "Pro: push when a Binance USDT-M print clears your size filter. Not on-chain wallet transfers."
    open val whaleThresholdTitle: String = "Whale Alert Minimum Size"
    open val whaleFilterAll: String = "All Whales"
    open val whaleFilterInflow: String = "🔻 Large sells"
    open val whaleFilterBuy: String = "🔺 Large buys"
    open val whaleCollapse: String = "Collapse"
    open val whaleViewAll: String = "View all (%d)"
    open val timeJustNow: String = "Just now"
    open val timeMinsAgo: String = "%dm ago"
    open val timeHoursAgo: String = "%dh ago"
    open val liveOrderFlowTitle: String = "24/7 LIVE ORDER FLOW (BINANCE)"
    open val liveOrderFlowSub: String = "Large Binance USDT-M prints across majors: buy vs sell pressure"
    open val buyDominanceLabel: String = "BUY PRESSURE"
    open val sellDominanceLabel: String = "SELL PRESSURE"
    open val liveExecutionFeed: String = "LIVE EXECUTIONS"

    // Macro Screen Strings
    open val macroScreenTitle: String = "Macro & Cycle Indices"
    open val macroScreenSubtitle: String = "Professional valuation models, Rainbow power law & market sentiment"
    open val macroProLockedTitle: String = "PRO historical cycle tools"
    open val macroProLockedSubtitle: String = "Unlock Rainbow history bands, Pi Cycle crosses, and the weekly fact briefing. Not a buy or sell signal."
    open val macroGateTitle: String = "Historical cycle tools"
    open val macroGateDesc: String = "Rainbow bands, Pi Cycle crosses, and the weekly fact briefing. History only — not a trade."

    open val macroRainbowBadge: String = "HISTORY"

    open val macroProLockedBadge: String = "🔒 Pro Locked"

    // Alt Season Strings
    open val altSeasonHeaderTitle: String = "Altcoin Season Index"
    open val altSeasonSubtitle: String = "75% of Top 50 coins vs Bitcoin over 90 days"
    open val altSeasonBtcSeasonLabel: String = "Bitcoin Season"
    open val altSeasonNeutralLabel: String = "Neutral Zone"
    open val altSeasonAltSeasonLabel: String = "Altcoin Season"
    open val topPerformers90d: String = "Top 90D Outperformers vs BTC"

    // Latest News Strings
    open val latestNewsTitle: String = "Latest News"
    open val latestNewsSubtitle: String = "Live curated headlines & market intelligence"

    // Fear & Greed Strings
    open val fearGreedHeaderTitle: String = "Fear & Greed Index"
    open val fearGreedSubtitle: String = "Market sentiment, euphoria & accumulation clock"
    open val fearGreedYesterday: String = "Yesterday"
    open val fearGreedLastWeek: String = "Last Week"
    open val fearGreedLastMonth: String = "Last Month"
    open val extremeFearLabel: String = "Extreme Fear"
    open val fearLabel: String = "Fear"
    open val neutralLabel: String = "Neutral"
    open val greedLabel: String = "Greed"
    open val extremeGreedLabel: String = "Extreme Greed"
    open val fearGreedAccumulationTip: String = "A live sentiment reading. Missing scores show as a dash. Not a buy or sell window."

    // Pi Cycle Strings
    open val piCycleHeaderTitle: String = "Pi Cycle Indicators (Top & Bottom)"
    open val piCycleSubtitle: String = "Top (111SMA vs 350SMA×2) & Bottom (150EMA vs 471SMA×0.745)"
    open val piCycleTopSectionTitle: String = "🔴 PI CYCLE TOP (HISTORICAL CROSS)"
    open val piCycleBottomSectionTitle: String = "🟢 PI CYCLE BOTTOM (HISTORICAL CROSS)"
    open val piCycleDma111: String = "111-Day SMA"
    open val piCycleDma350x2: String = "350-Day SMA × 2 (Top Threshold)"
    open val piCycleEma150: String = "150-Day EMA"
    open val piCycleSma471x0745: String = "471-Day SMA × 0.745 (Floor Threshold)"
    open val piCycleMa200w: String = "200-Week SMA Floor"
    open val piCycleStatusSafeText: String = "Accumulation & Expansion Phase (Safe Zone)"
    open val piCycleStatusAlertText: String = "🚨 PI CYCLE TOP CROSS DETECTED (Cycle Peak Risk)"
    open val piCycleBottomStatusSafeText: String = "Above Generational Bottom (Expansion Phase)"
    open val piCycleBottomStatusAlertText: String = "🚨 PI CYCLE BOTTOM CROSS DETECTED (historical floor record)"

    // Futures Terminal Strings
    open val futuresTerminalTitle: String = "FUTURES TERMINAL"
    open val futuresOfficialFeedsSub: String = "Binance USDT-M Official Feeds · Zero Mock Data"
    open val futuresStateLive: String = "LIVE"
    open val futuresStateStale: String = "STALE"
    open val futuresStateOffline: String = "OFFLINE"
    open val futuresStateUnavailable: String = "UNAVAILABLE"
    open val futuresLastMarkIndexTitle: String = "LAST / MARK / INDEX PRICES"
    open val futuresLastTradedPrice: String = "Last Traded Price"
    open val futuresMarkPrice: String = "Mark Price"
    open val futuresIndexPrice: String = "Index Price"
    open val futuresBasis: String = "Basis (Mark - Index)"
    open val futuresHigh24h: String = "24h High"
    open val futuresLow24h: String = "24h Low"
    open val futuresVolume24h: String = "24h Volume (USDT)"
    open val futuresBboSpreadTitle: String = "BEST BID / ASK (BBO) & SPREAD"
    open val futuresBestBid: String = "BEST BID"
    open val futuresBestAsk: String = "BEST ASK"
    open val futuresOrderbookSpread: String = "Orderbook Spread:"
    open val futuresPerpFundingTitle: String = "PERPETUAL FUNDING & COUNTDOWN"
    open val futuresCurrentFundingRate: String = "Current Funding Rate"
    open val futuresNextFundingIn: String = "Next Funding in"
    open val futuresApproxAprLabel: String = "Approx APR (rate × 3 × 365):"
    open val futuresOpenInterestTitle: String = "OPEN INTEREST (15S POLLED)"
    open val futuresContracts: String = "contracts"
    open val futuresLiquidationsListening: String = "Listening to live Binance liquidation stream..."
    open val futuresLongLiq: String = "LONG RECT (SELL)"
    open val futuresShortLiq: String = "SHORT RECT (BUY)"
    open val futuresTradesTapeTitle: String = "⚡ Real-Time Trades Tape"
    open val futuresTradesAwaiting: String = "Awaiting trade flow..."
    open val futuresPriceHeader: String = "Price (USDT)"
    open val futuresQtyHeader: String = "Qty"
    open val futuresTimeHeader: String = "Time"
    open val futuresLockedTitle: String = "Pro Futures Pair Locked"
    open val futuresLockedDesc: String = "%s (%s) is part of the Pro tier. Unlock full institutional live order flow, basis arbitrage, and perpetual data."
    open val futuresLockedButton: String = "Unlock Pro Pair Access"
    open val futuresSizePrefix: String = "Size:"
    open val futuresAllPairs: String = "All Pairs"
    open val futuresSearchPair: String = "Search crypto pair..."
    open val futuresSwitchPair: String = "Switch Pair"

    // Market Intelligence & "Why is Asset Moving?" (ChatGPT 5.6 Luna Product Upgrade)
    open val intelligenceLayerTitle: String = "MARKET INTELLIGENCE LAYER"
    open val whyAssetMovingHeader: String = "Why is %s moving?"
    open val interpretationLabel: String = "Interpretation: "
    open val cascadeRiskLabel: String = "Cascade Risk: "
    open val invalidationLabel: String = "Invalidation: "
    open val expandDeepDive: String = "Tap for Deep Dive & Invalidation"
    open val collapseDeepDive: String = "Hide Deep Dive"
    open val liquidationMapHeader: String = "AGGREGATED LIQUIDATION POOLS"
    open val liquidationMapSub: String = "Estimated liquidation clusters & sweep liquidity"
    open val shortLiquidationPools: String = "Short Liquidation Pools (Above Price)"
    open val longLiquidationPools: String = "Long Liquidation Pools (Below Price)"
    open val currentPriceAnchor: String = "CURRENT PRICE"
    open val tabClustersMap: String = "Clusters Map"
    open val tabLiveStream: String = "Live Feed"

    // Market Intelligence Explainer Modal ("What is this & How it works")
    open val marketIntelligenceExplainerTitle: String = "Market Intelligence Layer: How it Works"
    open val marketIntelligenceInfoButton: String = "What is this?"
    open val marketIntelligenceConfidenceTitle: String = "Confidence Score (e.g. 79%)"
    open val marketIntelligenceConfidenceDesc: String = "Measures the quantitative strength of current futures positioning based on volume, open interest, and momentum."
    open val marketIntelligenceRegimesHeader: String = "Market Regimes (What the signals mean)"
    open val regimeShortSqueezeTitle: String = "Short Squeeze Pressure"
    open val regimeShortSqueezeDesc: String = "Heavy short positions are trapped. A minor upward move could trigger cascading liquidations and sharp spikes."
    open val regimeSpotAccumulationTitle: String = "Spot Accumulation"
    open val regimeSpotAccumulationDesc: String = "Real spot buying absorption rather than risky leverage. Signals healthy, sustainable market structure."
    open val regimeBalancedConsolidationTitle: String = "Balanced Consolidation"
    open val regimeBalancedConsolidationDesc: String = "Derivatives leverage and spot demand are in equilibrium, indicating range-bound trading."
    open val marketIntelligenceTelemetryHeader: String = "Key Telemetry"
    open val telemetryOpenInterestTitle: String = "Open Interest (OI)"
    open val telemetryOpenInterestDesc: String = "Total capital locked in active derivatives contracts."
    open val telemetryFundingRateTitle: String = "Funding Rate"
    open val telemetryFundingRateDesc: String = "Periodic fee between longs and shorts. Highly positive rates warn of overleveraged longs prone to flushes."
    open val marketIntelligenceUnderstood: String = "Understood"

    // AI Market Analyst Assistant
    open val aiAssistantTitle: String = "AI Market Analyst"
    open val aiAssistantBadge: String = "LIVE CC"
    open val aiAssistantSubtitle: String = "Real-Time Cycle, Futures & Whale Intelligence"
    open val aiPromptPlaceholder: String = "Ask about BTC cycle, funding, support, altseason..."
    open val aiAskButton: String = "Analyze"
    open val aiQuickActionCycle: String = "Cycle & Halving Risk"
    open val aiQuickActionFutures: String = "Funding & Leverage Flow"
    open val aiQuickActionWhales: String = "Whale Accumulation"
    open val aiQuickActionAltseason: String = "Altseason Readiness"
    open val aiDisclaimer: String = "Powered by live Gemini AI models & real-time telemetry. Not financial advice."
    open val aiThinking: String = "Analyzing live telemetry & order book..."
    open val aiGreeting: String = "I'm here. What would you like to see?"
    open val aiClearHistory: String = "Clear Chat"
    open val aiLiveContextTag: String = "Live Feeds Attached"
    open val aiPromptPrefixCycle: String = "Analyze the current cycle phase, halving progress and risk/reward for the market."
    open val aiPromptPrefixFutures: String = "Interpret current futures funding rates, open interest, and liquidation risks."
    open val aiPromptPrefixWhales: String = "What are the latest whale alerts indicating for institutional accumulation or distribution?"
    open val aiPromptPrefixAltseason: String = "Assess whether market conditions, BTC dominance, and sentiment favor an Altseason right now."

    open val tabOverview: String = "Overview"
    open val tabAnalytics: String = "Analytics"
    open val tabOnChain: String = "On-Chain"
    open val tabEvents: String = "Events"
    open val onChainNetworkHeader: String = "Network & On-Chain Intelligence"
    open val eventsMilestonesHeader: String = "Protocol Milestones & Catalysts"
    open val upcomingTag: String = "UPCOMING"
    open val completedTag: String = "COMPLETED"
    open val activeTag: String = "ACTIVE"
    open val whaleConcentrationLabel: String = "Whale Holder Concentration"
    open val exchangeNetFlowLabel: String = "Exchange Reserve Flow"
    open val activeAddressesEstimate: String = "Active Network Wallets (24h)"
    open val cycleMomentumTitle: String = "Cycle Momentum & Relative Health"
    open val circulatingSupplyTitle: String = "Circulating Supply"
    open val liquidationWarningText: String = "High liquidation clusters detected. Increased volatility risk if support breaks."
    open val backtestBadgeText: String = "Live whale radar · live order flow · live liquidations"
    open val derivativesRiskGuardrail: String = "This screen reads the perpetual tape. It is not a trade desk and it does not size positions."
}

val EnglishStrings: AppStrings = AppStrings(AppLanguage.ENGLISH)
val GreekStrings: AppStrings = GreekAppStrings()
val GermanStrings: AppStrings = GermanAppStrings()
val FrenchStrings: AppStrings = FrenchAppStrings()
val SpanishStrings: AppStrings = SpanishAppStrings()
val ItalianStrings: AppStrings = ItalianAppStrings()

fun getAppStrings(language: AppLanguage): AppStrings {
    return when (language) {
        AppLanguage.ENGLISH -> EnglishStrings
        AppLanguage.GREEK -> GreekStrings
        AppLanguage.GERMAN -> GermanStrings
        AppLanguage.FRENCH -> FrenchStrings
        AppLanguage.SPANISH -> SpanishStrings
        AppLanguage.ITALIAN -> ItalianStrings
    }
}

fun CoinCategory.getLocalizedName(strings: AppStrings): String {
    return when (this) {
        CoinCategory.ALL -> strings.catAll
        CoinCategory.LAYER1 -> strings.catLayer1
        CoinCategory.LAYER2 -> strings.catLayer2
        CoinCategory.DEFI -> strings.catDefi
        CoinCategory.AI_INFRA -> strings.catAiInfra
        CoinCategory.MEME -> strings.catMeme
        CoinCategory.RWA_DEPIN -> strings.catRwaDepin
        CoinCategory.UTILITY -> strings.catUtility
        CoinCategory.FAVORITES -> strings.catFavorites
    }
}

val LocalAppStrings = compositionLocalOf { EnglishStrings }
