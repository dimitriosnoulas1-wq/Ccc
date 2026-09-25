package com.example.ui.rainbow

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

// ─── Παλέτα (σκούρο "glass" στυλ, ίδια επίσημα χρώματα ζωνών) ─────────────────
private val CARD_BG = Color(0xFF0D0F1C)
private val CARD_BG_2 = Color(0xFF141729)
private val LINE = Color(0xFFFFFFFF)
private val TEXT = Color(0xFFF3F4F6)
private val MUTED = Color(0xFF9CA3AF)
private val FAINT = Color(0xFF6B7280)
private val ACCENT = Color(0xFF22D3EE)
private val CRASH = Color(0xFFF87171)
private val DIVIDER = Color(0x1FFFFFFF)

private val CHART_HEIGHT = 460.dp
private val RIGHT_PAD = 58.dp
private val BOTTOM_PAD = 36.dp
private val TOP_PAD = 6.dp

// ─── Section (chart + ανάλυση) για το tab Cycle ──────────────────────────────
/**
 * Native Bitcoin Rainbow chart: πραγματικό ιστορικό (blockchain.info, 1 φορά τη μέρα, με cache)
 * και live τιμή (Coinbase, κάθε 15s). Το σημερινό σημείο αλλάζει μία φορά τη μέρα,
 * ή νωρίτερα αν γίνει κίνηση ±7% ή αλλαγή ζώνης. Μπαίνει σε LazyColumn item.
 */
@Composable
fun RainbowCycleSection(
    greek: Boolean,
    liveUsd: Double = 0.0,
    priceIsLive: Boolean = false,
    modifier: Modifier = Modifier,
    vm: RainbowViewModel = viewModel(),
) {
    val s by vm.state.collectAsState()
    val today = s.today
    val series = remember(s.history, s.shown, today) { buildSeries(s.history, s.shown, today) }
    val insights = remember(series, today) { CycleInsights.compute(series, today) }

    LaunchedEffect(liveUsd, priceIsLive) {
        if (priceIsLive && liveUsd > 0.0) vm.applyLive(liveUsd)
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            series.isEmpty() && s.loading -> PlaceholderCard {
                CircularProgressIndicator(color = ACCENT)
            }
            series.isEmpty() -> PlaceholderCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        (if (greek) "Δεν φορτώθηκαν τα δεδομένα: " else "Could not load data: ") + (s.error ?: ""),
                        color = TEXT
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.refreshHistory(force = true) }) { Text(if (greek) "Δοκίμασε ξανά" else "Retry") }
                }
            }
            else -> {
                RainbowChartCard(series, today, insights, greek)
                RainbowNowCard(s, series, today, insights.lastOrNull(), priceIsLive, greek)
                insights.filter { !it.isCurrent }.reversed().forEach { CycleInsightCard(it, greek) }
            }
        }
    }
}

@Composable
private fun PlaceholderCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Box(Modifier.fillMaxWidth().height(CHART_HEIGHT).padding(24.dp), Alignment.Center) { content() }
    }
}

