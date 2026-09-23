package com.example.ui.components

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CycleDayComparativeReport
import com.example.data.model.HistoricalCrashEvent
import com.example.data.model.HistoricalCycleComparison
import com.example.data.repository.CycleComparativeRepository
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.GainGreen
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.util.LocalAppStrings
import java.util.Locale

@Composable
fun CycleDayComparisonCard(
    currentBtcPrice: Double = 58240.0,
    onOpenAiAnalysis: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { CycleComparativeRepository() }
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    val liveDaysSinceAth = remember { repository.getLiveDaysSinceAth() }
    var selectedDayOffset by remember { mutableStateOf(liveDaysSinceAth) }

    val report = remember(selectedDayOffset, currentBtcPrice) {
        repository.getComparativeReport(selectedDayOffset, currentBtcPrice)
    }

    var showTimelineDossier by remember { mutableStateOf(false) }
    var expandedCrashId by remember { mutableStateOf<String?>("bch_hash_war_2018") }

    // Pulsing beacon for live day
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.border, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("cycle_day_comparison_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TOP HEADER ROW: Title, Live Day Indicator, Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(if (report.isTodayLive) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(if (report.isTodayLive) GainGreen else NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGreek) "ΙΣΤΟΡΙΚΗ ΣΥΓΚΡΙΣΗ ΚΥΚΛΩΝ" else "CYCLE DAYS & BEAR MARKET ANALOG",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = palette.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isGreek)
                        "Σύγκριση θέσης ${report.targetDayOffset} ημερών post-ATH με παρελθόντες κύκλους"
                    else
                        "Price position ${report.targetDayOffset} days post-ATH vs past cycles",
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isGreek) "Ερώτηση στο AI" else "Ask AI about this day",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable {
                            com.example.util.AppSoundManager.playTechClick()
                            onOpenAiAnalysis(
                                "Explain Bitcoin's cycle position ${report.targetDayOffset} days after the all-time high, compared with previous cycles. Current price is ${'$'}${String.format(Locale.US, "%.0f", currentBtcPrice)}."
                            )
                        }
                )
            }

            if (!report.isTodayLive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.primary.copy(alpha = 0.15f))
                        .border(1.dp, palette.primary, RoundedCornerShape(6.dp))
                        .clickable {
                            com.example.util.AppSoundManager.playTechClick()
                            selectedDayOffset = liveDaysSinceAth
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGreek) "Live Σήμερα" else "Live Today",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                }
            }
        }

        // 2. DAY OFFSET SELECTOR CHIPS
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isGreek) "ΕΠΙΛΟΓΗ ΗΜΕΡΑΣ ΜΕΤΑ ΤΟ ALL-TIME HIGH:" else "SELECT DAY OFFSET POST-ATH:",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chips = listOf(
                    Triple(60, "Day 60", if (isGreek) "Πρώτη Πτώση" else "Initial Drop"),
                    Triple(180, "Day 180", if (isGreek) "Κραχ Luna / UST" else "Luna / UST Crash"),
                    Triple(liveDaysSinceAth, "Day $liveDaysSinceAth", if (isGreek) "ΣΗΜΕΡΑ (Live)" else "TODAY (Live)"),
                    Triple(363, "Day 363", if (isGreek) "Πυθμένας 2018 ($3.1k)" else "2018 Bottom ($3.1k)"),
                    Triple(376, "Day 376", if (isGreek) "Πυθμένας 2022 ($15.5k)" else "2022 Bottom ($15.5k)"),
                    Triple(410, "Day 410", if (isGreek) "Πυθμένας 2015 ($152)" else "2015 Bottom ($152)")
                )

                chips.forEach { (offset, label, sub) ->
                    val isSelected = (selectedDayOffset == offset)
                    val isLiveToday = (offset == liveDaysSinceAth)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> palette.primary.copy(alpha = 0.22f)
                                    isLiveToday -> GainGreen.copy(alpha = 0.12f)
                                    else -> if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = when {
                                    isSelected -> palette.primary
                                    isLiveToday -> GainGreen.copy(alpha = 0.6f)
                                    else -> if (palette.isLight) palette.border else CosmicBorder
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                com.example.util.AppSoundManager.playSelectionPop()
                                selectedDayOffset = offset
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) palette.primary else if (isLiveToday) GainGreen else palette.textPrimary
                            )
                            Text(
                                text = sub,
                                fontSize = 8.5.sp,
                                color = if (isSelected) palette.primary else palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. CURRENT METRIC BANNER (Compact & Structured)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (palette.isLight) Color(0xFFF8FAFC) else Color(0xFF08101E))
                .border(1.dp, palette.primary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGreek) "ΦΑΣΗ ΣΤΗΝ ΗΜΕΡΑ ${report.targetDayOffset}:" else "PHASE AT DAY ${report.targetDayOffset}:",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textMuted
                        )
                        Text(
                            text = if (isGreek) report.phaseNameEl else report.phaseNameEn,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonAmber.copy(alpha = 0.15f))
                            .border(1.dp, NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isGreek) "ATH: $126.5k" else "ATH: $126.5k",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                }

                Text(
                    text = if (isGreek) report.phaseDiagnosisEl else report.phaseDiagnosisEn,
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    lineHeight = 15.sp
                )

                // Bottom Window Countdown Bar
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isGreek) "Απόσταση από Πυθμένα 1 Έτους (~Day 375):" else "Proximity to 1-Yr Bottom (~Day 375):",
                            fontSize = 9.5.sp,
                            color = palette.textMuted
                        )
                        Text(
                            text = if (report.daysUntilHistoricalBottom > 0)
                                (if (isGreek) "Απομένουν ~${report.daysUntilHistoricalBottom} ημ." else "~${report.daysUntilHistoricalBottom} days left")
                            else
                                (if (isGreek) "Ζώνη Πυθμένα" else "Bottom Window"),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.daysUntilHistoricalBottom in 0..45) GainGreen else palette.primary
                        )
                    }

                    val progress = (report.targetDayOffset / 375f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (report.targetDayOffset >= 350) GainGreen else palette.primary,
                        trackColor = if (palette.isLight) Color(0xFFE2E8F0) else CosmicVoidBg
                    )
                }
            }
        }

        // 4. COMPARISON MATRIX: Day X across all previous bull/bear cycles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isGreek) "ΠΟΥ ΒΡΙΣΚΟΤΑΝ ΤΟ BITCOIN ΣΕ ΑΥΤΗΝ ΤΗΝ ΗΜΕΡΑ ΣΤΟΥΣ ΠΡΟΗΓΟΥΜΕΝΟΥΣ ΚΥΚΛΟΥΣ:" else "WHERE WAS BITCOIN ON THIS EXACT DAY IN PAST CYCLES:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = palette.textMuted
            )

            // 4.1 Current Cycle Card
            CurrentCycleDayCard(
                report = report,
                isGreek = isGreek
            )

            // 4.2 Past Cycles Cards
            report.comparisons.forEach { cycle ->
                PastCycleComparisonRow(
                    cycle = cycle,
                    targetDay = report.targetDayOffset,
                    isGreek = isGreek
                )
            }
        }

        // 5. ALTCOIN BEHAVIOR ON THIS EXACT DAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = DrawdownRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGreek) "ΠΩΣ ΚΙΝΟΥΝΤΑΝ ΤΑ ALTCOINS ΣΕ ΑΥΤΗΝ ΤΗΝ ΠΕΡΙΟΔΟ;" else "WHAT WERE ALTCOINS DOING IN THIS PERIOD?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
                Text(
                    text = if (isGreek) report.altcoinComparisonSummaryEl else report.altcoinComparisonSummaryEn,
                    fontSize = 11.5.sp,
                    color = palette.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // 6. HISTORICAL CRASH DOSSIER: "Why did it crash in past bear markets?"
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.primary.copy(alpha = 0.08f))
                    .border(1.dp, palette.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable {
                        com.example.util.AppSoundManager.playTechClick()
                        showTimelineDossier = !showTimelineDossier
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isGreek) "ΓΙΑΤΙ ΚΡΑΣΑΡΕ Η ΑΓΟΡΑ ΤΟΤΕ; (Αληθινά Γεγονότα & Αιτίες)" else "WHY DID THE MARKET CRASH? (Historical Facts & Causes)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (isGreek) "BCH Hash War, FTX, Terra/Luna, 3AC, Celsius & Mt. Gox" else "BCH Hash War, FTX, Terra/Luna, 3AC, Celsius & Mt. Gox",
                            fontSize = 10.sp,
                            color = palette.textSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (showTimelineDossier) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = showTimelineDossier) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.allHistoricalCrashTimeline.forEach { event ->
                        HistoricalCrashEventCard(
                            event = event,
                            isExpanded = (expandedCrashId == event.id),
                            onToggleExpand = {
                                com.example.util.AppSoundManager.playSelectionPop()
                                expandedCrashId = if (expandedCrashId == event.id) null else event.id
                            },
                            isGreek = isGreek
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isGreek) "⚠️ Ιστορικές αναφορές & σενάρια προηγούμενων κύκλων. Δεν αποτελούν επενδυτική συμβουλή ούτε εγγυημένους στόχους τιμών." else "⚠️ Historical cycle references & past scenarios. Not investment advice or price targets.",
            fontSize = 9.5.sp,
            color = palette.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CurrentCycleDayCard(
    report: CycleDayComparativeReport,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.primary.copy(alpha = 0.07f))
            .border(1.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(palette.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGreek) "Τρέχων Κύκλος (2025 - 2026)" else "Current Cycle (2025 - 2026)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                Text(
                    text = com.example.util.AppNumberFormatter.formatRawPrice(report.currentBtcPrice, decimals = 0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isGreek) "Ημέρα ${report.targetDayOffset} από το ATH (06 Οκτ 2025)" else "Day ${report.targetDayOffset} from ATH (06 Oct 2025)",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = com.example.util.AppNumberFormatter.formatPercent(report.currentDrawdownPercent, includeSign = true, decimals = 1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DrawdownRed
                )
            }
        }
    }
}

