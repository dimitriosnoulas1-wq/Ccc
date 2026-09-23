package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.MacroMarketSentiment
import com.example.ui.components.MetricExplainerBox
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicVoidGlass
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.QuantumCornerReticleOverlay
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.holographicCard
import com.example.ui.theme.stitchHorizonPanel
import com.example.util.LocalAppStrings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrimaryFuturesPriceCard(
    symbol: String,
    coin: CryptoCoin? = null,
    ticker: FuturesTickerData?,
    markFunding: FuturesMarkFunding?
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val isMatchingTicker = ticker?.symbol.equals(symbol, ignoreCase = true) ||
            ticker?.symbol.equals(symbol.removePrefix("1000"), ignoreCase = true)
    val isMatchingMark = markFunding?.symbol.equals(symbol, ignoreCase = true) ||
            markFunding?.symbol.equals(symbol.removePrefix("1000"), ignoreCase = true)

    val lastPrice = if (isMatchingTicker) ticker?.lastPrice else null
    val markPrice = if (isMatchingMark) markFunding?.markPrice else null
    val indexPrice = if (isMatchingMark) markFunding?.indexPrice else null
    val basis = if (isMatchingMark) markFunding?.basis else null
    val basisPct = if (isMatchingMark) markFunding?.basisPercent else null

    val changePct = if (isMatchingTicker) ticker?.priceChangePercent24h else null
    val isPositiveChange = (changePct ?: 0.0) >= 0
    val changeColor = if (changePct == null) Color(0xFF94A3B8) else if (isPositiveChange) TachyonMint else SoftCrimson

    val ageSec = if (isMatchingTicker && ticker != null) ((nowMs - ticker.receivedTimeMs) / 1000.0).coerceAtLeast(0.0) else null
    val isStale = (ageSec ?: 0.0) > 5.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .stitchHorizonPanel(shape = RoundedCornerShape(16.dp), borderWidth = 1.2.dp)
            .padding(14.dp)
    ) {
        QuantumCornerReticleOverlay(color = QuantumCyan.copy(alpha = 0.35f))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.futuresLastMarkIndexTitle,
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    color = QuantumCyan
                )
                SourceAgeBadge(
                    source = "Binance Futures wss",
                    ageSec = ageSec,
                    isStale = isStale
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (lastPrice != null && lastPrice > 0.0) formatPrice(lastPrice) else "—",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SyneFont,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.futuresLastTradedPrice,
                            fontSize = 11.sp,
                            fontFamily = SpaceGroteskFont,
                            color = Color(0xFFCBD5E1)
                        )
                        val isBtc = symbol.uppercase().contains("BTC")
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(QuantumCyan.copy(alpha = 0.15f))
                                .border(0.6.dp, QuantumCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isBtc) "BTC PERP" else "${symbol.removeSuffix("USDT")} PERP",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                                fontFamily = JetBrainsMonoFont,
                                color = QuantumCyan
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(changeColor.copy(alpha = 0.15f))
                        .border(0.6.dp, changeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val changeFormatted = if (changePct == null) {
                        "—"
                    } else {
                        com.example.util.AppNumberFormatter.formatPercent(changePct, includeSign = true, decimals = 2)
                    }
                    Text(
                        text = changeFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = changeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF140D2E))
                    .border(0.6.dp, MauveAurora.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.futuresMarkPrice, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    Text(
                        text = if (markPrice != null && markPrice > 0.0) formatPrice(markPrice) else "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = QuantumCyan
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.futuresIndexPrice, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    Text(
                        text = if (indexPrice != null && indexPrice > 0.0) formatPrice(indexPrice) else "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = MauveAurora
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = strings.futuresBasis, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    val liveBasis = basis
                    val liveBasisPct = basisPct
                    val sign = if ((liveBasis ?: 0.0) >= 0) "+" else ""
                    val basisRaw = if (liveBasis == null) null else com.example.util.AppNumberFormatter.formatRawPrice(liveBasis, 2)
                    val basisPctFormatted = if (liveBasisPct == null) {
                        null
                    } else {
                        com.example.util.AppNumberFormatter.formatPercent(liveBasisPct, includeSign = false, decimals = 3)
                    }
                    Text(
                        text = if (basisRaw != null && basisPctFormatted != null) "$sign$basisRaw ($basisPctFormatted)" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = JetBrainsMonoFont,
                        color = if ((liveBasis ?: 0.0) >= 0) TachyonMint else SoftCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = strings.futuresHigh24h, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    Text(
                        text = if (ticker?.high24h != null) formatPrice(ticker.high24h) else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFont,
                        color = Color.White
                    )
                }
                Column {
                    Text(text = strings.futuresLow24h, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    Text(
                        text = if (ticker?.low24h != null) formatPrice(ticker.low24h) else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFont,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = strings.futuresVolume24h, fontSize = 10.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    Text(
                        text = if (ticker?.volumeQuote24h != null) formatUsdCompact(ticker.volumeQuote24h) else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = QuantumCyan
                    )
                }
            }
        }
    }
}

@Composable
fun BestBidAskCard(
    symbol: String,
    coin: CryptoCoin? = null,
    bookTicker: FuturesBookTicker?
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val isMatchingBook = bookTicker?.symbol.equals(symbol, ignoreCase = true) ||
            bookTicker?.symbol.equals(symbol.removePrefix("1000"), ignoreCase = true)

    val bidPrice = if (isMatchingBook) bookTicker?.bidPrice else null
    val askPrice = if (isMatchingBook) bookTicker?.askPrice else null
    val bidQty = if (isMatchingBook) bookTicker?.bidQty else null
    val askQty = if (isMatchingBook) bookTicker?.askQty else null
    val spread = if (isMatchingBook) bookTicker?.spread else null
    val spreadPct = if (isMatchingBook) bookTicker?.spreadPercent else null

    val ageSec = if (isMatchingBook && bookTicker != null) ((nowMs - bookTicker.receivedTimeMs) / 1000.0).coerceAtLeast(0.0) else null
    val isStale = (ageSec ?: 0.0) > 5.0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.futuresBboSpreadTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    color = palette.textMuted
                )
                SourceAgeBadge(
                    source = "Binance bookTicker",
                    ageSec = ageSec,
                    isStale = isStale
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TachyonMint.copy(alpha = 0.1f))
                        .border(1.dp, TachyonMint.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(text = strings.futuresBestBid, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TachyonMint)
                    Text(
                        text = if (bidPrice != null && bidPrice > 0.0) formatPrice(bidPrice) else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = JetBrainsMonoFont,
                        color = TachyonMint
                    )
                    Text(
                        text = "${strings.futuresSizePrefix} ${if (bidQty == null) "—" else com.example.util.AppNumberFormatter.formatRawPrice(bidQty, 3)}",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = palette.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SoftCrimson.copy(alpha = 0.1f))
                        .border(1.dp, SoftCrimson.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(text = strings.futuresBestAsk, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftCrimson)
                    Text(
                        text = if (askPrice != null && askPrice > 0.0) formatPrice(askPrice) else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = JetBrainsMonoFont,
                        color = SoftCrimson
                    )
                    Text(
                        text = "${strings.futuresSizePrefix} ${if (askQty == null) "—" else com.example.util.AppNumberFormatter.formatRawPrice(askQty, 3)}",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = strings.futuresOrderbookSpread, fontSize = 11.sp, color = palette.textMuted)
                val spreadRaw = if (spread == null) null else com.example.util.AppNumberFormatter.formatRawPrice(spread, 4)
                val spreadPctFormatted = if (spreadPct == null) {
                    null
                } else {
                    com.example.util.AppNumberFormatter.formatPercent(spreadPct, includeSign = false, decimals = 4)
                }
                Text(
                    text = if (spreadRaw != null && spreadPctFormatted != null) "$$spreadRaw ($spreadPctFormatted)" else "—",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFont,
                    color = palette.textPrimary
                )
            }
        }
    }
}