// ─── Chart card ──────────────────────────────────────────────────────────────
@Composable
fun RainbowChartCard(series: List<PricePoint>, today: Long, insights: List<CycleInsights.Insight>, greek: Boolean) {
    val first = series.first().day.toDouble()
    val end = (RainbowModel.HALVINGS.last().day + 150).toDouble()
    val lastHalving = RainbowModel.currentHalving(today).day.toDouble()

    val defMax = min(end, today + 900.0)
    var xMin by remember { mutableStateOf(first) }
    var xMax by remember { mutableStateOf(defMax) }
    var scrubDay by remember { mutableStateOf<Long?>(null) }
    var ghostOn by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rangeAnim = remember { Animatable(0f) }

    // Η γραμμή τιμής "ζωγραφίζεται" από την αρχή ως σήμερα στο πρώτο άνοιγμα.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(1600, easing = FastOutSlowInEasing)) }

    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "pulse"
    )
    val paints = remember { ChartPaints() }
    val markers = remember(series, today, insights) { buildMarkers(insights, series) }
    val yRange = remember(series, xMin, xMax) { logRange(series, xMin, xMax) }

    val ghosts = remember(series, today) {
        val cur = RainbowModel.currentHalving(today)
        val n = RainbowModel.daysSinceHalving(today)
        RainbowModel.HALVINGS.filter { !it.estimated && it != cur }
            .map { h -> h to RainbowVisuals.ghost(series, h, cur, n) }
            .filter { it.second.size > 1 }
    }

    // Ομαλή μετάβαση στο νέο εύρος (κουμπιά, διπλό πάτημα, mini-map).
    fun setRange(a: Double, b: Double) {
        val ta = a.coerceAtLeast(first)
        val tb = b.coerceAtMost(end)
        val fa = xMin
        val fb = xMax
        scrubDay = null
        scope.launch {
            rangeAnim.snapTo(0f)
            rangeAnim.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) {
                xMin = RainbowVisuals.lerp(fa, ta, value)
                xMax = RainbowVisuals.lerp(fb, tb, value)
            }
        }
    }

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Bitcoin Rainbow Price Chart", fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                        color = TEXT
                    )
                    Text(
                        if (greek) RainbowModel.FORMULA_NOTE_EL else RainbowModel.FORMULA_NOTE_EN,
                        fontSize = 11.sp, color = FAINT
                    )
                }
                Text("LOG SCALE", fontSize = 11.sp, color = MUTED, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(10.dp))

            Box(
                Modifier.fillMaxWidth().height(CHART_HEIGHT).pointerInput(series, first, end) {
                    val rightPad = RIGHT_PAD.toPx()
                    val slop = viewConfiguration.touchSlop
                    var lastTap = 0L
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val plotW = size.width - rightPad
                        fun toDay(x: Float) =
                            (xMin + (x / plotW).coerceIn(0f, 1f) * (xMax - xMin)).roundToLong()
                        var lastBand = -1
                        // Ελαφριά δόνηση όταν το tooltip περνά σε άλλη ζώνη.
                        fun scrubTo(x: Float) {
                            val d = toDay(x)
                            scrubDay = d
                            val pt = series.nearest(d) ?: return
                            val bnd = RainbowModel.bandIndex(pt.day, pt.price)
                            if (lastBand != -1 && bnd != lastBand) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            lastBand = bnd
                        }
                        scrubTo(down.position.x)
                        down.consume()
                        var multi = false
                        var moved = false
                        var upTime = down.uptimeMillis
                        while (true) {
                            val ev = awaitPointerEvent()
                            ev.changes.firstOrNull()?.let { upTime = it.uptimeMillis }
                            val pressed = ev.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break
                            if (pressed.size >= 2) {
                                // 2 δάχτυλα: zoom γύρω από το κέντρο + pan
                                if (!multi) { multi = true; scrubDay = null }
                                val zoom = ev.calculateZoom()
                                val pan = ev.calculatePan()
                                val c = ev.calculateCentroid(useCurrent = true)
                                val span = xMax - xMin
                                val newSpan = (span / zoom).coerceIn(45.0, end - first)
                                val fx = (c.x / plotW).coerceIn(0f, 1f).toDouble()
                                val focal = xMin + fx * span
                                val nMin = (focal - fx * newSpan - pan.x / plotW * newSpan)
                                    .coerceIn(first, end - newSpan)
                                xMin = nMin; xMax = nMin + newSpan
                                ev.changes.forEach { it.consume() }
                            } else if (!multi) {
                                // 1 δάχτυλο: tooltip
                                if ((pressed[0].position - down.position).getDistance() > slop) moved = true
                                scrubTo(pressed[0].position.x)
                                pressed[0].consume()
                            }
                        }
                        // Διπλό πάτημα: επιστροφή στην αρχική προβολή.
                        if (!multi && !moved && upTime - down.uptimeMillis < 250) {
                            if (down.uptimeMillis - lastTap < 320) {
                                lastTap = 0L
                                setRange(first, defMax)
                            } else {
                                lastTap = down.uptimeMillis
                            }
                        }
                    }
                }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRainbow(
                        series, insights, markers, if (ghostOn) ghosts else emptyList(), today,
                        xMin, xMax, yRange, scrubDay, reveal.value, greek, paints
                    )
                }
                // Ξεχωριστό layer για τον παλμό ώστε να μην ξαναζωγραφίζονται οι ζώνες σε κάθε frame.
                if (reveal.value >= 1f) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawLivePulse(series.last(), xMin, xMax, yRange, pulse)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            MiniMap(series, first, end, xMin, xMax) { center ->
                val span = xMax - xMin
                val a = (center - span / 2).coerceIn(first, max(first, end - span))
                xMin = a
                xMax = a + span
                scrubDay = null
            }

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RangeChip(if (greek) "Όλα" else "All") { setRange(first, end) }
                RangeChip(if (greek) "Κύκλος" else "Cycle") { setRange(lastHalving - 150, today + 400.0) }
                RangeChip(if (greek) "Σήμερα" else "Today") { setRange(today - 400.0, today + 120.0) }
                RangeChip(if (ghostOn) "Ghost ✓" else "Ghost") {
                    ghostOn = !ghostOn
                    if (ghostOn) setRange(lastHalving - 60, today + 200.0)
                }
                RangeChip("✕") { scrubDay = null }
            }
            if (ghostOn) {
                Text(
                    if (greek) "Ghost: η πορεία των κύκλων 2012/2016/2020 από το halving ως τη σημερινή μέρα-μέτρησης, " +
                        "μετατοπισμένη στο halving 2024 και κλιμακωμένη στην τιμή του. Δείχνει μόνο το παρελθόν, όχι πρόβλεψη."
                    else "Ghost: the 2012/2016/2020 paths from their halving up to today's day count, shifted onto the " +
                        "2024 halving and scaled to its price. Past only, not a forecast.",
                    fontSize = 11.sp, color = FAINT, lineHeight = 15.sp, modifier = Modifier.padding(top = 6.dp)
                )
            }
            Text(
                if (greek) "1 δάχτυλο: λεπτομέρειες · 2 δάχτυλα: zoom & μετακίνηση · διπλό πάτημα: αρχική προβολή"
                else "1 finger: details · 2 fingers: zoom & pan · double tap: reset view",
                fontSize = 11.sp, color = FAINT, modifier = Modifier.padding(top = 6.dp)
            )
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(CRASH, CircleShape))
                Text(
                    if (greek) " πτώση/κρίση   " else " crash/crisis   ",
                    fontSize = 11.sp, color = FAINT
                )
                Box(Modifier.size(7.dp).background(ACCENT, CircleShape))
                Text(if (greek) " γεγονός   " else " event   ", fontSize = 11.sp, color = FAINT)
                Box(Modifier.size(7.dp).background(Color(0xFFFACC15), CircleShape))
                Text(if (greek) " κορυφή/πάτος κύκλου" else " cycle top/bottom", fontSize = 11.sp, color = FAINT)
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = TEXT)
    }
}

