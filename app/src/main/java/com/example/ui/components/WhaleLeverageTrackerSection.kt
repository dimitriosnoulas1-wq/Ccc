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
    var kindFilter by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(true) }

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
                (kindFilter == null || pos.statusText == kindFilter)
    }

    val displayPositions = if (!isProUnlocked) {
        filteredPositions.take(3)
    } else {
        filteredPositions
    }

    val symbol = positions.firstOrNull { it.statusText == OI_KIND }?.coinSymbol
        ?: positions.firstOrNull()?.coinSymbol.orEmpty()
    val venueCount = positions.count { it.statusText == OI_KIND }
    val liqCount = positions.count { it.statusText == LIQ_KIND }

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
                    Text(text = "📊", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (if (isGreek) "Open interest & εκκαθαρίσεις" else "Open interest & liquidations") +
                                if (symbol.isNotBlank()) " · $symbol" else "",
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
                                    text = if (positions.isNotEmpty()) "LIVE OI / LIQS" else "NO DATA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isGreek) "Ζωντανό Open Interest και ρευστοποιήσεις (Binance / Bybit / OKX)" else "Live open interest and liquidations (Binance / Bybit / OKX)",
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
                // Longs volume (dashes, not zeros, while there are no positions)
                val hasPositions = positions.isNotEmpty()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = GainGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = (if (isGreek) "Λογαριασμοί long: " else "Long accounts: ") +
                            if (hasPositions && summary.longRatioPercent > 0.0) String.format(java.util.Locale.US, "%.1f%%", summary.longRatioPercent) else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GainGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Shorts volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = (if (hasPositions && summary.shortRatioPercent > 0.0) String.format(java.util.Locale.US, "%.1f%%", summary.shortRatioPercent) else "—") +
                            if (isGreek) " short" else " short",
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

            // Visual Long / Short Ratio Bar (neutral until there is data)
            val longFraction = if (positions.isNotEmpty()) {
                (summary.longRatioPercent / 100.0).toFloat().coerceIn(0.05f, 0.95f)
            } else {
                0.5f
            }
            if (positions.isNotEmpty()) Row(
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
                    text = run {
                        val totalOi = summary.totalLongVolumeUsd + summary.totalShortVolumeUsd
                        val value = if (totalOi > 0.0) formatMillionValue(totalOi * currency.rateToUsd, currency.symbol) else "—"
                        if (isGreek) "Συνολικό OI: $value" else "Total OI: $value"
                    },
                    fontSize = 10.sp,
                    color = NeonAmber,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isGreek) "Ανταλλακτήρια: $venueCount · Εκκαθαρίσεις: $liqCount"
                    else "Venues: $venueCount · Liquidations: $liqCount",
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
                    FilterPill(
                        label = if (isGreek) "Όλα" else "All",
                        isSelected = selectedSideFilter == null && kindFilter == null,
                        onClick = {
                            selectedSideFilter = null
                            kindFilter = null
                        }
                    )
                    FilterPill(
                        label = "Open interest",
                        isSelected = kindFilter == OI_KIND,
                        onClick = { kindFilter = if (kindFilter == OI_KIND) null else OI_KIND }
                    )
                    FilterPill(
                        label = if (isGreek) "Εκκαθαρίσεις" else "Liquidations",
                        isSelected = kindFilter == LIQ_KIND,
                        selectedColor = NeonAmber,
                        onClick = { kindFilter = if (kindFilter == LIQ_KIND) null else LIQ_KIND }
                    )
                    FilterPill(
                        label = if (isGreek) "🟢 Long" else "🟢 Long",
                        isSelected = selectedSideFilter == LeveragePositionSide.LONG,
                        selectedColor = GainGreen,
                        onClick = {
                            selectedSideFilter = if (selectedSideFilter == LeveragePositionSide.LONG) null else LeveragePositionSide.LONG
                        }
                    )
                    FilterPill(
                        label = if (isGreek) "🔴 Short" else "🔴 Short",
                        isSelected = selectedSideFilter == LeveragePositionSide.SHORT,
                        selectedColor = DrawdownRed,
                        onClick = {
                            selectedSideFilter = if (selectedSideFilter == LeveragePositionSide.SHORT) null else LeveragePositionSide.SHORT
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Whale Positions
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (displayPositions.isEmpty()) {
                        Text(
                            text = if (isGreek) "Δεν υπάρχουν δεδομένα για αυτό το φίλτρο." else "Nothing for this filter right now.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    displayPositions.forEach { pos ->
                        WhaleLeverageCard(
                            position = pos,
                            currency = currency,
                            isGreek = isGreek
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
                                    text = if (isGreek) "Ξεκλείδωσε όλα τα ανταλλακτήρια και τις εκκαθαρίσεις"
                                    else "Unlock all venues and liquidations",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isGreek) "Open interest από Binance, Bybit, OKX και εκκαθαρίσεις από $50K στη Binance"
                                else "Open interest from Binance, Bybit and OKX, and Binance liquidations from $50K",
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

private const val OI_KIND = "LIVE OI"
private const val LIQ_KIND = "LIQUIDATED"

/**
 * One row of real data: either a venue's aggregate open interest or a single liquidation.
 * Individual traders' positions are not public, so no entry price, leverage or PnL is shown.
 */
@Composable
private fun WhaleLeverageCard(
    position: WhaleLeveragePosition,
    currency: Currency,
    isGreek: Boolean
) {
    val isLong = position.side == LeveragePositionSide.LONG
    val isLiq = position.statusText == LIQ_KIND
    val themeColor = when {
        isLiq -> NeonAmber
        isLong -> GainGreen
        else -> DrawdownRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070D18))
            .border(1.dp, themeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinAvatar(symbol = position.coinSymbol, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = position.coinSymbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColor.copy(alpha = 0.18f))
                        .border(0.5.dp, themeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isLiq && isLong -> if (isGreek) "LONG ΕΚΚΑΘΑΡΙΣΗ" else "LONG LIQUIDATED"
                            isLiq -> if (isGreek) "SHORT ΕΚΚΑΘΑΡΙΣΗ" else "SHORT LIQUIDATED"
                            else -> "OPEN INTEREST"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }
            Text(
                text = position.exchange.displayName,
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLiq) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabeledValue(if (isGreek) "Μέγεθος" else "Size", formatMillionValue(position.notionalUsd * currency.rateToUsd, currency.symbol))
                LabeledValue(if (isGreek) "Τιμή" else "Price", formatPriceFormatted(position.liquidationPrice * currency.rateToUsd, currency.symbol))
                LabeledValue(
                    if (isGreek) "Ώρα" else "Time",
                    if (position.timestampMillis > 0L) {
                        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(position.timestampMillis))
                    } else "—",
                    alignEnd = true
                )
            }
        } else {
            val oi = position.collateralUsd
            val longPct = if (oi > 0.0) {
                val share = position.notionalUsd / oi * 100.0
                if (isLong) share else 100.0 - share
            } else null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabeledValue("Open interest", formatMillionValue(oi * currency.rateToUsd, currency.symbol))
                LabeledValue(
                    if (isGreek) "Λογαρ. long/short" else "Accounts long/short",
                    longPct?.let { String.format(java.util.Locale.US, "%.0f%% / %.0f%%", it, 100.0 - it) } ?: "—"
                )
                LabeledValue(
                    "Funding (8h)",
                    String.format(java.util.Locale.US, "%+.4f%%", position.fundingRate),
                    alignEnd = true
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = (if (isGreek) "24ω: " else "24h: ") + String.format(java.util.Locale.US, "%+.1f%%", position.pnlPercent),
                    fontSize = 10.sp,
                    color = if (position.pnlPercent >= 0) GainGreen else DrawdownRed
                )
                if (position.pnlUsd > 0.0) {
                    Text(
                        text = (if (isGreek) "Long εκκαθαρίσεις: " else "Long liquidations: ") +
                            formatMillionValue(position.pnlUsd * currency.rateToUsd, currency.symbol),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
