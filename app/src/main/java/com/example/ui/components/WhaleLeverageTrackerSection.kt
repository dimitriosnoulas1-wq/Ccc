package com.example.ui.components

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.LeveragePositionSide
import com.example.data.model.WhaleLeveragePosition
import com.example.data.model.WhaleLeverageSummary
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.GainGreen
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalAppStrings
import kotlin.math.abs

@Composable
fun WhaleLeverageTrackerSection(
    positions: List<WhaleLeveragePosition>,
    summary: WhaleLeverageSummary,
    currency: Currency,
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    var selectedSideFilter by remember { mutableStateOf<LeveragePositionSide?>(null) }
    var minLeverageFilter by remember { mutableStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }
    var expandedPositionId by remember { mutableStateOf<String?>(null) }

    // Live glowing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "whale_perp_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val filteredPositions = positions.filter { pos ->
        (selectedSideFilter == null || pos.side == selectedSideFilter) &&
                (pos.leverage >= minLeverageFilter)
    }

    val displayPositions = if (!isProUnlocked) {
        filteredPositions.take(3)
    } else {
        filteredPositions
    }

    val topMegaIds = remember(positions) {
        positions.filter { it.isMegaWhale }.sortedByDescending { it.notionalUsd }.take(3).map { it.id }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CosmicSurface)
            .border(1.dp, NeonEmerald.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("whale_leverage_tracker_section")
    ) {
        // Section Header: Title + Live Badge + Expand Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    NeonEmerald.copy(alpha = 0.25f),
                                    NeonCyan.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(1.dp, NeonEmerald.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐋", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isGreek) "Super Whale Leverage Radar" else "Whale Leverage Radar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonEmerald.copy(alpha = 0.20f))
                                .border(0.5.dp, NeonEmerald, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald.copy(alpha = glowAlpha))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "2x - 50x DEMO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isGreek) "Ζωντανές θέσεις εκατομμυρίων με μόχλευση (Longs/Shorts)" else "Live multi-million $ leverage positions & liquidations",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CosmicSurfaceElevated)
                    .clickable { isExpanded = !isExpanded }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand/Collapse",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Open Interest Dominance Ratio Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CosmicSurfaceElevated)
                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Longs volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = GainGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Whale Longs: ${String.format("%.1f", summary.longRatioPercent)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GainGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${formatMillionValue(summary.totalLongVolumeUsd * currency.rateToUsd, currency.symbol)})",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Shorts volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "(${formatMillionValue(summary.totalShortVolumeUsd * currency.rateToUsd, currency.symbol)})",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", summary.shortRatioPercent)}% Shorts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DrawdownRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = DrawdownRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Long / Short Ratio Bar
            val longFraction = (summary.longRatioPercent / 100.0).toFloat().coerceIn(0.05f, 0.95f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CosmicVoidBg)
            ) {
                Box(
                    modifier = Modifier
                        .weight(longFraction)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    GainGreen,
                                    NeonEmerald
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f - longFraction)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    DrawdownRed.copy(alpha = 0.8f),
                                    DrawdownRed
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isGreek) "Μεγαλύτερο Position: ${formatMillionValue(summary.largestPositionUsd * currency.rateToUsd, currency.symbol)}"
                    else "Largest Whale: ${formatMillionValue(summary.largestPositionUsd * currency.rateToUsd, currency.symbol)}",
                    fontSize = 10.sp,
                    color = NeonAmber,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isGreek) "Ενεργά Mega Orders: ${summary.activeMegaPositionsCount}"
                    else "Active Mega Orders: ${summary.activeMegaPositionsCount}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // All filter
                    FilterPill(
                        label = if (isGreek) "Όλα" else "All",
                        isSelected = selectedSideFilter == null && minLeverageFilter == 0,
                        onClick = {
                            selectedSideFilter = null
                            minLeverageFilter = 0
                        }
                    )

                    // Longs only
                    FilterPill(
                        label = if (isGreek) "🟢 Longs Μόνο" else "🟢 Longs Only",
                        isSelected = selectedSideFilter == LeveragePositionSide.LONG,
                        selectedColor = GainGreen,
                        onClick = {
                            selectedSideFilter = if (selectedSideFilter == LeveragePositionSide.LONG) null else LeveragePositionSide.LONG
                        }
                    )

                    // Shorts only
                    FilterPill(
                        label = if (isGreek) "🔴 Shorts Μόνο" else "🔴 Shorts Only",
                        isSelected = selectedSideFilter == LeveragePositionSide.SHORT,
                        selectedColor = DrawdownRed,
                        onClick = {
                            selectedSideFilter = if (selectedSideFilter == LeveragePositionSide.SHORT) null else LeveragePositionSide.SHORT
                        }
                    )

                    // Leverage 5x+
                    FilterPill(
                        label = "⚡ 5x+",
                        isSelected = minLeverageFilter == 5,
                        selectedColor = NeonAmber,
                        onClick = {
                            minLeverageFilter = if (minLeverageFilter == 5) 0 else 5
                        }
                    )

                    // Leverage 10x+
                    FilterPill(
                        label = "⚡ 10x+",
                        isSelected = minLeverageFilter == 10,
                        selectedColor = NeonAmber,
                        onClick = {
                            minLeverageFilter = if (minLeverageFilter == 10) 0 else 10
                        }
                    )

                    // Leverage 20x+
                    FilterPill(
                        label = "🔥 20x+ Degen",
                        isSelected = minLeverageFilter == 20,
                        selectedColor = NeonPurple,
                        onClick = {
                            minLeverageFilter = if (minLeverageFilter == 20) 0 else 20
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Whale Positions
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayPositions.forEach { pos ->
                        val isDetailsExpanded = expandedPositionId == pos.id
                        WhaleLeverageCard(
                            position = pos,
                            currency = currency,
                            isMegaWhale = topMegaIds.contains(pos.id),
                            isExpanded = isDetailsExpanded,
                            onToggleExpand = {
                                expandedPositionId = if (isDetailsExpanded) null else pos.id
                            }
                        )
                    }
                }

                // Pro Gatekeeper Banner for non-pro users
                if (!isProUnlocked && positions.size > 3) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF131D33),
                                        Color(0xFF0B1120)
                                    )
                                )
                            )
                            .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { onOpenProModal() }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock",
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGreek) "Ξεκλειδώστε όλο το Whale Leverage Stream (15+ Θέσεις)"
                                    else "Unlock Full Whale Leverage Stream (15+ Positions)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isGreek) "Δείτε σε πραγματικό χρόνο εντολές 2x-50x άνω των $1M στο Binance USDT-M Futures"
                                else "Stream real-time 2x-50x orders >$1M on Binance USDT-M Futures",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    selectedColor: Color = NeonCyan,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.20f) else CosmicSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) selectedColor else CosmicBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) selectedColor else TextSecondary
        )
    }
}