// ─── Mini-map: όλο το ιστορικό, με το ορατό παράθυρο. Άγγιγμα/σύρσιμο = μετακίνηση. ─────
@Composable
private fun MiniMap(
    series: List<PricePoint>, first: Double, end: Double, xMin: Double, xMax: Double,
    onCenter: (Double) -> Unit,
) {
    val runs = remember(series) {
        val stride = max(1, series.size / 400)
        RainbowVisuals.bandRuns(series.filterIndexed { i, _ -> i % stride == 0 || i == series.lastIndex })
    }
    val lo = remember(series) { log10(series.minOf { it.price }) }
    val hi = remember(series) { log10(series.maxOf { it.price }) }
    Canvas(
        Modifier.fillMaxWidth().height(44.dp).pointerInput(first, end) {
            val plotW = size.width - RIGHT_PAD.toPx()
            fun dayAt(x: Float) = first + (x / plotW).coerceIn(0f, 1f) * (end - first)
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                onCenter(dayAt(down.position.x))
                down.consume()
                while (true) {
                    val ev = awaitPointerEvent()
                    val c = ev.changes.firstOrNull { it.pressed } ?: break
                    onCenter(dayAt(c.position.x))
                    c.consume()
                }
            }
        }
    ) {
        val w = size.width - RIGHT_PAD.toPx()
        val h = size.height
        val pad = 4.dp.toPx()
        fun x(d: Double) = ((d - first) / (end - first) * w).toFloat()
        fun y(p: Double) = h - pad - ((log10(p) - lo) / max(1e-9, hi - lo) * (h - 2 * pad)).toFloat()
        drawRoundRect(CARD_BG_2, Offset.Zero, Size(w, h), CornerRadius(8.dp.toPx()))
        // Γραμμή τιμής στο χρώμα της ζώνης όπου βρισκόταν κάθε κομμάτι.
        runs.forEach { (band, pts) ->
            val path = Path()
            pts.forEachIndexed { i, pt ->
                val px = x(pt.day.toDouble()); val py = y(pt.price)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, Color(RainbowModel.BANDS[band].color), style = Stroke(1.6.dp.toPx(), join = StrokeJoin.Round))
        }
        val a = x(xMin); val b = x(xMax)
        drawRect(Color(0x2622D3EE), Offset(a, 0f), Size(max(2f, b - a), h))
        drawRoundRect(ACCENT, Offset(a, 0f), Size(max(2f, b - a), h), CornerRadius(6.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    }
}

// ─── Gauge: πού βρίσκεται η τιμή μέσα στο rainbow ─────────────────────────────
@Composable
private fun RainbowGauge(day: Long, price: Double) {
    val pos = RainbowVisuals.position(day, price).toFloat()
    Canvas(Modifier.fillMaxWidth().height(22.dp)) {
        val n = RainbowModel.BANDS.size
        val gap = 2.dp.toPx()
        val barH = 8.dp.toPx()
        val top = (size.height - barH) / 2
        val segW = (size.width - gap * (n - 1)) / n
        RainbowModel.BANDS.forEachIndexed { i, band ->
            drawRoundRect(
                Color(band.color), Offset(i * (segW + gap), top), Size(segW, barH), CornerRadius(barH / 2)
            )
        }
        val mx = pos * size.width
        val cy = size.height / 2
        drawCircle(Color(0x66FFFFFF), 10.dp.toPx(), Offset(mx, cy))
        drawCircle(Color(0xFF0B0B12), 6.5.dp.toPx(), Offset(mx, cy))
        drawCircle(LINE, 6.5.dp.toPx(), Offset(mx, cy), style = Stroke(2.dp.toPx()))
    }
}

// ─── Markers (γεγονότα + κορυφές/πάτοι) ──────────────────────────────────────
private class Marker(val day: Long, val price: Double, val color: Color, val el: String, val en: String)

private fun buildMarkers(insights: List<CycleInsights.Insight>, series: List<PricePoint>): List<Marker> {
    val out = mutableListOf<Marker>()
    RainbowEvents.ALL.forEach { ev ->
        val p = series.priceNear(ev.day) ?: return@forEach
        val c = if (ev.kind == RainbowEvents.Kind.CRASH) CRASH else ACCENT
        out += Marker(ev.day, p, c, ev.el, ev.en)
    }
    insights.forEach { ins ->
        listOfNotNull(ins.high, ins.low).forEach { x ->
            val (el, en) = CycleInsights.extremeLabel(x, ins.halving)
            out += Marker(x.point.day, x.point.price, Color(0xFFFACC15), el, en)
        }
    }
    return out.sortedBy { it.day }
}

// ─── Geometry ────────────────────────────────────────────────────────────────
/** log10 εύρος του άξονα Y για το ορατό παράθυρο (ζώνες + τιμές). */
private fun logRange(series: List<PricePoint>, xMin: Double, xMax: Double): Pair<Double, Double> {
    val span = xMax - xMin
    var lo = Double.MAX_VALUE; var hi = -Double.MAX_VALUE
    for (k in 0..40) {
        val d = xMin + span * k / 40
        lo = min(lo, log10(RainbowModel.edgePrice(d, 0)))
        hi = max(hi, log10(RainbowModel.edgePrice(d, RainbowModel.EDGES.lastIndex)))
    }
    for (pt in series) {
        if (pt.day < xMin - 2 || pt.day > xMax + 2) continue
        val lp = log10(pt.price); lo = min(lo, lp); hi = max(hi, lp)
    }
    val padY = (hi - lo) * 0.05
    return (lo - padY) to (hi + padY)
}

private class Frame(scope: DrawScope, val xMin: Double, xMax: Double, val lo: Double, val hi: Double) {
    val l = 0f
    val r = scope.size.width - with(scope) { RIGHT_PAD.toPx() }
    val t = with(scope) { TOP_PAD.toPx() }
    val b = scope.size.height - with(scope) { BOTTOM_PAD.toPx() }
    val w = r - l
    val h = b - t
    val span = xMax - xMin
    fun x(d: Double) = l + ((d - xMin) / span * w).toFloat()
    fun y(price: Double) = b - ((log10(price) - lo) / (hi - lo) * h).toFloat()
}

// ─── Drawing ─────────────────────────────────────────────────────────────────
private class ChartPaints {
    val axis = Paint().apply { isAntiAlias = true; color = 0xFF9CA3AF.toInt() }
    val label = Paint().apply {
        isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 0f, 1f, 0xCC000000.toInt())
    }
    val tip = Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
}

