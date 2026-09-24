package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.util.LocalAppStrings
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Bitcoin Rainbow Price Model Chart
 * Matches the reference implementation:
 * - White chart background, black price line
 * - Logarithmic Y axis ($3 to $4,000,000), X axis 2013–2031 (bounds 2012.5 to 2033.0)
 * - Rainbow bands plugin with 7 classic bands (Top -> Bottom)
 * - Halving markers with solid lines and dashed lines for future estimated halvings
 * - Uses existing live BTC price feed (~$77k+) as the latest data point
 * - Responsive, touch-friendly scrubber on Android
 */
@Composable
fun BitcoinRainbowChart(
    btcPriceUsd: Double,
    currency: Currency,
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier,
    btcSparkline: List<Double> = emptyList(),
    cycleMarkers: List<RainbowModelEngine.CycleMarker> = emptyList()
) {
    val strings = LocalAppStrings.current
    val isGreek = strings.language.code == "el"

    val hasLiveBtcPrice = btcPriceUsd > 0.0
    val effectiveBtcPrice = if (hasLiveBtcPrice) btcPriceUsd else 0.0

    // Build the monthly price data combined with the live price as the latest point
    val priceSeries = remember(effectiveBtcPrice) {
        RainbowModelEngine.buildPriceSeries(effectiveBtcPrice)
    }

    // Current band assessment based on latest data point
    val currentBandInfo = remember(priceSeries) {
        RainbowModelEngine.getBandForPoint(priceSeries.last())
    }

    var scrubbedPoint by remember { mutableStateOf<RainbowModelEngine.PricePoint?>(null) }
    var touchXPosition by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bitcoin_rainbow_chart_container")
    ) {
        // Main White Card containing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header inside the card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGreek) "Μοντέλο Τιμής Bitcoin Rainbow" else "Bitcoin Rainbow Price Model",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14161A)
                    )
                    Text(
                        text = if (hasLiveBtcPrice) "LOG SCALE" else "WAITING FOR LIVE BTC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chart Canvas Box (White background, touch-interactive)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .background(Color.White)
                        .pointerInput(priceSeries) {
                            detectTapGestures(
                                onTap = { offset ->
                                    scrubbedPoint = RainbowModelEngine.findNearestPoint(offset.x, size.width.toFloat(), priceSeries)
                                    touchXPosition = offset.x
                                }
                            )
                        }
                        .pointerInput(priceSeries) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    scrubbedPoint = RainbowModelEngine.findNearestPoint(offset.x, size.width.toFloat(), priceSeries)
                                    touchXPosition = offset.x
                                },
                                onHorizontalDrag = { change, _ ->
                                    scrubbedPoint = RainbowModelEngine.findNearestPoint(change.position.x, size.width.toFloat(), priceSeries)
                                    touchXPosition = change.position.x
                                },
                                onDragEnd = {
                                    // Keep latest touched point visible or retain selection
                                }
                            )
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                    ) {
                        RainbowModelEngine.drawRainbowChart(
                            drawScope = this,
                            priceSeries = priceSeries,
                            touchX = touchXPosition,
                            cycleMarkers = cycleMarkers
                        )
                    }

                    // Scrubber Inspection Tooltip Overlay
                    val activeScrub = scrubbedPoint
                    if (activeScrub != null && touchXPosition != null) {
                        val scrubBand = RainbowModelEngine.getBandForPoint(activeScrub)
                        val yr = activeScrub.x.toInt()
                        val month = ((activeScrub.x - yr) * 12).toInt().coerceIn(0, 11)
                        val monthName = RainbowModelEngine.MONTH_NAMES[month]

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp, top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF14161A).copy(alpha = 0.92f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "$monthName $yr",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD1D5DB)
                                )
                                Text(
                                    text = "$" + com.example.util.AppNumberFormatter.formatRawPrice(activeScrub.y, decimals = 0),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Text(
                                    text = scrubBand.band.name,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scrubBand.textColor
                                )
                            }
                        }
                    }

                    // Touch drag hint
                    if (touchXPosition == null) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGreek) "Σύρετε για έλεγχο ιστορικού" else "Touch or drag to inspect prices",
                                fontSize = 9.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Band Card: Matches reference `.band-card`
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isGreek) "Τρέχουσα ζώνη" else "Current band",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentBandInfo.band.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentBandInfo.textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = (if (isGreek) "Τελευταία τιμή: " else "Latest price: ") +
                            "$" + com.example.util.AppNumberFormatter.formatRawPrice(effectiveBtcPrice, decimals = 0),
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Line: Matches reference `.status`
        Text(
            text = if (isGreek) "Συγχρονισμένη ζωντανή ροή BTC/USD" else "Live BTC price feed active ($" +
                    com.example.util.AppNumberFormatter.formatRawPrice(effectiveBtcPrice, decimals = 0) + ").",
            fontSize = 11.5.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Rainbow Model Core Math & Canvas Drawing Engine
 * Direct Kotlin port of the reference JavaScript implementation.
 */
object RainbowModelEngine {

    const val X_MIN = 2012.5
    const val X_MAX = 2033.0
    const val BAND_END = 2032.9
    const val Y_MIN = 3.0
    const val Y_MAX = 4000000.0

    // Log-regression parameters (standard Rainbow model)
    const val A = 2.66167155005961
    const val B = -17.9183761889864
    const val CENTER_SHIFT = -0.05 // nudge whole rainbow in decades
    const val HALF_WIDTH = 0.5     // half of total rainbow thickness in decades

    data class BandDef(
        val name: String,
        val color: Color
    )

    data class BandEvaluation(
        val band: BandDef,
        val textColor: Color
    )

    data class HalvingDef(
        val t: Double,
        val label: String,
        val isEst: Boolean
    )

    data class PricePoint(
        val x: Double,
        val y: Double
    )

    data class CycleMarker(
        val x: Double,
        val y: Double,
        val color: Color,
        val label: String = ""
    )

    val BANDS = listOf(
        BandDef("Maximum Bubble Territory", Color(0xFFC00000)),
        BandDef("FOMO intensifies", Color(0xFFE8622A)),
        BandDef("Is this a bubble?", Color(0xFFF5A94A)),
        BandDef("HODL!", Color(0xFFFBE57F)),
        BandDef("Still cheap", Color(0xFFA3D18B)),
        BandDef("Accumulate", Color(0xFF4FB37A)),
        BandDef("Basically a Fire Sale", Color(0xFF4472C4))
    )

    val BAND_COLORS_TEXT = listOf(
        Color(0xFFC00000),
        Color(0xFFE8622A),
        Color(0xFFD98A1F),
        Color(0xFFC9A800),
        Color(0xFF6AA84F),
        Color(0xFF2E8B57),
        Color(0xFF4472C4)
    )

    val HALVINGS = listOf(
        HalvingDef(2012.91, "Halving", isEst = false), // 28 Nov 2012
        HalvingDef(2016.52, "Halving", isEst = false), // 9 Jul 2016
        HalvingDef(2020.36, "Halving", isEst = false), // 11 May 2020
        HalvingDef(2024.30, "Halving", isEst = false), // 20 Apr 2024
        HalvingDef(2028.30, "Halving 2028 (Est)", isEst = true),
        HalvingDef(2032.30, "Halving 2032 (Est)", isEst = true)
    )

    val MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Offsets (in decades) of the N+1 band edges, top -> bottom
    val EDGES: List<Double> = BANDS.indices.map { i ->
        HALF_WIDTH - (i * 2.0 * HALF_WIDTH) / BANDS.size
    } + listOf(-HALF_WIDTH)

    fun centerLog(t: Double): Double {
        val days = (t - 2009.0) * 365.25 - 8.0
        return A * ln(max(days, 1.0)) + B + CENTER_SHIFT
    }

    fun edgePrice(t: Double, i: Int): Double {
        val exponent = centerLog(t) + EDGES[i]
        return 10.0.pow(exponent)
    }

    // Historical month-end closing prices from reference code
    val MONTHLY: Map<Int, List<Double>> = mapOf(
        2013 to listOf(20.0, 33.0, 93.0, 140.0, 128.0, 97.0, 98.0, 135.0, 126.0, 198.0, 1120.0, 760.0),
        2014 to listOf(815.0, 565.0, 450.0, 450.0, 630.0, 640.0, 585.0, 480.0, 390.0, 340.0, 375.0, 320.0),
        2015 to listOf(220.0, 235.0, 245.0, 235.0, 230.0, 245.0, 285.0, 230.0, 240.0, 315.0, 380.0, 430.0),
        2016 to listOf(370.0, 435.0, 415.0, 450.0, 530.0, 675.0, 625.0, 575.0, 610.0, 700.0, 740.0, 960.0),
        2017 to listOf(970.0, 1180.0, 1080.0, 1350.0, 2300.0, 2500.0, 2870.0, 4700.0, 4360.0, 6470.0, 10900.0, 14200.0),
        2018 to listOf(10300.0, 10300.0, 6900.0, 9250.0, 7500.0, 6400.0, 7700.0, 7000.0, 6600.0, 6300.0, 4000.0, 3700.0),
        2019 to listOf(3450.0, 3800.0, 4100.0, 5300.0, 8600.0, 10800.0, 9600.0, 9600.0, 8300.0, 9200.0, 7500.0, 7200.0),
        2020 to listOf(9350.0, 8550.0, 6400.0, 8600.0, 9450.0, 9130.0, 11350.0, 11700.0, 10780.0, 13800.0, 19700.0, 29000.0),
        2021 to listOf(33100.0, 45200.0, 58800.0, 57700.0, 37300.0, 35000.0, 41500.0, 47100.0, 43800.0, 61300.0, 57000.0, 46300.0),
        2022 to listOf(38500.0, 43200.0, 45500.0, 37600.0, 31800.0, 19800.0, 23300.0, 20000.0, 19400.0, 20500.0, 17150.0, 16500.0),
        2023 to listOf(23100.0, 23150.0, 28450.0, 29250.0, 27100.0, 30450.0, 29230.0, 25940.0, 26970.0, 34500.0, 37700.0, 42250.0),
        2024 to listOf(42580.0, 61100.0, 71300.0, 60600.0, 67500.0, 62800.0, 64600.0, 58000.0, 63300.0, 70300.0, 96400.0, 93400.0),
        2025 to listOf(102400.0, 84300.0, 82500.0, 94200.0, 104600.0, 107100.0, 115800.0, 108200.0, 114000.0, 109500.0, 90400.0, 87500.0),
        2026 to listOf(84000.0, 68000.0, 66000.0, 76000.0, 74000.0, 65000.0, 66000.0, 64000.0)
    )

    fun buildPriceSeries(liveBtcPrice: Double): List<PricePoint> {
        val pts = mutableListOf<PricePoint>()
        MONTHLY.keys.sorted().forEach { y ->
            val months = MONTHLY[y] ?: emptyList()
            months.forEachIndexed { m, p ->
                pts.add(PricePoint(x = y.toDouble() + (m + 1).toDouble() / 12.0 - 0.02, y = p))
            }
        }
        if (liveBtcPrice > 0.0) {
            val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            val nowFractionalYear = now.get(java.util.Calendar.YEAR) +
                (now.get(java.util.Calendar.MONTH) + 1).toDouble() / 12.0 - 0.02
            pts.add(PricePoint(x = nowFractionalYear, y = liveBtcPrice))
        }
        return pts
    }

    fun getBandForPoint(pt: PricePoint): BandEvaluation {
        val t = pt.x
        var idx = BANDS.size - 1 // Default to Fire Sale
        for (i in BANDS.indices) {
            if (pt.y >= edgePrice(t, i + 1)) {
                idx = i
                break
            }
        }
        if (pt.y > edgePrice(t, 0)) {
            idx = 0
        }
        return BandEvaluation(BANDS[idx], BAND_COLORS_TEXT[idx])
    }

    fun findNearestPoint(touchX: Float, width: Float, series: List<PricePoint>): PricePoint? {
        if (series.isEmpty() || width <= 0f) return null
        val paddingLeft = 14f
        val paddingRight = 60f
        val plotWidth = (width - paddingLeft - paddingRight).coerceAtLeast(10f)
        val clampedX = (touchX - paddingLeft).coerceIn(0f, plotWidth)
        val frac = (clampedX / plotWidth).coerceIn(0f, 1f)
        val targetT = X_MIN + frac * (X_MAX - X_MIN)

        return series.minByOrNull { kotlin.math.abs(it.x - targetT) }
    }

    /**
     * Canvas rendering strictly replicating the Chart.js plugin pipeline:
     * 1) Solid white background
     * 2) Logarithmic Y grid & right labels ($10, $100, $1K, $10K, $100K, $1M)
     * 3) Linear X grid & 2-year ticks (2013, 2015, ..., 2031)
     * 4) 7 Rainbow bands drawn top-to-bottom with thin separators
     * 5) Halving lines with vertical labels (dashed red for estimated)
     * 6) Crisp solid black price line (1.6dp width) terminating at live price
     * 7) Optional touch crosshair
     */
    fun drawRainbowChart(
        drawScope: DrawScope,
        priceSeries: List<PricePoint>,
        touchX: Float?,
        cycleMarkers: List<CycleMarker> = emptyList()
    ) = with(drawScope) {
        val paddingLeft = 12f
        val paddingRight = 58f
        val paddingTop = 12f
        val paddingBottom = 48f

        val plotWidth = (size.width - paddingLeft - paddingRight).coerceAtLeast(10f)
        val plotHeight = (size.height - paddingTop - paddingBottom).coerceAtLeast(10f)

        val logYMin = log10(Y_MIN)
        val logYMax = log10(Y_MAX)

        fun getX(t: Double): Float {
            val frac = ((t - X_MIN) / (X_MAX - X_MIN)).toFloat().coerceIn(0f, 1f)
            return paddingLeft + frac * plotWidth
        }

        fun getY(price: Double): Float {
            val clamped = price.coerceIn(Y_MIN, Y_MAX)
            val logP = log10(clamped)
            val frac = ((logP - logYMin) / (logYMax - logYMin)).toFloat().coerceIn(0f, 1f)
            return paddingTop + plotHeight * (1f - frac)
        }

        val floorY = paddingTop + plotHeight

        // 1. Solid White Background
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = size
        )

        // 2. Rainbow Bands (Drawn behind the price line with clip to chartArea)
        val step = 0.1
        val ts = mutableListOf<Double>()
        var curT = X_MIN
        while (curT < BAND_END) {
            ts.add(curT)
            curT += step
        }
        ts.add(BAND_END)

        for (i in BANDS.indices) {
            val bandPath = Path()
            // Top edge forward
            ts.forEachIndexed { k, t ->
                val px = getX(t)
                val py = getY(edgePrice(t, i))
                if (k == 0) bandPath.moveTo(px, py) else bandPath.lineTo(px, py)
            }
            // Bottom edge backward
            for (k in ts.indices.reversed()) {
                val t = ts[k]
                val px = getX(t)
                val py = getY(edgePrice(t, i + 1))
                bandPath.lineTo(px, py)
            }
            bandPath.close()

            // Fill band color
            drawPath(bandPath, BANDS[i].color, style = Fill)

            // Thin light separator between bands: rgba(255,255,255,0.35), 0.6px
            drawPath(
                bandPath,
                color = Color.White.copy(alpha = 0.35f),
                style = Stroke(width = 0.6.dp.toPx())
            )
        }

        // 3. Grid Lines & Ticks (Chart.js scales)
        // X-Axis grid & ticks: 2013 to 2031 every 2 years
        val xGridColor = Color.Black.copy(alpha = 0.06f)
        val xBorderColor = Color.Black.copy(alpha = 0.25f)
        val tickPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#444444")
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        for (yr in 2013..2031 step 2) {
            val px = getX(yr.toDouble())
            if (px in paddingLeft..(paddingLeft + plotWidth)) {
                // Vertical grid line
                drawLine(
                    color = xGridColor,
                    start = Offset(px, paddingTop),
                    end = Offset(px, floorY),
                    strokeWidth = 1.dp.toPx()
                )
                // Rotated 45-degree year label
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(45f, px, floorY + 12f)
                drawContext.canvas.nativeCanvas.drawText(
                    yr.toString(),
                    px,
                    floorY + 22f,
                    tickPaint
                )
                drawContext.canvas.nativeCanvas.restore()
            }
        }

        // Y-Axis grid & right ticks: 10, 100, 1000, 10000, 100000, 1000000
        val yGridColor = Color.Black.copy(alpha = 0.08f)
        val yLabelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#555555")
            textSize = 22f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val yLevels = listOf(
            10.0 to "$10",
            100.0 to "$100",
            1000.0 to "$1,000",
            10000.0 to "$10,000",
            100000.0 to "$100,000",
            1000000.0 to "$1,000,000"
        )

        yLevels.forEach { (price, label) ->
            val py = getY(price)
            if (py in paddingTop..floorY) {
                // Horizontal grid line
                drawLine(
                    color = yGridColor,
                    start = Offset(paddingLeft, py),
                    end = Offset(paddingLeft + plotWidth, py),
                    strokeWidth = 1.dp.toPx()
                )
                // Right tick label
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    paddingLeft + plotWidth + 6f,
                    py + 7f,
                    yLabelPaint
                )
            }
        }

        // Outer chart bounding borders
        drawLine(color = xBorderColor, start = Offset(paddingLeft, paddingTop), end = Offset(paddingLeft + plotWidth, paddingTop), strokeWidth = 1.dp.toPx())
        drawLine(color = xBorderColor, start = Offset(paddingLeft, floorY), end = Offset(paddingLeft + plotWidth, floorY), strokeWidth = 1.dp.toPx())
        drawLine(color = xBorderColor, start = Offset(paddingLeft, paddingTop), end = Offset(paddingLeft, floorY), strokeWidth = 1.dp.toPx())
        drawLine(color = xBorderColor, start = Offset(paddingLeft + plotWidth, paddingTop), end = Offset(paddingLeft + plotWidth, floorY), strokeWidth = 1.dp.toPx())

        // 4. Halving Lines & Vertical Labels
        val halvingLabelPaint = Paint().apply {
            textSize = 20f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        HALVINGS.forEach { h ->
            val hx = getX(h.t)
            if (hx in paddingLeft..(paddingLeft + plotWidth)) {
                // Stroke halving line (dashed red for estimated, solid dark grey for past)
                val lineColor = if (h.isEst) Color(0xDCD74646) else Color(0x8C3C3C3C)
                val pathEffect = if (h.isEst) PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f) else null

                drawLine(
                    color = lineColor,
                    start = Offset(hx, paddingTop),
                    end = Offset(hx, floorY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = pathEffect
                )

                // Vertical label near bottom with semi-transparent badge
                val labelText = h.label
                halvingLabelPaint.color = if (h.isEst) android.graphics.Color.parseColor("#B91C1C") else android.graphics.Color.parseColor("#222222")
                val textWidth = halvingLabelPaint.measureText(labelText)

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.translate(hx - 4f, floorY - 8f)
                drawContext.canvas.nativeCanvas.rotate(-90f)

                // Label background pill
                val bgPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#D9FFFFFF")
                    style = Paint.Style.FILL
                }
                drawContext.canvas.nativeCanvas.drawRect(-4f, -22f, textWidth + 8f, 6f, bgPaint)
                drawContext.canvas.nativeCanvas.drawText(labelText, 0f, 0f, halvingLabelPaint)
                drawContext.canvas.nativeCanvas.restore()
            }
        }

        // 5. BTC Price Line: Black, 1.6dp width, tension 0
        if (priceSeries.isNotEmpty()) {
            val btcPath = Path()
            var started = false
            var lastPx = 0f
            var lastPy = 0f

            for (pt in priceSeries) {
                if (pt.x in X_MIN..X_MAX) {
                    val px = getX(pt.x)
                    val py = getY(pt.y)
                    if (!started) {
                        btcPath.moveTo(px, py)
                        started = true
                    } else {
                        btcPath.lineTo(px, py)
                    }
                    lastPx = px
                    lastPy = py
                }
            }

            if (started) {
                drawPath(
                    path = btcPath,
                    color = Color.Black,
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Active live price point circle marker
                drawCircle(
                    color = Color.Black,
                    radius = 3.5.dp.toPx(),
                    center = Offset(lastPx, lastPy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = Offset(lastPx, lastPy)
                )
            }
        }

        cycleMarkers.forEachIndexed { index, marker ->
            if (marker.y <= 0.0 || marker.x !in X_MIN..X_MAX) return@forEachIndexed
            val origin = priceSeries.minByOrNull { kotlin.math.abs(it.x - marker.x) }
                ?.takeIf { kotlin.math.abs(it.x - marker.x) < 0.6 }
            val startX = getX(origin?.x ?: marker.x)
            val startY = getY(origin?.y ?: marker.y)
            val stem = 26.dp.toPx() + index * 4.dp.toPx()
            val end = Offset(
                x = (startX + stem).coerceAtMost(paddingLeft + plotWidth - 8.dp.toPx()),
                y = (startY + stem * 0.72f).coerceAtMost(floorY - 10.dp.toPx())
            )
            val start = Offset(startX, startY)
            drawLine(
                color = Color.Black,
                start = start,
                end = end,
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.Black, radius = 3.2.dp.toPx(), center = end)
            drawCircle(color = marker.color, radius = 2.4.dp.toPx(), center = end)
            if (marker.label.isNotBlank()) {
                val labelPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#14161A")
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawContext.canvas.nativeCanvas.drawText(
                    marker.label,
                    end.x + 6.dp.toPx(),
                    end.y + 4.dp.toPx(),
                    labelPaint
                )
            }
        }

        // 6. Touch Crosshair
        if (touchX != null && touchX in paddingLeft..(paddingLeft + plotWidth)) {
            drawLine(
                color = Color.Black.copy(alpha = 0.5f),
                start = Offset(touchX, paddingTop),
                end = Offset(touchX, floorY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        }
    }
}