@Composable
private fun PastCycleComparisonRow(
    cycle: HistoricalCycleComparison,
    targetDay: Int,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cycle.cycleName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = com.example.util.AppNumberFormatter.formatRawPrice(cycle.priceOnDay, decimals = 0),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${cycle.dateOnDay} (Day $targetDay)",
                    fontSize = 10.5.sp,
                    color = palette.textMuted
                )
                val ddOnDayFormatted = com.example.util.AppNumberFormatter.formatPercent(cycle.drawdownPercentOnDay, includeSign = true, decimals = 1)
                Text(
                    text = "$ddOnDayFormatted vs ATH",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DrawdownRed
                )
            }

            Text(
                text = if (isGreek) cycle.statusSummaryEl else cycle.statusSummaryEn,
                fontSize = 11.sp,
                color = palette.textSecondary,
                lineHeight = 15.sp
            )

            // Bottom anchor details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.surfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isGreek) "Τελικός Πυθμένας Κύκλου:" else "Ultimate Cycle Bottom:",
                    fontSize = 10.sp,
                    color = palette.textMuted
                )
                val botPriceFormatted = com.example.util.AppNumberFormatter.formatRawPrice(cycle.bottomPriceUsd, decimals = 0)
                Text(
                    text = "$botPriceFormatted (${if (isGreek) "Ημέρα" else "Day"} ${cycle.bottomDayOffset})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GainGreen
                )
            }
        }
    }
}

