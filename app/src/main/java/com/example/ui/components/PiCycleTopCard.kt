package com.example.ui.components

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.PiCycleData
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.TachyonMint
import com.example.util.LocalAppStrings

enum class PiCycleTab(val titleEn: String, val titleEl: String) {
    BOTH("Both (Top & Bottom)", "Όλα (Κορυφή & Πυθμένας)"),
    TOP("🔴 Pi Cycle Top", "🔴 Pi Cycle Top (Κορυφή)"),
    BOTTOM("🟢 Pi Cycle Bottom", "🟢 Pi Cycle Bottom (Πυθμένας)")
}

@Composable
fun PiCycleTopCard(
    piData: PiCycleData,
    currency: Currency,
    isProUnlocked: Boolean = true,
    onOpenProModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"
    var selectedTab by remember { mutableStateOf(PiCycleTab.BOTH) }

    // Always render full interactive Pi Cycle charts & moving averages

    val topStatusColor = if (piData.isCrossed) SoftCrimson else TachyonMint
    val topStatusText = if (piData.isCrossed) strings.piCycleStatusAlertText else strings.piCycleStatusSafeText

    val bottomStatusColor = if (piData.isBottomCrossed) TachyonMint else QuantumCyan
    val bottomStatusText = if (piData.isBottomCrossed) strings.piCycleBottomStatusAlertText else strings.piCycleBottomStatusSafeText

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (palette.isLight) Color.White else CosmicVoidSurface)
            .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
            .testTag("pi_cycle_top_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MauveAurora.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CrisisAlert,
                            contentDescription = "Pi Cycle Indicators",
                            tint = MauveAurora,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.piCycleHeaderTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.piCycleSubtitle,
                    fontSize = 10.5.sp,
                    color = palette.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector (Both / Top / Bottom)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PiCycleTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) palette.primary
                            else if (palette.isLight) Color(0xFFF1F5F9)
                            else CosmicVoidSurface
                        )
                        .border(
                            1.dp,
                            if (isSelected) palette.primary else if (palette.isLight) palette.border else CosmicBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isGreek) tab.titleEl else tab.titleEn,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (palette.isLight) Color.White else CosmicVoidBg) else palette.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔴 SECTION 1: PI CYCLE TOP INDICATOR
        if (selectedTab == PiCycleTab.BOTH || selectedTab == PiCycleTab.TOP) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (palette.isLight) Color(0xFFFEF2F2).copy(alpha = 0.6f) else CosmicVoidSurface)
                    .border(1.dp, SoftCrimson.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Pi Top",
                            tint = SoftCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.piCycleTopSectionTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCrimson,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(topStatusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        val gapFormatted = com.example.util.AppNumberFormatter.formatPercent(piData.distanceToTopCrossPct, includeSign = true, decimals = 1)
                        Text(
                            text = if (piData.isCrossed) "CROSS ALERT" else "$gapFormatted Gap",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = topStatusColor
                        )
                    }
                }

                // Top Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(topStatusColor.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(topStatusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = topStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = topStatusColor
                        )
                    }
                }

                // Top MAs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 111 DMA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (palette.isLight) Color.White else CosmicVoidSurfaceElevated)
                            .border(1.dp, QuantumCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(QuantumCyan)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.piCycleDma111,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(piData.dma111, currency, decimals = 0),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = QuantumCyan
                            )
                        }
                    }

                    // 350 DMA x 2 (Top Threshold)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (palette.isLight) Color.White else CosmicVoidSurfaceElevated)
                            .border(1.dp, SoftCrimson.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SoftCrimson)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.piCycleDma350x2,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(piData.dma350x2, currency, decimals = 0),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftCrimson
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 🟢 SECTION 2: PI CYCLE BOTTOM INDICATOR
        if (selectedTab == PiCycleTab.BOTH || selectedTab == PiCycleTab.BOTTOM) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (palette.isLight) Color(0xFFF0FDF4).copy(alpha = 0.6f) else CosmicVoidSurface)
                    .border(1.dp, TachyonMint.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Pi Bottom",
                            tint = TachyonMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.piCycleBottomSectionTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TachyonMint,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bottomStatusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        val floorFormatted = com.example.util.AppNumberFormatter.formatPercent(piData.distanceToBottomCrossPct, includeSign = true, decimals = 1)
                        Text(
                            text = if (piData.isBottomCrossed) "BUY TRIGGER" else "$floorFormatted Above Floor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = bottomStatusColor
                        )
                    }
                }

                // Bottom Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bottomStatusColor.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(bottomStatusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bottomStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = bottomStatusColor
                        )
                    }
                }

                // Bottom MAs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 150 EMA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (palette.isLight) Color.White else CosmicVoidSurfaceElevated)
                            .border(1.dp, QuantumCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(QuantumCyan)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.piCycleEma150,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(piData.ema150, currency, decimals = 0),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = QuantumCyan
                            )
                        }
                    }

                    // 471 SMA x 0.745 (Bottom Threshold)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (palette.isLight) Color.White else CosmicVoidSurfaceElevated)
                            .border(1.dp, TachyonMint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(TachyonMint)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.piCycleSma471x0745,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(piData.sma471x0745, currency, decimals = 0),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TachyonMint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 🛡️ 200W MA Floor Support Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurfaceElevated)
                .border(1.dp, QuantumCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(QuantumCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "200W MA Floor",
                            tint = QuantumCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.piCycleMa200w,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                        Text(
                            text = com.example.util.AppNumberFormatter.formatPrice(piData.ma200w, currency, decimals = 0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = QuantumCyan
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val distFormatted = com.example.util.AppNumberFormatter.formatPercent(piData.distanceAbove200wPct, includeSign = true, decimals = 1)
                    Text(
                        text = "$distFormatted Above Floor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STRATEGY & HISTORICAL REFERENCE GUIDE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface)
                .border(1.dp, if (palette.isLight) Color(0xFFCBD5E1) else CosmicBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "Historical Reference Guide",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGreek) "📖 Οδηγός Στρατηγικής & Ιστορικά Σήματα" else "📖 Strategy Guide & Historical Crosses",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            // 1. BUY / ACCUMULATION RULES (PI CYCLE BOTTOM)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TachyonMint.copy(alpha = 0.12f))
                    .border(1.dp, TachyonMint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isGreek) "🟢 Πότε ΑΓΟΡΑΖΟΥΜΕ (Pi Cycle Bottom Cross)" else "🟢 When to BUY (Pi Cycle Bottom Cross)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TachyonMint
                    )
                    Text(
                        text = if (isGreek)
                            "• Σήμα Πυθμένα: Όταν ο 150-Day EMA πέσει κάτω από τον 471-Day SMA × 0.745.\n• Ιστορικές Επιτυχίες: Δεκέμβριος 2018 ($3.200), Μάρτιος 2020 ($4.000 Covid crash), Δεκέμβριος 2022 ($16.000 FTX crash). Σηματοδοτεί το τέλος του bear market και τη μέγιστη ευκαιρία συσσώρευσης!\n• 200W MA Floor: Όταν η τιμή πλησιάζει το 200W MA, παρέχει το ασφαλέστερο πολυετές δίχτυ προστασίας."
                        else
                            "• Bottom Trigger: When 150-Day EMA crosses below 471-Day SMA × 0.745.\n• Historical Track Record: Dec 2018 ($3.2k), Mar 2020 ($4k Covid crash), Dec 2022 ($16k FTX crash). Marks the absolute bear market capitulation and generational entry point!\n• 200W MA Floor: Touching or approaching the 200W MA provides generational asymmetry.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = palette.textPrimary
                    )
                }
            }

            // 2. SELL / TAKE PROFIT RULES (PI CYCLE TOP)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftCrimson.copy(alpha = 0.12f))
                    .border(1.dp, SoftCrimson.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isGreek) "🔴 Πότε ΠΟΥΛΑΜΕ (Pi Cycle Top Cross)" else "🔴 When to SELL (Pi Cycle Top Cross)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftCrimson
                    )
                    Text(
                        text = if (isGreek)
                            "• Σήμα Κορυφής: Όταν ο 111-Day SMA διασταυρωθεί ανοδικά πάνω από τον 350-Day SMA × 2.\n• Ιστορικές Επιτυχίες: Απρίλιος 2013 ($260), Δεκέμβριος 2013 ($1.150), Δεκέμβριος 2017 ($20.000), Απρίλιος 2021 ($64.800) – εντόπισε την κορυφή εντός ±3 ημερών!\n• Στρατηγική: Όταν η απόσταση μειωθεί κάτω από 5%, συνιστάται σταδιακή ρευστοποίηση σε Stablecoins (DCA Out)."
                        else
                            "• Top Trigger: When 111-Day SMA crosses above 350-Day SMA × 2.\n• Historical Track Record: Apr 2013 ($260), Dec 2013 ($1,150), Dec 2017 ($20k), Apr 2021 ($64.8k) – flagged the exact cycle top within ±3 days!\n• Strategy: When the gap narrows below 5%, scale profits systematically into Stablecoins (DCA Out).",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = palette.textPrimary
                    )
                }
            }

            // 3. CURRENT SUMMARY CALLOUT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceElevated)
                    .padding(10.dp)
            ) {
                val topDistFormatted = com.example.util.AppNumberFormatter.formatPercent(piData.distanceToTopCrossPct, includeSign = true, decimals = 1)
                val botDistFormatted = com.example.util.AppNumberFormatter.formatPercent(piData.distanceToBottomCrossPct, includeSign = true, decimals = 1)
                Text(
                    text = if (isGreek)
                        "⚡ Τρέχουσα Κατάσταση: Ο 111 SMA απέχει ασφαλή απόσταση από τον 350 SMA × 2 ($topDistFormatted απόσταση κορυφής), ενώ ο 150 EMA βρίσκεται $botDistFormatted πάνω από το όριο πυθμένα. Βρισκόμαστε σε υγιή φάση επέκτασης κύκλου."
                    else
                        "⚡ Current Verdict: 111 SMA is at a healthy $topDistFormatted gap below the 350 SMA × 2 top trigger, while 150 EMA is $botDistFormatted above the bottom floor. Market remains in a normal cycle expansion phase.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonCyan,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
