package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicVoidGlass
import com.example.ui.theme.GainGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.TachyonMintBright
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalAppStrings
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/**
 * Supported Timeframes for the Grok Zig-Zag Chart
 */
enum class ChartTimeframe(val label: String) {
    DAY_1("1D"),
    WEEK_1("1W"),
    MONTH_1("1M"),
    YEAR_1("1Y"),
    CYCLE("Cycle")
}

/**
 * Single Milestone/Data Point on the Zig-Zag Horizon
 */
data class ChartPoint(
    val normalizedX: Float, // 0.0f (Start of Range) to 0.65f (NOW) to 1.0f (Future Horizon)
    val price: Float,
    val isKeyPivot: Boolean = false,
    val isNowMarker: Boolean = false,
    val isFutureProjection: Boolean = false,
    val label: String? = null,
    val timeLabel: String = "",
    val confidenceUpper: Float? = null,
    val confidenceLower: Float? = null
)

/**
 * Data bundle representing past, future, and historical analog curves
 */
internal data class CyclePointsBundle(
    val pastPoints: List<ChartPoint>,
    val futurePoints: List<ChartPoint>,
    val analog2020Points: List<ChartPoint>,
    val analog2016Points: List<ChartPoint>,
    val isProjectedBullish: Boolean,
    val trajectorySummary: String,
    val projectedTargetPrice: Float,
    val projectedTargetLabel: String
)

/**
 * Comprehensive, High-Fidelity Grok Zig-Zag Chart:
 * - Dynamic intraday and multi-day sparkline rendering tailored to each coin
 * - Dynamic trend-aware projections (Bullish Green or Retracement/Support Coral)
 * - Individualized 2020 & 2016 cycle fractals per coin
 * - Interactive scrubber HUD with live price & target crosshairs
 */