private fun DrawScope.drawLivePulse(
    last: PricePoint, xMin: Double, xMax: Double, yRange: Pair<Double, Double>, pulse: Float,
) {
    val f = Frame(this, xMin, xMax, yRange.first, yRange.second)
    val x = f.x(last.day.toDouble()); val y = f.y(last.price)
    if (x < f.l - 1 || x > f.r + 1) return
    val bandCol = Color(RainbowModel.BANDS[RainbowModel.bandIndex(last.day, last.price)].color)
    clipRect(f.l, f.t, f.r + 24.dp.toPx(), f.b) {
        drawCircle(bandCol.copy(alpha = 0.6f * (1 - pulse)), (6 + 18 * pulse).dp.toPx(), Offset(x, y))
        val p2 = (pulse + 0.5f) % 1f
        drawCircle(bandCol.copy(alpha = 0.4f * (1 - p2)), (6 + 18 * p2).dp.toPx(), Offset(x, y), style = Stroke(1.5.dp.toPx()))
        drawCircle(bandCol, 6.dp.toPx(), Offset(x, y))
        drawCircle(LINE, 6.dp.toPx(), Offset(x, y), style = Stroke(2.5.dp.toPx()))
    }
}

private fun shorten(s: String, max: Int) = if (s.length <= max) s else s.take(max - 1).trimEnd() + "…"