@Composable
fun FundingCountdownBadge(
    nextFundingMs: Long,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(nextFundingMs) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val countdownText = if (nextFundingMs > currentTime) {
        val diffSec = (nextFundingMs - currentTime) / 1000
        val hrs = diffSec / 3600
        val mins = (diffSec % 3600) / 60
        val secs = diffSec % 60
        "%02d:%02d:%02d".format(hrs, mins, secs)
    } else "00:00:00"

    Text(
        text = countdownText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = JetBrainsMonoFont,
        color = LocalAppColors.current.textPrimary,
        modifier = modifier
    )
}

@Composable
fun FundingRateCard(
    symbol: String,
    markFunding: FuturesMarkFunding?,
    isProUnlocked: Boolean
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val isMatchingMark = markFunding?.symbol.equals(symbol, ignoreCase = true) ||
            markFunding?.symbol.equals(symbol.removePrefix("1000"), ignoreCase = true)

    val rate: Double? = if (isMatchingMark) markFunding?.fundingRate else null
    val nextFundingMs: Long = (if (isMatchingMark) markFunding?.nextFundingTimeMs else null)?.takeIf { it > 0L } ?: 0L
    val approxApr: Double? = if (isMatchingMark) markFunding?.approxApr else null

    val ratePct = rate?.times(100.0)
    val rateColor = when {
        rate == null -> Color(0xFF94A3B8)
        rate > 0.0 -> TachyonMint
        rate < 0.0 -> SoftCrimson
        else -> palette.textPrimary
    }

    val ageSec = if (isMatchingMark && markFunding != null) ((nowMs - markFunding.receivedTimeMs) / 1000.0).coerceAtLeast(0.0) else null
    val isStale = (ageSec ?: 0.0) > 5.0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.futuresPerpFundingTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    color = palette.textMuted
                )
                if (!isProUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(QuantumCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FREE TIER · 15-MIN DELAYED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }
                } else {
                    SourceAgeBadge(
                        source = "Binance USDT-M @markPrice",
                        ageSec = ageSec,
                        isStale = isStale
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = strings.futuresCurrentFundingRate, fontSize = 11.sp, color = palette.textMuted)
                    val formattedRate = if (ratePct == null) {
                        "—"
                    } else {
                        com.example.util.AppNumberFormatter.formatPercent(ratePct, includeSign = true, decimals = 4)
                    }
                    Text(
                        text = formattedRate,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = JetBrainsMonoFont,
                        color = rateColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = strings.futuresNextFundingIn, fontSize = 11.sp, color = palette.textMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FundingCountdownBadge(nextFundingMs = nextFundingMs)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.futuresApproxAprLabel, fontSize = 11.sp, color = palette.textSecondary)
                    val aprFormatted = if (approxApr == null) {
                        "—"
                    } else {
                        com.example.util.AppNumberFormatter.formatPercent(approxApr, includeSign = true, decimals = 2)
                    }
                    Text(
                        text = if (approxApr == null) "—" else "$aprFormatted (approx)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = rateColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MetricExplainerBox(
                whatItIs = "Periodic funding fee exchanged between longs and shorts on perpetual contracts.",
                whatItShows = "Traders' positioning crowding and directional leverage bias across perpetual venues.",
                whatItDoesNotMean = "Crowded skew reflects market leverage, not an immediate or guaranteed price reversal."
            )
        }
    }
}