@Composable
fun GrokZigZagChart(
    coin: CryptoCoin,
    selectedTimeframe: ChartTimeframe,
    onTimeframeSelected: (ChartTimeframe) -> Unit,
    showProjection: Boolean = false,
    onToggleProjection: () -> Unit = {},
    isLogScale: Boolean = false,
    onToggleLogScale: (() -> Unit)? = null,
    isProUnlocked: Boolean = false,
    onOpenProModal: () -> Unit = {},
    currency: Currency = Currency.USD,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var localLogScale by remember { mutableStateOf(isLogScale) }
    val effectiveLogScale = if (onToggleLogScale != null) isLogScale else localLogScale
    var touchXNormalized by remember { mutableStateOf<Float?>(null) }

    // Pulsing animation for the "LIVE / NOW" point
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animated Traveling Light Packet along the projected trajectory
    val travelingPacketOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "travelingPacketOffset"
    )

    // Animated Vertical Scan Particles along Halving guides
    val scanParticleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanParticleY"
    )

    // Pulsing alpha for future projected dashed vector stroke
    val projectedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "projectedPulseAlpha"
    )

    var dailyCloses by remember(coin.symbol) {
        mutableStateOf<List<com.example.data.repository.HistoricalMarketRepository.Candle>>(emptyList())
    }
    LaunchedEffect(coin.symbol, selectedTimeframe) {
        dailyCloses = com.example.data.repository.HistoricalMarketRepository.loadRecentDaily(
            coin.symbol,
            HorizonChartBuilder.daysFor(selectedTimeframe)
        ).orEmpty()
    }
    val cycleData = remember(
        coin.id, coin.priceUsd, coin.change24h, coin.sparkline, selectedTimeframe, dailyCloses
    ) {
        HorizonChartBuilder.bundle(coin, selectedTimeframe, dailyCloses)
    }

    val pastPoints = cycleData.pastPoints
    val futurePoints = cycleData.futurePoints
    val analog2020Points = cycleData.analog2020Points
    val analog2016Points = cycleData.analog2016Points
    val isProjectedBullish = cycleData.isProjectedBullish
    val trajectorySummary = cycleData.trajectorySummary
    val projectedTargetPrice = cycleData.projectedTargetPrice
    val projectedTargetLabel = cycleData.projectedTargetLabel

    val trendColor = if (isProjectedBullish) TachyonMint else SoftCrimson

    // Determine currently scrubbed point if user touches the canvas
    val allCombinedPoints = remember(pastPoints, futurePoints) { pastPoints + futurePoints }
    val selectedPoint = remember(touchXNormalized, allCombinedPoints) {
        touchXNormalized?.let { tx ->
            allCombinedPoints.minByOrNull { abs(it.normalizedX - tx) }
        }
    }

    // Pre-allocated Native Paints
    val axisTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 24f
            isAntiAlias = true
        }
    }
    val xTickPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val badgeBullishPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00FF88")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
    }
    val badgeBearishPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#F43F5E")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("grok_zigzag_chart")
    ) {
        // Top Header: Coin Name, Live Status & Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (coin.change24h >= 0) TachyonMint else SoftCrimson)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${coin.symbol} · ${coin.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(QuantumCyan.copy(alpha = 0.20f))
                                .border(0.8.dp, QuantumCyan.copy(alpha = 0.60f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI HORIZON",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuantumCyan
                            )
                        }
                    }
                    Text(
                        text = "Historical cycle path & live pivot zones",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Scale Mode Toggle (Linear / Log)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicVoidSurfaceElevated)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                    .clickable { 
                        if (onToggleLogScale != null) onToggleLogScale()
                        else localLogScale = !localLogScale
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (effectiveLogScale) "Log Scale" else "Linear Scale",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (effectiveLogScale) QuantumCyan else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe Selector Row (1D, 1W, 1M, 1Y, Cycle) - Holographic Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CosmicVoidSurface.copy(alpha = 0.90f))
                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val timeframes = listOf(
                ChartTimeframe.DAY_1 to strings.timeframe1D,
                ChartTimeframe.WEEK_1 to strings.timeframe1W,
                ChartTimeframe.MONTH_1 to strings.timeframe1M,
                ChartTimeframe.YEAR_1 to strings.timeframe1Y,
                ChartTimeframe.CYCLE to strings.timeframeCycle
            )

            timeframes.forEach { (tf, label) ->
                val isSelected = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                QuantumCyan.copy(alpha = 0.22f),
                                                MauveAurora.copy(alpha = 0.30f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.2.dp,
                                        Brush.horizontalGradient(
                                            listOf(
                                                QuantumCyan,
                                                MauveAurora
                                            )
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                            } else {
                                Modifier
                                    .background(CosmicVoidSurfaceElevated.copy(alpha = 0.6f))
                                    .border(1.dp, CosmicBorder.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            }
                        )
                        .clickable {
                            com.example.util.AppSoundManager.playTechClick()
                            onTimeframeSelected(tf)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSelected && tf == ChartTimeframe.CYCLE) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(QuantumCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) QuantumCyan else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cycle Horizon Stats Card (Halving position + Dynamic Forecast summary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CosmicVoidSurfaceElevated)
                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProjectedBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val halvingDays = com.example.util.HalvingCycleUtils.getDaysSince4thHalving()
                    val athDays = coin.calculatedAthDaysAgo
                    Text(
                        text = if (coin.symbol == "BTC") "Post-Halving Day $halvingDays" else "${coin.symbol} · Day $athDays after ATH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = trajectorySummary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isProjectedBullish) TachyonMint else SoftCrimson
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = if (!isProUnlocked) Modifier.clickable {
                    com.example.util.AppSoundManager.playTechClick()
                    onOpenProModal()
                } else Modifier
            ) {
                Text(
                    text = projectedTargetLabel,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (coin.priceUsd > 0.0) {
                        formatAxisPrice(projectedTargetPrice * currency.rateToUsd.toFloat(), currency.symbol)
                    } else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CosmicVoidBg)
                .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val leftAxisWidth = 55.dp.toPx()
                            val plotWidth = size.width - leftAxisWidth - 12.dp.toPx()
                            if (plotWidth > 0) {
                                val normX = ((offset.x - leftAxisWidth) / plotWidth).coerceIn(0f, 1f)
                                touchXNormalized = normX
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val leftAxisWidth = 55.dp.toPx()
                            val plotWidth = size.width - leftAxisWidth - 12.dp.toPx()
                            if (plotWidth > 0) {
                                val normX = ((offset.x - leftAxisWidth) / plotWidth).coerceIn(0f, 1f)
                                touchXNormalized = normX
                            }
                        },
                        onDrag = { change, _ ->
                            val leftAxisWidth = 55.dp.toPx()
                            val plotWidth = size.width - leftAxisWidth - 12.dp.toPx()
                            if (plotWidth > 0) {
                                val normX = ((change.position.x - leftAxisWidth) / plotWidth).coerceIn(0f, 1f)
                                touchXNormalized = normX
                            }
                        },
                        onDragEnd = {}
                    )
                }
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val leftAxisWidth = 55.dp.toPx()
                val bottomAxisHeight = 24.dp.toPx()
                val plotWidth = size.width - leftAxisWidth - 12.dp.toPx()
                val plotHeight = size.height - bottomAxisHeight

                if (plotWidth <= 0 || plotHeight <= 0) return@Canvas

                val allPrices = pastPoints.map { it.price }.filter { it > 0f }

                val minSeen = (allPrices.minOrNull() ?: 1f).coerceAtLeast(0.0000001f)
                val maxSeen = (allPrices.maxOrNull() ?: 10f).coerceAtLeast(minSeen)
                val pad = ((maxSeen - minSeen) * 0.04f).coerceAtLeast(minSeen * 0.01f)
                val minRawPrice = (minSeen - pad).coerceAtLeast(minSeen * 0.92f)
                val maxRawPrice = (maxSeen + pad).coerceAtLeast(minRawPrice * 1.01f)

                val logMin = log10(minRawPrice.toDouble()).toFloat()
                val logMax = log10(maxRawPrice.toDouble()).toFloat()

                fun getY(price: Float): Float {
                    val safeP = price.coerceAtLeast(minRawPrice)
                    val fraction = if (effectiveLogScale && logMax > logMin) {
                        (log10(safeP.toDouble()).toFloat() - logMin) / (logMax - logMin)
                    } else {
                        (safeP - minRawPrice) / (maxRawPrice - minRawPrice)
                    }
                    return plotHeight - (fraction.coerceIn(0f, 1f) * (plotHeight - 24.dp.toPx()) + 12.dp.toPx())
                }

                fun getX(normalizedX: Float): Float {
                    return leftAxisWidth + (normalizedX.coerceIn(0f, 1f) * plotWidth)
                }

                val nowPoint = pastPoints.lastOrNull() ?: ChartPoint(0.65f, coin.priceUsd.toFloat(), isNowMarker = true)
                val nowX = getX(nowPoint.normalizedX)

                drawRect(
                    color = QuantumCyan.copy(alpha = 0.04f),
                    topLeft = Offset(leftAxisWidth, 0f),
                    size = Size(plotWidth, plotHeight)
                )

                // 2. Y-Axis Grid Lines & Price Labels
                val yStepCount = 4
                for (i in 0..yStepCount) {
                    val normY = i / yStepCount.toFloat()
                    val y = plotHeight * (1f - normY)
                    drawLine(
                        color = CosmicBorder.copy(alpha = 0.35f),
                        start = Offset(leftAxisWidth, y),
                        end = Offset(leftAxisWidth + plotWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    val tickPrice = if (effectiveLogScale) {
                        val logVal = logMin + normY * (logMax - logMin)
                        Math.pow(10.0, logVal.toDouble()).toFloat()
                    } else {
                        minRawPrice + normY * (maxRawPrice - minRawPrice)
                    }

                    val priceLabel = formatAxisPrice(tickPrice * currency.rateToUsd.toFloat(), currency.symbol)
                    drawContext.canvas.nativeCanvas.drawText(priceLabel, 0f, y + 8f, axisTextPaint)
                }

                val xTicks = realizedAxisTicks(pastPoints)

                xTicks.forEach { (normX, label) ->
                    val x = getX(normX)
                    val isNow = label.contains("NOW") || label.contains("LIVE") || label.contains("TODAY")
                    val isHalving = label.contains("Halving")
                    val lineColor = when {
                        isNow -> QuantumCyan.copy(alpha = 0.6f)
                        isHalving -> PhotonGold.copy(alpha = 0.45f)
                        else -> CosmicBorder.copy(alpha = 0.35f)
                    }
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, plotHeight),
                        strokeWidth = if (isNow) 1.5f else 1f,
                        pathEffect = if (isNow || isHalving) PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f) else PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(label, x, plotHeight + 24f, xTickPaint)
                }

                // Halving Vertical Guides (Ultra-fine translucent gold laser lines with animated vertical scan particles)
                if (selectedTimeframe == ChartTimeframe.CYCLE) {
                    val halvingX = getX(0.32f)
                    val goldLaser = PhotonGold
                    val goldAmber = NeonAmber

                    // Ultra-fine translucent gold laser line
                    drawLine(
                        color = goldLaser.copy(alpha = 0.50f),
                        start = Offset(halvingX, 0f),
                        end = Offset(halvingX, plotHeight),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )

                    // Animated vertical scan particle 1
                    val particleY1 = scanParticleY * plotHeight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(goldAmber.copy(alpha = 0.65f), Color.Transparent),
                            center = Offset(halvingX, particleY1),
                            radius = 8.dp.toPx()
                        ),
                        radius = 8.dp.toPx(),
                        center = Offset(halvingX, particleY1)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(halvingX, particleY1)
                    )

                    // Animated vertical scan particle 2 (phase offset)
                    val particleY2 = ((scanParticleY + 0.5f) % 1.0f) * plotHeight
                    drawCircle(
                        color = goldLaser.copy(alpha = 0.70f),
                        radius = 1.8.dp.toPx(),
                        center = Offset(halvingX, particleY2)
                    )
                }

                // 4. Draw Confidence Corridor for Future Projection (Soft Gradient Area)
                if (futurePoints.isNotEmpty()) {
                    val corridorPath = Path()
                    val upperPoints = futurePoints.mapNotNull { pt -> pt.confidenceUpper?.let { pt.normalizedX to it } }
                    val lowerPoints = futurePoints.mapNotNull { pt -> pt.confidenceLower?.let { pt.normalizedX to it } }

                    if (upperPoints.isNotEmpty() && lowerPoints.isNotEmpty()) {
                        // Move across upper points
                        upperPoints.forEachIndexed { i, (nx, p) ->
                            val x = getX(nx)
                            val y = getY(p)
                            if (i == 0) corridorPath.moveTo(x, y) else corridorPath.lineTo(x, y)
                        }
                        // Move backwards across lower points
                        lowerPoints.reversed().forEach { (nx, p) ->
                            val x = getX(nx)
                            val y = getY(p)
                            corridorPath.lineTo(x, y)
                        }
                        corridorPath.close()

                        val corridorColors = if (isProjectedBullish) {
                            listOf(TachyonMint.copy(alpha = 0.16f), TachyonMint.copy(alpha = 0.05f))
                        } else {
                            listOf(SoftCrimson.copy(alpha = 0.16f), SoftCrimson.copy(alpha = 0.05f))
                        }

                        drawPath(
                            path = corridorPath,
                            brush = Brush.verticalGradient(corridorColors)
                        )
                    }
                }

                // 5. Draw 2016 Analog Comparison Curve (Neon Purple)
                if (analog2016Points.isNotEmpty()) {
                    val p2016 = Path()
                    analog2016Points.forEachIndexed { i, pt ->
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)
                        if (i == 0) p2016.moveTo(x, y) else p2016.lineTo(x, y)
                    }
                    drawPath(
                        path = p2016,
                        color = MauveAurora.copy(alpha = 0.65f),
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )
                }

                // 6. Draw 2020 Analog Comparison Curve (Neon Amber)
                if (analog2020Points.isNotEmpty()) {
                    val p2020 = Path()
                    analog2020Points.forEachIndexed { i, pt ->
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)
                        if (i == 0) p2020.moveTo(x, y) else p2020.lineTo(x, y)
                    }
                    drawPath(
                        path = p2020,
                        color = PhotonGold.copy(alpha = 0.75f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )
                }

                // 7. Draw Historical Realized Price Curve with 3D Topographic Contour Layers
                if (pastPoints.isNotEmpty()) {
                    val pastPath = Path()
                    val fillPath = Path()
                    val contourLayer1 = Path()
                    val contourLayer2 = Path()

                    pastPoints.forEachIndexed { i, pt ->
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)
                        if (i == 0) {
                            pastPath.moveTo(x, y)
                            fillPath.moveTo(x, plotHeight)
                            fillPath.lineTo(x, y)

                            contourLayer1.moveTo(x, plotHeight)
                            val yMid = y + (plotHeight - y) * 0.45f
                            contourLayer1.lineTo(x, yMid)

                            contourLayer2.moveTo(x, plotHeight)
                            val yHigh = y + (plotHeight - y) * 0.18f
                            contourLayer2.lineTo(x, yHigh)
                        } else {
                            pastPath.lineTo(x, y)
                            fillPath.lineTo(x, y)

                            val yMid = y + (plotHeight - y) * 0.45f
                            contourLayer1.lineTo(x, yMid)

                            val yHigh = y + (plotHeight - y) * 0.18f
                            contourLayer2.lineTo(x, yHigh)
                        }
                    }

                    fillPath.lineTo(nowX, plotHeight)
                    fillPath.close()

                    contourLayer1.lineTo(nowX, plotHeight)
                    contourLayer1.close()

                    contourLayer2.lineTo(nowX, plotHeight)
                    contourLayer2.close()

                    // Tier 1: Deep Basal Ambient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                QuantumCyan.copy(alpha = 0.22f),
                                CosmicVoidBg.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = plotHeight
                        )
                    )

                    // Tier 2: Mid-Elevation Topographic Contour Step
                    drawPath(
                        path = contourLayer1,
                        brush = Brush.verticalGradient(
                            listOf(
                                QuantumCyan.copy(alpha = 0.14f),
                                CosmicVoidBg.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Tier 3: Near-Ridge Topographic Contour Step
                    drawPath(
                        path = contourLayer2,
                        brush = Brush.verticalGradient(
                            listOf(
                                MauveAurora.copy(alpha = 0.16f),
                                CosmicVoidBg.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Topographic Iso-Contour Hairline Traces
                    val isoTrace1 = Path()
                    val isoTrace2 = Path()
                    pastPoints.forEachIndexed { i, pt ->
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)
                        val y1 = y + (plotHeight - y) * 0.45f
                        val y2 = y + (plotHeight - y) * 0.18f
                        if (i == 0) {
                            isoTrace1.moveTo(x, y1)
                            isoTrace2.moveTo(x, y2)
                        } else {
                            isoTrace1.lineTo(x, y1)
                            isoTrace2.lineTo(x, y2)
                        }
                    }
                    drawPath(
                        path = isoTrace1,
                        color = QuantumCyan.copy(alpha = 0.20f),
                        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f))
                    )
                    drawPath(
                        path = isoTrace2,
                        color = MauveAurora.copy(alpha = 0.35f),
                        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                    )

                    // Multi-tier Quantum Cyan Laser Bloom for Realized Curve
                    drawPath(
                        path = pastPath,
                        color = QuantumCyan.copy(alpha = 0.22f),
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = pastPath,
                        color = QuantumCyan.copy(alpha = 0.65f),
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Realized Core Laser Stroke
                    drawPath(
                        path = pastPath,
                        color = QuantumCyan,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Crest Laser Filament Highlight (Ice White)
                    drawPath(
                        path = pastPath,
                        color = QuantumCyanBright.copy(alpha = 0.95f),
                        style = Stroke(width = 0.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 8. Draw Future Projected Curve (Animated Pulsing Dashed Vector Line in Neon Emerald with Traveling Light Packet)
                if (futurePoints.isNotEmpty()) {
                    val futurePath = Path()
                    futurePoints.forEachIndexed { i, pt ->
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)
                        if (i == 0) futurePath.moveTo(x, y) else futurePath.lineTo(x, y)
                    }

                    val emeraldColor = TachyonMint
                    val emeraldBright = TachyonMintBright

                    // Diffuse Neon Halo (Pulsing)
                    drawPath(
                        path = futurePath,
                        color = emeraldColor.copy(alpha = 0.22f * projectedPulseAlpha),
                        style = Stroke(
                            width = 8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )
                    // Mid Glow outer layer
                    drawPath(
                        path = futurePath,
                        color = emeraldBright.copy(alpha = 0.65f * projectedPulseAlpha),
                        style = Stroke(
                            width = 4.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )
                    // Core vector dashed line
                    drawPath(
                        path = futurePath,
                        color = emeraldColor,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )
                    // Core highlight filament
                    drawPath(
                        path = futurePath,
                        color = TachyonMintBright.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 0.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )

                    // Traveling Light Packet along the projected trajectory
                    if (futurePoints.size >= 2) {
                        val segmentLengths = mutableListOf<Float>()
                        var totalPathLength = 0f
                        for (i in 0 until futurePoints.size - 1) {
                            val p1x = getX(futurePoints[i].normalizedX)
                            val p1y = getY(futurePoints[i].price)
                            val p2x = getX(futurePoints[i + 1].normalizedX)
                            val p2y = getY(futurePoints[i + 1].price)
                            val dist = kotlin.math.hypot(p2x - p1x, p2y - p1y)
                            segmentLengths.add(dist)
                            totalPathLength += dist
                        }

                        if (totalPathLength > 0f) {
                            val targetDistance = travelingPacketOffset * totalPathLength
                            var accumulated = 0f
                            var packetX = getX(futurePoints[0].normalizedX)
                            var packetY = getY(futurePoints[0].price)

                            for (i in 0 until futurePoints.size - 1) {
                                val segLen = segmentLengths[i]
                                if (accumulated + segLen >= targetDistance || i == futurePoints.size - 2) {
                                    val segFraction = if (segLen > 0f) (targetDistance - accumulated) / segLen else 0f
                                    val clampedFrac = segFraction.coerceIn(0f, 1f)
                                    val p1x = getX(futurePoints[i].normalizedX)
                                    val p1y = getY(futurePoints[i].price)
                                    val p2x = getX(futurePoints[i + 1].normalizedX)
                                    val p2y = getY(futurePoints[i + 1].price)
                                    packetX = p1x + clampedFrac * (p2x - p1x)
                                    packetY = p1y + clampedFrac * (p2y - p1y)
                                    break
                                }
                                accumulated += segLen
                            }

                            // Outer radial flare
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(emeraldBright.copy(alpha = 0.45f), Color.Transparent),
                                    center = Offset(packetX, packetY),
                                    radius = 12.dp.toPx()
                                ),
                                radius = 12.dp.toPx(),
                                center = Offset(packetX, packetY)
                            )
                            // Mid energetic core
                            drawCircle(
                                color = emeraldBright,
                                radius = 4.5.dp.toPx(),
                                center = Offset(packetX, packetY)
                            )
                            // Center white supernova photon
                            drawCircle(
                                color = Color.White,
                                radius = 2.2.dp.toPx(),
                                center = Offset(packetX, packetY)
                            )
                        }
                    }
                }

                // 9. Draw "NOW / LIVE" Separator Pillar & Animated Pulse Circle
                val nowY = getY(nowPoint.price)

                // Vertical glowing dividing line for "NOW"
                drawLine(
                    color = QuantumCyan,
                    start = Offset(nowX, 0f),
                    end = Offset(nowX, plotHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                // Pulsing wave circle at Live Point
                drawCircle(
                    color = QuantumCyan.copy(alpha = pulseAlpha),
                    radius = pulseRadius.dp.toPx(),
                    center = Offset(nowX, nowY)
                )
                drawCircle(
                    color = CosmicVoidBg,
                    radius = 6.dp.toPx(),
                    center = Offset(nowX, nowY)
                )
                drawCircle(
                    color = QuantumCyan,
                    radius = 4.dp.toPx(),
                    center = Offset(nowX, nowY)
                )

                // 10. Milestone Pins (Future Targets & Apex)
                futurePoints.forEach { pt ->
                    if (pt.isKeyPivot && pt.label != null) {
                        val x = getX(pt.normalizedX)
                        val y = getY(pt.price)

                        drawCircle(
                            color = trendColor.copy(alpha = 0.3f),
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = trendColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )

                        // Milestone Label
                        val textY = if (y < plotHeight * 0.4f) y + 26f else y - 16f
                        val paintToUse = if (isProjectedBullish) badgeBullishPaint else badgeBearishPaint
                        drawContext.canvas.nativeCanvas.drawText(pt.label, x, textY, paintToUse)
                    }
                }

                // 11. Interactive Touch Scrubber Crosshair & Floating HUD
                touchXNormalized?.let { tx ->
                    val scrubX = getX(tx)
                    selectedPoint?.let { sp ->
                        val scrubY = getY(sp.price)

                        // Vertical inspection guideline
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(scrubX, 0f),
                            end = Offset(scrubX, plotHeight),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Target circle at intersection
                        drawCircle(
                            color = if (sp.isFutureProjection) trendColor else QuantumCyan,
                            radius = 5.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                    }
                }
            }

            // Scrubbed Point Floating HUD Card overlay
            selectedPoint?.let { sp ->
                val isFuture = sp.isFutureProjection
                val priceFormatted = formatAxisPrice(sp.price * currency.rateToUsd.toFloat(), currency.symbol)
                val deltaPct = ((sp.price - coin.priceUsd) / coin.priceUsd) * 100.0

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicVoidSurface.copy(alpha = 0.92f))
                        .border(1.dp, if (isFuture) trendColor else QuantumCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isFuture) "Projected: ${sp.timeLabel}" else "Realized: ${sp.timeLabel}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFuture) trendColor else QuantumCyan
                            )
                            Text(
                                text = priceFormatted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (abs(deltaPct) >= 0.01) {
                            Text(
                                text = "${if (deltaPct >= 0) "+" else ""}${String.format("%.1f", deltaPct)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deltaPct >= 0) TachyonMint else SoftCrimson
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-Curve Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CosmicVoidSurfaceElevated)
                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Realized Live
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(QuantumCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Realized Price (Live)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Explanatory Note & Touch Tip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "These closes already happened. They are not a forecast.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

private fun formatAxisPrice(value: Float, symbol: String): String {
    return when {
        value >= 1_000_000f -> "${symbol}${String.format(java.util.Locale.US, "%.2f", value / 1_000_000f)}M"
        value >= 1_000f -> "${symbol}${String.format(java.util.Locale.US, "%.1f", value / 1_000f)}k"
        value >= 1f -> "${symbol}${String.format(java.util.Locale.US, "%.2f", value)}"
        value >= 0.01f -> "${symbol}${String.format(java.util.Locale.US, "%.3f", value)}"
        else -> "${symbol}${String.format(java.util.Locale.US, "%.5f", value)}"
    }
}

private fun realizedAxisTicks(points: List<ChartPoint>): List<Pair<Float, String>> {
    if (points.isEmpty()) return listOf(1f to "Now")
    val first = points.first()
    val mid = points[points.size / 2]
    val last = points.last()
    return listOf(
        first.normalizedX to first.timeLabel.ifBlank { "Start" },
        mid.normalizedX to mid.timeLabel.ifBlank { "Mid" },
        last.normalizedX to "Now"
    ).distinctBy { it.first }
}