@Composable
private fun HistoricalCrashEventCard(
    event: HistoricalCrashEvent,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceElevated)
            .border(
                1.dp,
                if (event.severity == "CRITICAL") DrawdownRed.copy(alpha = 0.5f) else palette.border,
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggleExpand() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DrawdownRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = event.dayOffsetRange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DrawdownRed
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.approxDate,
                            fontSize = 10.sp,
                            color = palette.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGreek) event.titleEl else event.titleEn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = palette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick drop stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BTC: ${event.btcDropSummary}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DrawdownRed
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // What happened
                    Text(
                        text = if (isGreek) "Τι συνέβη:" else "What Happened:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                    Text(
                        text = if (isGreek) event.whatHappenedEl else event.whatHappenedEn,
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        lineHeight = 15.sp
                    )

                    // Why it crashed
                    Text(
                        text = if (isGreek) "Γιατί κράσαρε η αγορά:" else "Why the Market Crashed:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DrawdownRed
                    )
                    Text(
                        text = if (isGreek) event.whyItCrashedEl else event.whyItCrashedEn,
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        lineHeight = 15.sp
                    )

                    // Altcoin impact
                    Text(
                        text = if (isGreek) "Επίπτωση στα Altcoins:" else "Altcoin Impact:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                    Text(
                        text = if (isGreek) event.altcoinImpactEl else event.altcoinImpactEn,
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        lineHeight = 15.sp
                    )

                    // Lesson
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GainGreen.copy(alpha = 0.1f))
                            .border(1.dp, GainGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isGreek) "Μάθημα Επιβίωσης:" else "Survival Lesson:",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GainGreen
                            )
                            Text(
                                text = if (isGreek) event.survivalLessonEl else event.survivalLessonEn,
                                fontSize = 10.5.sp,
                                color = palette.textPrimary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