@Composable
fun OpenInterestCard(
    symbol: String,
    coin: CryptoCoin? = null,
    openInterest: FuturesOpenInterest?,
    isProUnlocked: Boolean
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val isMatchingOi = openInterest?.symbol.equals(symbol, ignoreCase = true) ||
            openInterest?.symbol.equals(symbol.removePrefix("1000"), ignoreCase = true)

    val oi = if (isMatchingOi) openInterest?.openInterest else null
    val oiUsd = if (isMatchingOi) openInterest?.openInterestUsd else null
    val isAvail = isMatchingOi && openInterest?.isAvailable == true && oiUsd != null && oiUsd > 0.0
    val lastRefreshMs: Long = (if (isMatchingOi) openInterest?.lastRefreshTimeMs else null)?.takeIf { it > 0L } ?: 0L
    val ageSec = if (lastRefreshMs > 0) ((nowMs - lastRefreshMs) / 1000).coerceAtLeast(0) else 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.futuresOpenInterestTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    color = palette.textMuted
                )
                if (!isProUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(QuantumCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FREE TIER · 15-MIN DELAYED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }
                } else {
                    Text(
                        text = if (isAvail) "Binance USDT-M REST · ${ageSec}s ago" else "Binance USDT-M REST · ${strings.futuresStateUnavailable}",
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        color = if (isAvail) palette.textMuted else SoftCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAvail && oiUsd != null) formatUsdCompact(oiUsd) else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = JetBrainsMonoFont,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (oi != null) "${formatNumberCompact(oi)} ${strings.futuresContracts} (${symbol.removeSuffix("USDT")})" else "—",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = palette.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "15s REST", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMonoFont, color = palette.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MetricExplainerBox(
                whatItIs = "Aggregate gross open positions and dollar valuation across active perpetual contracts.",
                whatItShows = "Net inflow or outflow of derivatives capital and ongoing leverage expansion.",
                whatItDoesNotMean = "Measures outstanding derivatives commitment, not directional outcome without price context."
            )
        }
    }
}

