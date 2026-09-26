package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.DriverImpact
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.MarketDriver
import com.example.data.model.MarketIntelligenceReport
import com.example.data.model.MarketRegimeState
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicVoidGlass
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.holographicCard
import com.example.util.GreekAppStrings
import com.example.util.LocalAppStrings
import java.util.Locale

@Composable
fun MarketIntelligenceCard(
    report: MarketIntelligenceReport,
    symbol: String = report.symbol,
    coin: CryptoCoin? = null,
    ticker: FuturesTickerData? = null,
    markFunding: FuturesMarkFunding? = null,
    openInterest: FuturesOpenInterest? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val isGreek = strings is GreekAppStrings
    var isExpanded by remember { mutableStateOf(false) }
    var showExplainerModal by remember { mutableStateOf(false) }

    val cleanSym = symbol.uppercase().removeSuffix("USDT").removePrefix("1000")
    val baseSymbol = (coin?.symbol ?: cleanSym).uppercase().ifEmpty { "BTC" }

    val tickerPrice = ticker?.lastPrice
    val markPrice = markFunding?.markPrice
    val liveCoin = coin?.takeIf { it.quoteState == com.example.data.model.QuoteState.LIVE }
    val resolvedPrice = when {
        ticker?.fromExchange == true && tickerPrice != null && tickerPrice > 0.0 -> tickerPrice
        markFunding?.fromExchange == true && markPrice != null && markPrice > 0.0 -> markPrice
        report.hasLivePrice && report.currentPrice > 0.0 -> report.currentPrice
        liveCoin != null && liveCoin.priceUsd > 0.0 -> liveCoin.priceUsd
        else -> 0.0
    }

    val resolvedChange24h = when {
        ticker?.fromExchange == true && ticker?.priceChangePercent24h != null -> ticker?.priceChangePercent24h
        report.hasLiveChange24h -> report.priceChange24h
        liveCoin != null -> liveCoin.change24h
        else -> null
    }

    val oiFromSocket = openInterest?.openInterestUsd
    val resolvedOiUsd = when {
        report.hasLiveOpenInterest && report.openInterestUsd > 0.0 -> report.openInterestUsd
        openInterest?.isAvailable == true && oiFromSocket != null && oiFromSocket > 0.0 -> oiFromSocket
        else -> 0.0
    }

    val socketFunding = markFunding?.fundingRate
    val resolvedFundingRate = when {
        report.hasLiveFunding -> report.fundingRate
        markFunding?.fromExchange == true && socketFunding != null -> socketFunding
        else -> null
    }

    val sourceLabel = report.sourceLabel.ifBlank { "Binance" }
    val tickerMs = ticker?.receivedTimeMs ?: 0L
    val freshnessMs = when {
        report.derivativesAsOfMs > 0L -> report.derivativesAsOfMs
        tickerMs > 0L -> tickerMs
        else -> 0L
    }
    val freshnessStatus = when (report.derivativesFreshness) {
        com.example.data.model.DerivativesFreshness.FRESH -> DataFreshnessStatus.LIVE
        com.example.data.model.DerivativesFreshness.DELAYED -> DataFreshnessStatus.DELAYED
        com.example.data.model.DerivativesFreshness.STALE -> DataFreshnessStatus.STALE
        com.example.data.model.DerivativesFreshness.DEGRADED -> DataFreshnessStatus.DELAYED
        com.example.data.model.DerivativesFreshness.UNAVAILABLE -> freshnessFor(freshnessMs)
    }

    val activeReport = report.copy(
        symbol = "${baseSymbol}USDT",
        currentPrice = resolvedPrice,
        priceChange24h = resolvedChange24h ?: 0.0,
        openInterestUsd = resolvedOiUsd,
        fundingRate = resolvedFundingRate ?: 0.0,
        hasLivePrice = resolvedPrice > 0.0,
        hasLiveChange24h = resolvedChange24h != null,
        hasLiveFunding = resolvedFundingRate != null,
        hasLiveOpenInterest = resolvedOiUsd > 0.0,
        sourceLabel = sourceLabel
    )

    val isPos = activeReport.priceChange24h >= 0

    val regimeColor = when (activeReport.regime) {
        MarketRegimeState.SPOT_ACCUMULATION -> TachyonMint
        MarketRegimeState.LEVERAGE_EXPANSION -> QuantumCyan
        MarketRegimeState.SHORT_SQUEEZE -> QuantumCyan
        MarketRegimeState.LONG_CASCADE -> SoftCrimson
        MarketRegimeState.RANGE_COMPRESSION -> MauveAurora
        MarketRegimeState.CHOPPY_NEUTRAL -> CosmicBorder
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(16.dp),
                glowColor = QuantumCyan,
                pulseColor = MauveAurora,
                baseContainerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidGlass
            )
            .testTag("market_intelligence_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Top Section: Label + Freshness + Confidence Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(QuantumCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.intelligenceLayerTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = QuantumCyan
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DataFreshnessBadge(
                        status = freshnessStatus,
                        timeAgo = formatPriceAge(freshnessMs),
                        source = sourceLabel
                    )

                    // Interactive Info Icon Button (Icons.Default.Info / "What is this?")
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(QuantumCyan.copy(alpha = 0.14f))
                            .border(0.8.dp, QuantumCyan.copy(alpha = 0.45f), CircleShape)
                            .clickable { showExplainerModal = true }
                            .testTag("btn_market_intelligence_info"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = strings.marketIntelligenceInfoButton,
                            tint = QuantumCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Confidence pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(QuantumCyan.copy(alpha = 0.12f))
                            .border(0.8.dp, QuantumCyan.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                            .clickable { showExplainerModal = true }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = QuantumCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activeReport.confidencePercent}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuantumCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. MODERN BULLISH CONFIDENCE ORB CARD (Dual Concentric Holographic Core)
            BullishConfidenceOrbCard(
                report = activeReport,
                baseSymbol = baseSymbol
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Core Market Insight Narrative (Dark Cosmic Palette)
            Text(
                text = if (isGreek)
                    activeReport.interpretationGr.ifEmpty { "Αναμονή για ζωντανά δεδομένα παραγώγων." }
                else
                    activeReport.interpretationEn.ifEmpty { "Waiting for live derivatives data." },
                fontSize = 12.sp,
                color = if (palette.isLight) palette.textSecondary else Color(0xFF94A3B8),
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurface)
                    .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Signature: "Why is BTC moving?" (Drivers Breakdown)
            Text(
                text = String.format(strings.whyAssetMovingHeader, baseSymbol),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of 5 Drivers
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                activeReport.drivers.forEach { driver ->
                    DriverRowItem(driver = driver, isGreek = isGreek)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Expandable Deep Dive & Invalidation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurfaceElevated)
                    .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        com.example.util.AppSoundManager.playTechClick()
                        isExpanded = !isExpanded
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = QuantumCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExpanded) strings.collapseDeepDive else strings.expandDeepDive,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface)
                        .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Interpretation
                    Column {
                        Text(
                            text = strings.interpretationLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                        Text(
                            text = if (isGreek) activeReport.interpretationGr else activeReport.interpretationEn,
                            fontSize = 12.sp,
                            color = palette.textPrimary,
                            lineHeight = 17.sp
                        )
                    }

                    // Cascade Risk
                    Column {
                        Text(
                            text = strings.cascadeRiskLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCrimson
                        )
                        Text(
                            text = if (isGreek) activeReport.cascadeRiskGr else activeReport.cascadeRiskEn,
                            fontSize = 12.sp,
                            color = palette.textPrimary,
                            lineHeight = 17.sp
                        )
                    }

                    // Invalidation Level
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = QuantumCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.invalidationLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuantumCyan
                            )
                        }
                        Text(
                            text = if (isGreek) activeReport.invalidationLevelGr else activeReport.invalidationLevelEn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }

    if (showExplainerModal) {
        MarketIntelligenceExplainerModal(
            onDismiss = { showExplainerModal = false }
        )
    }
}

@Composable
private fun DriverRowItem(
    driver: MarketDriver,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    val impactColor = when (driver.impact) {
        DriverImpact.HIGH -> TachyonMint
        DriverImpact.MEDIUM -> QuantumCyan
        DriverImpact.LOW -> CosmicBorder
        DriverImpact.ABSORPTION -> MauveAurora
    }

    val impactLabel = if (isGreek) driver.impact.labelGr else driver.impact.labelEn

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurface)
            .border(0.8.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = driver.title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                text = driver.detail,
                fontSize = 11.sp,
                color = palette.textSecondary,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(impactColor.copy(alpha = 0.15f))
                .border(0.8.dp, impactColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.5.dp)
        ) {
            Text(
                text = impactLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = impactColor
            )
        }
    }
}