private fun DrawScope.drawRainbow(
    series: List<PricePoint>, insights: List<CycleInsights.Insight>, markers: List<Marker>,
    ghosts: List<Pair<Halving, List<PricePoint>>>, today: Long,
    xMin: Double, xMax: Double, yRange: Pair<Double, Double>,
    scrubDay: Long?, reveal: Float, greek: Boolean, p: ChartPaints,
) {
    val f = Frame(this, xMin, xMax, yRange.first, yRange.second)
    val (lo, hi) = yRange
    val l = f.l; val r = f.r; val t = f.t; val b = f.b
    val native = drawContext.canvas.nativeCanvas
    p.axis.textSize = 10.sp.toPx()

    drawRoundRect(CARD_BG_2, Offset(l, t), Size(f.w, f.h), CornerRadius(10.dp.toPx()))

    clipRect(l, t, r, b) {
        // 9 ζώνες με τα επίσημα χρώματα
        val n = 160
        for (i in RainbowModel.BANDS.indices) {
            val path = Path()
            for (k in 0..n) {
                val d = xMin + f.span * k / n
                val y = f.y(RainbowModel.edgePrice(d, i + 1))
                if (k == 0) path.moveTo(f.x(d), y) else path.lineTo(f.x(d), y)
            }
            for (k in n downTo 0) {
                val d = xMin + f.span * k / n
                path.lineTo(f.x(d), f.y(RainbowModel.edgePrice(d, i)))
            }
            path.close()
            drawPath(path, Color(RainbowModel.BANDS[i].color).copy(alpha = 0.88f))
        }

        // "Ομίχλη μέλλοντος": οι ζώνες μετά από σήμερα φαίνονται αχνά (είναι μόνο ο τύπος, όχι δεδομένα).
        val xt = f.x(today + 0.5)
        if (xt < r) {
            val fx = max(xt, l)
            drawRect(Color(0x990D0F1C), Offset(fx, t), Size(r - fx, f.h))
            drawLine(Color(0x66FFFFFF), Offset(fx, t), Offset(fx, b), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)))
            if (r - fx > 60.dp.toPx()) {
                p.axis.color = 0xFFCBD5E1.toInt()
                native.drawText(if (greek) "ΜΕΛΛΟΝ · μόνο ο τύπος" else "FUTURE · model only",
                    fx + 6.dp.toPx(), t + 14.dp.toPx(), p.axis)
                p.axis.color = 0xFF9CA3AF.toInt()
            }
        }

        // Grid τιμών (δυνάμεις του 10) και ετών
        for (e in floor(lo).toInt()..ceil(hi).toInt()) {
            val y = f.y(10.0.pow(e))
            drawLine(Color(0x22FFFFFF), Offset(l, y), Offset(r, y), 1.dp.toPx())
        }
        val y0 = DateUtil.civil(xMin.toLong()).first
        val y1 = DateUtil.civil(xMax.toLong()).first
        for (yr in y0..y1 + 1) {
            val x = f.x(DateUtil.epochDay(yr, 1, 1).toDouble())
            drawLine(Color(0x14FFFFFF), Offset(x, t), Offset(x, b), 1.dp.toPx())
        }

        // Halvings: συνεχείς για τα πραγματικά, κόκκινες διακεκομμένες για τα εκτιμώμενα
        p.label.textSize = 10.sp.toPx()
        for (hv in RainbowModel.HALVINGS) {
            val x = f.x(hv.day.toDouble())
            if (x < l - 2 || x > r + 2) continue
            if (hv.estimated) {
                drawLine(
                    Color(0xE6EF4444), Offset(x, t), Offset(x, b), 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            } else {
                drawLine(Color(0xB3FFFFFF), Offset(x, t), Offset(x, b), 1.5.dp.toPx())
            }
            p.label.color = if (hv.estimated) 0xFFFCA5A5.toInt() else 0xFFFFFFFF.toInt()
            val txt = if (hv.estimated) "Halving ${hv.year} (Est)" else "Halving ${hv.year}"
            native.save()
            native.rotate(-90f, x - 4.dp.toPx(), b - 6.dp.toPx())
            native.drawText(txt, x - 4.dp.toPx(), b - 6.dp.toPx(), p.label)
            native.restore()
        }

        // Γραμμή τιμής με glow (ζωγραφίζεται σταδιακά με το reveal)
        val visible = series.filter { it.day >= xMin - 2 && it.day <= xMax + 2 }
        val revealDay = series.first().day + (series.last().day - series.first().day) * reveal
        if (visible.size > 1) {
            val path = Path()
            val stride = max(1, visible.size / max(1, (f.w / 1.2f).toInt()))
            var started = false
            visible.forEachIndexed { i, pt ->
                if (pt.day > revealDay) return@forEachIndexed
                if (i % stride != 0 && i != visible.lastIndex) return@forEachIndexed
                val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
                if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
            }
            drawPath(path, Color(0x80FFFFFF), style = Stroke(5.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
            drawPath(path, Color(0xFF0B0B12), style = Stroke(2.4.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        }

        if (reveal >= 1f) {
            // Ghost cycles: προηγούμενοι κύκλοι ευθυγραμμισμένοι στο τρέχον halving (μόνο ως σήμερα)
            ghosts.forEach { (h, pts) ->
                val gp = Path()
                val stride = max(1, pts.size / max(1, (f.w / 1.5f).toInt()))
                var started = false
                pts.forEachIndexed { i, pt ->
                    if (i % stride != 0 && i != pts.lastIndex) return@forEachIndexed
                    val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
                    if (!started) { gp.moveTo(x, y); started = true } else gp.lineTo(x, y)
                }
                drawPath(gp, Color(0x99000000), style = Stroke(4.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
                drawPath(gp, Color(h.color).copy(alpha = 0.95f), style = Stroke(2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
                val last = pts.last()
                val gx = f.x(last.day.toDouble()); val gy = f.y(last.price)
                if (gx in l..r) {
                    p.label.textSize = 11.sp.toPx()
                    p.label.color = Color(h.color).toArgb()
                    native.drawText("${h.year}", gx + 5.dp.toPx(), gy + 4.dp.toPx(), p.label)
                }
            }

            // Γεγονότα / κορυφές / πάτοι πάνω στη γραμμή
            markers.forEach { m ->
                val x = f.x(m.day.toDouble())
                if (x < l || x > r) return@forEach
                val y = f.y(m.price)
                drawCircle(Color(0xFF0B0B12), 4.5.dp.toPx(), Offset(x, y))
                drawCircle(m.color, 3.dp.toPx(), Offset(x, y))
            }

            // Ίδια μέρα σε κάθε προηγούμενο κύκλο: dot στη γραμμή + διαγώνια κάτω-δεξιά σε ετικέτα έτους
            p.label.textSize = 14.sp.toPx()
            p.label.color = 0xFFFFFFFF.toInt()
            insights.filter { !it.isCurrent }.forEach { c ->
                val price = c.price ?: return@forEach
                val x = f.x(c.day.toDouble()); val y = f.y(price)
                if (x < l || x > r) return@forEach
                val ex = x + 56.dp.toPx(); val ey = y + 52.dp.toPx()
                drawLine(LINE, Offset(x, y), Offset(ex, ey), 1.5.dp.toPx())
                drawCircle(LINE, 4.5.dp.toPx(), Offset(x, y))
                drawCircle(Color(0xFF0B0B12), 2.dp.toPx(), Offset(x, y))
                drawCircle(Color(c.halving.color), 6.dp.toPx(), Offset(ex, ey))
                drawCircle(LINE, 6.dp.toPx(), Offset(ex, ey), style = Stroke(1.5.dp.toPx()))
                drawRoundRect(
                    Color(0xCC0B0B12), Offset(ex + 9.dp.toPx(), ey - 10.dp.toPx()),
                    Size(p.label.measureText("${c.halving.year}") + 10.dp.toPx(), 20.dp.toPx()),
                    CornerRadius(6.dp.toPx())
                )
                native.drawText("${c.halving.year}", ex + 14.dp.toPx(), ey + 5.dp.toPx(), p.label)
            }
        }

        // Crosshair + tooltip
        scrubDay?.let { sd ->
            val pt = series.nearest(sd) ?: return@let
            if (abs(pt.day - sd) > 5) return@let
            val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
            drawLine(Color(0xB3FFFFFF), Offset(x, t), Offset(x, b), 1.dp.toPx())
            drawLine(Color(0x66FFFFFF), Offset(l, y), Offset(r, y), 1.dp.toPx())
            drawCircle(Color(0xFF0B0B12), 6.dp.toPx(), Offset(x, y))
            drawCircle(LINE, 6.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))

            val bandI = RainbowModel.bandIndex(pt.day, pt.price)
            val hv = RainbowModel.cycleOf(pt.day)
            val dayLine = when {
                hv == null && greek -> "Πριν το 1ο halving"
                hv == null -> "Before the 1st halving"
                greek -> "Μέρα ${pt.day - hv.day} μετά το halving (${hv.year})"
                else -> "Day ${pt.day - hv.day} after the halving (${hv.year})"
            }
            val near = markers.filter { abs(it.day - pt.day) <= 3 }.minByOrNull { abs(it.day - pt.day) }
            val lines = buildList<Pair<String, Pair<Float, Int>>> {
                add(DateUtil.format(pt.day, greek) to (13.sp.toPx() to 0xFFD1D5DB.toInt()))
                add(usd(pt.price) to (22.sp.toPx() to 0xFFFFFFFF.toInt()))
                add(RainbowModel.BANDS[bandI].name to (14.sp.toPx() to RainbowModel.BANDS[bandI].color.toInt()))
                add(dayLine to (11.sp.toPx() to 0xFF9CA3AF.toInt()))
                near?.let {
                    add(shorten(if (greek) it.el else it.en, 40) to (11.sp.toPx() to it.color.toArgb()))
                }
            }
            val gap = 6.dp.toPx()
            val boxW = min(
                f.w - 20.dp.toPx(),
                lines.maxOf { (s, st) -> p.tip.apply { textSize = st.first }.measureText(s) } + 28.dp.toPx()
            )
            val boxH = lines.sumOf { (it.second.first + gap).toDouble() }.toFloat() + 18.dp.toPx()
            val bx = if (x > l + f.w / 2) l + 10.dp.toPx() else r - boxW - 10.dp.toPx()
            val by = t + 10.dp.toPx()
            drawRoundRect(Color(0xF0181B2E), Offset(bx, by), Size(boxW, boxH), CornerRadius(16.dp.toPx()))
            drawRoundRect(Color(0x33FFFFFF), Offset(bx, by), Size(boxW, boxH), CornerRadius(16.dp.toPx()), style = Stroke(1.dp.toPx()))
            var ty = by + 12.dp.toPx()
            lines.forEach { (s, st) ->
                ty += st.first
                p.tip.textSize = st.first; p.tip.color = st.second
                native.drawText(s, bx + 14.dp.toPx(), ty, p.tip)
                ty += gap
            }
        }
    }

    // Ετικέτες Y (δεξιά)
    for (e in floor(lo).toInt()..ceil(hi).toInt()) {
        val y = f.y(10.0.pow(e))
        if (y < t || y > b) continue
        val v = 10.0.pow(e)
        val s = when {
            v >= 1e6 -> "$${(v / 1e6).toInt()}M"
            v >= 1e3 -> "$${(v / 1e3).toInt()}K"
            v >= 1 -> "$${v.toInt()}"
            else -> "$" + String.format(java.util.Locale.US, "%.2f", v)
        }
        native.drawText(s, r + 6.dp.toPx(), y + 4.dp.toPx(), p.axis)
    }
    // Ετικέτες X (έτη)
    val y0 = DateUtil.civil(xMin.toLong()).first
    val y1 = DateUtil.civil(xMax.toLong()).first
    val step = if (f.span > 3000) 2 else 1
    for (yr in y0..y1 + 1) {
        if (yr % step != 1 % step) continue
        val x = f.x(DateUtil.epochDay(yr, 1, 1).toDouble())
        if (x < l || x > r) continue
        native.save()
        native.rotate(40f, x, b + 12.dp.toPx())
        native.drawText("$yr", x, b + 14.dp.toPx(), p.axis)
        native.restore()
    }
}

// ─── Κάρτα "τώρα": ζώνη, τιμή, μέρα, κανόνας ενημέρωσης ───────────────────────
@Composable
private fun RainbowNowCard(
    s: RainbowUiState, series: List<PricePoint>, today: Long, cur: CycleInsights.Insight?, priceIsLive: Boolean, greek: Boolean,
) {
    val last = series.last()
    val band = RainbowModel.BANDS[RainbowModel.bandIndex(last.day, last.price)]
    val hv = RainbowModel.currentHalving(today)
    val n = RainbowModel.daysSinceHalving(today)

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                if (greek) "ΣΗΜΕΡΑ · ΜΕΡΑ $n ΜΕΤΑ ΤΟ HALVING ${hv.year}" else "TODAY · DAY $n AFTER THE ${hv.year} HALVING",
                fontSize = 11.sp, color = MUTED, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(band.name, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(band.color))
            Text(
                usd(last.price) + "  ·  " + DateUtil.format(last.day, greek),
                fontSize = 18.sp, fontFamily = FontFamily.Monospace, color = TEXT
            )
            Spacer(Modifier.height(10.dp))
            RainbowGauge(last.day, last.price)
            Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp)) {
                Text(RainbowModel.BANDS.first().name, fontSize = 10.sp, color = FAINT, modifier = Modifier.weight(1f))
                Text(RainbowModel.BANDS.last().name, fontSize = 10.sp, color = FAINT)
            }
            cur?.let { c -> CycleInsights.summary(c, greek).forEach { Text("• $it", fontSize = 13.sp, color = MUTED) } }
            cur?.nearby?.take(3)?.forEach { NearbyRow(it, greek) }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = DIVIDER)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(if (priceIsLive && s.live != null) Color(0xFF10B981) else FAINT, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    when {
                        priceIsLive && s.live != null && greek -> "Live τώρα: ${usd(s.live)}"
                        priceIsLive && s.live != null -> "Live now: ${usd(s.live)}"
                        greek -> "Η ζωντανή τιμή λείπει"
                        else -> "Live price missing"
                    },
                    fontSize = 13.sp, color = TEXT, fontFamily = FontFamily.Monospace
                )
            }
            Text(
                if (greek) "Το rainbow ενημερώνεται μία φορά τη μέρα, ή αμέσως σε κίνηση ±7% ή αλλαγή ζώνης."
                else "The rainbow updates once a day, or immediately on a ±7% move or a band change.",
                fontSize = 11.sp, color = FAINT, modifier = Modifier.padding(top = 4.dp)
            )
            if (s.shownReason == LiveGate.Reason.BIG_MOVE || s.shownReason == LiveGate.Reason.BAND_CHANGE) {
                Text(
                    when {
                        s.shownReason == LiveGate.Reason.BIG_MOVE && greek -> "⚡ Μεγάλη κίνηση: το rainbow ενημερώθηκε τώρα"
                        s.shownReason == LiveGate.Reason.BIG_MOVE -> "⚡ Big move: the rainbow just updated"
                        greek -> "⚡ Αλλαγή ζώνης: το rainbow ενημερώθηκε τώρα"
                        else -> "⚡ Band change: the rainbow just updated"
                    },
                    fontSize = 12.sp, color = Color(0xFFFACC15), modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (s.offline) {
                Text(
                    if (greek) "Χωρίς σύνδεση: εμφανίζονται τα αποθηκευμένα δεδομένα."
                    else "Offline: showing saved data.",
                    fontSize = 11.sp, color = CRASH, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ─── Κάρτα ανά προηγούμενο κύκλο ─────────────────────────────────────────────
@Composable
private fun CycleInsightCard(i: CycleInsights.Insight, greek: Boolean) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(Color(i.halving.color), CircleShape))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (greek) "Κύκλος ${i.halving.year} · μέρα ${i.n}" else "${i.halving.year} cycle · day ${i.n}",
                        color = TEXT, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                    Text(DateUtil.format(i.day, greek), color = MUTED, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        i.price?.let(::usd) ?: "—", color = TEXT,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    i.band?.let {
                        Text(
                            RainbowModel.BANDS[it].name, color = Color(RainbowModel.BANDS[it].color),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            val lines = CycleInsights.summary(i, greek)
            if (lines.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                lines.forEach { Text("• $it", fontSize = 13.sp, color = MUTED) }
            }
            if (i.nearby.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (greek) "Κοντά σε αυτή τη μέρα (±${CycleInsights.NEAR_DAYS} μέρες)"
                    else "Near this day (±${CycleInsights.NEAR_DAYS} days)",
                    fontSize = 12.sp, color = FAINT
                )
                i.nearby.forEach { NearbyRow(it, greek) }
            } else {
                Text(
                    if (greek) "Κανένα καταγεγραμμένο γεγονός μέσα σε ±${CycleInsights.NEAR_DAYS} μέρες."
                    else "No recorded event within ±${CycleInsights.NEAR_DAYS} days.",
                    fontSize = 12.sp, color = FAINT, modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NearbyRow(nb: CycleInsights.Nearby, greek: Boolean) {
    val color = when (nb.kind) {
        RainbowEvents.Kind.CRASH -> CRASH
        null -> Color(0xFFFACC15)
        else -> ACCENT
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 5.dp).size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                CycleInsights.offsetText(nb.offset, greek) + " · " + DateUtil.format(nb.day, greek),
                fontSize = 11.sp, color = FAINT
            )
            Text(nb.text(greek), fontSize = 13.sp, color = TEXT, lineHeight = 18.sp)
        }
    }
}