@Composable
private fun WhaleLeverageCard(
    position: WhaleLeveragePosition,
    currency: Currency,
    isMegaWhale: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val isLong = position.side == LeveragePositionSide.LONG
    val themeColor = if (isLong) GainGreen else DrawdownRed
    val pnlColor = if (position.pnlUsd >= 0) GainGreen else DrawdownRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070D18))
            .border(1.dp, themeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onToggleExpand() }
            .padding(12.dp)
    ) {
        // Top Row: Coin Symbol + Side Badge + Leverage Badge + Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Coin Avatar + Badge
                CoinAvatar(
                    symbol = position.coinSymbol,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111C30))
                        .border(1.dp, Color(0xFF1E2E4A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = position.coinSymbol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Side & Leverage Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColor.copy(alpha = 0.18f))
                        .border(0.5.dp, themeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLong) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${position.leverage}x ${position.side.shortLabel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }
                }

                if (isMegaWhale) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔥 MEGA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                }
            }

            // Time & Exchange Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = position.exchange.displayName,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "· ${position.timeAgo}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Middle Row: Total Position Size vs Live Unrealized PnL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Position Size (Notional)",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = formatMillionValue(position.notionalUsd * currency.rateToUsd, currency.symbol),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Unrealized PnL",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (position.pnlUsd >= 0) "+" else ""}${formatPriceFormatted(position.pnlUsd * currency.rateToUsd, currency.symbol)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${if (position.pnlPercent >= 0) "+" else ""}${String.format("%.1f", position.pnlPercent)}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price Milestones: Entry -> Current -> Liquidation Price Meter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0C1424))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Entry Price", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = formatPriceFormatted(position.entryPrice * currency.rateToUsd, currency.symbol),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Current Mark", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = formatPriceFormatted(position.currentPrice * currency.rateToUsd, currency.symbol),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = DrawdownRed,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Est. Liq Price", fontSize = 9.sp, color = DrawdownRed)
                }
                Text(
                    text = formatPriceFormatted(position.liquidationPrice * currency.rateToUsd, currency.symbol),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DrawdownRed
                )
            }
        }

        // Expanded Details (Trader ID, Margin, Funding Rate)
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Trader: ${position.traderLabel}",
                        fontSize = 10.sp,
                        color = NeonAmber,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Margin: ${formatMillionValue(position.collateralUsd * currency.rateToUsd, currency.symbol)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val liqDistPct = abs((position.currentPrice - position.liquidationPrice) / position.currentPrice) * 100.0
                    Text(
                        text = "Liquidation Distance: ${String.format("%.1f", liqDistPct)}% safe buffer",
                        fontSize = 10.sp,
                        color = if (liqDistPct > 10.0) GainGreen else DrawdownRed
                    )
                    Text(
                        text = "Funding (8h): +${String.format("%.3f", position.fundingRate)}%",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

private fun formatMillionValue(value: Double, symbol: String): String {
    val prefix = if (value < 0) "-" else ""
    val formatted = com.example.util.AppNumberFormatter.formatCompactCurrency(abs(value), currencySymbol = symbol)
    return prefix + formatted
}

private fun formatPriceFormatted(value: Double, symbol: String): String {
    val prefix = if (value < 0) "-" else ""
    val absVal = abs(value)
    val decimals = when {
        absVal >= 1000.0 -> 2
        absVal >= 1.0 -> 2
        absVal >= 0.01 -> 4
        else -> 6
    }
    val formatted = com.example.util.AppNumberFormatter.formatRawPrice(absVal, decimals = decimals)
    return prefix + symbol + formatted.removePrefix("$")
}
