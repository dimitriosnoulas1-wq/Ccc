package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumBlue
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CycleFractalData
import com.example.util.CycleFractalEngine
import com.example.util.FractalPoint
import com.example.util.LocalAppStrings
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class CycleChartMode {
    CURRENT,
    ALL,
    PROJECTION,
    CYCLE_2020,
    CYCLE_2016
}

@Composable
fun HistoricalCycleChart(
    modifier: Modifier = Modifier,
    coinSymbol: String = "BTC",
    currentPrice: Double? = null,
    coinName: String = "Bitcoin",
    coinId: String = "",
    isProUnlocked: Boolean = false
) {
    val strings = LocalAppStrings.current
    val isGreek = strings.language.code == "el"

    var reloadNonce by remember(coinId, coinSymbol) { mutableStateOf(0) }
    var historyModel by remember(coinId, coinSymbol) { mutableStateOf<CycleFractalData?>(null) }
    var historyState by remember(coinId, coinSymbol) { mutableStateOf("loading") }
    LaunchedEffect(coinId, coinSymbol, reloadNonce) {
        historyState = "loading"
        val loaded = com.example.data.repository.HistoricalMarketRepository.load(coinId, coinSymbol, "HALVING")
        historyModel = loaded
        historyState = if (loaded == null) "error" else "ready"
    }
    val chartData = historyModel
    var selectedMode by remember { mutableStateOf(if (isProUnlocked) CycleChartMode.ALL else CycleChartMode.CURRENT) }
    androidx.compose.runtime.LaunchedEffect(isProUnlocked) {
        if (!isProUnlocked) selectedMode = CycleChartMode.CURRENT
    }
    var touchXNormalized by remember { mutableStateOf<Float?>(null) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 4.5f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
    if (chartData == null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CosmicVoidSurface.copy(alpha = 0.85f))
                .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("historical_cycle_chart_container")
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (historyState == "error") {
                    if (isGreek) "Το ιστορικό δεν είναι διαθέσιμο αυτή τη στιγμή. Δοκίμασε ξανά." else "History is unavailable right now. Try again."
                } else {
                    if (isGreek) "Φόρτωση κύκλων από halving σε halving…" else "Loading halving-to-halving cycles…"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
            if (historyState == "error") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isGreek) "Ανανέωση" else "Retry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan,
                    modifier = Modifier.clickable {
                        historyState = "loading"
                        historyModel = null
                        reloadNonce += 1
                    }
                )
            }
        }
        return
    }
    val fractalData = chartData
    val axisMax = fractalData.axisDays.coerceAtLeast(1)
    val hasProjection = fractalData.projectedPoints.isNotEmpty()

    // Calculate current inspected day and price based on touch or default current point
    val currentDayFraction = (fractalData.currentDay / axisMax.toFloat()).coerceIn(0f, 1f)
    val activeFraction = touchXNormalized ?: currentDayFraction
    val inspectedDay = (activeFraction * axisMax.toFloat()).roundToInt()

    val currentPoint = fractalData.currentPoints.minByOrNull { kotlin.math.abs(it.day - inspectedDay) }
    val pastPoint = fractalData.points2020.minByOrNull { kotlin.math.abs(it.day - inspectedDay) }
    val earlierPoint = fractalData.points2016.minByOrNull { kotlin.math.abs(it.day - inspectedDay) }
    val readingPastCycle = inspectedDay > fractalData.currentDay
    val inspectedPrice = when {
        !readingPastCycle && currentPoint != null -> currentPoint.price
        selectedMode == CycleChartMode.CYCLE_2016 && earlierPoint != null -> earlierPoint.price
        pastPoint != null && readingPastCycle -> pastPoint.price
        earlierPoint != null && readingPastCycle -> earlierPoint.price
        currentPoint != null -> currentPoint.price
        else -> currentPrice ?: 0.0
    }
    val priceCaption = when {
        readingPastCycle && selectedMode == CycleChartMode.CYCLE_2016 ->
            if (isGreek) "Κλείσιμο κύκλου 2016" else "2016 cycle close"
        readingPastCycle ->
            if (isGreek) "Κλείσιμο κύκλου 2020" else "2020 cycle close"
        else ->
            if (isGreek) "Κλείσιμο αυτού του κύκλου" else "This cycle close"
    }
    val phaseDescription = if (!readingPastCycle) {
        currentPoint?.phaseTag ?: fractalData.windowLabel
    } else {
        when (selectedMode) {
            CycleChartMode.CYCLE_2016 -> earlierPoint?.phaseTag
            else -> pastPoint?.phaseTag ?: earlierPoint?.phaseTag
        } ?: fractalData.windowLabel
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CosmicVoidSurface.copy(alpha = 0.85f))
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("historical_cycle_chart_container")
    ) {
        // 1. Header: Title, Correlation Score Badge & Matching Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = QuantumCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.historicalAnalogHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = QuantumCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(QuantumCyan.copy(alpha = 0.18f))
                            .border(0.5.dp, QuantumCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "4Y",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = QuantumCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.historicalMatchLabel,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val windowChange = fractalData.correlationScore2020
                val changeColor = if (windowChange < 0.0) SoftCrimson else TachyonMint
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(changeColor.copy(alpha = 0.15f))
                        .border(0.5.dp, changeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(changeColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val formattedMatch = java.lang.String.format(java.util.Locale.US, "%+.2f%%", windowChange)
                        Text(
                            text = formattedMatch,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = fractalData.windowLabel.ifBlank { strings.cycleFractalLabel },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModeFilterChip(
                label = strings.cycleChartModeCurrent,
                selected = selectedMode == CycleChartMode.CURRENT,
                accentColor = QuantumCyan,
                modifier = Modifier.weight(1f),
                onClick = { selectedMode = CycleChartMode.CURRENT }
            )
            if (isProUnlocked) {
            ModeFilterChip(
                label = strings.cycleChartModeAll,
                selected = selectedMode == CycleChartMode.ALL,
                accentColor = MauveAurora,
                modifier = Modifier.weight(0.8f),
                onClick = { selectedMode = CycleChartMode.ALL }
            )
            }
            if (hasProjection) {
                ModeFilterChip(
                    label = strings.cycleChartModeProjection,
                    selected = selectedMode == CycleChartMode.PROJECTION,
                    accentColor = QuantumBlue,
                    modifier = Modifier.weight(1.1f),
                    onClick = { selectedMode = CycleChartMode.PROJECTION }
                )
            }
            if (isProUnlocked && fractalData.points2020.isNotEmpty()) {
            ModeFilterChip(
                label = fractalData.pastCycleLabel,
                selected = selectedMode == CycleChartMode.CYCLE_2020,
                accentColor = MauveAurora,
                modifier = Modifier.weight(0.9f),
                onClick = { selectedMode = CycleChartMode.CYCLE_2020 }
            )
            }
            if (isProUnlocked && fractalData.points2016.isNotEmpty()) {
                ModeFilterChip(
                    label = fractalData.earlierCycleLabel,
                    selected = selectedMode == CycleChartMode.CYCLE_2016,
                    accentColor = PhotonGold,
                    modifier = Modifier.weight(0.9f),
                    onClick = { selectedMode = CycleChartMode.CYCLE_2016 }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Interactive Floating Inspection HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CosmicVoidSurfaceElevated)
                .border(0.5.dp, if (touchXNormalized != null) QuantumCyan.copy(alpha = 0.6f) else CosmicBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${strings.cycleChartDayLabel} $inspectedDay / $axisMax",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (touchXNormalized != null) QuantumCyan else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (inspectedDay == fractalData.currentDay) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(QuantumCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isGreek) "ΤΡΕΧΟΥΣΑ" else "CURRENT",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = QuantumCyan
                                )
                            }
                        }
                    }
                    Text(
                        text = phaseDescription,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = com.example.util.AppNumberFormatter.formatPrice(inspectedPrice, com.example.data.model.Currency.USD),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TachyonMint
                    )
                    Text(
                        text = priceCaption,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Advanced High-Resolution Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicVoidBg)
                .border(0.5.dp, CosmicBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val norm = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            touchXNormalized = norm
                            tryAwaitRelease()
                            touchXNormalized = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchXNormalized = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            touchXNormalized = null
                        },
                        onDragCancel = {
                            touchXNormalized = null
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            touchXNormalized = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padY = 22f
                val padX = 14f
                val chartW = w - (padX * 2)
                val chartH = h - (padY * 2)

                // Coordinate mapping lambda
                fun mapPoint(day: Int, normVal: Float): Offset {
                    val clampedDay = day.coerceIn(0, axisMax)
                    val x = padX + chartW * (clampedDay.toFloat() / axisMax.toFloat())
                    val y = padY + chartH * (1f - normVal.coerceIn(0.01f, 1.05f))
                    return Offset(x, y)
                }

                // 1. Horizontal Reference Grid Lines & Y-Axis Labels
                val gridLevels = listOf(0.2f, 0.45f, 0.75f, 1.0f)
                gridLevels.forEach { lvl ->
                    val y = padY + chartH * (1f - lvl)
                    drawLine(
                        color = CosmicBorder.copy(alpha = 0.45f),
                        start = Offset(padX, y),
                        end = Offset(w - padX, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                    )
                }

                // Real events in this window: ATH, ATL, listing, and Bitcoin halvings only.
                val verticalGuides = fractalData.eventDays.map { it.first }
                verticalGuides.forEach { dayGuide ->
                    val gx = padX + chartW * (dayGuide.toFloat() / axisMax.toFloat())
                    drawLine(
                        color = if (dayGuide == 210) QuantumCyan.copy(alpha = 0.35f) else CosmicBorder.copy(alpha = 0.35f),
                        start = Offset(gx, padY),
                        end = Offset(gx, h - padY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                    )
                }

                // 3. Projected Confidence Corridor (Shaded Confidence Area)
                if (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.PROJECTION) {
                    val upperBandOffsets = fractalData.projectedBandUpper.map { mapPoint(it.day, it.normalizedValue) }
                    val lowerBandOffsets = fractalData.projectedBandLower.map { mapPoint(it.day, it.normalizedValue) }

                    if (upperBandOffsets.size > 1 && lowerBandOffsets.size > 1) {
                        val corridorPath = Path()
                        CycleFractalEngine.buildSmoothSplinePath(upperBandOffsets, corridorPath)
                        // Trace back along lower band
                        for (i in lowerBandOffsets.indices.reversed()) {
                            val pt = lowerBandOffsets[i]
                            corridorPath.lineTo(pt.x, pt.y)
                        }
                        corridorPath.close()

                        drawPath(
                            path = corridorPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    QuantumCyan.copy(alpha = 0.16f),
                                    QuantumCyan.copy(alpha = 0.03f)
                                ),
                                startY = padY,
                                endY = h - padY
                            ),
                            style = Fill
                        )
                    }
                }

                // 4. Historical 2016-2020 Cycle (Amber Dashed Spline)
                if (isProUnlocked && (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.CYCLE_2016)) {
                    val offsets2016 = fractalData.points2016.map { mapPoint(it.day, it.normalizedValue) }
                    if (offsets2016.isNotEmpty()) {
                        val path2016 = Path()
                        CycleFractalEngine.buildSmoothSplinePath(offsets2016, path2016)
                        drawPath(
                            path = path2016,
                            color = PhotonGold.copy(alpha = 0.75f),
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f), 0f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // 5. Historical 2020-2024 Cycle (Purple Solid Spline)
                if (isProUnlocked && (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.CYCLE_2020)) {
                    val offsets2020 = fractalData.points2020.map { mapPoint(it.day, it.normalizedValue) }
                    if (offsets2020.isNotEmpty()) {
                        val path2020 = Path()
                        CycleFractalEngine.buildSmoothSplinePath(offsets2020, path2020)
                        drawPath(
                            path = path2020,
                            color = MauveAurora.copy(alpha = 0.85f),
                            style = Stroke(
                                width = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // 6. Projected Continuation Fractal Path (Dashed Glowing Cyan)
                if (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.PROJECTION) {
                    val projectedOffsets = fractalData.projectedPoints.map { mapPoint(it.day, it.normalizedValue) }
                    if (projectedOffsets.isNotEmpty()) {
                        val projPath = Path()
                        CycleFractalEngine.buildSmoothSplinePath(projectedOffsets, projPath)

                        // Underglow for projected path
                        drawPath(
                            path = projPath,
                            color = QuantumBlue.copy(alpha = 0.35f),
                            style = Stroke(
                                width = 5.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        )
                        // Crisp dashed core
                        drawPath(
                            path = projPath,
                            color = QuantumBlue,
                            style = Stroke(
                                width = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
                }

                // 7. Current Cycle Curve (Radiant Neon Cyan Solid Spline with Area Gradient Fill)
                val currentOffsets = fractalData.currentPoints.map { mapPoint(it.day, it.normalizedValue) }
                if (currentOffsets.isNotEmpty()) {
                    // Shaded gradient under active curve
                    val areaPath = Path()
                    CycleFractalEngine.buildSmoothSplinePath(currentOffsets, areaPath)
                    val lastPt = currentOffsets.last()
                    val firstPt = currentOffsets.first()
                    areaPath.lineTo(lastPt.x, h - padY)
                    areaPath.lineTo(firstPt.x, h - padY)
                    areaPath.close()

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                QuantumCyan.copy(alpha = 0.32f),
                                QuantumCyan.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = padY,
                            endY = h - padY
                        ),
                        style = Fill
                    )

                    // Luminous underglow
                    val curvePath = Path()
                    CycleFractalEngine.buildSmoothSplinePath(currentOffsets, curvePath)
                    drawPath(
                        path = curvePath,
                        color = QuantumCyan.copy(alpha = 0.40f),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Sharp core line
                    drawPath(
                        path = curvePath,
                        color = QuantumCyan,
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Current Point Beacon (Pulsing halo and white center)
                    drawCircle(
                        color = QuantumCyan.copy(alpha = 0.35f),
                        radius = pulseGlow.dp.toPx(),
                        center = lastPt
                    )
                    drawCircle(
                        color = QuantumCyan,
                        radius = 5.dp.toPx(),
                        center = lastPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = lastPt
                    )
                }

                // 8. Interactive Touch Crosshair Laser & Indicator Dots
                touchXNormalized?.let { txNorm ->
                    val scrubX = padX + chartW * txNorm
                    // Draw vertical laser line
                    drawLine(
                        color = QuantumCyan.copy(alpha = 0.85f),
                        start = Offset(scrubX, padY),
                        end = Offset(scrubX, h - padY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Draw intersection beacon dot on active trajectory
                    val allPoints = fractalData.currentPoints + fractalData.projectedPoints
                    val closest = allPoints.minByOrNull { kotlin.math.abs(it.day - (txNorm * axisMax)) }
                    if (closest != null) {
                        val intPt = mapPoint(closest.day, closest.normalizedValue)
                        drawCircle(
                            color = QuantumCyan.copy(alpha = 0.5f),
                            radius = 7.dp.toPx(),
                            center = Offset(scrubX, intPt.y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = Offset(scrubX, intPt.y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Timeline Milestones Footer Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val eventTags = fractalData.eventDays.take(4)
            if (eventTags.isEmpty()) {
                TimelineTag(day = "D0", label = fractalData.windowLabel, color = TextMuted)
            } else {
                eventTags.forEach { (day, label) ->
                    val color = when {
                        label.contains("halving") && fractalData.usesHalving -> PhotonGold
                        label == "ATH" || label == "High" -> TachyonMint
                        label == "ATL" || label == "Low" -> SoftCrimson
                        else -> TextMuted
                    }
                    TimelineTag(day = "D$day", label = label, color = color)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = QuantumCyan, label = strings.cycleChartCurrentPoint)
            if (hasProjection && (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.PROJECTION)) {
                LegendItem(color = QuantumBlue, label = strings.cycleChartProjectedPath, isDashed = true)
            }
            if (isProUnlocked && fractalData.points2020.isNotEmpty() &&
                (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.CYCLE_2020)
            ) {
                LegendItem(color = MauveAurora, label = fractalData.pastCycleLabel)
            }
            if (isProUnlocked && fractalData.points2016.isNotEmpty() &&
                (selectedMode == CycleChartMode.ALL || selectedMode == CycleChartMode.CYCLE_2016)
            ) {
                LegendItem(color = PhotonGold, label = fractalData.earlierCycleLabel, isDashed = true)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7. Interactive touch tip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = strings.cycleChartTapToInspect,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isGreek) {
                "Κυανό: αυτός ο κύκλος, από το halving του 2024 μέχρι σήμερα. Μωβ: κύκλος 2020. Χρυσό: κύκλος 2016. Ίδιες ημέρες μετά το halving. Είναι ό,τι έγινε ήδη, όχι πρόβλεψη."
            } else {
                "Cyan is this cycle, from the 2024 halving until today. Purple is 2020 and gold is 2016, on the same day-count. Those lines already happened. They are not a forecast."
            },
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = com.example.util.CycleReadingText.disclaimer(isGreek),
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ModeFilterChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accentColor.copy(alpha = 0.22f) else CosmicVoidSurfaceElevated)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) accentColor else CosmicBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                com.example.util.AppSoundManager.playTechClick()
                onClick()
            }
            .padding(vertical = 5.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accentColor else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TimelineTag(
    day: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 8.5.sp,
            color = TextMuted
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 12.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = TextSecondary
        )
    }
}