@Composable
fun BullishConfidenceOrbCard(
    report: MarketIntelligenceReport,
    baseSymbol: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val isGreek = strings is GreekAppStrings

    val infiniteTransition = rememberInfiniteTransition(label = "orb_quantum")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Funding Rate driven orbital dynamics: positive funding = clockwise high-speed spin; negative funding = counter-clockwise plasma turbulence
    val isPositiveFunding = report.fundingRate >= 0
    val fundingMagnitude = kotlin.math.abs(report.fundingRate * 100.0).toFloat().coerceIn(0.005f, 0.15f)
    val orbitalDurationMs = ((0.015f / fundingMagnitude) * 10000).toInt().coerceIn(2000, 16000)

    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = if (isPositiveFunding) 0f else 360f,
        targetValue = if (isPositiveFunding) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(orbitalDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val isPos = report.priceChange24h >= 0
    val changeColor = when {
        !report.hasLiveChange24h -> Color(0xFF94A3B8)
        isPos -> TachyonMint
        else -> SoftCrimson
    }

    // Dynamic Live Price
    val decimals = if (report.currentPrice >= 1.0) 2 else 4
    val displayPrice = if (report.hasLivePrice && report.currentPrice > 0.0) {
        "$" + com.example.util.AppNumberFormatter.formatRawPrice(report.currentPrice, decimals = decimals)
    } else {
        "—"
    }

    // Dynamic Regime Title inside Orb
    val regimeTitle = if (isGreek) {
        "ΑΝΑΓΝΩΣΗ: ${report.regime.labelGr.uppercase()}"
    } else {
        "READ: ${report.regime.labelEn.uppercase()}"
    }

    // Activity level: the % measures how extreme the inputs are, so more bars means a busier
    // tape. (It used to read "LOW RISK" exactly when the inputs were most extreme.)
    val (riskLabel, riskColor, riskBarCount) = when {
        report.regime == MarketRegimeState.LONG_CASCADE || report.regime == MarketRegimeState.SHORT_SQUEEZE ->
            Triple(if (isGreek) "ΕΚΚΑΘΑΡΙΣΕΙΣ" else "LIQUIDATIONS", SoftCrimson, 5)
        report.confidencePercent >= 60 ->
            Triple(if (isGreek) "ΕΝΤΟΝΗ ΚΙΝΗΣΗ" else "ACTIVE", NeonAmber, 4)
        report.confidencePercent >= 30 ->
            Triple(if (isGreek) "ΜΕΤΡΙΑ" else "MODERATE", QuantumCyan, 3)
        else ->
            Triple(if (isGreek) "ΗΡΕΜΗ" else "QUIET", TachyonMint, 2)
    }

    val priceWord = when (strings) {
        is GreekAppStrings -> "ΤΙΜΗ"
        is com.example.util.GermanAppStrings -> "PREIS"
        is com.example.util.FrenchAppStrings -> "PRIX"
        is com.example.util.SpanishAppStrings -> "PRECIO"
        is com.example.util.ItalianAppStrings -> "PREZZO"
        else -> "PRICE"
    }

    // Pod 1: Live Base Symbol Price (Cyan)
    val card1Title = "• $baseSymbol $priceWord"
    val card1Value = when {
        !report.hasLivePrice || report.currentPrice <= 0.0 -> "—"
        report.currentPrice >= 1000.0 -> com.example.util.AppNumberFormatter.formatCompactCurrency(report.currentPrice)
        else -> "$" + com.example.util.AppNumberFormatter.formatRawPrice(report.currentPrice, decimals = decimals)
    }

    // Pod 2: Live Open Interest (Gold)
    val card2Title = if (isGreek) "ΑΝΟΙΧΤΟ ΣΥΜΒΟΛΑΙΟ" else "OPEN INTEREST"
    val card2Value = if (report.hasLiveOpenInterest && report.openInterestUsd > 0.0) {
        com.example.util.AppNumberFormatter.formatCompactCurrency(report.openInterestUsd)
    } else {
        "—"
    }

    // Pod 3: Live Funding Rate (Amber)
    val fundingPercent = report.fundingRate * 100.0
    val card3Title = if (isGreek) "ΕΠΙΤΟΚΙΟ" else "FUNDING RATE"
    val card3Value = if (report.hasLiveFunding) {
        com.example.util.AppNumberFormatter.formatPercent(fundingPercent, includeSign = true, decimals = 4)
    } else {
        "—"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(CosmicVoidSurface, CosmicVoidBg)
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(QuantumCyan.copy(alpha = 0.35f), CosmicBorder.copy(alpha = 0.40f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top row: CYCLE breadcrumb & Asset header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "CYCLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = QuantumCyan
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$baseSymbol / USD",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⌵",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayPrice,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                val chgFormatted = if (report.hasLiveChange24h) {
                    com.example.util.AppNumberFormatter.formatPercent(report.priceChange24h, includeSign = true, decimals = 2)
                } else {
                    "—"
                }
                Text(
                    text = "$chgFormatted 24H CHANGE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
            }
        }

        // 1. SECTION HEADER: Move Title Above the Circular Gauge
        Text(
            text = regimeTitle,
            style = TextStyle(
                fontSize = 12.sp,
                color = palette.textSecondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(top = 2.dp)
        )

        // Center: DUAL CONCENTRIC HOLOGRAPHIC RING GAUGE
        val wavePath = remember { Path() }
        val confidenceRatio = (report.confidencePercent / 100f).coerceIn(0.05f, 1.0f)
        val targetSweepAngle = 360f * confidenceRatio
        val sweepAngle by animateFloatAsState(
            targetValue = targetSweepAngle,
            animationSpec = tween(800, easing = FastOutSlowInEasing),
            label = "confidence_sweep_anim"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(175.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.width * 0.44f
                val innerRadius = outerRadius * 0.78f

                // Deep ambient cyan back-glow behind center text
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QuantumCyan.copy(alpha = 0.28f * pulseGlow),
                            QuantumCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = outerRadius * 1.1f
                    ),
                    radius = outerRadius * 1.1f,
                    center = center
                )

                // 1. OUTER RING: Background Track
                drawCircle(
                    color = CosmicVoidSurfaceElevated.copy(alpha = 0.70f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )

                // 1. OUTER RING: Glowing Progress Arc with SweepGradient
                val arcBrush = Brush.sweepGradient(
                    0.0f to QuantumCyan,
                    0.5f to TachyonMint,
                    1.0f to QuantumCyan,
                    center = center
                )

                // Arc glow halo
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                    alpha = 0.35f * pulseGlow
                )

                // Arc sharp core
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 2. INNER RING: Slowly rotating dashed quantum orbital
                withTransform({
                    rotate(degrees = orbitalAngle, pivot = center)
                }) {
                    // Dashed orbital ring
                    drawCircle(
                        color = QuantumCyan.copy(alpha = 0.50f * pulseGlow),
                        radius = innerRadius,
                        center = center,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
                        )
                    )

                    // 2 Orbital Nodes / Satellites
                    val node1 = Offset(center.x + innerRadius, center.y)
                    val node2 = Offset(center.x - innerRadius, center.y)

                    drawCircle(
                        color = QuantumCyan,
                        radius = 3.dp.toPx(),
                        center = node1
                    )
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 1.5.dp.toPx(),
                        center = node1
                    )

                    drawCircle(
                        color = TachyonMint,
                        radius = 3.dp.toPx(),
                        center = node2
                    )
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 1.5.dp.toPx(),
                        center = node2
                    )
                }

                // Internal subtle quantum sine stream below center text
                wavePath.reset()
                val waveWidth = innerRadius * 1.3f
                val startX = center.x - (waveWidth / 2f)
                val waveY = center.y + (innerRadius * 0.52f)
                wavePath.moveTo(startX, waveY)
                val waveSteps = 24
                for (step in 0..waveSteps) {
                    val progress = step.toFloat() / waveSteps
                    val x = startX + progress * waveWidth
                    val y = waveY + (kotlin.math.sin(progress * 4 * Math.PI + wavePhase).toFloat() * 5f * pulseGlow)
                    wavePath.lineTo(x, y)
                }
                drawPath(
                    path = wavePath,
                    color = QuantumCyan.copy(alpha = 0.65f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 2. INSIDE THE CIRCLE: Only essential metrics centered vertically
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "${report.confidencePercent}%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isGreek) "ΕΜΠΙΣΤΟΣΥΝΗ" else "CONFIDENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = palette.textSecondary
                )
            }

            // Left side dynamic risk indicator
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = riskLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = riskColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(riskBarCount) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(riskColor)
                        )
                    }
                }
            }
        }

        // 3 DARK GLASSMORPHIC STAT PODS (Price: Cyan, OI: Gold, Funding: Amber)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DarkGlassStatPod(
                title = card1Title,
                value = card1Value,
                accentColor = QuantumCyan, // Cyan for Price
                modifier = Modifier.weight(1f)
            )
            DarkGlassStatPod(
                title = card2Title,
                value = card2Value,
                accentColor = PhotonGold, // Gold for Open Interest
                modifier = Modifier.weight(1f)
            )
            DarkGlassStatPod(
                title = card3Title,
                value = card3Value,
                accentColor = NeonAmber, // Amber for Funding Rate
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Dark Glassmorphic Stat Pod with subtle top-highlight border (Year 3026).
 */
@Composable
fun DarkGlassStatPod(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(CosmicVoidSurfaceElevated, CosmicVoidSurface)
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.55f), CosmicBorder.copy(alpha = 0.30f))
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = LocalAppColors.current.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Sleek Modal Bottom Sheet styled in Galaxy Dark explaining the
 * Market Intelligence Layer, Confidence Score, Market Regimes, and Key Telemetry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketIntelligenceExplainerModal(
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CosmicVoidBg,
        scrimColor = Color(0xB3000000),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = QuantumCyan.copy(alpha = 0.40f)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("modal_market_intelligence_explainer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(QuantumCyan.copy(alpha = 0.15f))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = QuantumCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Text(
                        text = strings.marketIntelligenceExplainerTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.2.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_close_explainer_modal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.marketIntelligenceUnderstood,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 1. Confidence Score Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(QuantumCyan)
                        )
                        Text(
                            text = strings.marketIntelligenceConfidenceTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }
                    Text(
                        text = strings.marketIntelligenceConfidenceDesc,
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }

            // 2. Market Regimes Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(QuantumCyan)
                        )
                        Text(
                            text = strings.marketIntelligenceRegimesHeader,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }

                    // Regime 1: Short Squeeze Pressure
                    ExplainerBulletItem(
                        accentColor = QuantumCyan,
                        title = strings.regimeShortSqueezeTitle,
                        description = strings.regimeShortSqueezeDesc
                    )

                    // Regime 2: Spot Accumulation
                    ExplainerBulletItem(
                        accentColor = TachyonMint,
                        title = strings.regimeSpotAccumulationTitle,
                        description = strings.regimeSpotAccumulationDesc
                    )

                    // Regime 3: Balanced Consolidation
                    ExplainerBulletItem(
                        accentColor = MauveAurora,
                        title = strings.regimeBalancedConsolidationTitle,
                        description = strings.regimeBalancedConsolidationDesc
                    )
                }
            }

            // 3. Key Telemetry Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(TachyonMint)
                        )
                        Text(
                            text = strings.marketIntelligenceTelemetryHeader,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TachyonMint
                        )
                    }

                    // Telemetry 1: Open Interest (OI)
                    ExplainerBulletItem(
                        accentColor = QuantumCyan,
                        title = strings.telemetryOpenInterestTitle,
                        description = strings.telemetryOpenInterestDesc
                    )

                    // Telemetry 2: Funding Rate
                    ExplainerBulletItem(
                        accentColor = QuantumCyan,
                        title = strings.telemetryFundingRateTitle,
                        description = strings.telemetryFundingRateDesc
                    )
                }
            }

            // 4. "Understood" Dismiss Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(QuantumCyan, MauveAurora)
                        )
                    )
                    .clickable(onClick = onDismiss)
                    .testTag("btn_market_intelligence_understood"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = strings.marketIntelligenceUnderstood,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplainerBulletItem(
    accentColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 17.sp
            )
        }
    }
}