@Composable
fun MacroSentimentRow(
    sentiment: MacroMarketSentiment
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = strings.futuresFearGreed5m, fontSize = 10.sp, color = palette.textMuted)
                Spacer(modifier = Modifier.height(2.dp))
                if (sentiment.fearAndGreedValue != null) {
                    val fngColor = when {
                        sentiment.fearAndGreedValue < 25 -> SoftCrimson
                        sentiment.fearAndGreedValue < 45 -> NeonAmber
                        sentiment.fearAndGreedValue < 60 -> PhotonGold
                        else -> TachyonMint
                    }
                    Text(
                        text = "${sentiment.fearAndGreedValue} · ${sentiment.fearAndGreedClassification}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = fngColor
                    )
                } else {
                    Text(text = strings.futuresStateUnavailable, fontSize = 12.sp, color = palette.textMuted)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = strings.futuresBtcDominance60s, fontSize = 10.sp, color = palette.textMuted)
                Spacer(modifier = Modifier.height(2.dp))
                if (sentiment.btcDominance != null) {
                    val domFormatted = com.example.util.AppNumberFormatter.formatPercent(sentiment.btcDominance, includeSign = false, decimals = 2)
                    Text(
                        text = domFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                } else {
                    Text(text = strings.futuresStateUnavailable, fontSize = 12.sp, color = palette.textMuted)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    MetricExplainerBox(
        whatItIs = "Sentiment index (0-100) and BTC market capitalization share.",
        whatItShows = "Broader market risk appetite skew and capital rotation between Bitcoin and Altcoins.",
        whatItDoesNotMean = "Broad market macro posture, not a short-term trade execution signal."
    )
}

@Composable
fun LiveTradesTapeCard(
    trades: List<FuturesTrade>,
    symbol: String
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (palette.isLight) palette.border else CosmicBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.futuresTradesTapeTitle} ($symbol)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 6.dp),
                    color = palette.textMuted
                )
                Text(
                    text = "Binance @aggTrade",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = palette.textMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = strings.futuresPriceHeader, fontSize = 10.sp, color = palette.textMuted)
                Text(text = strings.futuresQtyHeader, fontSize = 10.sp, color = palette.textMuted)
                Text(text = strings.futuresTimeHeader, fontSize = 10.sp, color = palette.textMuted)
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (trades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = strings.futuresTradesAwaiting, fontSize = 11.sp, color = palette.textMuted)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    trades.take(12).forEach { trade ->
                        val tradeColor = if (trade.isSell) SoftCrimson else TachyonMint

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatPrice(trade.price),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFont,
                                color = tradeColor
                            )
                            Text(
                                text = com.example.util.AppNumberFormatter.formatRawPrice(trade.qty, 4),
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFont,
                                color = palette.textPrimary
                            )
                            Text(
                                text = timeFormat.format(Date(trade.timeMs)),
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFont,
                                color = palette.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceAgeBadge(
    source: String,
    ageSec: Double?,
    isStale: Boolean
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val badgeColor = when {
        ageSec == null -> TachyonMint
        isStale -> QuantumCyan
        else -> TachyonMint
    }
    val badgeText = when {
        ageSec == null -> strings.futuresStateLive
        isStale -> strings.futuresStateStale
        else -> strings.futuresStateLive
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        val ageLabel = if (ageSec != null && ageSec >= 1.0) "Updated ${ageSec.toInt()}s ago" else "Updated just now"
        Text(
            text = "$source · $ageLabel",
            fontSize = 9.5.sp,
            maxLines = 1,
            softWrap = false,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = badgeText,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                color = badgeColor
            )
        }
    }
}

@Composable
fun LockedFuturesPairCard(
    coin: CryptoCoin?,
    onOpenProModal: () -> Unit
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (palette.isLight) Color(0xFFFFFFFF) else CosmicVoidSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, QuantumCyan.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(QuantumCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = QuantumCyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.futuresLockedTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.futuresLockedDesc.format(coin?.name ?: "This asset", coin?.symbol ?: ""),
                fontSize = 12.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.primary)
                    .clickable { onOpenProModal() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("unlock_pro_futures_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.futuresLockedButton,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
internal fun formatPrice(price: Double): String {
    val cur = LocalFuturesCurrency.current
    return com.example.util.AppNumberFormatter.formatPrice(price, currency = cur)
}

@Composable
internal fun formatUsdCompact(usd: Double): String {
    val cur = LocalFuturesCurrency.current
    return com.example.util.AppNumberFormatter.formatCompactCurrency(usd, currency = cur)
}

internal fun formatNumberCompact(num: Double): String {
    return com.example.util.AppNumberFormatter.formatCompactNumber(num)
}
